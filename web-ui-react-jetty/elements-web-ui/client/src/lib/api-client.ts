import { getApiPath, getApiConfig } from './config';

// Helper function to determine if current route is a core resource page
// (not an API explorer page where session overrides might be in use)
function isCoreResourcePage(): boolean {
  const path = window.location.pathname;
  const basePath = import.meta.env.BASE_URL || '/';
  
  // Normalize base path to ensure it ends with /
  const normalizedBase = basePath.endsWith('/') ? basePath : basePath + '/';
  
  // Remove base path from pathname to get relative path
  let relativePath = path.startsWith(normalizedBase) 
    ? path.slice(normalizedBase.length) 
    : path;
  
  // Normalize relative path (remove leading slash if present)
  if (relativePath.startsWith('/')) {
    relativePath = relativePath.slice(1);
  }
  
  // Check if this is a core resource page (starts with 'resource' - with or without trailing content)
  let isCoreResource = relativePath === 'resource' || relativePath.startsWith('resource/');
  
  // Development quirk: if BASE_URL is / but path starts with /admin/, also check with /admin/ stripped
  if (!isCoreResource && basePath === '/' && path.startsWith('/admin/')) {
    const devRelativePath = path.slice('/admin/'.length);
    isCoreResource = devRelativePath === 'resource' || devRelativePath.startsWith('resource/');
  }
  
  // Exclude API explorer pages
  const isApiExplorer = path.includes('api-explorer');
  
  console.log('[AUTH] Path check - pathname:', path, 'basePath:', basePath, 'relativePath:', relativePath, 'isCoreResource:', isCoreResource, 'isApiExplorer:', isApiExplorer, 'result:', isCoreResource && !isApiExplorer);
  
  return isCoreResource && !isApiExplorer;
}

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
          // Only redirect on 403 if we're on a core resource page
          // API explorer pages use session overrides and shouldn't trigger auto-redirect
          const shouldRedirect = response.status === 401 || (response.status === 403 && isCoreResourcePage());
          
          if (shouldRedirect) {
            // Clear session token and localStorage before redirecting
            this.setSessionToken(null);
            localStorage.removeItem('elements-user');
            console.log('[AUTH] Session expired - cleared session token and localStorage');
            
            // Session expired, redirect to login with correct base path
            const basePath = import.meta.env.BASE_URL || '/';
            // In development, app is at /admin/ even when BASE_URL is /
            // In production, BASE_URL will be set correctly (e.g., /admin/)
            const isDev = import.meta.env.DEV;
            const adminPath = isDev && basePath === '/' ? '/admin' : basePath;
            const loginPath = adminPath.endsWith('/') 
              ? `${adminPath}login?expired=true` 
              : `${adminPath}/login?expired=true`;
            console.log('[AUTH] Redirecting to:', loginPath);
            window.location.href = loginPath;
            throw new Error('Session expired. Please login again.');
          }
        }
        // During discovery, when suppressed, or on non-core pages, just throw with status
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

  async createUsernamePasswordSession(username: string, password: string, rememberMe = false): Promise<{ success: boolean; session?: { userId?: string; level?: string; expiry?: number } }> {
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
    
    // Extract expiry timestamp - try multiple field names and formats
    let expiryTimestamp: number | undefined;
    const expiresAt = responseData.session?.expiresAt || responseData.expiresAt;
    const expiry = responseData.session?.expiry || responseData.expiry;
    
    if (expiresAt) {
      // Convert ISO string to timestamp
      expiryTimestamp = new Date(expiresAt).getTime();
      console.log('[LOGIN] Session expires at:', expiresAt, '(timestamp:', expiryTimestamp, ')');
    } else if (expiry) {
      // Could be a number timestamp or ISO string
      expiryTimestamp = typeof expiry === 'string' ? new Date(expiry).getTime() : expiry;
      console.log('[LOGIN] Session expiry:', expiry, '(timestamp:', expiryTimestamp, ')');
    }
    
    return {
      success: true,
      session: {
        userId: responseData.session?.user?.name || username,
        level: responseData.session?.user?.level,
        expiry: expiryTimestamp,
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
