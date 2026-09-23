import { getApiPath, getApiConfig } from './config';

/** Thrown by {@link ApiClient.createUsernamePasswordSession} when the account has TOTP 2FA enrolled and enforced. */
export class MfaRequiredError extends Error {
  challengeId: string;
  expiresAt?: number;

  constructor(challengeId: string, expiresAt?: number) {
    super('A second authentication factor is required to complete this login.');
    this.name = 'MfaRequiredError';
    this.challengeId = challengeId;
    this.expiresAt = expiresAt;
  }
}

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
        if (errorData.code === 'MFA_REQUIRED' && errorData.challengeId) {
          throw new MfaRequiredError(errorData.challengeId, errorData.expiresAt);
        }
        errorMessage = errorData.error || errorMessage;
      } catch (e) {
        if (e instanceof MfaRequiredError) {
          throw e;
        }
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

  /** Completes a login that {@link createUsernamePasswordSession} interrupted with an {@link MfaRequiredError}. */
  async completeMfaSession(challengeId: string, code: string): Promise<{ success: boolean; session?: { userId?: string; level?: string; expiry?: number } }> {
    const endpoint = await getApiPath('/api/rest/session/mfa');

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ challengeId, code }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      let errorMessage = 'Invalid or expired code';
      try {
        errorMessage = JSON.parse(errorText).message || errorMessage;
      } catch {
        errorMessage = errorText || errorMessage;
      }
      throw new Error(errorMessage);
    }

    const responseData = await response.json();
    const info = this.extractOidcSessionInfo(responseData);

    if (info.sessionSecret) {
      this.setSessionToken(info.sessionSecret);
    }

    return {
      success: true,
      session: {
        userId: info.userId,
        level: info.level,
        expiry: info.expiry,
      },
    };
  }

  // Extracts the same { userId, level, sessionSecret, expiry } shape used for a username/password login from
  // a raw SessionCreation-shaped response body, shared by every OIDC completion path below.
  private extractOidcSessionInfo(responseData: any): { userId?: string; level?: string; sessionSecret?: string; expiry?: number } {
    const sessionSecret = responseData.sessionSecret || responseData.session?.sessionSecret;

    let expiryTimestamp: number | undefined;
    const expiresAt = responseData.session?.expiresAt || responseData.expiresAt;
    const expiry = responseData.session?.expiry || responseData.expiry;

    if (expiresAt) {
      expiryTimestamp = new Date(expiresAt).getTime();
    } else if (expiry) {
      expiryTimestamp = typeof expiry === 'string' ? new Date(expiry).getTime() : expiry;
    }

    return {
      userId: responseData.session?.user?.name,
      level: responseData.session?.user?.level,
      sessionSecret,
      expiry: expiryTimestamp,
    };
  }

  /** Lists the OIDC providers offered as admin-panel login options. No authentication required. */
  async getOidcAdminLoginProviders(): Promise<{ id: string; name: string; displayName?: string; iconUrl?: string }[]> {
    const endpoint = await getApiPath('/api/rest/oidc/admin_login_providers');
    const response = await fetch(endpoint, { credentials: 'include' });

    if (!response.ok) {
      throw new Error('Failed to load OIDC login providers');
    }

    return response.json();
  }

  /** Begins a browser-redirect OIDC login attempt for the given provider name. */
  async beginOidcSession(provider: string): Promise<{ id: string; authorizeUrl: string }> {
    const endpoint = await getApiPath('/api/rest/oidc/session');

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ provider }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Failed to start OIDC login');
    }

    const data = await response.json();

    if (!data.id || !data.authorizeUrl) {
      throw new Error('Unexpected response starting OIDC login');
    }

    return { id: data.id, authorizeUrl: data.authorizeUrl };
  }

  /** Polls a pending OIDC login attempt started via {@link beginOidcSession}. */
  async pollOidcSession(id: string): Promise<{
    status: 'PENDING' | 'COMPLETE' | 'FAILED';
    session?: { userId?: string; level?: string; sessionSecret?: string; expiry?: number };
    reason?: string;
  }> {
    const endpoint = await getApiPath(`/api/rest/oidc/session/${id}`);
    const response = await fetch(endpoint, { credentials: 'include' });

    if (response.status === 404) {
      return { status: 'FAILED', reason: 'The login attempt expired. Please try again.' };
    }

    if (!response.ok) {
      throw new Error('Failed to poll OIDC login status');
    }

    const data = await response.json();

    if (data.status === 'COMPLETE') {
      return { status: 'COMPLETE', session: this.extractOidcSessionInfo(data) };
    }

    if (data.status === 'FAILED') {
      return { status: 'FAILED', reason: data.reason || 'The login attempt failed.' };
    }

    return { status: 'PENDING' };
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
