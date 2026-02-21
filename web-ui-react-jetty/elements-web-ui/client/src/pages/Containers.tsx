import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Loader2, Container, RefreshCw, ExternalLink, ChevronDown, ChevronRight, ArrowLeft } from 'lucide-react';
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

interface ElementRuntimeStatus {
  deployment: ElementDeployment;
  status: string;
  deploymentFiles: string[];
  logs: string[];
  elements: ElementMetadata[];
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
                      <div className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${getStatusColor(container.status)}`} />
                      <span className="font-mono text-sm font-medium" data-testid={`text-container-deployment-${idx}`}>
                        {container.runtime?.deployment?.id || `Container ${idx + 1}`}
                      </span>
                      <Badge variant={getStatusVariant(container.status)} data-testid={`badge-container-status-${idx}`}>
                        {container.status}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-4 text-xs text-muted-foreground flex-wrap">
                      {container.elements && container.elements.length > 0 && (
                        <span>{container.elements.length} element{container.elements.length !== 1 ? 's' : ''}</span>
                      )}
                      {container.uris && container.uris.length > 0 && (
                        <span>{container.uris.length} URI{container.uris.length !== 1 ? 's' : ''}</span>
                      )}
                      {container.logs && container.logs.length > 0 && (
                        <span>{container.logs.length} log entr{container.logs.length !== 1 ? 'ies' : 'y'}</span>
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
        </div>

        <Tabs defaultValue="overview">
          <TabsList>
            <TabsTrigger value="overview" data-testid="tab-container-overview">Overview</TabsTrigger>
            <TabsTrigger value="elements" data-testid="tab-container-elements">Elements</TabsTrigger>
            <TabsTrigger value="logs" data-testid="tab-container-logs">Logs</TabsTrigger>
            <TabsTrigger value="json" data-testid="tab-container-json">Raw JSON</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="mt-4 space-y-4">
            {container.uris && container.uris.length > 0 && (
              <div className="space-y-2">
                <h3 className="text-sm font-medium">URIs</h3>
                <div className="space-y-1">
                  {container.uris.map((uri, i) => (
                    <div key={i} className="flex items-center gap-2">
                      <ExternalLink className="w-3 h-3 text-muted-foreground flex-shrink-0" />
                      <span className="font-mono text-xs break-all" data-testid={`text-uri-${i}`}>{uri}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {container.runtime?.deployment && (
              <div className="space-y-2">
                <h3 className="text-sm font-medium">Deployment</h3>
                <pre className="bg-muted p-3 rounded-md text-xs overflow-auto">
                  {JSON.stringify(container.runtime.deployment, null, 2)}
                </pre>
              </div>
            )}

            {container.runtime?.deploymentFiles && container.runtime.deploymentFiles.length > 0 && (
              <Collapsible>
                <CollapsibleTrigger className="flex items-center gap-2 text-sm font-medium hover-elevate rounded-md px-1">
                  Deployment Files ({container.runtime.deploymentFiles.length})
                  <ChevronDown className="w-3 h-3" />
                </CollapsibleTrigger>
                <CollapsibleContent className="mt-2">
                  <div className="space-y-1">
                    {container.runtime.deploymentFiles.map((file, i) => (
                      <p key={i} className="font-mono text-xs text-muted-foreground">{file}</p>
                    ))}
                  </div>
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
