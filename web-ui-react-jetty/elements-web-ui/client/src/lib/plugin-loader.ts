import { getApiPath } from './config';
import { apiClient } from './api-client';

export interface PluginEntry {
  label: string;
  icon: string;
  bundlePath: string;
  route: string;
}

export interface PluginManifest {
  schema: string;
  entries: PluginEntry[];
}

export interface LoadedPlugin {
  label: string;
  icon: string;
  route: string;
  component: React.ComponentType;
}

declare global {
  interface Window {
    React: typeof import('react');
    __elementsApiClient: {
      getSessionToken(): string | null;
      setSessionToken(token: string | null): void;
    };
    __elementsSettings: {
      getResultsPerPage(): number;
    };
    __elementsPlugins: {
      _registry: Record<string, React.ComponentType>;
      register(route: string, component: React.ComponentType): void;
    };
  }
}

/**
 * Extracts /app/ui/ path segments from container URIs.
 * Containers expose absolute URIs like http://host:port/app/ui/prefix/.
 */
export function extractUiBasePaths(containers: Array<{ uris?: string[] }>): string[] {
  const paths: string[] = [];
  for (const container of containers) {
    for (const uri of container.uris ?? []) {
      try {
        const url = new URL(uri);
        if (url.pathname.includes('/app/ui/')) {
          const basePath = url.pathname.endsWith('/') ? url.pathname : url.pathname + '/';
          paths.push(basePath);
        }
      } catch {
        // Try treating as a relative path
        if (uri.includes('/app/ui/')) {
          const basePath = uri.endsWith('/') ? uri : uri + '/';
          paths.push(basePath);
        }
      }
    }
  }
  return [...new Set(paths)];
}

/**
 * Fetches {segment}/plugin.json from the given UI base path.
 * Returns null on 404 or any error (silently skipped).
 */
export async function fetchPluginManifest(uiBasePath: string, segment: string): Promise<PluginManifest | null> {
  try {
    const manifestPath = `${uiBasePath}${segment}/plugin.json`;
    const fullUrl = await getApiPath(manifestPath);
    const sessionToken = apiClient.getSessionToken();
    const headers: Record<string, string> = {};
    if (sessionToken) {
      headers['Elements-SessionSecret'] = sessionToken;
    }
    const response = await fetch(fullUrl, { headers, credentials: 'include' });
    if (!response.ok) {
      return null;
    }
    const data = await response.json();
    if (!data?.schema || !Array.isArray(data?.entries)) {
      return null;
    }
    return data as PluginManifest;
  } catch {
    return null;
  }
}

/**
 * Injects a <script> tag and waits for it to load or error.
 * Resolves in both cases so one failed bundle doesn't block others.
 */
export function loadPluginBundle(bundleUrl: string): Promise<void> {
  return new Promise((resolve) => {
    const script = document.createElement('script');
    script.src = bundleUrl;
    script.onload = () => resolve();
    script.onerror = () => resolve();
    document.head.appendChild(script);
  });
}

/**
 * Orchestrates full plugin discovery and loading from a list of containers.
 * @param segment - UI content segment directory, e.g. 'superuser' or 'user'.
 * Returns successfully loaded plugins; failures are silently skipped.
 */
export async function discoverAndLoadPlugins(
  containers: Array<{ uris?: string[] }>,
  segment: string
): Promise<LoadedPlugin[]> {
  const uiBasePaths = extractUiBasePaths(containers);
  const loadedPlugins: LoadedPlugin[] = [];

  for (const uiBasePath of uiBasePaths) {
    const manifest = await fetchPluginManifest(uiBasePath, segment);
    if (!manifest) continue;

    for (const entry of manifest.entries) {
      try {
        const bundleRelPath = `${uiBasePath}${segment}/${entry.bundlePath}`;
        const bundleUrl = await getApiPath(bundleRelPath);
        await loadPluginBundle(bundleUrl);

        const component = window.__elementsPlugins?._registry?.[entry.route];
        if (component) {
          loadedPlugins.push({
            label: entry.label,
            icon: entry.icon,
            route: entry.route,
            component,
          });
        }
      } catch {
        // Error loading this entry: skip it
      }
    }
  }

  return loadedPlugins;
}
