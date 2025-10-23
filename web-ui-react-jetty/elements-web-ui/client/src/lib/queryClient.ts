import { QueryClient, QueryFunction } from "@tanstack/react-query";
import { getApiPath } from "./config";

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
  const headers: HeadersInit = {};

  if (data) {
    headers["Content-Type"] = "application/json";
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
          // Get the correct API path based on production vs development
          const queryPath = queryKey.join("/") as string;
          const fullPath = await getApiPath(queryPath);

          const res = await fetch(fullPath, {
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
