import { createContext, useContext, useState, useEffect } from 'react';
import { apiClient } from '@/lib/api-client';

interface AuthContextType {
  isAuthenticated: boolean;
  userLevel: string | null;
  username: string | null;
  login: (username: string, password: string, rememberMe?: boolean) => Promise<void>;
  logout: () => Promise<void>;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userLevel, setUserLevel] = useState<string | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // On initial load, check localStorage for "remember me" user info
    // The actual authentication is cookie-based (sent automatically)
    const storedUser = localStorage.getItem('elements-user');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        setUsername(userData.username);
        setUserLevel(userData.level);
        setIsAuthenticated(true);
      } catch (error) {
        console.error('Failed to parse stored user data:', error);
        localStorage.removeItem('elements-user');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string, rememberMe = false) => {
    setIsLoading(true);
    try {
      const response = await apiClient.createUsernamePasswordSession(username, password, rememberMe);
      
      // Use the session data returned from login response
      const level = response.session?.level || 'SUPERUSER';
      const userId = response.session?.userId || username;
      
      setUserLevel(level);
      setUsername(userId);
      setIsAuthenticated(true);
      
      // If "remember me" is checked, store user info in localStorage
      // This allows the UI to restore the logged-in state on page refresh
      // The actual authentication is handled by HTTP-only cookies
      if (rememberMe) {
        localStorage.setItem('elements-user', JSON.stringify({
          username: userId,
          level: level
        }));
      } else {
        // Clear any existing stored user
        localStorage.removeItem('elements-user');
      }
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
    <AuthContext.Provider value={{ isAuthenticated, userLevel, username, login, logout, isLoading }}>
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
