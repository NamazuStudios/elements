import { QueryClient, QueryFunction } from "@tanstack/react-query";
import { getApiPath, getApiConfig } from "./config";
import { apiClient } from "./api-client";

async function throwIfResNotOk(res: Response) {
  if (!res.ok) {
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

    if (unauthorizedBehavior === "returnNull" && res.status === 401) {
      return null;
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
