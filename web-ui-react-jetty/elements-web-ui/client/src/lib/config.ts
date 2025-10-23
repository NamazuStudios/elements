// Centralized configuration loader for production vs development API routing
// In production: calls Elements backend directly at /api/rest
// In development: calls Node.js proxy at /api/proxy

let cachedConfig: { baseUrl: string; mode: 'production' | 'development' } | null = null;

export async function getApiConfig(): Promise<{ baseUrl: string; mode: 'production' | 'development' }> {
    // Return cached config if already loaded
    if (cachedConfig) {
        return cachedConfig;
    }

    try {
        const configResponse = await fetch('./config.json');
        if (configResponse.ok) {
            const config = await configResponse.json();
            if (config?.api?.url) {
                // Production mode: Extract path from backend URL
                // e.g., "http://backend:8080/api/rest" -> "/api/rest"
                const url = new URL(config.api.url);
                cachedConfig = {
                    baseUrl: url.pathname,
                    mode: 'production'
                };
                return cachedConfig;
            }
        }
    } catch (error) {
        // Config not available, use development proxy
    }

    // Development mode: use Node.js proxy
    cachedConfig = {
        baseUrl: '/api/proxy',
        mode: 'development'
    };
    return cachedConfig;
}

// Helper to get the full API path
export async function getApiPath(path: string): Promise<string> {
    const config = await getApiConfig();

    // If path already starts with /api/proxy, strip it in production mode
    if (config.mode === 'production' && path.startsWith('/api/proxy')) {
        // Remove /api/proxy prefix - the rest is already the correct path
        // e.g., "/api/proxy/api/rest/health" → "/api/rest/health"
        return path.replace('/api/proxy', '');
    }

    // If path starts with absolute /api/rest, use it as-is in production
    if (config.mode === 'production' && path.startsWith('/api/rest')) {
        return path;
    }

    // For development, prefix with /api/proxy if not already present
    if (config.mode === 'development' && !path.startsWith('/api/proxy')) {
        return `${config.baseUrl}${path}`;
    }

    return path;
}

// Reset cache (useful for testing)
export function resetConfigCache(): void {
    cachedConfig = null;
}
