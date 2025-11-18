import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { Loader2, ChevronDown, ExternalLink, Box, Radio, Code, RefreshCw } from 'lucide-react';
import { queryClient } from '@/lib/queryClient';
import { Alert, AlertDescription } from '@/components/ui/alert';

interface ElementService {
  implementation?: {
    type?: string;
    expose?: boolean;
    default?: boolean;
  };
  export?: {
    exposed?: string[];
    name?: string;
    named?: boolean;
  };
}

interface ElementDefinition {
  name: string;
  recursive: boolean;
  additionalPackages: string[];
  loader: string;
}

interface ElementEvent {
  name?: string;
  description?: string;
  parameters?: any;
  eventKey?: {
    serviceKey?: {
      type?: string;
      name?: string;
      named?: boolean;
    };
    eventName?: string;
  };
  method?: any;
}

interface ElementData {
  type: string;
  definition: ElementDefinition;
  services: ElementService[];
  producedEvents: (string | ElementEvent)[];
  consumedEvents: (string | ElementEvent)[];
  dependencies: (string | any)[];
  attributes: Record<string, any>;
  defaultAttributes: any[];
}

interface SystemElements {
  elements: Record<string, ElementData>;
}

export default function CoreElements() {
  const [expandedElements, setExpandedElements] = useState<Set<string>>(new Set());

  // Fetch Elements version
  const { data: versionData } = useQuery<{ version: string }>({
    queryKey: ['/api/rest/version'],
    staleTime: Infinity,
  });

  // Fetch Core Elements system data
  const { data: systemData, isLoading, isFetching, error } = useQuery<SystemElements | ElementData[]>({
    queryKey: ['/api/rest/elements/system'],
    staleTime: 60000, // Cache for 1 minute
  });

  const toggleElement = (elementName: string) => {
    setExpandedElements(prev => {
      const next = new Set(prev);
      if (next.has(elementName)) {
        next.delete(elementName);
      } else {
        next.add(elementName);
      }
      return next;
    });
  };

  // Generate javadoc URL for a service class
  const getJavadocUrl = (className: string, version?: string): string | null => {
    if (!version) return null;
    
    // Strip -SNAPSHOT suffix for javadoc URL (use released version docs)
    const cleanVersion = version.replace('-SNAPSHOT', '');
    
    // Convert class name to path: dev.getelements.elements.sdk.dao.ApplicationDao
    // -> dev/getelements/elements/sdk/dao/ApplicationDao.html
    const path = className.replace(/\./g, '/');
    return `https://javadoc.getelements.dev/${cleanVersion}/${path}.html`;
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <Alert variant="destructive">
          <AlertDescription>
            Failed to load Core Elements system data: {(error as Error).message}
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  // Helper function to convert object with numeric keys to array
  const toArray = (obj: any): any[] => {
    if (Array.isArray(obj)) return obj;
    if (!obj || typeof obj !== 'object') return [];
    const values = Object.values(obj);
    // Check if it looks like an array (all keys are numeric)
    const keys = Object.keys(obj);
    if (keys.length > 0 && keys.every(k => !isNaN(Number(k)))) {
      return values;
    }
    return [];
  };

  // Handle both response formats:
  // 1. Object with elements property: { elements: { name: data, ... } }
  // 2. Array of elements: [{ definition: { name: ... }, ... }, ...]
  let rawElements = Array.isArray(systemData) 
    ? systemData
    : toArray(systemData);
  
  // Normalize each element by converting object-based arrays to real arrays
  const elements = rawElements.reduce((acc, elem, idx) => {
    const name = elem.definition?.name || `element-${idx}`;
    acc[name] = {
      ...elem,
      services: toArray(elem.services),
      producedEvents: toArray(elem.producedEvents),
      consumedEvents: toArray(elem.consumedEvents),
      dependencies: toArray(elem.dependencies),
    };
    return acc;
  }, {} as Record<string, ElementData>);
  
  const elementEntries = Object.entries(elements) as [string, ElementData][];

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Core Elements</h1>
          <p className="text-muted-foreground mt-1">
            System Elements and their exposed services
{versionData?.version && ` (v${versionData.version})`}
          </p>
        </div>
        <Button 
          onClick={async () => {
            await queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/system'] });
          }} 
          variant="outline" 
          size="sm"
          disabled={isFetching}
          data-testid="button-refresh-elements"
        >
          <RefreshCw className={`w-4 h-4 mr-2 ${isFetching ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {elementEntries.length === 0 ? (
        <Card>
          <CardContent className="py-8">
            <p className="text-center text-muted-foreground">No Core Elements found</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {elementEntries.map(([elementName, elementData]) => {
            const isExpanded = expandedElements.has(elementName);
            const displayName = elementName.split('.').pop() || elementName;

            return (
              <Card key={elementName}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <CardTitle className="text-lg flex items-center gap-2">
                        <Box className="w-5 h-5" />
                        {displayName}
                      </CardTitle>
                      <CardDescription className="font-mono text-xs mt-1">
                        {elementName}
                      </CardDescription>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => toggleElement(elementName)}
                      data-testid={`button-toggle-${elementName}`}
                    >
                      <ChevronDown
                        className={`w-4 h-4 transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                      />
                    </Button>
                  </div>
                </CardHeader>

                {isExpanded && (
                  <CardContent className="space-y-4">
                    {/* Services */}
                    {elementData.services && elementData.services.length > 0 && (
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <Code className="w-4 h-4 text-muted-foreground" />
                          <h3 className="text-sm font-semibold">
                            Services
                            <Badge variant="secondary" className="ml-2">
                              {elementData.services.length}
                            </Badge>
                          </h3>
                        </div>
                        <div className="space-y-1 pl-6">
                          {elementData.services.flatMap((service, serviceIdx) => {
                            // Extract all exposed services from the array
                            const exposedServices = service.export?.exposed || [];
                            
                            return exposedServices.map((serviceName, exposedIdx) => {
                              const javadocUrl = getJavadocUrl(serviceName, versionData?.version);
                              
                              return (
                                <div
                                  key={`${serviceName}-${serviceIdx}-${exposedIdx}`}
                                  className="flex items-center justify-between gap-2 p-2 rounded-md hover-elevate"
                                >
                                  <code className="text-xs break-all" data-testid={`service-${serviceName}`}>
                                    {serviceName}
                                  </code>
                                  {javadocUrl && (
                                    <Button
                                      variant="ghost"
                                      size="sm"
                                      onClick={() => window.open(javadocUrl, '_blank')}
                                      data-testid={`button-javadoc-${serviceName}`}
                                    >
                                      <ExternalLink className="w-3 h-3" />
                                    </Button>
                                  )}
                                </div>
                              );
                            });
                          })}
                        </div>
                      </div>
                    )}

                    {/* Produced Events */}
                    {elementData.producedEvents && elementData.producedEvents.length > 0 && (
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <Radio className="w-4 h-4 text-muted-foreground" />
                          <h3 className="text-sm font-semibold">
                            Produced Events
                            <Badge variant="secondary" className="ml-2">
                              {elementData.producedEvents.length}
                            </Badge>
                          </h3>
                        </div>
                        <div className="space-y-1 pl-6">
                          {elementData.producedEvents.map((event, idx) => {
                            // Handle different event formats
                            let eventName: string;
                            let eventDesc: string | null = null;
                            
                            if (typeof event === 'string') {
                              eventName = event;
                            } else {
                              // Produced events have simple structure: { name: ..., description: ... }
                              eventName = event.name || 'Unknown Event';
                              eventDesc = event.description || null;
                            }
                            
                            return (
                              <div
                                key={`${eventName}-${idx}`}
                                className="p-2 rounded-md hover-elevate space-y-1"
                              >
                                <code className="text-xs break-all" data-testid={`produced-event-${eventName}`}>
                                  {eventName}
                                </code>
                                {eventDesc && (
                                  <p className="text-xs text-muted-foreground">{eventDesc}</p>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    )}

                    {/* Consumed Events */}
                    {elementData.consumedEvents && elementData.consumedEvents.length > 0 && (
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <Radio className="w-4 h-4 text-muted-foreground" />
                          <h3 className="text-sm font-semibold">
                            Consumed Events
                            <Badge variant="secondary" className="ml-2">
                              {elementData.consumedEvents.length}
                            </Badge>
                          </h3>
                        </div>
                        <div className="space-y-1 pl-6">
                          {elementData.consumedEvents.map((event, idx) => {
                            // Handle different event formats
                            let eventName: string;
                            let eventDesc: string | null = null;
                            let serviceType: string | null = null;
                            
                            if (typeof event === 'string') {
                              eventName = event;
                            } else if (event.eventKey) {
                              // Complex structure: { eventKey: { serviceKey: { type: ... }, eventName: ... } }
                              eventName = event.eventKey.eventName || 'Unknown Event';
                              serviceType = event.eventKey.serviceKey?.type || null;
                            } else {
                              // Simple structure: { name: ..., description: ... }
                              eventName = event.name || 'Unknown Event';
                              eventDesc = event.description || null;
                            }
                            
                            return (
                              <div
                                key={`${eventName}-${idx}`}
                                className="p-2 rounded-md hover-elevate space-y-1"
                              >
                                <code className="text-xs break-all" data-testid={`consumed-event-${eventName}`}>
                                  {eventName}
                                </code>
                                {serviceType && (
                                  <p className="text-xs text-muted-foreground">from: {serviceType}</p>
                                )}
                                {eventDesc && (
                                  <p className="text-xs text-muted-foreground">{eventDesc}</p>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    )}

                    {/* Dependencies */}
                    {elementData.dependencies && elementData.dependencies.length > 0 && (
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <Box className="w-4 h-4 text-muted-foreground" />
                          <h3 className="text-sm font-semibold">
                            Dependencies
                            <Badge variant="secondary" className="ml-2">
                              {elementData.dependencies.length}
                            </Badge>
                          </h3>
                        </div>
                        <div className="space-y-1 pl-6">
                          {elementData.dependencies.map((dep, idx) => {
                            const depName = typeof dep === 'string' 
                              ? dep 
                              : dep.name || dep.type || JSON.stringify(dep);
                            
                            return (
                              <div
                                key={`${depName}-${idx}`}
                                className="p-2 rounded-md hover-elevate"
                              >
                                <code className="text-xs break-all" data-testid={`dependency-${depName}`}>
                                  {depName}
                                </code>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    )}
                  </CardContent>
                )}
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
