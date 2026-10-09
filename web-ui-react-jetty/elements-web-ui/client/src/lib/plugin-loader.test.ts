import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import type { ComponentType } from 'react';
import {
  extractUiBasePaths,
  fetchPluginManifest,
  discoverAndLoadPlugins,
  PluginManifest,
} from './plugin-loader';
import { apiClient } from './api-client';
import { getApiPath } from './config';

vi.mock('./api-client', () => ({
  apiClient: { getSessionToken: vi.fn() },
}));

vi.mock('./config', () => ({
  getApiPath: vi.fn(),
}));

const mockedGetApiPath = vi.mocked(getApiPath);
const mockedGetSessionToken = vi.mocked(apiClient.getSessionToken);

describe('extractUiBasePaths', () => {
  it('captures default /app/ui/{prefix} paths', () => {
    const paths = extractUiBasePaths([{ uris: ['http://host:8080/app/ui/superuser/'] }]);
    expect(paths).toEqual(['/app/ui/superuser/']);
  });

  it('captures deployment-scoped /app/ui/{deploymentId}/{suffix} paths', () => {
    const paths = extractUiBasePaths([
      { uris: ['http://host:8080/app/ui/deploy-42/extra/'] },
    ]);
    expect(paths).toEqual(['/app/ui/deploy-42/extra/']);
  });

  it('captures a custom foreign UI base path (#102 regression)', () => {
    const paths = extractUiBasePaths([
      { uris: ['http://host:8080/agents/conductor/admin-console/ui'] },
    ]);
    expect(paths).toEqual(['/agents/conductor/admin-console/ui/']);
  });

  it('excludes /app/rest/..., /app/ws/..., /app/static/..., and /element/... paths', () => {
    const paths = extractUiBasePaths([
      { uris: ['http://host:8080/app/rest/v1/config'] },
      { uris: ['http://host:8080/app/ws/stream'] },
      { uris: ['http://host:8080/app/static/css/app.css'] },
      { uris: ['http://host:8080/element/foo/bar'] },
    ]);
    expect(paths).toEqual([]);
  });

  it('excludes empty and root paths', () => {
    const paths = extractUiBasePaths([{ uris: ['', '/'] }]);
    expect(paths).toEqual([]);
  });

  it('handles absolute URLs and relative path forms', () => {
    const paths = extractUiBasePaths([
      { uris: ['http://host:8080/app/ui/x/'] },
      { uris: ['/app/ui/y'] },
    ]);
    expect(paths).toContain('/app/ui/x/');
    expect(paths).toContain('/app/ui/y/');
  });

  it('deduplicates and normalizes trailing slashes', () => {
    const paths = extractUiBasePaths([
      { uris: ['http://host:8080/app/ui/superuser'] },
      { uris: ['http://host:8080/app/ui/superuser/'] },
      { uris: [] },
    ]);
    expect(paths).toEqual(['/app/ui/superuser/']);
  });
});

describe('fetchPluginManifest', () => {
  let originalFetch: typeof globalThis.fetch;
  let fetchMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    originalFetch = globalThis.fetch;
    fetchMock = vi.fn();
    globalThis.fetch = fetchMock as unknown as typeof fetch;
    mockedGetApiPath.mockResolvedValue('http://host:8080/api/proxy/app/ui/seg/plugin.json');
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    vi.clearAllMocks();
  });

  async function respondWith(status: number, body: unknown) {
    fetchMock.mockResolvedValue(new Response(JSON.stringify(body), { status }));
  }

  it('returns null on non-2xx', async () => {
    respondWith(404, {});
    const result = await fetchPluginManifest('/app/ui/seg/', 'seg');
    expect(result).toBeNull();
  });

  it('returns null when schema is missing', async () => {
    respondWith(200, { entries: [] });
    const result = await fetchPluginManifest('/app/ui/seg/', 'seg');
    expect(result).toBeNull();
  });

  it('returns null when entries is not an array', async () => {
    respondWith(200, { schema: 'elements/plugin', entries: 'nope' });
    const result = await fetchPluginManifest('/app/ui/seg/', 'seg');
    expect(result).toBeNull();
  });

  it('parses and returns a valid manifest', async () => {
    const manifest: PluginManifest = {
      schema: 'elements/plugin',
      entries: [{ label: 'Dashboard', icon: 'Home', bundlePath: 'dashboard.js', route: 'dashboard' }],
    };
    respondWith(200, manifest);
    const result = await fetchPluginManifest('/app/ui/seg/', 'seg');
    expect(result).toEqual(manifest);
  });

  it('returns null on network failure', async () => {
    fetchMock.mockRejectedValue(new Error('ECONNREFUSED'));
    const result = await fetchPluginManifest('/app/ui/seg/', 'seg');
    expect(result).toBeNull();
  });

  it('forwards the session token as Elements-SessionSecret when present', async () => {
    mockedGetSessionToken.mockReturnValue('secret-token');
    respondWith(200, { schema: 'elements/plugin', entries: [] });
    await fetchPluginManifest('/app/ui/seg/', 'seg');
    const [, init] = fetchMock.mock.calls[0];
    expect((init as RequestInit).headers).toMatchObject({
      'Elements-SessionSecret': 'secret-token',
    });
  });

  it('omits Elements-SessionSecret when no session token', async () => {
    mockedGetSessionToken.mockReturnValue(null);
    respondWith(200, { schema: 'elements/plugin', entries: [] });
    await fetchPluginManifest('/app/ui/seg/', 'seg');
    const [, init] = fetchMock.mock.calls[0];
    expect(init as RequestInit).not.toMatchObject({ 'Elements-SessionSecret': expect.anything() });
  });

  it('assembles the path and routes it through getApiPath', async () => {
    respondWith(200, { schema: 'elements/plugin', entries: [] });
    await fetchPluginManifest('/app/ui/custom/', 'superuser');
    expect(mockedGetApiPath).toHaveBeenCalledWith('/app/ui/custom/superuser/plugin.json');
  });
});

