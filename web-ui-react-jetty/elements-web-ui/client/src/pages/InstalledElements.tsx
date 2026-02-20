import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useLocation } from 'wouter';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, Package, Info } from 'lucide-react';
import { ElementApiTester } from '../components/ElementApiTester';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';

interface ApplicationElement {
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

interface ApplicationStatus {
  application: {
    id: string;
    name: string;
    description?: string;
  };
  status: 'CLEAN' | 'UNSTABLE' | 'FAILED' | string;
  uris: string[];
  logs: string[];
  elements: ApplicationElement[];
}

export default function InstalledElements() {
  const [location, setLocation] = useLocation();
  const [selectedApp, setSelectedApp] = useState<string | null>(null);
  const [selectedElement, setSelectedElement] = useState<string | null>(null);

  // Fetch all element containers (applications with installed elements)
  const { data: applicationStatuses, isLoading } = useQuery<ApplicationStatus[]>({
    queryKey: ['/api/rest/elements/container'],
    enabled: true,
  });

  // Sync state with URL params
  useEffect(() => {
    const params = new URLSearchParams(location.split('?')[1] || '');
    const appId = params.get('app');
    const elementName = params.get('element');
    
    if (appId && elementName) {
      setSelectedApp(appId);
      setSelectedElement(elementName);
    }
  }, [location]);

  // Get selected application and element data
  const currentApp = applicationStatuses?.find(app => app.application?.id === selectedApp);
  const currentElement = currentApp?.elements?.find(
    el => el.definition?.name === selectedElement
  );

  // Filter applications that have at least one installed element
  const appsWithElements = applicationStatuses?.filter(
    app => app.elements && app.elements.length > 0
  ) || [];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (appsWithElements.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
        <Package className="w-12 h-12 mb-4 opacity-50" />
        <p className="text-lg font-medium">No Installed Elements</p>
        <p className="text-sm mt-2">No applications have installed elements yet.</p>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col space-y-6">
      <div>
        <h1 className="text-3xl font-bold" data-testid="text-page-title">Installed Elements</h1>
        <p className="text-muted-foreground mt-1">
          View and test installed Elements across applications
        </p>
      </div>

      {!selectedElement ? (
        <div className="grid gap-4">
          {appsWithElements.map((app) => (
            <Card key={app.application?.id} className="hover-elevate">
              <CardHeader>
                <CardTitle className="text-lg">{app.application?.name || app.application?.id}</CardTitle>
                <CardDescription>
                  {app.elements?.length || 0} installed element{app.elements?.length !== 1 ? 's' : ''}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {app.elements?.map((element, idx) => {
                    const elementName = element.definition?.name;
                    return (
                      <Badge
                        key={`${elementName}-${idx}`}
                        variant="outline"
                        className="cursor-pointer hover-elevate"
                        onClick={() => {
                          setSelectedApp(app.application?.id);
                          setSelectedElement(elementName);
                          setLocation(`/installed-elements?app=${encodeURIComponent(app.application?.id || '')}&element=${encodeURIComponent(elementName || '')}`);
                        }}
                        data-testid={`badge-element-${elementName}`}
                      >
                        <Package className="w-3 h-3 mr-1" />
                        {elementName || `Element ${idx + 1}`}
                      </Badge>
                    );
                  })}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : (
        <div className="flex flex-col space-y-4">
          <div className="flex items-center gap-2">
            <Badge
              variant="outline"
              className="cursor-pointer hover-elevate"
              onClick={() => {
                setSelectedApp(null);
                setSelectedElement(null);
                setLocation('/installed-elements');
              }}
              data-testid="badge-back"
            >
              ← Back to all applications
            </Badge>
          </div>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Package className="w-5 h-5" />
                {selectedElement}
              </CardTitle>
              <CardDescription>
                Application: {currentApp?.application?.name || currentApp?.application?.id}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Tabs defaultValue="api" className="w-full">
                <TabsList>
                  <TabsTrigger value="api" data-testid="tab-api">API Testing</TabsTrigger>
                  <TabsTrigger value="metadata" data-testid="tab-metadata">Metadata</TabsTrigger>
                </TabsList>

                <TabsContent value="api" className="mt-4">
                  {selectedElement && (
                    <ElementApiTester 
                      elementName={selectedElement} 
                    />
                  )}
                </TabsContent>

                <TabsContent value="metadata" className="mt-4">
                  {currentElement && (
                    <ScrollArea className="h-[600px] w-full rounded-md border p-4">
                      <div className="space-y-4">
                        <div>
                          <h3 className="font-semibold mb-2">Element Type</h3>
                          <Badge variant="outline">{currentElement.type}</Badge>
                        </div>

                        <div>
                          <h3 className="font-semibold mb-2">Definition</h3>
                          <div className="space-y-2 text-sm">
                            <p><span className="font-medium">Name:</span> {currentElement.definition.name}</p>
                            <p><span className="font-medium">Loader:</span> {currentElement.definition.loader}</p>
                            <p><span className="font-medium">Recursive:</span> {currentElement.definition.recursive ? 'Yes' : 'No'}</p>
                          </div>
                        </div>

                        {currentElement.services && currentElement.services.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Services ({currentElement.services.length})</h3>
                            <div className="space-y-1">
                              {currentElement.services.map((service: any, idx: number) => (
                                <Badge key={idx} variant="secondary">{service.name || `Service ${idx + 1}`}</Badge>
                              ))}
                            </div>
                          </div>
                        )}

                        {currentElement.dependencies && currentElement.dependencies.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Dependencies ({currentElement.dependencies.length})</h3>
                            <div className="space-y-1">
                              {currentElement.dependencies.map((dep: any, idx: number) => (
                                <Badge key={idx} variant="outline">{dep.name || `Dependency ${idx + 1}`}</Badge>
                              ))}
                            </div>
                          </div>
                        )}

                        {currentElement.attributes && Object.keys(currentElement.attributes).length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Attributes</h3>
                            <pre className="bg-muted p-3 rounded-md text-xs overflow-auto">
                              {JSON.stringify(currentElement.attributes, null, 2)}
                            </pre>
                          </div>
                        )}

                        {currentElement.producedEvents && currentElement.producedEvents.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Produced Events ({currentElement.producedEvents.length})</h3>
                            <div className="space-y-1">
                              {currentElement.producedEvents.map((event: any, idx: number) => (
                                <Badge key={idx} variant="secondary">{event.name || `Event ${idx + 1}`}</Badge>
                              ))}
                            </div>
                          </div>
                        )}

                        {currentElement.consumedEvents && currentElement.consumedEvents.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Consumed Events ({currentElement.consumedEvents.length})</h3>
                            <div className="space-y-1">
                              {currentElement.consumedEvents.map((event: any, idx: number) => (
                                <Badge key={idx} variant="outline">{event.name || `Event ${idx + 1}`}</Badge>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    </ScrollArea>
                  )}
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
