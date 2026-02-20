import { useState, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { analyzeOpenAPISpec, ResourceOperations } from '@/lib/openapi-analyzer';
import { DynamicResourceView } from '@/components/DynamicResourceView';
import { DynamicFormGenerator } from '@/components/DynamicFormGenerator';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Loader2, Database, Lock, User as UserIcon } from 'lucide-react';
import { queryClient } from '@/lib/queryClient';
import { getApiPath, apiClient } from '@/lib/api-client';
import { useToast } from '@/hooks/use-toast';

export default function DynamicApiExplorer() {
  const [selectedResource, setSelectedResource] = useState<ResourceOperations | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [getDialogOpen, setGetDialogOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [lastResponse, setLastResponse] = useState<{
    operation: string;
    status: number;
    data: any;
    timestamp: string;
  } | null>(null);
  const [sessionOverride, setSessionOverride] = useState(() => localStorage.getItem('elements-session-override') || '');
  const [profileId, setProfileId] = useState(() => localStorage.getItem('elements-profile-id') || '');
  const [overrideEnabled, setOverrideEnabled] = useState(() => {
    const saved = localStorage.getItem('elements-override-enabled');
    return saved !== null ? saved === 'true' : true;
  });

  useEffect(() => {
    if (sessionOverride) {
      localStorage.setItem('elements-session-override', sessionOverride);
    } else {
      localStorage.removeItem('elements-session-override');
    }
  }, [sessionOverride]);

  useEffect(() => {
    if (profileId) {
      localStorage.setItem('elements-profile-id', profileId);
    } else {
      localStorage.removeItem('elements-profile-id');
    }
  }, [profileId]);

  useEffect(() => {
    localStorage.setItem('elements-override-enabled', String(overrideEnabled));
  }, [overrideEnabled]);

  const { toast } = useToast();

  const getCustomHeaders = (): Record<string, string> => {
    const headers: Record<string, string> = {};
    const useOverride = overrideEnabled && sessionOverride;
    const token = (useOverride ? sessionOverride : '') || apiClient.getSessionToken() || '';
    if (token) {
      headers['Elements-SessionSecret'] = token;
    }
    if (profileId) {
      headers['Elements-ProfileId'] = profileId;
    }
    return headers;
  };

  // Reset last response when switching resources
  useEffect(() => {
    setLastResponse(null);
  }, [selectedResource?.resourceName]);

  // Fetch OpenAPI spec from Elements backend (JSON endpoint)
  const { data: spec, isLoading: specLoading, error: specError } = useQuery({
    queryKey: ['/api/rest/openapi.json'],
    queryFn: async () => {
      // Get session token for authentication
      const { apiClient: client } = await import('@/lib/api-client');
      const sessionToken = client.getSessionToken();
      
      const headers: Record<string, string> = {};
      if (sessionToken) {
        headers['Elements-SessionSecret'] = sessionToken;
      }
      
      // Use getApiPath to handle production vs development mode
      const specPath = await getApiPath('/api/rest/openapi.json');
      const response = await fetch(specPath, { 
        credentials: 'include',
        headers
      });
      if (!response.ok) {
        throw new Error(`Failed to fetch OpenAPI spec: ${response.status} ${response.statusText}`);
      }
      return response.json();
    },
    staleTime: 60000, // Cache for 1 minute
  });

  // Analyze spec to extract all resources
  const resources = spec ? Array.from(analyzeOpenAPISpec(spec as any).values()) : [];

  // Create mutation
  const createMutation = useMutation({
    mutationFn: async (data: { pathParams: Record<string, string>; queryParams: Record<string, string>; body: any }) => {
      if (!selectedResource?.create || selectedResource.create.length === 0) throw new Error('No create endpoint');
      const createOp = selectedResource.create[0]; // Use first create operation
      
      // Build URL with path parameters
      let path = `/api/rest${createOp.path}`;
      for (const [key, value] of Object.entries(data.pathParams)) {
        path = path.replace(`{${key}}`, encodeURIComponent(value));
      }
      
      // Add query parameters
      const queryString = Object.entries(data.queryParams)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&');
      if (queryString) {
        path += `?${queryString}`;
      }
      
      const url = await getApiPath(path);
      const headers: Record<string, string> = { 'Content-Type': 'application/json', ...getCustomHeaders() };
      const response = await fetch(url, { method: 'POST', headers, body: JSON.stringify(data.body), credentials: 'include' });
      if (!response.ok) {
        const errorText = await response.text().catch(() => response.statusText);
        setLastResponse({ operation: `POST ${path}`, status: response.status, data: errorText, timestamp: new Date().toISOString() });
        return { __error: true, message: `${response.status}: ${errorText}` };
      }
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null;
      }
      setLastResponse({
        operation: `POST ${path}`,
        status: response.status,
        data: responseData,
        timestamp: new Date().toISOString(),
      });
      return responseData;
    },
    onSuccess: async (result: any) => {
      if (result?.__error) {
        toast({ title: 'Error', description: result.message || 'Failed to create item', variant: 'destructive' });
        return;
      }
      toast({ title: 'Success', description: 'Item created successfully' });
      setCreateDialogOpen(false);
      if (selectedResource?.list) {
        const listPath = await getApiPath(`/api/rest${selectedResource.list.path}`);
        queryClient.invalidateQueries({ queryKey: [listPath] });
      }
    },
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: async (data: { pathParams: Record<string, string>; queryParams: Record<string, string>; body: any }) => {
      if (!selectedResource?.update || selectedResource.update.length === 0 || !selectedItem) throw new Error('No update endpoint');
      const updateOp = selectedResource.update[0]; // Use first update operation
      
      // Build URL with path parameters (from form data, not selectedItem)
      let path = `/api/rest${updateOp.path}`;
      for (const [key, value] of Object.entries(data.pathParams)) {
        path = path.replace(`{${key}}`, encodeURIComponent(value));
      }
      
      // Add query parameters
      const queryString = Object.entries(data.queryParams)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&');
      if (queryString) {
        path += `?${queryString}`;
      }
      
      const url = await getApiPath(path);
      const headers: Record<string, string> = { 'Content-Type': 'application/json', ...getCustomHeaders() };
      const response = await fetch(url, { method: 'PUT', headers, body: JSON.stringify(data.body), credentials: 'include' });
      if (!response.ok) {
        const errorText = await response.text().catch(() => response.statusText);
        setLastResponse({ operation: `PUT ${path}`, status: response.status, data: errorText, timestamp: new Date().toISOString() });
        return { __error: true, message: `${response.status}: ${errorText}` };
      }
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null;
      }
      setLastResponse({
        operation: `PUT ${path}`,
        status: response.status,
        data: responseData,
        timestamp: new Date().toISOString(),
      });
      return responseData;
    },
    onSuccess: async (result: any) => {
      if (result?.__error) {
        toast({ title: 'Error', description: result.message || 'Failed to update item', variant: 'destructive' });
        return;
      }
      toast({ title: 'Success', description: 'Item updated successfully' });
      setEditDialogOpen(false);
      setSelectedItem(null);
      if (selectedResource?.list) {
        const listPath = await getApiPath(`/api/rest${selectedResource.list.path}`);
        queryClient.invalidateQueries({ queryKey: [listPath] });
      }
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: async () => {
      if (!selectedResource?.delete || selectedResource.delete.length === 0 || !selectedItem) throw new Error('No delete endpoint');
      const deleteOp = selectedResource.delete[0]; // Use first delete operation
      
      // Build path with params
      let path = deleteOp.path;
      for (const param of deleteOp.pathParams) {
        // Try to find the value in selectedItem using various strategies
        let value = selectedItem[param]; // Exact match
        if (!value) {
          // Try common variations: nameOrId -> try 'name' then 'id'
          if (param.includes('OrId')) {
            const nameField = param.replace('OrId', '');
            value = selectedItem[nameField] || selectedItem.id || selectedItem.name;
          } else {
            // Fallback to id or name
            value = selectedItem.id || selectedItem.name;
          }
        }
        if (!value) {
          throw new Error(`Cannot find value for path parameter: ${param}`);
        }
        path = path.replace(`{${param}}`, encodeURIComponent(value));
      }
      
      const url = await getApiPath(`/api/rest${path}`);
      const headers: Record<string, string> = { ...getCustomHeaders() };
      const response = await fetch(url, { method: 'DELETE', headers, credentials: 'include' });
      if (!response.ok) {
        const errorText = await response.text().catch(() => response.statusText);
        setLastResponse({ operation: `DELETE ${path}`, status: response.status, data: errorText, timestamp: new Date().toISOString() });
        return { __error: true, message: `${response.status}: ${errorText}` };
      }
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null;
      }
      setLastResponse({
        operation: `DELETE ${path}`,
        status: response.status,
        data: responseData,
        timestamp: new Date().toISOString(),
      });
      return responseData;
    },
    onSuccess: async (result: any) => {
      if (result?.__error) {
        toast({ title: 'Error', description: result.message || 'Failed to delete item', variant: 'destructive' });
        return;
      }
      toast({ title: 'Success', description: 'Item deleted successfully' });
      setDeleteDialogOpen(false);
      setSelectedItem(null);
      if (selectedResource?.list) {
        const listPath = await getApiPath(`/api/rest${selectedResource.list.path}`);
        queryClient.invalidateQueries({ queryKey: [listPath] });
      }
    },
  });

  // GET mutation (for GET endpoints with path params)
  const getMutation = useMutation({
    mutationFn: async (data: { pathParams: Record<string, string>; queryParams: Record<string, string>; body: any }) => {
      if (!selectedResource?.get) throw new Error('No GET endpoint');
      const getOp = selectedResource.get;
      
      let path = `/api/rest${getOp.path}`;
      for (const [key, value] of Object.entries(data.pathParams)) {
        path = path.replace(`{${key}}`, encodeURIComponent(value));
      }
      
      const queryString = Object.entries(data.queryParams)
        .filter(([, value]) => value !== '' && value !== null && value !== undefined)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&');
      if (queryString) {
        path += `?${queryString}`;
      }
      
      const url = await getApiPath(path);
      const headers: Record<string, string> = { ...getCustomHeaders() };
      const response = await fetch(url, { headers, credentials: 'include' });
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        try {
          responseData = await response.text();
        } catch {
          responseData = null;
        }
      }
      
      setLastResponse({
        operation: `GET ${path}`,
        status: response.status,
        data: responseData,
        timestamp: new Date().toISOString(),
      });
      
      if (!response.ok) {
        return { __error: true, message: responseData?.message || `Request failed: ${response.status} ${response.statusText}` };
      }
      
      return responseData;
    },
    onSuccess: (result: any) => {
      if (result?.__error) {
        toast({ title: 'Error', description: result.message || 'GET request failed', variant: 'destructive' });
        return;
      }
      toast({ title: 'Success', description: 'GET request completed successfully' });
      setGetDialogOpen(false);
    },
  });

  const handleCreateClick = () => {
    setCreateDialogOpen(true);
  };

  const handleEditClick = (item: any) => {
    setSelectedItem(item);
    setEditDialogOpen(true);
  };

  const handleDeleteClick = (item: any) => {
    setSelectedItem(item);
    setDeleteDialogOpen(true);
  };

  const handleGetClick = () => {
    setGetDialogOpen(true);
  };

  if (specLoading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="text-center">
          <Loader2 className="w-12 h-12 animate-spin text-muted-foreground mx-auto mb-4" />
          <p className="text-lg font-medium">Loading API Specification...</p>
        </div>
      </div>
    );
  }

  if (!spec) {
    return (
      <div className="flex items-center justify-center h-screen">
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-lg font-medium text-destructive">Failed to Load API Specification</p>
            <p className="text-sm text-muted-foreground mt-2">
              Could not fetch OpenAPI spec from the Elements backend.
            </p>
            {specError && (
              <p className="text-xs text-destructive mt-2">
                Error: {(specError as Error).message}
              </p>
            )}
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <Tabs defaultValue="explorer" className="h-full flex flex-col">
      <div className="border-b px-4 pt-2">
        <TabsList>
          <TabsTrigger value="explorer" data-testid="tab-explorer">Explorer</TabsTrigger>
          <TabsTrigger value="raw-json" data-testid="tab-raw-json">Raw JSON</TabsTrigger>
        </TabsList>
      </div>
      <TabsContent value="raw-json" className="flex-1 overflow-hidden m-0">
        <ScrollArea className="h-full p-4">
          <pre className="text-xs font-mono bg-muted p-4 rounded-md">
            <code>{spec ? JSON.stringify(spec, null, 2) : 'No data'}</code>
          </pre>
        </ScrollArea>
      </TabsContent>
      <TabsContent value="explorer" className="flex-1 overflow-hidden m-0">
    <div className="h-full flex">
      {/* Resource List Sidebar */}
      <div className="w-64 border-r bg-muted/30 p-4 overflow-y-auto">
        <div className="mb-4">
          <h2 className="text-lg font-bold mb-1">API Resources</h2>
          <p className="text-xs text-muted-foreground">
            {resources.length} resources available
          </p>
        </div>
        
        <div className="space-y-1">
          {resources.map((resource) => (
            <button
              key={resource.resourceName}
              onClick={() => setSelectedResource(resource)}
              className={`w-full text-left px-3 py-2 rounded-md text-sm hover-elevate transition-colors ${
                selectedResource?.resourceName === resource.resourceName
                  ? 'bg-primary text-primary-foreground'
                  : ''
              }`}
              data-testid={`button-select-${resource.resourceName}`}
            >
              <div className="flex items-center gap-2">
                <Database className="w-4 h-4 flex-shrink-0" />
                <span className="truncate">
                  {resource.resourceName.charAt(0).toUpperCase() + resource.resourceName.slice(1)}
                </span>
              </div>
              <div className="flex gap-1 mt-1 flex-wrap">
                {resource.list && <Badge variant="outline" className="text-[10px] px-1 py-0">LIST</Badge>}
                {resource.create && <Badge variant="outline" className="text-[10px] px-1 py-0">CREATE</Badge>}
                {resource.update && <Badge variant="outline" className="text-[10px] px-1 py-0">UPDATE</Badge>}
                {resource.delete && <Badge variant="outline" className="text-[10px] px-1 py-0">DELETE</Badge>}
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 p-6 overflow-y-auto">
        {selectedResource ? (
          <div className="space-y-4">
            <Card>
              <CardContent className="pt-4 pb-4">
                <div className="flex items-end gap-4 flex-wrap">
                  <div className="flex-1 min-w-[200px]">
                    <Label htmlFor="session-override" className="text-xs flex items-center gap-1 mb-1">
                      <Lock className="w-3 h-3" />
                      Session Secret Override
                    </Label>
                    <Input
                      id="session-override"
                      type="text"
                      placeholder="Leave empty to use current session"
                      value={sessionOverride}
                      onChange={(e) => setSessionOverride(e.target.value)}
                      className="font-mono text-xs"
                      data-testid="input-session-override"
                    />
                  </div>
                  <div className="flex-1 min-w-[200px]">
                    <Label htmlFor="profile-id" className="text-xs flex items-center gap-1 mb-1">
                      <UserIcon className="w-3 h-3" />
                      Profile ID
                    </Label>
                    <Input
                      id="profile-id"
                      type="text"
                      placeholder="Optional"
                      value={profileId}
                      onChange={(e) => setProfileId(e.target.value)}
                      className="font-mono text-xs"
                      data-testid="input-profile-id"
                    />
                  </div>
                  {(sessionOverride || profileId) && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => { setSessionOverride(''); setProfileId(''); }}
                      data-testid="button-clear-headers"
                    >
                      Clear
                    </Button>
                  )}
                </div>
                {sessionOverride && (
                  <div className="flex items-center gap-2 mt-3">
                    <Checkbox
                      id="override-enabled"
                      checked={overrideEnabled}
                      onCheckedChange={(checked) => setOverrideEnabled(!!checked)}
                      data-testid="checkbox-override-enabled"
                    />
                    <Label htmlFor="override-enabled" className="text-xs text-muted-foreground cursor-pointer">
                      Enable Override
                    </Label>
                    {overrideEnabled && (
                      <span className="text-[11px] text-muted-foreground">
                        — Using custom session secret for all requests
                      </span>
                    )}
                    {!overrideEnabled && (
                      <span className="text-[11px] text-muted-foreground">
                        — Override disabled, using current session
                      </span>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>

            <DynamicResourceView
              resource={selectedResource}
              spec={spec}
              customHeaders={getCustomHeaders()}
              onCreateClick={selectedResource.create ? handleCreateClick : undefined}
              onEditClick={selectedResource.update ? handleEditClick : undefined}
              onDeleteClick={selectedResource.delete ? handleDeleteClick : undefined}
              onGetClick={selectedResource.get ? handleGetClick : undefined}
            />
            
            {/* API Response Display */}
            {lastResponse && (() => {
              const isError = lastResponse.status >= 400;
              const errorMessage = isError ? (
                typeof lastResponse.data === 'string' 
                  ? lastResponse.data 
                  : lastResponse.data?.message || lastResponse.data?.error || lastResponse.data?.detail || null
              ) : null;
              return (
              <Card data-testid="card-api-response" className={isError ? 'border-destructive' : ''}>
                <CardHeader>
                  <div className="flex items-center justify-between gap-2 flex-wrap">
                    <div className="flex items-center gap-2">
                      <CardTitle className="text-base">API Response</CardTitle>
                      <Badge 
                        variant={isError ? 'destructive' : 'outline'} 
                        className="font-mono"
                        data-testid="badge-response-status"
                      >
                        {lastResponse.status}
                      </Badge>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-muted-foreground font-mono">{lastResponse.operation}</span>
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
                  {isError && errorMessage && (
                    <div className="bg-destructive/10 border border-destructive/30 rounded-md p-3 mb-3" data-testid="error-summary">
                      <p className="text-sm font-medium text-destructive">
                        {errorMessage}
                      </p>
                    </div>
                  )}
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
              );
            })()}
          </div>
        ) : (
          <div className="flex items-center justify-center h-full text-muted-foreground">
            <div className="text-center">
              <Database className="w-16 h-16 mx-auto mb-4 opacity-50" />
              <p className="text-lg font-medium">Select a Resource</p>
              <p className="text-sm mt-2">Choose a resource from the sidebar to view and manage its data</p>
            </div>
          </div>
        )}
      </div>

      {/* Create Dialog */}
      <Dialog open={createDialogOpen} onOpenChange={setCreateDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto overflow-x-hidden">
          <DialogHeader>
            <DialogTitle>Create {selectedResource?.resourceName}</DialogTitle>
            <DialogDescription className="break-words">
              {selectedResource?.create?.[0]?.operation.description || selectedResource?.create?.[0]?.operation.summary}
            </DialogDescription>
          </DialogHeader>
          <div className="min-w-0">
            {selectedResource?.create?.[0]?.requestSchema && (
              <DynamicFormGenerator
                spec={spec}
                schema={selectedResource.create[0].requestSchema}
                onSubmit={async (data) => {
                  await createMutation.mutateAsync(data);
                }}
                onCancel={() => setCreateDialogOpen(false)}
                isLoading={createMutation.isPending}
                submitLabel="Create"
                pathParams={selectedResource.create[0].pathParams || []}
                queryParams={selectedResource.create[0].queryParams || []}
              />
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto overflow-x-hidden">
          <DialogHeader>
            <DialogTitle>Edit {selectedResource?.resourceName}</DialogTitle>
            <DialogDescription className="break-words">
              {selectedResource?.update?.[0]?.operation.description || selectedResource?.update?.[0]?.operation.summary}
            </DialogDescription>
          </DialogHeader>
          <div className="min-w-0">
            {selectedResource?.update?.[0]?.requestSchema && selectedItem && (
              <DynamicFormGenerator
                spec={spec}
                schema={selectedResource.update[0].requestSchema}
                onSubmit={async (data) => {
                  await updateMutation.mutateAsync(data);
                }}
                onCancel={() => {
                  setEditDialogOpen(false);
                  setSelectedItem(null);
                }}
                initialData={selectedItem}
                isLoading={updateMutation.isPending}
                submitLabel="Update"
                pathParams={selectedResource.update[0].pathParams || []}
                queryParams={selectedResource.update[0].queryParams || []}
              />
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* GET Dialog */}
      <Dialog open={getDialogOpen} onOpenChange={setGetDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto overflow-x-hidden">
          <DialogHeader>
            <DialogTitle>GET {selectedResource?.resourceName}</DialogTitle>
            <DialogDescription className="break-words">
              {selectedResource?.get?.operation.description || selectedResource?.get?.operation.summary || 'Retrieve a specific item by providing the required parameters'}
            </DialogDescription>
          </DialogHeader>
          <div className="min-w-0">
            {selectedResource?.get && (
              <DynamicFormGenerator
                spec={spec}
                schema={{}}
                onSubmit={async (data) => {
                  await getMutation.mutateAsync(data);
                }}
                onCancel={() => setGetDialogOpen(false)}
                isLoading={getMutation.isPending}
                submitLabel="Send GET Request"
                pathParams={selectedResource.get.pathParams || []}
                queryParams={selectedResource.get.queryParams || []}
              />
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. This will permanently delete this {selectedResource?.resourceName}.
              {selectedItem && (
                <div className="mt-2 p-2 bg-muted rounded-md overflow-x-auto">
                  <code className="text-xs break-all whitespace-pre-wrap">{JSON.stringify(selectedItem, null, 2)}</code>
                </div>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel
              onClick={() => {
                setDeleteDialogOpen(false);
                setSelectedItem(null);
              }}
              data-testid="button-cancel-delete"
            >
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={() => deleteMutation.mutate()}
              disabled={deleteMutation.isPending}
              data-testid="button-confirm-delete"
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {deleteMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
      </TabsContent>
    </Tabs>
  );
}
