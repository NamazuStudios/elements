import { createContext, useContext, useState, useEffect } from 'react';
import { apiClient } from '@/lib/api-client';

interface AuthContextType {
  isAuthenticated: boolean;
  userLevel: string | null;
  username: string | null;
  login: (username: string, password: string, rememberMe?: boolean) => Promise<void>;
  logout: () => Promise<void>;
  isLoading: boolean;
  sessionExpired: boolean;
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
    <AuthContext.Provider value={{ isAuthenticated, userLevel, username, login, logout, isLoading, sessionExpired }}>
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