describe('discoverAndLoadPlugins', () => {
  const registry: Record<string, Record<string, ComponentType>> = {};
  let originalFetch: typeof globalThis.fetch;
  let realCreateElement: typeof document.createElement;
  let fetchMock: ReturnType<typeof vi.fn>;
  const responses = new Map<string, { status: number; body: unknown }>();

  function manifestFor(route: string): PluginManifest {
    return {
      schema: 'elements/plugin',
      entries: [{ label: route, icon: 'Box', bundlePath: `${route}.js`, route }],
    };
  }

  function mockManifest(pathWithHost: string, manifest: PluginManifest) {
    responses.set(pathWithHost, { status: 200, body: manifest });
  }

  function mock404(pathWithHost: string) {
    responses.set(pathWithHost, { status: 404, body: {} });
  }

  function deployContainer(basePath: string, meta: { application?: string; deploymentId?: string; deploymentName?: string }) {
    return { uris: [`http://host:8080${basePath}`], ...meta };
  }

  beforeEach(() => {
    originalFetch = globalThis.fetch;
    realCreateElement = document.createElement.bind(document);
    fetchMock = vi.fn();
    globalThis.fetch = fetchMock as unknown as typeof fetch;
    registry[''] = {};

    window.__elementsPlugins = {
      _registry: registry,
      _activeNamespace: null,
      register: (route, component) => {
        const ns = window.__elementsPlugins._activeNamespace ?? '';
        (registry[ns] ??= {})[route] = component;
      },
    };

    // Failing bundles carry "broken.js"; everything else simulates a loaded plugin that
    // self-registers under the route encoded in its bundle filename. The load promise is
    // resolved by loadPluginBundle's onload/onerror handler.
    vi.spyOn(document, 'createElement').mockImplementation(((tag: string, options?: ElementCreationOptions) => {
      const el = realCreateElement(tag, options);
      if (tag === 'script') {
        Object.defineProperty(el, 'src', {
          set(value: string) {
            const that = this as HTMLScriptElement & { _srcVal?: string };
            that._srcVal = value;
            queueMicrotask(() => {
              const src = that._srcVal ?? '';
              const route = src.split('/').pop()?.replace(/\.js$/, '');
              if (route && !src.includes('broken.js')) {
                const ns = window.__elementsPlugins._activeNamespace ?? '';
                registry[ns] ??= {};
                registry[ns][route] = (() => null) as unknown as ComponentType;
              }
              (that.onerror ?? that.onload ?? ((_event: Event) => {}))(new Event('error'));
            });
          },
          get() {
            return (this as HTMLScriptElement & { _srcVal?: string })._srcVal ?? '';
          },
        });
      }
      return el;
    }) as typeof document.createElement);

    mockedGetApiPath.mockImplementation((p) => Promise.resolve(`http://host:8080${p}`));
    mockedGetSessionToken.mockReturnValue(null);
    fetchMock.mockImplementation((input: RequestInfo | URL) => {
      const rawUrl = typeof input === 'string' ? input : input instanceof URL ? input.href : (input as Request).url;
      const url = rawUrl.replace(/^http:\/\/host:8080/, '');
      const entry = responses.get(url);
      if (!entry) return Promise.resolve(new Response('{}', { status: 404 }));
      return Promise.resolve(new Response(JSON.stringify(entry.body), { status: entry.status }));
    });
  });

  afterEach(() => {
    globalThis.fetch = originalFetch;
    vi.restoreAllMocks();
    vi.clearAllMocks();
    responses.clear();
    for (const key of Object.keys(registry)) delete registry[key];
  });

  it('yields a loaded plugin for a container exposing only a custom UI URI (#102 regression)', async () => {
    mockManifest('/agents/conductor/admin-console/ui/superuser/plugin.json', manifestFor('dashboard'));
    const containers = [deployContainer('/agents/conductor/admin-console/ui/', { application: 'conductor' })];

    const plugins = await discoverAndLoadPlugins(containers, 'superuser');

    expect(plugins).toHaveLength(1);
    expect(plugins[0].qualifiedKey).toBe('conductor:dashboard');
    expect(plugins[0].route).toBe('dashboard');
  });

  it('yields no plugin entries for REST/WS/static URI-only containers', async () => {
    const containers = [
      {
        uris: ['http://host:8080/app/rest/v1', 'http://host:8080/app/ws/stream', 'http://host:8080/app/static/css/'],
        application: 'app',
      },
    ];
    const plugins = await discoverAndLoadPlugins(containers, 'superuser');
    expect(plugins).toEqual([]);
  });

  it('applies qualifiedKey precedence application -> deploymentName -> deploymentId', async () => {
    mockManifest('/app/ui/a/superuser/plugin.json', manifestFor('one'));
    mockManifest('/app/ui/b/superuser/plugin.json', manifestFor('two'));
    mockManifest('/app/ui/c/superuser/plugin.json', manifestFor('three'));

    const containers = [
      { uris: ['http://host:8080/app/ui/a/'], application: 'app', deploymentName: 'byName', deploymentId: 'byId' },
      { uris: ['http://host:8080/app/ui/b/'], deploymentName: 'named', deploymentId: 'idonly' },
      { uris: ['http://host:8080/app/ui/c/'], deploymentId: 'deploy-3' },
    ];

    const plugins = await discoverAndLoadPlugins(containers, 'superuser');

    expect(plugins.map(p => p.qualifiedKey).sort()).toEqual([
      'app:one',
      'deploy-3:three',
      'named:two',
    ]);
  });

  it('suppresses duplicate qualified keys across deployments with a warning', async () => {
    const warnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    mockManifest('/app/ui/a/superuser/plugin.json', manifestFor('same'));
    mockManifest('/app/ui/b/superuser/plugin.json', manifestFor('same'));

    const containers = [
      deployContainer('/app/ui/a/', { application: 'dup-app' }),
      deployContainer('/app/ui/b/', { application: 'dup-app' }),
    ];

    const plugins = await discoverAndLoadPlugins(containers, 'superuser');

    expect(plugins).toHaveLength(1);
    expect(plugins[0].qualifiedKey).toBe('dup-app:same');
    expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('dup-app:same'));
    warnSpy.mockRestore();
  });

  it('de-duplicates the same UI base path within one deployment', async () => {
    mockManifest('/app/ui/x/superuser/plugin.json', manifestFor('dash'));

    const containers = [
      { uris: ['http://host:8080/app/ui/x/', 'http://host:8080/app/ui/x/'], application: 'appA', deploymentId: 'dd1' },
      { uris: ['http://host:8080/app/ui/x/'], application: 'appB', deploymentId: 'dd1' },
    ];

    const plugins = await discoverAndLoadPlugins(containers, 'superuser');

    // One manifest fetch, one load: the shared base path is only processed once per namespace.
    expect(plugins).toHaveLength(1);
    expect(plugins[0].qualifiedKey).toBe('appA:dash');
    expect(mockedGetApiPath.mock.calls.filter(([p]) => p.includes('/plugin.json'))).toHaveLength(1);
  });

  it('skips stale manifests and failing bundles without breaking other entries', async () => {
    mockManifest('/app/ui/good/superuser/plugin.json', manifestFor('good'));
    mock404('/app/ui/stale/superuser/plugin.json');
    mockManifest('/app/ui/broken/superuser/plugin.json', {
      schema: 'elements/plugin',
      entries: [{ label: 'broken', icon: 'Box', bundlePath: 'broken.js', route: 'broken' }],
    });

    const containers = [
      deployContainer('/app/ui/good/', { application: 'good-app' }),
      deployContainer('/app/ui/stale/', { application: 'stale-app' }),
      deployContainer('/app/ui/broken/', { application: 'broken-app' }),
    ];

    const plugins = await discoverAndLoadPlugins(containers, 'superuser');

    expect(plugins).toHaveLength(1);
    expect(plugins[0].qualifiedKey).toBe('good-app:good');
  });
});