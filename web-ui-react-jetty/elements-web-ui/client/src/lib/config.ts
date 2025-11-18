// Centralized configuration loader for production vs development API routing
// In production: calls Elements backend directly at /api/rest
// In development: calls Node.js proxy at /api/proxy

let cachedConfig: { baseUrl: string; mode: 'production' | 'development' } | null = null;

export async function getApiConfig(): Promise<{ baseUrl: string; mode: 'production' | 'development' }> {
  // Return cached config if already loaded
  if (cachedConfig) {
    console.log('[CONFIG] Returning cached config:', cachedConfig);
    return cachedConfig;
  }

  // Check if running in development mode (Vite dev server)
  // Only use DEV flag - BASE_URL can be '/' in both dev and prod
  if (import.meta.env.DEV) {
    console.log('[CONFIG] Development mode detected (DEV:', import.meta.env.DEV, ')');
    cachedConfig = {
      baseUrl: '/api/proxy',
      mode: 'development'
    };
    console.log('[CONFIG] ✓ Using development proxy. Base URL:', cachedConfig.baseUrl);
    return cachedConfig;
  }

  console.log('[CONFIG] Production mode - loading config.json...');
  try {
    // Normalize BASE_URL to ensure it ends with a trailing slash
    let baseUrl = import.meta.env.BASE_URL || '/';
    if (!baseUrl.endsWith('/')) {
      baseUrl = baseUrl + '/';
    }
    const configPath = `${baseUrl}config.json`;
    console.log('[CONFIG] Fetching config from:', configPath);
    const configResponse = await fetch(configPath);
    console.log('[CONFIG] config.json response status:', configResponse.status);
    
    if (configResponse.ok) {
      const config = await configResponse.json();
      console.log('[CONFIG] Loaded config:', config);
      
      if (config?.api?.url) {
        // Production mode: Store full backend URL
        // This allows us to handle both same-origin and cross-origin backends
        cachedConfig = {
          baseUrl: config.api.url,
          mode: 'production'
        };
        console.log('[CONFIG] ✓ Production mode detected. Backend URL:', cachedConfig.baseUrl);
        return cachedConfig;
      } else {
        console.warn('[CONFIG] ⚠️ config.json loaded but missing api.url field. Config:', config);
      }
    } else {
      console.warn('[CONFIG] ⚠️ config.json fetch failed with status:', configResponse.status);
    }
  } catch (error) {
    console.log('[CONFIG] Failed to load config.json:', error);
  }

  // Fallback to development mode
  cachedConfig = {
    baseUrl: '/api/proxy',
    mode: 'development'
  };
  console.log('[CONFIG] ✓ Fallback to development mode. Base URL:', cachedConfig.baseUrl);
  return cachedConfig;
}

// Helper to get the full API path
export async function getApiPath(path: string): Promise<string> {
  const config = await getApiConfig();
  
  console.log('[getApiPath] Input path:', path);
  console.log('[getApiPath] Mode:', config.mode);
  console.log('[getApiPath] Base URL:', config.baseUrl);
  
  // Development mode: prefix with /api/proxy if not already present
  if (config.mode === 'development' && !path.startsWith('/api/proxy')) {
    // Normalize relative paths (./path -> /path)
    let normalizedPath = path.startsWith('./') ? path.substring(1) : path;
    // Ensure path starts with / for proper concatenation
    if (!normalizedPath.startsWith('/')) {
      normalizedPath = '/' + normalizedPath;
    }
    const result = `${config.baseUrl}${normalizedPath}`;
    console.log('[getApiPath] Development: Adding proxy prefix → ', result);
    return result;
  }
  
  // Production mode: handle both same-origin and cross-origin backends
  if (config.mode === 'production') {
    // Strip /api/proxy prefix if present (legacy path format)
    let cleanPath = path;
    if (path.startsWith('/api/proxy')) {
      cleanPath = path.replace('/api/proxy', '');
      console.log('[getApiPath] Production: Stripped /api/proxy prefix → ', cleanPath);
    }
    
    // Check if baseUrl is a full URL (http:// or https://)
    if (config.baseUrl.startsWith('http://') || config.baseUrl.startsWith('https://')) {
      const backendUrl = new URL(config.baseUrl);
      const isSameOrigin = typeof window !== 'undefined' && 
                          backendUrl.origin === window.location.origin;
      
      if (isSameOrigin) {
        // Same origin: use just the pathname to avoid CORS preflight
        const result = cleanPath.startsWith('/') ? cleanPath : `/${cleanPath}`;
        console.log('[getApiPath] Production (same-origin): Using pathname → ', result);
        return result;
      } else {
        // Cross-origin: use full URL
        // Replace the backend's pathname with our request path
        const fullUrl = new URL(cleanPath, backendUrl.origin).href;
        console.log('[getApiPath] Production (cross-origin): Using full URL → ', fullUrl);
        return fullUrl;
      }
    }
    
    // baseUrl is just a pathname (fallback for legacy configs)
    const result = cleanPath.startsWith('/') ? cleanPath : `/${cleanPath}`;
    console.log('[getApiPath] Production (pathname only): Using path as-is → ', result);
    return result;
  }
  
  console.log('[getApiPath] Fallback: Returning path as-is → ', path);
  return path;
}

// Reset cache (useful for testing)
export function resetConfigCache(): void {
  cachedConfig = null;
}
