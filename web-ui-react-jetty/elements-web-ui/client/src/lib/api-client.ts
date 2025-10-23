const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/proxy';

export class ApiClient {
  // Cookies are now managed by the browser, no need for localStorage
  // Session token is stored in HTTP-only cookie and sent automatically

  async request<T>(endpoint: string, options: RequestInit & { suppressAuthRedirect?: boolean } = {}): Promise<T> {
    const { suppressAuthRedirect, ...fetchOptions } = options;
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...fetchOptions.headers,
    };

    // Credentials: 'include' ensures cookies are sent with requests
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...fetchOptions,
      headers,
      credentials: 'include', // Send cookies with all requests
    });

    if (!response.ok) {
      // Handle session expiry/invalid tokens
      if (response.status === 401 || response.status === 403) {
        if (!suppressAuthRedirect) {
          // Session expired, redirect to login
          window.location.href = '/login';
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
    // Try to get backend URL from config
    let backendUrl: string | null = null;
    let useProxy = false;

    try {
      const configResponse = await fetch('./config.json');
      if (configResponse.ok) {
        const config = await configResponse.json();
        if (config?.api?.url) {
          // When served by Java, the config URL is the absolute backend URL
          // Use relative path to avoid CORS (same origin)
          const fullUrl = config.api.url;
          // Extract just the path portion for same-origin calls
          const url = new URL(fullUrl);
          backendUrl = url.pathname; // e.g., "/api/rest"
        }
      }
    } catch (error) {
      // Config not available, use proxy (development mode)
      useProxy = true;
    }

    // Determine endpoint and request format
    const loginEndpoint = useProxy
        ? '/api/auth/login'
        : backendUrl
            ? `${backendUrl}/session`
            : '/api/auth/login';

    const requestBody = useProxy
        ? { username, password, rememberMe }
        : { userId: username, password: password };

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

    // If calling Elements backend directly, format response to match proxy format
    if (!useProxy && backendUrl) {
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
    await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'include',
    });
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
