import { useMemo, useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useLocation } from 'wouter';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { Loader2, Package, Globe, RefreshCw, ChevronDown } from 'lucide-react';
import { queryClient } from '@/lib/queryClient';

interface ElementMetadata {
  type: string;
  definition: {
    name: string;
    recursive: boolean;
    additionalPackages: Array<any>;
    loader: string;
  };
  services: Array<any>;
  producedEvents: Array<any>;
  consumedEvents: Array<any>;
  dependencies: Array<any>;
  attributes: Record<string, any>;
  defaultAttributes: Array<any>;
}

interface ElementContainerStatus {
  runtime: {
    deployment: {
      id: string;
      application?: { id: string; name: string; description?: string; };
    };
  };
  status: string;
  uris: string[];
  logs: string[];
  elements: ElementMetadata[];
}

interface GroupedItem {
  element: ElementMetadata;
  container: ElementContainerStatus;
}

interface AppGroup {
  label: string;
  isGlobal: boolean;
  items: GroupedItem[];
}

function getStatusDotColor(status: string): string {
  switch (status?.toUpperCase()) {
    case 'LOADING':
      return 'bg-blue-500';
    case 'CLEAN':
    case 'RUNNING':
    case 'ACTIVE':
      return 'bg-green-500';
    case 'UNSTABLE':
    case 'DEGRADED':
      return 'bg-orange-500';
    case 'FAILED':
    case 'ERROR':
      return 'bg-red-500';
    default:
      return 'bg-gray-400';
  }
}

export default function InstalledElements() {
  const [_, setLocation] = useLocation();
  const [pollingActive, setPollingActive] = useState(false);

  const { data: containers, isLoading } = useQuery<ElementContainerStatus[]>({
    queryKey: ['/api/rest/elements/container'],
    refetchInterval: pollingActive ? 3000 : false,
  });

  // Auto-poll while any container is still initialising in the background.
  useEffect(() => {
    setPollingActive(containers?.some(c => c.status === 'LOADING') ?? false);
  }, [containers]);

  // Group all elements by application (or global), flattening across deployments
  const groups = useMemo<[string, AppGroup][]>(() => {
    const byApp = new Map<string, AppGroup>();

    for (const container of containers ?? []) {
      if (!container.elements?.length) continue;
      const app = container.runtime?.deployment?.application;
      const key = app?.id ?? '__global__';

      if (!byApp.has(key)) {
        byApp.set(key, {
          label: app?.name || app?.id || 'Global',
          isGlobal: !app,
          items: [],
        });
      }

      for (const element of container.elements) {
        byApp.get(key)!.items.push({ element, container });
      }
    }

    // App sections alphabetically, global last
    return Array.from(byApp.entries()).sort(([, a], [, b]) => {
      if (a.isGlobal) return 1;
      if (b.isGlobal) return -1;
      return a.label.localeCompare(b.label);
    });
  }, [containers]);

  return (
    <div className="h-full flex flex-col space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-3xl font-bold" data-testid="text-page-title">Element APIs</h1>
          <p className="text-muted-foreground mt-1">
            Select an element to explore its API endpoints
          </p>
        </div>
        <Button
          variant="outline"
          size="icon"
          onClick={() => queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/container'] })}
          data-testid="button-refresh-elements"
        >
          <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
        </div>
      ) : groups.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
          <Package className="w-12 h-12 mb-4 opacity-50" />
          <p className="text-lg font-medium">No Active Elements</p>
          <p className="text-sm mt-2">No containers are currently serving element APIs.</p>
        </div>
      ) : (
        <div className="space-y-1">
          {groups.map(([key, group]) => (
            <Collapsible key={key} defaultOpen className="group/collapsible">
              <CollapsibleTrigger className="flex w-full items-center gap-2 rounded-md px-2 py-2 text-sm font-semibold hover-elevate">
                {group.isGlobal
                  ? <Globe className="w-4 h-4 text-muted-foreground shrink-0" />
                  : <Package className="w-4 h-4 text-muted-foreground shrink-0" />
                }
                <span>{group.label}</span>
                <Badge variant="secondary" className="text-xs font-normal">
                  {group.items.length}
                </Badge>
                <ChevronDown className="w-4 h-4 text-muted-foreground ml-auto transition-transform group-data-[state=open]/collapsible:rotate-180" />
              </CollapsibleTrigger>

              <CollapsibleContent>
                <div className="mt-1 ml-2 space-y-1 border-l pl-4 pb-2">
                  {group.items.map(({ element, container }, idx) => {
                    const elementName = element.definition?.name;
                    const displayName = elementName?.split('.').pop() || `Element ${idx + 1}`;
                    const deploymentId = container.runtime?.deployment?.id;
                    const isTransient = deploymentId?.match(/^T\d/) != null;
                    const servePrefix = element.attributes?.['dev.getelements.elements.app.serve.prefix'];
                    const rsRoot = element.attributes?.['dev.getelements.elements.element.rs.root'];
                    const uri = servePrefix
                      ? container.uris?.find(u => u.includes('/' + servePrefix))
                      : rsRoot
                        ? container.uris?.find(u => { try { return new URL(u).pathname.startsWith(rsRoot as string); } catch { return u.includes(rsRoot as string); } })
                        : container.uris?.[0];

                    const params = new URLSearchParams();
                    if (deploymentId) params.set('deployment', deploymentId);
                    if (elementName) params.set('element', elementName);
                    if (uri) params.set('uri', uri);

                    return (
                      <button
                        key={`${elementName}-${idx}`}
                        onClick={() => setLocation(`/element-api-explorer?${params.toString()}`)}
                        className="w-full text-left flex items-center gap-3 rounded-md px-3 py-2 hover-elevate border bg-card"
                        data-testid={`button-element-${displayName}`}
                      >
                        <div className={`w-2 h-2 rounded-full shrink-0 ${getStatusDotColor(container.status)} ${container.status === 'LOADING' ? 'animate-pulse' : ''}`} />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="font-medium text-sm">{displayName}</span>
                            <Badge
                              variant={isTransient ? 'secondary' : 'outline'}
                              className="text-[10px] font-normal"
                            >
                              {isTransient ? 'Transient' : 'Deployment'}
                            </Badge>
                          </div>
                          <p className="text-xs text-muted-foreground font-mono truncate mt-0.5">
                            {elementName}
                          </p>
                        </div>
                      </button>
                    );
                  })}
                </div>
              </CollapsibleContent>
            </Collapsible>
          ))}
        </div>
      )}
    </div>
  );
}
