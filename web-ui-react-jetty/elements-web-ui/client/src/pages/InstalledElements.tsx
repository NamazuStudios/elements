import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useLocation } from 'wouter';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Loader2, Package, Info } from 'lucide-react';
import { ElementApiTester } from '../components/ElementApiTester';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';

interface ElementDefinitionMetadata {
  name: string;
  recursive: boolean;
  additionalPackages: Array<{
    name: string;
    version: string;
  }>;
  loader: string;
}

interface ElementMetadata {
  type: string;
  definition: ElementDefinitionMetadata;
  services: Array<any>;
  producedEvents: Array<any>;
  consumedEvents: Array<any>;
  dependencies: Array<any>;
  attributes: Record<string, any>;
  defaultAttributes: Array<any>;
}

interface ApplicationElement {
  id: string;
  applicationId: string;
  uri?: string; // Element REST API URI (may contain localhost URL that needs fixing)
  element: ElementMetadata;
}

interface ApplicationStatus {
  id: string;
  name: string;
  status?: 'CLEAN' | 'FAILED' | string;
  elements: ApplicationElement[];
}

export default function InstalledElements() {
  const [location, setLocation] = useLocation();
  const [selectedApp, setSelectedApp] = useState<string | null>(null);
  const [selectedElement, setSelectedElement] = useState<string | null>(null);

  // Fetch all applications with installed elements
  const { data: applicationStatuses, isLoading } = useQuery<ApplicationStatus[]>({
    queryKey: ['/api/rest/elements/application'],
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
  const currentApp = applicationStatuses?.find(app => app.id === selectedApp);
  const currentElement = currentApp?.elements?.find(
    el => el.element?.definition?.name === selectedElement
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
            <Card key={app.id} className="hover-elevate">
              <CardHeader>
                <CardTitle className="text-lg">{app.name || app.id}</CardTitle>
                <CardDescription>
                  {app.elements?.length || 0} installed element{app.elements?.length !== 1 ? 's' : ''}
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-2">
                  {app.elements?.map((element) => (
                    <Badge
                      key={element.id}
                      variant="outline"
                      className="cursor-pointer hover-elevate"
                      onClick={() => {
                        const elementName = element.element?.definition?.name;
                        setSelectedApp(app.id);
                        setSelectedElement(elementName);
                        setLocation(`/installed-elements?app=${encodeURIComponent(app.id)}&element=${encodeURIComponent(elementName || '')}`);
                      }}
                      data-testid={`badge-element-${element.element?.definition?.name}`}
                    >
                      <Package className="w-3 h-3 mr-1" />
                      {element.element?.definition?.name || element.id}
                    </Badge>
                  ))}
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
                Application: {currentApp?.name || currentApp?.id}
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
                      elementUri={currentElement?.uri}
                    />
                  )}
                </TabsContent>

                <TabsContent value="metadata" className="mt-4">
                  {currentElement && (
                    <ScrollArea className="h-[600px] w-full rounded-md border p-4">
                      <div className="space-y-4">
                        <div>
                          <h3 className="font-semibold mb-2">Element Type</h3>
                          <Badge variant="outline">{currentElement.element.type}</Badge>
                        </div>

                        <div>
                          <h3 className="font-semibold mb-2">Definition</h3>
                          <div className="space-y-2 text-sm">
                            <p><span className="font-medium">Name:</span> {currentElement.element.definition.name}</p>
                            <p><span className="font-medium">Loader:</span> {currentElement.element.definition.loader}</p>
                            <p><span className="font-medium">Recursive:</span> {currentElement.element.definition.recursive ? 'Yes' : 'No'}</p>
                          </div>
                        </div>

                        {currentElement.element.services && currentElement.element.services.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Services ({currentElement.element.services.length})</h3>
                            <div className="space-y-1">
                              {currentElement.element.services.map((service: any, idx: number) => (
                                <Badge key={idx} variant="secondary">{service.name || `Service ${idx + 1}`}</Badge>
                              ))}
                            </div>
                          </div>
                        )}

                        {currentElement.element.dependencies && currentElement.element.dependencies.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Dependencies ({currentElement.element.dependencies.length})</h3>
                            <div className="space-y-1">
                              {currentElement.element.dependencies.map((dep: any, idx: number) => (
                                <Badge key={idx} variant="outline">{dep.name || `Dependency ${idx + 1}`}</Badge>
                              ))}
                            </div>
                          </div>
                        )}

                        {currentElement.element.attributes && Object.keys(currentElement.element.attributes).length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Attributes</h3>
                            <pre className="bg-muted p-3 rounded-md text-xs overflow-auto">
                              {JSON.stringify(currentElement.element.attributes, null, 2)}
                            </pre>
                          </div>
                        )}

                        {currentElement.element.producedEvents && currentElement.element.producedEvents.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Produced Events ({currentElement.element.producedEvents.length})</h3>
                            <div className="space-y-1">
                              {currentElement.element.producedEvents.map((event: any, idx: number) => (
                                <Badge key={idx} variant="secondary">{event.name || `Event ${idx + 1}`}</Badge>
                              ))}
                            </div>
                          </div>
                        )}

                        {currentElement.element.consumedEvents && currentElement.element.consumedEvents.length > 0 && (
                          <div>
                            <h3 className="font-semibold mb-2">Consumed Events ({currentElement.element.consumedEvents.length})</h3>
                            <div className="space-y-1">
                              {currentElement.element.consumedEvents.map((event: any, idx: number) => (
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
