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
  qualifiedKey: string;
  component: React.ComponentType;
  application?: string;
  deploymentId?: string;
  deploymentName?: string;
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
      _registry: Record<string, Record<string, React.ComponentType>>;
      _activeNamespace: string | null;
      register(route: string, component: React.ComponentType): void;
    };
  }
}

/**
 * Path prefixes produced by the other element loaders. Anything the backend reports that
 * isn't one of these is treated as a candidate UI base path: UI content is normally mounted
 * at {@code /app/ui/{...}} but its full URI can be moved anywhere via the element's
 * {@code UI_CONTENT_URI} override, so UI bases can no longer be identified by substring alone.
 */
const NON_UI_PATH_PREFIXES = ['/app/rest/', '/app/ws/', '/app/static/', '/element/'];

function isNonUiPath(path: string): boolean {
  return path === '' || path === '/' || NON_UI_PATH_PREFIXES.some(prefix => path.startsWith(prefix));
}

function normalizeBasePath(path: string): string {
  return path.endsWith('/') ? path : path + '/';
}

/**
 * Extracts candidate UI base paths from container URIs.
 * Containers expose absolute URIs; the path portion is used, e.g.
 * http://host:port/app/ui/prefix/ or http://host:port/custom/ui/path.
 */
export function extractUiBasePaths(containers: Array<{ uris?: string[] }>): string[] {
  const paths: string[] = [];
  for (const container of containers) {
    for (const uri of container.uris ?? []) {
      try {
        const url = new URL(uri);
        if (!isNonUiPath(url.pathname)) {
          paths.push(normalizeBasePath(url.pathname));
        }
      } catch {
        // Try treating as a relative path
        if (!isNonUiPath(uri)) {
          paths.push(normalizeBasePath(uri));
        }
      }
    }
  }
  return Array.from(new Set(paths));
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
 *
 * Each plugin is registered into a namespace keyed by its deployment id, then
 * surfaced with a `qualifiedKey` of `{application ?? deploymentName ?? deploymentId}:{route}`
 * so that deployments sharing a `route` value never collide in the registry,
 * sidebar, or plugin URL.
 */
export async function discoverAndLoadPlugins(
  containers: Array<{ uris?: string[]; application?: string; deploymentId?: string; deploymentName?: string }>,
  segment: string
): Promise<LoadedPlugin[]> {
  const loadedPlugins: LoadedPlugin[] = [];
  const seenPathsByNamespace = new Map<string, Set<string>>();
  const seenQualifiedKeys = new Set<string>();

  for (const container of containers) {
    const qualifier = container.application ?? container.deploymentName ?? container.deploymentId;
    const namespace = container.deploymentId ?? '';

    const containerPaths = seenPathsByNamespace.get(namespace) ?? new Set<string>();
    seenPathsByNamespace.set(namespace, containerPaths);

    const uiBasePaths = extractUiBasePaths([container]);
    for (const uiBasePath of uiBasePaths) {
      if (containerPaths.has(uiBasePath)) continue;
      containerPaths.add(uiBasePath);

      const manifest = await fetchPluginManifest(uiBasePath, segment);
      if (!manifest) continue;

      for (const entry of manifest.entries) {
        try {
          if (!qualifier) {
            console.warn(
              `[Elements] Skipping plugin entry "${entry.route}": its container has no application, deployment name, or deployment id to qualify it.`
            );
            continue;
          }

          const bundleRelPath = `${uiBasePath}${segment}/${entry.bundlePath}`;
          const bundleUrl = await getApiPath(bundleRelPath);

          window.__elementsPlugins._activeNamespace = namespace;
          try {
            await loadPluginBundle(bundleUrl);
          } finally {
            window.__elementsPlugins._activeNamespace = null;
          }

          const component = window.__elementsPlugins?._registry?.[namespace]?.[entry.route];
          if (component) {
            const qualifiedKey = `${qualifier}:${entry.route}`;
            if (seenQualifiedKeys.has(qualifiedKey)) {
              console.warn(
                `[Elements] Plugin menu collision: multiple deployments expose "${qualifiedKey}". Only the first is shown; the later registration is ignored.`
              );
              continue;
            }
            seenQualifiedKeys.add(qualifiedKey);
            loadedPlugins.push({
              label: entry.label,
              icon: entry.icon,
              route: entry.route,
              qualifiedKey,
              component,
              application: container.application,
              deploymentId: container.deploymentId,
              deploymentName: container.deploymentName,
            });
          }
        } catch {
          // Error loading this entry: skip it
        }
      }
    }
  }

  return loadedPlugins;
}
