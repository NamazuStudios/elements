import fs from 'fs';
import path from 'path';

interface Config {
  apiUrl: string;
}

let cachedConfig: Config | null = null;

/**
 * Reads configuration from /config/config.json or falls back to environment variables
 * The config file is expected to be created by the Java application at runtime
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
      const config = JSON.parse(configContent) as Config;
      
      if (config.apiUrl) {
        cachedConfig = config;
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
 * Gets the Elements backend URL
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
