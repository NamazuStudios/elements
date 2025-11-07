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

    // Send session token as header in both development and production
    const config = await getApiConfig();
    console.log('[API] Request endpoint:', endpoint);
    if (this.sessionToken) {
      (headers as Record<string, string>)['Elements-SessionSecret'] = this.sessionToken;
      console.log('[API] Sending request with session token to:', endpoint);
    } else {
      console.warn('[API] No session token available for request to:', endpoint);
    }

    // Get the correct API path based on production vs development
    const fullPath = await getApiPath(endpoint);
    console.log('[API] Full path after getApiPath:', fullPath);

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
    const { getApiConfig, getApiPath } = await import('./config');
    const config = await getApiConfig();
    
    console.log('[LOGIN] Mode:', config.mode);
    console.log('[LOGIN] Config baseUrl:', config.baseUrl);
    
    // Always use /api/rest/session - getApiPath will add proxy prefix in development
    const loginEndpoint = await getApiPath('/api/rest/session');
    console.log('[LOGIN] Login endpoint:', loginEndpoint);
    
    const requestBody = { userId: username, password: password };

    const response = await fetch(loginEndpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
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
    
    // Extract session token from response and store it
    // Try multiple possible paths for session token
    let sessionToken = responseData.session?.sessionSecret 
      || responseData.sessionSecret 
      || responseData.token;
    
    if (sessionToken) {
      this.setSessionToken(sessionToken);
      console.log('[LOGIN] ✓ Session token stored');
    } else {
      console.error('[LOGIN] ✗ No session token found in response');
    }
    
    return {
      success: true,
      session: {
        userId: responseData.session?.user?.name || username,
        level: responseData.session?.user?.level,
      },
    };
  }

  async logout(): Promise<void> {
    // Always use /api/rest/session - getApiPath will add proxy prefix in development
    const logoutEndpoint = await getApiPath('/api/rest/session');
    
    // Send DELETE request to logout with session token header
    await fetch(logoutEndpoint, {
      method: 'DELETE',
      headers: this.sessionToken 
        ? { 'Elements-SessionSecret': this.sessionToken }
        : {},
      credentials: 'include',
    });
    
    // Clear session token after logout
    this.setSessionToken(null);
  }

  async verifySession(): Promise<{ level: string; username: string }> {
    // Verify by calling /api/rest/session with POST and session token
    const verifyEndpoint = await getApiPath('/api/rest/session');
    
    const response = await fetch(verifyEndpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(this.sessionToken ? { 'Elements-SessionSecret': this.sessionToken } : {}),
      },
      credentials: 'include',
      body: JSON.stringify({}),
    });

    if (!response.ok) {
      throw new Error('Session verification failed');
    }

    const data = await response.json();
    return {
      level: data.session?.user?.level,
      username: data.session?.user?.name || '',
    };
  }
}

export const apiClient = new ApiClient();

// Re-export helpers for direct use
export { getApiPath, getApiConfig } from './config';
