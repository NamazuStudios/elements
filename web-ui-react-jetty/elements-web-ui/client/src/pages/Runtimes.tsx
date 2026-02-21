import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Loader2, Cpu, RefreshCw, ChevronDown, ChevronRight, ArrowLeft } from 'lucide-react';
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

export default function Runtimes() {
  const [selectedRuntime, setSelectedRuntime] = useState<number | null>(null);

  const { data: runtimes, isLoading, error } = useQuery<ElementRuntimeStatus[]>({
    queryKey: ['/api/rest/elements/runtime'],
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  const selected = selectedRuntime !== null ? runtimes?.[selectedRuntime] : null;

  return (
    <div className="h-full flex flex-col space-y-6">
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold" data-testid="text-runtimes-title">Runtimes</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Deployed in-memory Elements. A runtime may or may not expose endpoints but is loaded in memory.
          </p>
        </div>
        <Button
          variant="outline"
          size="icon"
          onClick={() => queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/runtime'] })}
          data-testid="button-refresh-runtimes"
        >
          <RefreshCw className="w-4 h-4" />
        </Button>
      </div>

      {error && (
        <Card className="border-destructive">
          <CardContent className="p-4">
            <p className="text-sm text-destructive" data-testid="text-error">
              {(error as Error).message || 'Failed to load runtimes'}
            </p>
          </CardContent>
        </Card>
      )}

      {!isLoading && !error && (!runtimes || runtimes.length === 0) && (
        <Card>
          <CardContent className="p-8 text-center">
            <Cpu className="w-12 h-12 mx-auto mb-4 text-muted-foreground opacity-50" />
            <p className="text-lg font-medium" data-testid="text-no-runtimes">No Runtimes</p>
            <p className="text-sm text-muted-foreground mt-2">No element runtimes are currently loaded.</p>
          </CardContent>
        </Card>
      )}

      {selected ? (
        <div className="flex flex-col space-y-4">
          <Button
            variant="outline"
            className="self-start"
            onClick={() => setSelectedRuntime(null)}
            data-testid="button-back-runtimes"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back to all runtimes
          </Button>
          <RuntimeDetail runtime={selected} />
        </div>
      ) : (
        <div className="space-y-3">
          {runtimes?.map((runtime, idx) => (
            <Card
              key={idx}
              className="hover-elevate cursor-pointer"
              onClick={() => setSelectedRuntime(idx)}
              data-testid={`card-runtime-${idx}`}
            >
              <CardContent className="p-4">
                <div className="flex items-start justify-between gap-4 flex-wrap">
                  <div className="flex-1 min-w-0 space-y-2">
                    <div className="flex items-center gap-2 flex-wrap">
                      <div className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${getStatusColor(runtime.status)}`} />
                      <span className="font-mono text-sm font-medium" data-testid={`text-runtime-deployment-${idx}`}>
                        {runtime.deployment?.id || `Runtime ${idx + 1}`}
                      </span>
                      <Badge variant={getStatusVariant(runtime.status)} data-testid={`badge-runtime-status-${idx}`}>
                        {runtime.status}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-4 text-xs text-muted-foreground flex-wrap">
                      {runtime.elements && runtime.elements.length > 0 && (
                        <span>{runtime.elements.length} element{runtime.elements.length !== 1 ? 's' : ''}</span>
                      )}
                      {runtime.deploymentFiles && runtime.deploymentFiles.length > 0 && (
                        <span>{runtime.deploymentFiles.length} file{runtime.deploymentFiles.length !== 1 ? 's' : ''}</span>
                      )}
                      {runtime.logs && runtime.logs.length > 0 && (
                        <span>{runtime.logs.length} log entr{runtime.logs.length !== 1 ? 'ies' : 'y'}</span>
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

function RuntimeDetail({ runtime }: { runtime: ElementRuntimeStatus }) {
  return (
    <Card>
      <CardContent className="p-4 space-y-4">
        <div className="flex items-center gap-2 flex-wrap">
          <Cpu className="w-5 h-5" />
          <span className="font-mono text-sm font-medium">
            {runtime.deployment?.id || 'Runtime'}
          </span>
          <Badge variant={getStatusVariant(runtime.status)}>
            {runtime.status}
          </Badge>
        </div>

        <Tabs defaultValue="overview">
          <TabsList>
            <TabsTrigger value="overview" data-testid="tab-runtime-overview">Overview</TabsTrigger>
            <TabsTrigger value="elements" data-testid="tab-runtime-elements">Elements</TabsTrigger>
            <TabsTrigger value="files" data-testid="tab-runtime-files">Files</TabsTrigger>
            <TabsTrigger value="logs" data-testid="tab-runtime-logs">Logs</TabsTrigger>
            <TabsTrigger value="json" data-testid="tab-runtime-json">Raw JSON</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="mt-4 space-y-4">
            {runtime.deployment && (
              <div className="space-y-2">
                <h3 className="text-sm font-medium">Deployment</h3>
                <pre className="bg-muted p-3 rounded-md text-xs overflow-auto">
                  {JSON.stringify(runtime.deployment, null, 2)}
                </pre>
              </div>
            )}
          </TabsContent>

          <TabsContent value="elements" className="mt-4">
            {!runtime.elements || runtime.elements.length === 0 ? (
              <p className="text-sm text-muted-foreground italic">No elements in this runtime.</p>
            ) : (
              <div className="space-y-3">
                {runtime.elements.filter(e => e != null).map((element, idx) => (
                  <Card key={idx} data-testid={`card-runtime-element-${idx}`}>
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
            )}
          </TabsContent>

          <TabsContent value="files" className="mt-4">
            {!runtime.deploymentFiles || runtime.deploymentFiles.length === 0 ? (
              <p className="text-sm text-muted-foreground italic">No deployment files.</p>
            ) : (
              <ScrollArea className="h-[300px] w-full rounded-md border">
                <div className="p-3 space-y-0.5">
                  {runtime.deploymentFiles.map((file, i) => (
                    <p key={i} className="font-mono text-xs text-muted-foreground">{file}</p>
                  ))}
                </div>
              </ScrollArea>
            )}
          </TabsContent>

          <TabsContent value="logs" className="mt-4">
            {!runtime.logs || runtime.logs.length === 0 ? (
              <p className="text-sm text-muted-foreground italic">No logs available.</p>
            ) : (
              <ScrollArea className="h-[300px] w-full rounded-md border">
                <div className="p-3 space-y-0.5">
                  {runtime.logs.map((log, i) => (
                    <p key={i} className="font-mono text-[11px] text-muted-foreground">{log}</p>
                  ))}
                </div>
              </ScrollArea>
            )}
          </TabsContent>

          <TabsContent value="json" className="mt-4">
            <ScrollArea className="h-[500px] w-full rounded-md border">
              <pre className="p-4 text-xs font-mono">
                {JSON.stringify(runtime, null, 2)}
              </pre>
            </ScrollArea>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
}
