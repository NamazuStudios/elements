import fs from 'fs';
import path from 'path';

interface RawConfig {
  api: {
    url: string;
  };
}

interface Config {
  apiUrl: string;
}

let cachedConfig: Config | null = null;

/**
 * Reads configuration from /config/config.json or falls back to environment variables
 * The config file is expected to be created by the Java application at runtime
 *
 * Expected format: {"api": {"url": "http://localhost:8080/api/rest"}}
 */
export function getConfig(): Config {
  if (cachedConfig) {
    return cachedConfig;
  }

  // Try reading from config file first
  const configPath = '/config/config.json';

  try {
    if (fs.existsSync(configPath)) {
      const configContent = fs.readFileSync(configPath, 'utf-8');
      const rawConfig = JSON.parse(configContent) as RawConfig;

      if (rawConfig.api?.url) {
        // Extract base URL without /api/rest suffix
        let baseUrl = rawConfig.api.url;
        // Remove /api/rest suffix if present to get the base backend URL
        baseUrl = baseUrl.replace(/\/api\/rest\/?$/, '');

        cachedConfig = { apiUrl: baseUrl };
        console.log('[CONFIG] Loaded configuration from /config/config.json');
        return cachedConfig;
      }
    }
  } catch (error) {
    console.warn('[CONFIG] Failed to read /config/config.json:', error);
  }

  // Fallback to environment variables for development
  const apiUrl = process.env.ELEMENTS_BACKEND_URL || 'http://localhost:8080';
  cachedConfig = { apiUrl };

  console.log('[CONFIG] Using environment variable fallback');
  return cachedConfig;
}

/**
 * Gets the Elements backend URL (base URL without /api/rest)
 */
export function getBackendUrl(): string {
  return getConfig().apiUrl;
}

/**
 * Clears the cached configuration (useful for testing or hot-reloading)
 */
export function clearConfigCache(): void {
  cachedConfig = null;
}
