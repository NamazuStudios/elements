import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Loader2, Container, RefreshCw, ExternalLink, ChevronDown, ChevronRight, ArrowLeft, AlertTriangle, AlertCircle } from 'lucide-react';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
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

interface ElementDeployment {
  id: string;
  state: string;
  version: number;
  [key: string]: any;
}

interface ElementManifestRecord {
  version?: { version: string; revision: string; timestamp: string };
  builtinSpis?: string[];
}

interface ElementRuntimeStatus {
  deployment: ElementDeployment;
  status: string;
  elementPaths: string[];
  deploymentFiles: string[];
  logs: string[];
  warnings: string[];
  errors: string[];
  elements: ElementMetadata[];
  elementManifests: Record<string, ElementManifestRecord>;
}

interface ElementContainerStatus {
  runtime: ElementRuntimeStatus;
  status: string;
  uris: string[];
  logs: string[];
  elements: ElementMetadata[];
}

function getStatusColor(status: string) {
  switch (status?.toUpperCase()) {
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
      return 'bg-gray-500';
  }
}

function getStatusVariant(status: string): 'default' | 'secondary' | 'destructive' | 'outline' {
  switch (status?.toUpperCase()) {
    case 'CLEAN':
    case 'RUNNING':
    case 'ACTIVE':
      return 'default';
    case 'UNSTABLE':
    case 'DEGRADED':
      return 'secondary';
    case 'FAILED':
    case 'ERROR':
      return 'destructive';
    default:
      return 'outline';
  }
}

function getContainerDotColor(container: ElementContainerStatus): string {
  if (container.runtime?.errors?.length) return 'bg-red-500';
  if (container.runtime?.warnings?.length) return 'bg-yellow-500';
  return getStatusColor(container.status);
}

