import { getApiPath, getApiConfig } from './config';

export class ApiClient {
  private sessionToken: string | null = null;

  setSessionToken(token: string | null) {
    this.sessionToken = token;
  }

  getSessionToken(): string | null {
    return this.sessionToken;
  }

  async request<T>(endpoint: string, options: RequestInit & { suppressAuthRedirect?: boolean } = {}): Promise<T> {
    const { suppressAuthRedirect, ...fetchOptions } = options;
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...fetchOptions.headers,
    };

    // In production mode, send session token as header
    const config = await getApiConfig();
    if (config.mode === 'production' && this.sessionToken) {
      (headers as Record<string, string>)['Elements-SessionSecret'] = this.sessionToken;
      console.log('[API] Sending request with session token to:', endpoint);
    } else if (config.mode === 'production' && !this.sessionToken) {
      console.warn('[API] Production mode but no session token! Request to:', endpoint);
    }

    // Get the correct API path based on production vs development
    const fullPath = await getApiPath(endpoint);

    // Credentials: 'include' ensures cookies are sent with requests
    const response = await fetch(fullPath, {
      ...fetchOptions,
      headers,
      credentials: 'include', // Send cookies with all requests
    });

    if (!response.ok) {
      // Handle session expiry/invalid tokens
      if (response.status === 401 || response.status === 403) {
        if (!suppressAuthRedirect) {
          // Session expired, redirect to login with correct base path
          const basePath = import.meta.env.BASE_URL || '/';
          // Ensure proper path formation: /admin/ → /admin/login, / → /login
          const loginPath = basePath.endsWith('/') 
            ? `${basePath}login` 
            : `${basePath}/login`;
          window.location.href = loginPath;
          throw new Error('Session expired. Please login again.');
        }
        // During discovery or when suppressed, just throw with status
        const error = new Error(`Auth required: ${response.status}`) as Error & { status: number };
        error.status = response.status;
        throw error;
      }
      
      // Get error message from response (read as text first, then try to parse as JSON)
      let errorMessage = '';
      try {
        const errorText = await response.text();
        if (errorText) {
          try {
            const errorData = JSON.parse(errorText);
            errorMessage = errorData.message || errorData.error || errorText;
          } catch {
            errorMessage = errorText;
          }
        }
      } catch {
        errorMessage = `API Error: ${response.status}`;
      }
      
      // Create error with status code and message
      const error = new Error(errorMessage || `API Error: ${response.status}`) as Error & { status: number };
      error.status = response.status;
      throw error;
    }

    if (response.status === 204 || response.status === 205 || response.headers.get('content-length') === '0') {
      return undefined as T;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      try {
        return await response.json();
      } catch {
        return undefined as T;
      }
    }
    
    return undefined as T;
  }

  async createUsernamePasswordSession(username: string, password: string, rememberMe = false): Promise<{ success: boolean; session?: { userId?: string; level?: string } }> {
    // Use the config system to determine production vs development mode
    const { getApiConfig } = await import('./config');
    const config = await getApiConfig();
    
    const isProduction = config.mode === 'production';
    console.log('[LOGIN] Mode:', config.mode, '| Production?', isProduction);
    
    // Determine endpoint and request format
    const loginEndpoint = isProduction
      ? `${config.baseUrl}/session`
      : '/api/auth/login';
    
    const requestBody = isProduction
      ? { userId: username, password: password }
      : { username, password, rememberMe };

    const response = await fetch(loginEndpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Allow setting cookies
      body: JSON.stringify(requestBody),
    });

    if (!response.ok) {
      const errorText = await response.text();
      let errorMessage = 'Authentication failed';
      try {
        const errorData = JSON.parse(errorText);
        errorMessage = errorData.error || errorMessage;
      } catch {
        errorMessage = errorText || errorMessage;
      }
      throw new Error(errorMessage);
    }

    const responseData = await response.json();
    
    // If calling Elements backend directly, extract and store session token
    if (isProduction) {
      // Extract session token from response
      console.log('[LOGIN] Full response data:', JSON.stringify(responseData, null, 2));
      
      // Try multiple possible paths for session token
      let sessionToken = responseData.session?.sessionSecret 
        || responseData.sessionSecret 
        || responseData.token;
      
      console.log('[LOGIN] Session token paths checked:');
      console.log('  - responseData.session?.sessionSecret:', responseData.session?.sessionSecret);
      console.log('  - responseData.sessionSecret:', responseData.sessionSecret);
      console.log('  - responseData.token:', responseData.token);
      console.log('[LOGIN] Final extracted token:', sessionToken ? 'PRESENT (' + sessionToken.substring(0, 10) + '...)' : 'MISSING');
      
      if (sessionToken) {
        this.setSessionToken(sessionToken);
        console.log('[LOGIN] ✓ Session token stored in apiClient');
      } else {
        console.error('[LOGIN] ✗ No session token found in response!');
      }
      
      return {
        success: true,
        session: {
          userId: responseData.session?.user?.name || username,
          level: responseData.session?.user?.level || 'SUPERUSER',
        },
      };
    }

    return responseData;
  }

  async logout(): Promise<void> {
    const config = await getApiConfig();
    const isProduction = config.mode === 'production';
    
    // In production, call Elements backend logout; in dev, call proxy
    const logoutEndpoint = isProduction
      ? `${config.baseUrl}/session`
      : '/api/auth/logout';
    
    // Send DELETE request to logout
    await fetch(logoutEndpoint, {
      method: 'DELETE',
      headers: isProduction && this.sessionToken 
        ? { 'Elements-SessionSecret': this.sessionToken }
        : {},
      credentials: 'include',
    });
    
    // Clear session token after logout
    this.setSessionToken(null);
  }

  async verifySession(): Promise<{ level: string; username: string }> {
    const response = await fetch('/api/auth/verify', {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Send cookies
    });

    if (!response.ok) {
      throw new Error('Session verification failed');
    }

    return response.json();
  }
}

export const apiClient = new ApiClient();

// Re-export helpers for direct use
export { getApiPath, getApiConfig } from './config';
