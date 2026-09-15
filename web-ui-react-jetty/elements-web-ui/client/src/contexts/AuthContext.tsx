import { createContext, useContext, useState, useEffect } from 'react';
import { apiClient } from '@/lib/api-client';

interface AuthContextType {
  isAuthenticated: boolean;
  userLevel: string | null;
  username: string | null;
  login: (username: string, password: string, rememberMe?: boolean) => Promise<void>;
  loginWithOidcProvider: (providerName: string, rememberMe?: boolean) => Promise<void>;
  logout: () => Promise<void>;
  isLoading: boolean;
  sessionExpired: boolean;
}

// Applies a completed session's level/userId to auth state (and, if rememberMe, localStorage), enforcing the
// same SUPERUSER-only admin panel gate regardless of how the session was created (username/password or OIDC).
function applySessionOrThrow(
  session: { userId?: string; level?: string; expiry?: number } | undefined,
  rememberMe: boolean,
  setUserLevel: (level: string) => void,
  setUsername: (username: string) => void,
  setIsAuthenticated: (value: boolean) => void,
) {
  const level = session?.level;
  const userId = session?.userId || 'unknown';

  // SECURITY: Only allow SUPERUSER level to access admin interface. A non-SUPERUSER session was still
  // created successfully (the account exists and is usable elsewhere) -- it just can't use this panel
  // until an existing administrator elevates it via the user-management UI/API.
  if (level !== 'SUPERUSER') {
    setIsAuthenticated(false);
    throw new Error(
      'Your account was authenticated, but only SUPERUSER level accounts can access the admin ' +
      'interface. Ask an existing administrator to elevate your account, then sign in again.'
    );
  }

  setUserLevel(level);
  setUsername(userId);
  setIsAuthenticated(true);

  if (rememberMe) {
    localStorage.setItem('elements-user', JSON.stringify({
      username: userId,
      level,
      sessionToken: apiClient.getSessionToken(),
      expiry: session?.expiry,
    }));
  } else {
    localStorage.removeItem('elements-user');
  }
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userLevel, setUserLevel] = useState<string | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [sessionExpired, setSessionExpired] = useState(false);

  useEffect(() => {
    // On initial load, check localStorage for "remember me" user info
    const storedUser = localStorage.getItem('elements-user');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        
        // Check if session has expired
        const now = Date.now();
        const isExpired = userData.expiry && typeof userData.expiry === 'number' && userData.expiry < now;
        
        if (isExpired) {
          console.log('[AUTH] Session expired on initial load (expiry:', userData.expiry, ', now:', now, ')');
          localStorage.removeItem('elements-user');
          setSessionExpired(true);
          setIsLoading(false);
          return;
        }
        
        // SECURITY: Only restore session if user is SUPERUSER
        if (userData.level === 'SUPERUSER') {
          setUsername(userData.username);
          setUserLevel(userData.level);
          setIsAuthenticated(true);
          setSessionExpired(false); // Clear expired flag when valid session is restored
          
          // Restore session token to apiClient if available
          if (userData.sessionToken) {
            apiClient.setSessionToken(userData.sessionToken);
            console.log('[AUTH] Restored session token from localStorage');
          }
        } else {
          // Clear non-SUPERUSER stored data
          localStorage.removeItem('elements-user');
          setSessionExpired(false);
        }
      } catch (error) {
        console.error('Failed to parse stored user data:', error);
        localStorage.removeItem('elements-user');
        setSessionExpired(false);
      }
    } else {
      // No stored session - ensure expired flag is cleared
      setSessionExpired(false);
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string, rememberMe = false) => {
    setIsLoading(true);
    try {
      const response = await apiClient.createUsernamePasswordSession(username, password, rememberMe);
      
      // Use the session data returned from login response
      const level = response.session?.level;
      const userId = response.session?.userId || username;
      
      // SECURITY: Only allow SUPERUSER level to access admin interface
      if (level !== 'SUPERUSER') {
        setIsAuthenticated(false);
        throw new Error('Access denied. Only SUPERUSER level accounts can access the admin interface.');
      }
      
      setUserLevel(level);
      setUsername(userId);
      setIsAuthenticated(true);
      
      // If "remember me" is checked, store user info and session token in localStorage
      // This allows the UI to restore the logged-in state on page refresh
      if (rememberMe) {
        const sessionToken = apiClient.getSessionToken();
        localStorage.setItem('elements-user', JSON.stringify({
          username: userId,
          level: level,
          sessionToken: sessionToken,
          expiry: response.session?.expiry
        }));
      } else {
        // Clear any existing stored user
        localStorage.removeItem('elements-user');
      }
      
      // Clear session expired flag on successful login
      setSessionExpired(false);
    } catch (error) {
      setIsAuthenticated(false);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const loginWithOidcProvider = async (providerName: string, rememberMe = false) => {
    setIsLoading(true);

    let popup: Window | null = null;

    try {
      const { id, authorizeUrl } = await apiClient.beginOidcSession(providerName);

      popup = window.open(authorizeUrl, 'oidc-admin-login', 'width=500,height=700');

      if (!popup) {
        throw new Error('Could not open the login popup. Please allow popups for this site and try again.');
      }

      // Poll for completion. The provider callback finishes in the popup; this tab has no way to know
      // when that happens other than polling the attempt's status.
      const POLL_INTERVAL_MS = 1500;
      const TIMEOUT_MS = 2 * 60 * 1000;
      const deadline = Date.now() + TIMEOUT_MS;

      while (Date.now() < deadline) {

        if (popup.closed) {
          throw new Error('Login window was closed before completing sign-in.');
        }

        const status = await apiClient.pollOidcSession(id);

        if (status.status === 'COMPLETE') {
          if (status.session?.sessionSecret) {
            apiClient.setSessionToken(status.session.sessionSecret);
          }
          applySessionOrThrow(status.session, rememberMe, setUserLevel, setUsername, setIsAuthenticated);
          setSessionExpired(false);
          return;
        }

        if (status.status === 'FAILED') {
          throw new Error(status.reason || 'The login attempt failed.');
        }

        await new Promise((resolve) => setTimeout(resolve, POLL_INTERVAL_MS));
      }

      throw new Error('Login attempt timed out. Please try again.');
    } catch (error) {
      setIsAuthenticated(false);
      throw error;
    } finally {
      if (popup && !popup.closed) {
        popup.close();
      }
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      await apiClient.logout();
    } catch (error) {
      console.error('Logout error:', error);
    }
    
    // Clear local state and localStorage
    localStorage.removeItem('elements-user');
    setIsAuthenticated(false);
    setUserLevel(null);
    setUsername(null);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, userLevel, username, login, loginWithOidcProvider, logout, isLoading, sessionExpired }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