export default function Containers() {
  const [selectedContainer, setSelectedContainer] = useState<number | null>(null);

  const { data: containers, isLoading, error } = useQuery<ElementContainerStatus[]>({
    queryKey: ['/api/rest/elements/container'],
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  const selected = selectedContainer !== null ? containers?.[selectedContainer] : null;

  return (
    <div className="h-full flex flex-col space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold" data-testid="text-containers-title">Containers</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Containers serving endpoints via Elements. Only Elements serving data are visible here.
          </p>
        </div>
        <Button
          variant="outline"
          size="icon"
          onClick={() => queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/container'] })}
          data-testid="button-refresh-containers"
        >
          <RefreshCw className="w-4 h-4" />
        </Button>
      </div>

      {error && (
        <Card className="border-destructive">
          <CardContent className="p-4">
            <p className="text-sm text-destructive" data-testid="text-error">
              {(error as Error).message || 'Failed to load containers'}
            </p>
          </CardContent>
        </Card>
      )}

      {!isLoading && !error && (!containers || containers.length === 0) && (
        <Card>
          <CardContent className="p-8 text-center">
            <Container className="w-12 h-12 mx-auto mb-4 text-muted-foreground opacity-50" />
            <p className="text-lg font-medium" data-testid="text-no-containers">No Containers</p>
            <p className="text-sm text-muted-foreground mt-2">No containers are currently running.</p>
          </CardContent>
        </Card>
      )}

      {selected ? (
        <div className="flex flex-col space-y-4">
          <Button
            variant="outline"
            className="self-start"
            onClick={() => setSelectedContainer(null)}
            data-testid="button-back-containers"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to all containers
          </Button>
          <ContainerDetail container={selected} />
        </div>
      ) : (
        <div className="space-y-3">
          {containers?.map((container, idx) => (
            <Card
              key={idx}
              className="hover-elevate cursor-pointer"
              onClick={() => setSelectedContainer(idx)}
              data-testid={`card-container-${idx}`}
            >
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4 flex-wrap">
                  <div className="flex-1 min-w-0 space-y-2">
                    <div className="flex items-center gap-2 flex-wrap">
                      <div className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${getContainerDotColor(container)}`} />
                      <span className="font-mono text-sm font-medium" data-testid={`text-container-deployment-${idx}`}>
                        {container.runtime?.deployment?.id || `Container ${idx + 1}`}
                      </span>
                      <Badge variant={getStatusVariant(container.status)} data-testid={`badge-container-status-${idx}`}>
                        {container.status}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-4 text-xs flex-wrap">
                      {container.runtime?.errors && container.runtime.errors.length > 0 && (
                        <span className="flex items-center gap-1 text-destructive font-medium">
                          <AlertCircle className="w-3 h-3" />{container.runtime.errors.length} error{container.runtime.errors.length !== 1 ? 's' : ''}
                        </span>
                      )}
                      {container.runtime?.warnings && container.runtime.warnings.length > 0 && (
                        <span className="flex items-center gap-1 text-yellow-600 dark:text-yellow-500 font-medium">
                          <AlertTriangle className="w-3 h-3" />{container.runtime.warnings.length} warning{container.runtime.warnings.length !== 1 ? 's' : ''}
                        </span>
                      )}
                      {container.elements && container.elements.length > 0 && (
                        <span className="text-muted-foreground">{container.elements.length} element{container.elements.length !== 1 ? 's' : ''}</span>
                      )}
                      {container.uris && container.uris.length > 0 && (
                        <span className="text-muted-foreground">{container.uris.length} URI{container.uris.length !== 1 ? 's' : ''}</span>
                      )}
                      {container.logs && container.logs.length > 0 && (
                        <span className="text-muted-foreground">{container.logs.length} log entr{container.logs.length !== 1 ? 'ies' : 'y'}</span>
                      )}
                    </div>
                  </div>
                  <ChevronRight className="w-4 h-4 text-muted-foreground flex-shrink-0 mt-1" />
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function ContainerDetail({ container }: { container: ElementContainerStatus }) {
  return (
    <Card>
      <CardContent className="p-4 space-y-4">
        <div className="flex items-center gap-2 flex-wrap">
          <Container className="w-5 h-5" />
          <span className="font-mono text-sm font-medium">
            {container.runtime?.deployment?.id || 'Container'}
          </span>
          <Badge variant={getStatusVariant(container.status)}>
            {container.status}
          </Badge>
          {container.runtime && (
            <Badge variant="outline" className="text-xs">
              Runtime: {container.runtime.status}
            </Badge>
          )}
          {container.runtime?.errors && container.runtime.errors.length > 0 && (
            <Badge variant="destructive" className="gap-1">
              <AlertCircle className="w-3 h-3" />
              {container.runtime.errors.length} error{container.runtime.errors.length !== 1 ? 's' : ''}
            </Badge>
          )}
          {container.runtime?.warnings && container.runtime.warnings.length > 0 && (
            <Badge variant="outline" className="gap-1 text-yellow-600 dark:text-yellow-500 border-yellow-500/50">
              <AlertTriangle className="w-3 h-3" />
              {container.runtime.warnings.length} warning{container.runtime.warnings.length !== 1 ? 's' : ''}
            </Badge>
          )}
        </div>

        <Tabs defaultValue="overview">
          <TabsList>
            <TabsTrigger value="overview" data-testid="tab-container-overview">Overview</TabsTrigger>
            <TabsTrigger value="elements" data-testid="tab-container-elements">Elements</TabsTrigger>
            <TabsTrigger value="logs" data-testid="tab-container-logs">Logs</TabsTrigger>
            <TabsTrigger value="json" data-testid="tab-container-json">Raw JSON</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="mt-4 space-y-4">
            {container.runtime?.errors && container.runtime.errors.length > 0 && (
              <div className="rounded-md border border-destructive/50 bg-destructive/5 p-3 space-y-1.5">
                <div className="flex items-center gap-1.5">
                  <AlertCircle className="w-4 h-4 text-destructive" />
                  <span className="text-sm font-medium text-destructive">
                    {container.runtime.errors.length} Error{container.runtime.errors.length !== 1 ? 's' : ''}
                  </span>
                </div>
                <ul className="space-y-0.5 ml-5">
                  {container.runtime.errors.map((err, i) => (
                    <li key={i} className="font-mono text-xs text-destructive/80">{err}</li>
                  ))}
                </ul>
              </div>
            )}
            {container.runtime?.warnings && container.runtime.warnings.length > 0 && (
              <div className="rounded-md border border-yellow-500/50 bg-yellow-500/5 p-3 space-y-1.5">
                <div className="flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4 text-yellow-600 dark:text-yellow-500" />
                  <span className="text-sm font-medium text-yellow-600 dark:text-yellow-500">
                    {container.runtime.warnings.length} Warning{container.runtime.warnings.length !== 1 ? 's' : ''}
                  </span>
                </div>
                <ul className="space-y-0.5 ml-5">
                  {container.runtime.warnings.map((warn, i) => (
                    <li key={i} className="font-mono text-xs text-yellow-700 dark:text-yellow-400">{warn}</li>
                  ))}
                </ul>
              </div>
            )}
            {container.uris && container.uris.length > 0 && (
              <div className="space-y-2">
                <h3 className="text-sm font-medium">Endpoints</h3>
                <div className="space-y-1">
                  {container.uris.map((uri, i) => (
                    <div key={i} className="flex items-center gap-2 px-3 py-2 rounded-md border bg-muted/20">
                      <ExternalLink className="w-3 h-3 text-muted-foreground flex-shrink-0" />
                      <span className="font-mono text-xs break-all" data-testid={`text-uri-${i}`}>{uri}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {container.runtime?.deployment && (
              <DeploymentInfoSection deployment={container.runtime.deployment} />
            )}

            {container.runtime?.elementPaths && container.runtime.elementPaths.length > 0 && (
              <Collapsible>
                <CollapsibleTrigger className="flex items-center gap-2 text-sm font-medium hover-elevate rounded-md px-1">
                  Element Paths ({container.runtime.elementPaths.length})
                  <ChevronDown className="w-3 h-3" />
                </CollapsibleTrigger>
                <CollapsibleContent className="mt-2 space-y-0.5">
                  {container.runtime.elementPaths.map((p, i) => (
                    <p key={i} className="font-mono text-xs text-muted-foreground break-all">{p}</p>
                  ))}
                </CollapsibleContent>
              </Collapsible>
            )}

            {container.runtime?.deploymentFiles && container.runtime.deploymentFiles.length > 0 && (
              <Collapsible>
                <CollapsibleTrigger className="flex items-center gap-2 text-sm font-medium hover-elevate rounded-md px-1">
                  Deployment Files ({container.runtime.deploymentFiles.length})
                  <ChevronDown className="w-3 h-3" />
                </CollapsibleTrigger>
                <CollapsibleContent className="mt-2 space-y-0.5">
                  {container.runtime.deploymentFiles.map((file, i) => (
                    <p key={i} className="font-mono text-xs text-muted-foreground">{file}</p>
                  ))}
                </CollapsibleContent>
              </Collapsible>
            )}

            {container.runtime?.elementManifests && Object.keys(container.runtime.elementManifests).length > 0 && (
              <Collapsible>
                <CollapsibleTrigger className="flex items-center gap-2 text-sm font-medium hover-elevate rounded-md px-1">
                  Element Manifests ({Object.keys(container.runtime.elementManifests).length})
                  <ChevronDown className="w-3 h-3" />
                </CollapsibleTrigger>
                <CollapsibleContent className="mt-2 space-y-2">
                  {Object.entries(container.runtime.elementManifests).map(([key, manifest]) => (
                    <div key={key} className="rounded-md border p-2 space-y-1">
                      <p className="font-mono text-xs font-medium break-all">{key}</p>
                      {manifest.version && (
                        <p className="text-xs text-muted-foreground">
                          v{manifest.version.version} · {manifest.version.revision}
                        </p>
                      )}
                      {manifest.builtinSpis && manifest.builtinSpis.length > 0 && (
                        <div className="flex flex-wrap gap-1">
                          {manifest.builtinSpis.map(spi => (
                            <Badge key={spi} variant="secondary" className="text-[10px]">{spi}</Badge>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </CollapsibleContent>
              </Collapsible>
            )}
          </TabsContent>

          <TabsContent value="elements" className="mt-4">
            <ElementsGrid elements={container.elements} />
          </TabsContent>

          <TabsContent value="logs" className="mt-4">
            <LogsView
              containerLogs={container.logs}
              runtimeLogs={container.runtime?.logs}
            />
          </TabsContent>

          <TabsContent value="json" className="mt-4">
            <ScrollArea className="h-[500px] w-full rounded-md border">
              <pre className="p-4 text-xs font-mono">
                {JSON.stringify(container, null, 2)}
              </pre>
            </ScrollArea>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
}

function DeploymentInfoSection({ deployment }: { deployment: ElementDeployment }) {
  const [extraOpen, setExtraOpen] = useState(false);
  const { id, state, version, application, useDefaultRepositories, elements, packages, repositories, elm, ...rest } = deployment;
  const extraKeys = Object.keys(rest);

  return (
    <div className="space-y-2">
      <h3 className="text-sm font-medium">Deployment</h3>
      <div className="rounded-md border divide-y">
        <div className="grid grid-cols-[auto_1fr] gap-x-6 gap-y-2 p-3 items-center">
          {id && (
            <>
              <span className="text-xs text-muted-foreground">ID</span>
              <span className="font-mono text-xs break-all">{id}</span>
            </>
          )}
          {state && (
            <>
              <span className="text-xs text-muted-foreground">State</span>
              <span>
                <Badge
                  variant={state === 'ENABLED' ? 'default' : state === 'DISABLED' ? 'secondary' : 'outline'}
                  className="text-xs"
                >
                  {state}
                </Badge>
              </span>
            </>
          )}
          {version !== undefined && (
            <>
              <span className="text-xs text-muted-foreground">Version</span>
              <span className="text-xs font-mono">v{version}</span>
            </>
          )}
          {application && (
            <>
              <span className="text-xs text-muted-foreground">Application</span>
              <span className="text-xs">{application.name || application.id}</span>
            </>
          )}
          {typeof useDefaultRepositories === 'boolean' && (
            <>
              <span className="text-xs text-muted-foreground">Default Repos</span>
              <span className="text-xs">{useDefaultRepositories ? 'Yes' : 'No'}</span>
            </>
          )}
          {Array.isArray(elements) && elements.length > 0 && (
            <>
              <span className="text-xs text-muted-foreground">Elements</span>
              <span className="text-xs">{elements.length}</span>
            </>
          )}
          {Array.isArray(packages) && packages.length > 0 && (
            <>
              <span className="text-xs text-muted-foreground">Packages</span>
              <span className="text-xs">{packages.length}</span>
            </>
          )}
          {Array.isArray(repositories) && repositories.length > 0 && (
            <>
              <span className="text-xs text-muted-foreground">Repositories</span>
              <span className="text-xs">{repositories.length}</span>
            </>
          )}
          {elm && (
            <>
              <span className="text-xs text-muted-foreground">ELM</span>
              <span className="font-mono text-xs truncate">{elm.id}</span>
            </>
          )}
        </div>
        {extraKeys.length > 0 && (
          <div className="p-3">
            <Collapsible open={extraOpen} onOpenChange={setExtraOpen}>
              <CollapsibleTrigger className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors">
                {extraOpen ? <ChevronDown className="w-3 h-3" /> : <ChevronRight className="w-3 h-3" />}
                Additional properties ({extraKeys.length})
              </CollapsibleTrigger>
              <CollapsibleContent className="mt-2">
                <pre className="bg-muted/50 p-2 rounded text-[10px] overflow-auto">
                  {JSON.stringify(rest, null, 2)}
                </pre>
              </CollapsibleContent>
            </Collapsible>
          </div>
        )}
      </div>
    </div>
  );
}

function ElementsGrid({ elements }: { elements?: ElementMetadata[] }) {
  if (!elements || elements.length === 0) {
    return <p className="text-sm text-muted-foreground italic">No elements in this container.</p>;
  }

  return (
    <div className="space-y-3">
      {elements.filter(e => e != null).map((element, idx) => (
        <Card key={idx} data-testid={`card-element-${idx}`}>
          <CardContent className="p-3 space-y-2">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-mono text-xs font-medium">{element.definition?.name || `Element ${idx + 1}`}</span>
              <Badge variant="outline" className="text-[10px]">{element.type}</Badge>
            </div>
            {element.definition && (
              <div className="text-xs text-muted-foreground space-y-0.5">
                <p>Loader: {element.definition.loader}</p>
                {element.definition.recursive && <p>Recursive: Yes</p>}
              </div>
            )}
            {element.services && element.services.length > 0 && (
              <div className="flex flex-wrap gap-1">
                {element.services.map((svc: any, i: number) => (
                  <Badge key={i} variant="secondary" className="text-[10px]">{svc.name || `Service ${i + 1}`}</Badge>
                ))}
              </div>
            )}
            {element.attributes && Object.keys(element.attributes).length > 0 && (
              <Collapsible>
                <CollapsibleTrigger className="text-xs text-muted-foreground hover-elevate rounded px-1 flex items-center gap-1">
                  Attributes <ChevronDown className="w-3 h-3" />
                </CollapsibleTrigger>
                <CollapsibleContent className="mt-1">
                  <pre className="bg-muted p-2 rounded text-[10px] overflow-auto">
                    {JSON.stringify(element.attributes, null, 2)}
                  </pre>
                </CollapsibleContent>
              </Collapsible>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function LogsView({ containerLogs, runtimeLogs }: { containerLogs?: string[]; runtimeLogs?: string[] }) {
  const allEmpty = (!containerLogs || containerLogs.length === 0) && (!runtimeLogs || runtimeLogs.length === 0);
  if (allEmpty) {
    return <p className="text-sm text-muted-foreground italic">No logs available.</p>;
  }

  return (
    <div className="space-y-4">
      {containerLogs && containerLogs.length > 0 && (
        <div className="space-y-2">
          <h3 className="text-sm font-medium">Container Logs</h3>
          <ScrollArea className="h-[200px] w-full rounded-md border">
            <div className="p-3 space-y-0.5">
              {containerLogs.map((log, i) => (
                <p key={i} className="font-mono text-[11px] text-muted-foreground">{log}</p>
              ))}
            </div>
          </ScrollArea>
        </div>
      )}
      {runtimeLogs && runtimeLogs.length > 0 && (
        <div className="space-y-2">
          <h3 className="text-sm font-medium">Runtime Logs</h3>
          <ScrollArea className="h-[200px] w-full rounded-md border">
            <div className="p-3 space-y-0.5">
              {runtimeLogs.map((log, i) => (
                <p key={i} className="font-mono text-[11px] text-muted-foreground">{log}</p>
              ))}
            </div>
          </ScrollArea>
        </div>
      )}
    </div>
  );
}
