import { QueryClient, QueryFunction } from "@tanstack/react-query";
import { getApiPath, getApiConfig } from "./config";
import { apiClient } from "./api-client";

// Helper function to determine if current route is a core resource page
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
  
  console.log('[QUERY-AUTH] Path check - pathname:', path, 'basePath:', basePath, 'relativePath:', relativePath, 'isCoreResource:', isCoreResource, 'isApiExplorer:', isApiExplorer, 'result:', isCoreResource && !isApiExplorer);
  
  return isCoreResource && !isApiExplorer;
}

// Helper function to handle session expiration
function handleSessionExpired() {
  console.log('[QUERY] Session expired or invalid - redirecting to login');
  
  // Clear session token
  apiClient.setSessionToken(null);
  localStorage.removeItem('elements-user');
  
  // Redirect to login with correct base path
  const basePath = import.meta.env.BASE_URL || '/';
  // In development, app is at /admin/ even when BASE_URL is /
  // In production, BASE_URL will be set correctly (e.g., /admin/)
  const isDev = import.meta.env.DEV;
  const adminPath = isDev && basePath === '/' ? '/admin' : basePath;
  const loginPath = adminPath.endsWith('/') 
    ? `${adminPath}login?expired=true` 
    : `${adminPath}/login?expired=true`;
  console.log('[QUERY] Redirecting to:', loginPath);
  window.location.href = loginPath;
}

async function throwIfResNotOk(res: Response) {
  if (!res.ok) {
    // Handle 403 errors (session expired/invalid)
    if (res.status === 403 && isCoreResourcePage()) {
      handleSessionExpired();
      throw new Error('Session expired. Redirecting to login...');
    }
    
    const text = (await res.text()) || res.statusText;
    throw new Error(`${res.status}: ${text}`);
  }
}

export async function apiRequest(
  method: string,
  url: string,
  data?: unknown | undefined,
): Promise<Response> {
  const headers: Record<string, string> = {};
  
  if (data) {
    headers["Content-Type"] = "application/json";
  }

  // Add session token header in both development and production
  const config = await getApiConfig();
  const sessionToken = apiClient.getSessionToken();
  if (sessionToken) {
    headers['Elements-SessionSecret'] = sessionToken;
  }

  // Get the correct API path based on production vs development
  const fullPath = await getApiPath(url);

  const res = await fetch(fullPath, {
    method,
    headers,
    body: data ? JSON.stringify(data) : undefined,
    credentials: "include",
  });

  await throwIfResNotOk(res);
  return res;
}

type UnauthorizedBehavior = "returnNull" | "throw";
export const getQueryFn: <T>(options: {
  on401: UnauthorizedBehavior;
}) => QueryFunction<T> =
  ({ on401: unauthorizedBehavior }) =>
  async ({ queryKey }) => {
    // Build headers object
    const headers: Record<string, string> = {};
    
    // Get config and session token
    const config = await getApiConfig();
    const sessionToken = apiClient.getSessionToken();
    
    console.log('[QUERY] Fetching:', queryKey);
    console.log('[QUERY] Config mode:', config.mode);
    
    // Add session token header in both development and production
    if (sessionToken) {
      headers['Elements-SessionSecret'] = sessionToken;
      console.log('[QUERY] ✓ Added Elements-SessionSecret header');
    } else {
      console.warn('[QUERY] ✗ NO session token! Request may fail if authentication required.');
    }
    
    // Get the correct API path based on production vs development
    const queryPath = queryKey.join("/") as string;
    const fullPath = await getApiPath(queryPath);
    
    console.log('[QUERY] Full path:', fullPath);
    console.log('[QUERY] Headers:', headers);
    
    const res = await fetch(fullPath, {
      headers,
      credentials: "include",
    });

    // Handle 401/403 errors - session expired or invalid
    if (res.status === 401 || res.status === 403) {
      if (unauthorizedBehavior === "returnNull" && res.status === 401) {
        return null;
      }
      
      // For 403, or 401 when not returnNull behavior, check if we should redirect
      if (isCoreResourcePage()) {
        handleSessionExpired();
        throw new Error('Session expired. Redirecting to login...');
      }
    }

    await throwIfResNotOk(res);
    return await res.json();
  };

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      queryFn: getQueryFn({ on401: "throw" }),
      refetchInterval: false,
      refetchOnWindowFocus: false,
      staleTime: Infinity,
      retry: false,
    },
    mutations: {
      retry: false,
    },
  },
});
