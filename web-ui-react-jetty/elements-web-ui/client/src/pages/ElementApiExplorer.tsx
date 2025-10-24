import { useState, useMemo, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useLocation } from 'wouter';
import { analyzeOpenAPISpec, ResourceOperations } from '@/lib/openapi-analyzer';
import { DynamicResourceView } from '@/components/DynamicResourceView';
import { DynamicFormGenerator } from '@/components/DynamicFormGenerator';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Loader2, Database, RefreshCw, Info, ExternalLink } from 'lucide-react';
import { apiRequest, queryClient } from '@/lib/queryClient';
import { getApiPath } from '@/lib/api-client';
import { useToast } from '@/hooks/use-toast';

export default function ElementApiExplorer() {
  const [selectedResource, setSelectedResource] = useState<ResourceOperations | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [lastResponse, setLastResponse] = useState<{operation: string; status: number; data: any; timestamp: string} | null>(null);
  const { toast } = useToast();

  // Parse URL params - watch for changes in query string
  const [location] = useLocation();
  const [queryString, setQueryString] = useState(window.location.search);
  
  // Watch for query string changes
  useEffect(() => {
    const checkQueryChange = () => {
      if (window.location.search !== queryString) {
        setQueryString(window.location.search);
      }
    };
    
    // Check every 100ms for query string changes
    const interval = setInterval(checkQueryChange, 100);
    
    // Also check on popstate (browser back/forward)
    window.addEventListener('popstate', checkQueryChange);
    
    return () => {
      clearInterval(interval);
      window.removeEventListener('popstate', checkQueryChange);
    };
  }, [queryString]);
  
  const params = new URLSearchParams(queryString);
  const appId = params.get('app');
  const elementName = params.get('element');
  const elementUri = params.get('uri');

  // Fetch element metadata from Elements backend
  const { data: appsData } = useQuery({
    queryKey: ['/api/proxy/api/rest/elements/application'],
    enabled: !!appId && !!elementName,
    staleTime: 30000,
  });

  // Find the specific element and app status
  const { currentElement, currentAppStatus } = useMemo(() => {
    if (!appsData || !appId || !elementName) return { currentElement: null, currentAppStatus: null };
    const appStatus = (appsData as any[])?.find((a: any) => a.application?.id === appId);
    if (!appStatus) return { currentElement: null, currentAppStatus: null };
    const element = appStatus.elements?.find((e: any) => 
      e.definition?.name === elementName
    );
    return { currentElement: element, currentAppStatus: appStatus };
  }, [appsData, appId, elementName]);

  // Extract the path from the URI (remove protocol and domain)
  const elementPath = useMemo(() => {
    if (!elementUri) return '';
    try {
      const url = new URL(elementUri);
      return url.pathname; // e.g., /app/rest/example-element
    } catch {
      // If it's already a path, use it as-is
      return elementUri;
    }
  }, [elementUri]);

  // Fetch OpenAPI spec from element path
  // Include appId and elementName in the query key to force refetch when switching elements
  const { data: spec, isLoading: specLoading, error: specError, refetch: refetchSpec } = useQuery({
    queryKey: [`${elementPath}/openapi.yaml`, appId, elementName],
    queryFn: async () => {
      if (!elementPath) throw new Error('No element path provided');
      
      // Try YAML first, using getApiPath to handle production vs development
      const yamlPath = await getApiPath(`${elementPath}/openapi.yaml`);
      let response = await fetch(yamlPath, { credentials: 'include' });
      
      // If YAML fails, try JSON
      if (!response.ok) {
        const jsonPath = await getApiPath(`${elementPath}/openapi.json`);
        response = await fetch(jsonPath, { credentials: 'include' });
      }
      
      if (!response.ok) {
        throw new Error(`Failed to fetch OpenAPI spec: ${response.status} ${response.statusText}`);
      }
      return response.json();
    },
    enabled: !!elementPath,
    staleTime: 0, // Always fetch fresh data
    refetchOnMount: true, // Refetch when component mounts
    retry: false, // Don't retry if spec doesn't exist
  });

  // Analyze spec to extract resources (returns array now)
  const resources = spec ? analyzeOpenAPISpec(spec as any) : [];

  // Reset last response when switching resources
  useEffect(() => {
    setLastResponse(null);
  }, [selectedResource?.resourceName]);

  // Build full path for API requests
  const buildFullPath = async (path: string) => {
    return await getApiPath(`${elementPath}${path}`);
  };

  // Create mutation
  const createMutation = useMutation({
    mutationFn: async (data: { pathParams: Record<string, string>; queryParams: Record<string, string>; body: any }) => {
      if (!selectedResource?.create || selectedResource.create.length === 0) throw new Error('No create endpoint');
      const createOp = selectedResource.create[0]; // Use first create operation
      
      // Build URL with path parameters
      let path = createOp.path;
      for (const [key, value] of Object.entries(data.pathParams)) {
        path = path.replace(`{${key}}`, encodeURIComponent(value));
      }
      
      // Add query parameters
      const queryString = Object.entries(data.queryParams)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&');
      const fullPath = queryString ? `${path}?${queryString}` : path;
      
      const response = await apiRequest('POST', await buildFullPath(fullPath), data.body);
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null; // Handle 204 No Content or empty responses
      }
      setLastResponse({
        operation: `POST ${fullPath}`,
        status: response.status,
        data: responseData,
        timestamp: new Date().toISOString(),
      });
      return responseData;
    },
    onSuccess: async () => {
      toast({ title: 'Success', description: 'Item created successfully' });
      setCreateDialogOpen(false);
      if (selectedResource?.list) {
        const listPath = await buildFullPath(selectedResource.list.path);
        queryClient.invalidateQueries({ queryKey: [listPath] });
      }
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create item',
        variant: 'destructive',
      });
    },
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: async (data: { pathParams: Record<string, string>; queryParams: Record<string, string>; body: any }) => {
      if (!selectedResource?.update || selectedResource.update.length === 0 || !selectedItem) throw new Error('No update endpoint');
      
      const opIndex = selectedItem._operationIndex || 0;
      const updateOp = selectedResource.update[opIndex];
      
      // Build URL with path parameters (from form data)
      let path = updateOp.path;
      for (const [key, value] of Object.entries(data.pathParams)) {
        path = path.replace(`{${key}}`, encodeURIComponent(value));
      }
      
      // Add query parameters
      const queryString = Object.entries(data.queryParams)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&');
      const fullPath = queryString ? `${path}?${queryString}` : path;
      
      const response = await apiRequest('PUT', await buildFullPath(fullPath), data.body);
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null; // Handle 204 No Content or empty responses
      }
      setLastResponse({
        operation: `PUT ${fullPath}`,
        status: response.status,
        data: responseData,
        timestamp: new Date().toISOString(),
      });
      return responseData;
    },
    onSuccess: async () => {
      toast({ title: 'Success', description: 'Item updated successfully' });
      setEditDialogOpen(false);
      setSelectedItem(null);
      if (selectedResource?.list) {
        const listPath = await buildFullPath(selectedResource.list.path);
        queryClient.invalidateQueries({ queryKey: [listPath] });
      }
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to update item',
        variant: 'destructive',
      });
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: async () => {
      if (!selectedResource?.delete || selectedResource.delete.length === 0 || !selectedItem) throw new Error('No delete endpoint');
      
      const opIndex = selectedItem._operationIndex || 0;
      const deleteOp = selectedResource.delete[opIndex];
      
      let path = deleteOp.path;
      for (const param of deleteOp.pathParams) {
        let value = selectedItem[param];
        if (!value) {
          if (param.includes('OrId')) {
            const nameField = param.replace('OrId', '');
            value = selectedItem[nameField] || selectedItem.id || selectedItem.name;
          } else {
            value = selectedItem.id || selectedItem.name;
          }
        }
        if (!value) {
          throw new Error(`Cannot find value for path parameter: ${param}`);
        }
        path = path.replace(`{${param}}`, encodeURIComponent(value));
      }
      
      const response = await apiRequest('DELETE', await buildFullPath(path));
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null; // Handle 204 No Content or empty responses
      }
      setLastResponse({
        operation: `DELETE ${path}`,
        status: response.status,
        data: responseData,
        timestamp: new Date().toISOString(),
      });
      return responseData;
    },
    onSuccess: async () => {
      toast({ title: 'Success', description: 'Item deleted successfully' });
      setDeleteDialogOpen(false);
      setSelectedItem(null);
      if (selectedResource?.list) {
        const listPath = await buildFullPath(selectedResource.list.path);
        queryClient.invalidateQueries({ queryKey: [listPath] });
      }
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete item',
        variant: 'destructive',
      });
    },
  });

  if (specLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <Loader2 className="w-8 h-8 animate-spin" data-testid="loading-spinner" />
      </div>
    );
  }

  // If spec failed to load but we have element metadata, show that instead
  if (specError && currentElement) {
    const appName = currentAppStatus?.application?.name || appId;
    const servePrefix = currentElement.attributes?.['dev.getelements.elements.app.serve.prefix'];
    const allUris = currentAppStatus?.uris || [];
    
    // Filter URIs to only those matching this element's serve prefix
    const elementUris = servePrefix 
      ? allUris.filter((uri: string) => uri.includes(`/${servePrefix}`))
      : allUris;
    
    return (
      <div className="p-6 space-y-6">
        <div className="border-b pb-4">
          <div className="flex items-center gap-3 mb-2">
            <Database className="w-6 h-6" />
            <div>
              <h1 className="text-2xl font-bold" data-testid="page-title">
                {currentElement.definition?.name?.split('.').pop() || elementName}
              </h1>
              <div className="text-sm text-muted-foreground" data-testid="page-subtitle">
                Application: {appName}
                {currentAppStatus?.status && (
                  <Badge 
                    variant={currentAppStatus.status === 'CLEAN' ? 'default' : currentAppStatus.status === 'UNSTABLE' ? 'secondary' : 'destructive'}
                    className="ml-2 text-[10px]"
                  >
                    {currentAppStatus.status}
                  </Badge>
                )}
              </div>
            </div>
          </div>
          <Badge variant="secondary" className="mt-2">No OpenAPI Specification Available</Badge>
        </div>

        <Card>
          <CardContent className="p-6 space-y-4">
            <div>
              <h2 className="text-lg font-semibold mb-2">Element Information</h2>
              <div className="grid gap-2 text-sm">
                <div className="flex gap-2">
                  <span className="font-medium">Type:</span>
                  <span className="text-muted-foreground">{currentElement.type || 'Unknown'}</span>
                </div>
                <div className="flex gap-2">
                  <span className="font-medium">Full Name:</span>
                  <span className="text-muted-foreground font-mono text-xs">{currentElement.definition?.name || 'Unknown'}</span>
                </div>
                <div className="flex gap-2">
                  <span className="font-medium">Loader:</span>
                  <span className="text-muted-foreground font-mono text-xs">{currentElement.definition?.loader || 'Unknown'}</span>
                </div>
                {servePrefix && (
                  <div className="flex gap-2">
                    <span className="font-medium">Serve Prefix:</span>
                    <span className="text-muted-foreground font-mono text-xs">{servePrefix}</span>
                  </div>
                )}
              </div>
            </div>

            {elementUris.length > 0 && (
              <div>
                <h3 className="text-md font-semibold mb-2">Element URIs</h3>
                <div className="space-y-2">
                  {elementUris.map((uri: string, idx: number) => (
                    <div key={idx} className="p-3 bg-muted rounded-md flex items-start gap-2">
                      <code className="text-xs break-all flex-1">{uri}</code>
                      {uri.startsWith('ws://') || uri.startsWith('wss://') ? (
                        <Badge variant="secondary" className="text-[10px] shrink-0">WebSocket</Badge>
                      ) : (
                        <Badge variant="default" className="text-[10px] shrink-0">HTTP</Badge>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {currentElement.services && currentElement.services.length > 0 && (
              <div>
                <h3 className="text-md font-semibold mb-2">Services</h3>
                <div className="space-y-2">
                  {currentElement.services.map((service: any, idx: number) => (
                    <div key={idx} className="p-3 bg-muted rounded-md">
                      <div className="font-medium text-xs font-mono">{service.export?.exposed?.[0] || 'Service'}</div>
                      <div className="text-xs text-muted-foreground font-mono mt-1">{service.implementation?.type}</div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {currentElement.producedEvents && currentElement.producedEvents.length > 0 && (
              <div>
                <h3 className="text-md font-semibold mb-2">Produced Events</h3>
                <div className="space-y-2">
                  {currentElement.producedEvents.map((event: any, idx: number) => (
                    <div key={idx} className="p-3 bg-muted rounded-md">
                      <div className="font-medium">{event.name}</div>
                      <div className="text-sm text-muted-foreground">{event.type}</div>
                      {event.description && (
                        <div className="text-xs text-muted-foreground mt-1">{event.description}</div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {currentElement.attributes && Object.keys(currentElement.attributes).length > 0 && (
              <div>
                <h3 className="text-md font-semibold mb-2">Attributes</h3>
                <div className="space-y-1">
                  {Object.entries(currentElement.attributes).map(([key, value]) => (
                    <div key={key} className="flex gap-2 text-xs">
                      <span className="font-medium font-mono">{key}:</span>
                      <span className="text-muted-foreground font-mono">{String(value)}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    );
  }

  if (specError) {
    return (
      <div className="flex items-center justify-center h-full">
        <Card>
          <CardContent className="p-6">
            <p className="text-destructive" data-testid="error-message">
              Failed to load element information: {(specError as Error).message}
            </p>
            <p className="text-sm text-muted-foreground mt-2">
              No OpenAPI specification available and element metadata not found.
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <div className="border-b p-4">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <Database className="w-6 h-6" />
            <div>
              <h1 className="text-2xl font-bold" data-testid="page-title">
                {elementName || 'Element'} API Explorer
              </h1>
              <p className="text-sm text-muted-foreground" data-testid="page-subtitle">
                {appId ? `Application: ${appId}` : 'Explore element endpoints dynamically'}
              </p>
            </div>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetchSpec()}
            disabled={specLoading}
            data-testid="button-refresh-spec"
          >
            <RefreshCw className={`w-4 h-4 ${specLoading ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden">
        <div className="w-64 border-r overflow-y-auto p-4">
          <h2 className="font-semibold mb-3 text-sm uppercase tracking-wider">Resources</h2>
          <div className="space-y-1">
            {resources.length === 0 && (
              <p className="text-sm text-muted-foreground" data-testid="no-resources-message">
                No resources available
              </p>
            )}
            {resources.map((resource) => {
              return (
                <button
                  key={resource.resourceName}
                  onClick={() => setSelectedResource(resource)}
                  className={`w-full text-left px-3 py-2 rounded text-sm hover-elevate ${
                    selectedResource?.resourceName === resource.resourceName ? 'bg-accent' : ''
                  }`}
                  data-testid={`button-resource-${resource.resourceName.toLowerCase().replace(/\s+/g, '-')}`}
                >
                  <div className="truncate">{resource.resourceName}</div>
                  <div className="flex flex-wrap gap-1 mt-1">
                    {resource.list && (
                      <Badge variant="secondary" className="text-xs">
                        GET
                      </Badge>
                    )}
                    {resource.create && resource.create.length > 0 && <Badge variant="secondary" className="text-xs">POST</Badge>}
                    {resource.update && resource.update.length > 0 && <Badge variant="secondary" className="text-xs">PUT</Badge>}
                    {resource.delete && resource.delete.length > 0 && <Badge variant="secondary" className="text-xs">DELETE</Badge>}
                    {resource.get && !resource.list && <Badge variant="secondary" className="text-xs">GET</Badge>}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        <div className="flex-1 overflow-y-auto">
          {selectedResource ? (
            <div className="space-y-6 p-6">
              <DynamicResourceView
                resource={selectedResource}
                spec={spec}
                baseUrl={`/api/proxy${elementPath}`}
                onCreateClick={() => setCreateDialogOpen(true)}
                onEditClick={(item) => {
                  setSelectedItem(item);
                  setEditDialogOpen(true);
                }}
                onDeleteClick={(item) => {
                  setSelectedItem(item);
                  setDeleteDialogOpen(true);
                }}
              />
              
              {lastResponse && (
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle className="text-base">Last Response</CardTitle>
                        <CardDescription className="font-mono text-xs mt-1">
                          {lastResponse.operation}
                        </CardDescription>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge variant="outline" className="font-mono">
                          {lastResponse.status}
                        </Badge>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => {
                            const text = lastResponse.data === null 
                              ? 'null' 
                              : JSON.stringify(lastResponse.data, null, 2);
                            navigator.clipboard.writeText(text);
                            toast({ title: 'Copied', description: 'Response copied to clipboard' });
                          }}
                          data-testid="button-copy-response"
                        >
                          Copy
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <pre className="text-xs bg-muted p-4 rounded overflow-x-auto max-h-96 overflow-y-auto">
                      <code>
                        {lastResponse.data === null 
                          ? 'No content (status ' + lastResponse.status + ')' 
                          : JSON.stringify(lastResponse.data, null, 2)}
                      </code>
                    </pre>
                    <p className="text-xs text-muted-foreground mt-2">
                      {new Date(lastResponse.timestamp).toLocaleString()}
                    </p>
                  </CardContent>
                </Card>
              )}
            </div>
          ) : (
            <div className="p-6 space-y-6">
              {spec?.info && (
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Info className="w-5 h-5" />
                      {spec.info.title || 'API Information'}
                    </CardTitle>
                    {spec.info.version && (
                      <CardDescription>Version: {spec.info.version}</CardDescription>
                    )}
                  </CardHeader>
                  {spec.info.description && (
                    <CardContent>
                      <p className="text-sm text-muted-foreground whitespace-pre-wrap">
                        {spec.info.description}
                      </p>
                    </CardContent>
                  )}
                </Card>
              )}
              
              {spec?.externalDocs && (
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <ExternalLink className="w-5 h-5" />
                      External Documentation
                    </CardTitle>
                    {spec.externalDocs.description && (
                      <CardDescription>{spec.externalDocs.description}</CardDescription>
                    )}
                  </CardHeader>
                  {spec.externalDocs.url && (
                    <CardContent>
                      <a
                        href={spec.externalDocs.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-sm text-primary hover:underline flex items-center gap-1"
                        data-testid="link-external-docs"
                      >
                        {spec.externalDocs.url}
                        <ExternalLink className="w-3 h-3" />
                      </a>
                    </CardContent>
                  )}
                </Card>
              )}
              
              {!spec?.info && !spec?.externalDocs && (
                <div className="flex items-center justify-center h-full text-muted-foreground">
                  <p data-testid="select-resource-message">Select a resource to explore</p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create {selectedResource?.resourceName}</DialogTitle>
            <DialogDescription>
              Fill in the details to create a new {selectedResource?.resourceName.toLowerCase()}
            </DialogDescription>
          </DialogHeader>
          {selectedResource?.create && selectedResource.create.length > 0 && spec && (
            <DynamicFormGenerator
              spec={spec}
              schema={selectedResource.create[0].requestSchema || selectedResource.create[0].operation.requestBody?.content?.['application/json']?.schema}
              onSubmit={async (data) => createMutation.mutate(data)}
              onCancel={() => setCreateDialogOpen(false)}
              isLoading={createMutation.isPending}
              submitLabel="Create"
              pathParams={selectedResource.create[0].pathParams || []}
              queryParams={selectedResource.create[0].queryParams || []}
            />
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit {selectedResource?.resourceName}</DialogTitle>
            <DialogDescription>
              Update the details for this {selectedResource?.resourceName.toLowerCase()}
            </DialogDescription>
          </DialogHeader>
          {selectedResource?.update && selectedResource.update.length > 0 && selectedItem && spec && (
            <DynamicFormGenerator
              spec={spec}
              schema={(() => {
                const opIndex = selectedItem._operationIndex || 0;
                const updateOp = selectedResource.update[opIndex];
                return updateOp.requestSchema || updateOp.operation.requestBody?.content?.['application/json']?.schema;
              })()}
              initialData={selectedItem}
              onSubmit={async (data) => updateMutation.mutate(data)}
              onCancel={() => setEditDialogOpen(false)}
              isLoading={updateMutation.isPending}
              submitLabel="Update"
              pathParams={(() => {
                const opIndex = selectedItem._operationIndex || 0;
                const updateOp = selectedResource.update[opIndex];
                return updateOp.pathParams || [];
              })()}
              queryParams={(() => {
                const opIndex = selectedItem._operationIndex || 0;
                const updateOp = selectedResource.update[opIndex];
                return updateOp.queryParams || [];
              })()}
            />
          )}
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete this {selectedResource?.resourceName.toLowerCase()}. This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel data-testid="button-cancel-delete">Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => deleteMutation.mutate()}
              disabled={deleteMutation.isPending}
              data-testid="button-confirm-delete"
            >
              {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
