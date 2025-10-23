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
import { Loader2, Database } from 'lucide-react';
import { apiRequest, queryClient } from '@/lib/queryClient';
import { getApiPath } from '@/lib/api-client';
import { useToast } from '@/hooks/use-toast';

export default function DynamicApiExplorer() {
  const [selectedResource, setSelectedResource] = useState<ResourceOperations | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [lastResponse, setLastResponse] = useState<{
    operation: string;
    status: number;
    data: any;
    timestamp: string;
  } | null>(null);
  const { toast } = useToast();

  // Reset last response when switching resources
  useEffect(() => {
    setLastResponse(null);
  }, [selectedResource?.resourceName]);

  // Fetch OpenAPI spec from Elements backend (YAML endpoint, converted to JSON by our proxy)
  const { data: spec, isLoading: specLoading, error: specError } = useQuery({
    queryKey: ['/api/rest/openapi.yaml'],
    queryFn: async () => {
      // Use getApiPath to handle production vs development mode
      const specPath = await getApiPath('/api/rest/openapi.yaml');
      const response = await fetch(specPath, { credentials: 'include' });
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
      const response = await apiRequest('POST', url, data.body);
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null; // Handle 204 No Content or empty responses
      }
      setLastResponse({
        operation: `POST ${url}`,
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
        const listPath = await getApiPath(`/api/rest${selectedResource.list.path}`);
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
      const response = await apiRequest('PUT', url, data.body);
      let responseData;
      try {
        responseData = await response.json();
      } catch {
        responseData = null; // Handle 204 No Content or empty responses
      }
      setLastResponse({
        operation: `PUT ${url}`,
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
        const listPath = await getApiPath(`/api/rest${selectedResource.list.path}`);
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
      const response = await apiRequest('DELETE', url);
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
        const listPath = await getApiPath(`/api/rest${selectedResource.list.path}`);
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
            <DynamicResourceView
              resource={selectedResource}
              spec={spec}
              onCreateClick={selectedResource.create ? handleCreateClick : undefined}
              onEditClick={selectedResource.update ? handleEditClick : undefined}
              onDeleteClick={selectedResource.delete ? handleDeleteClick : undefined}
            />
            
            {/* API Response Display */}
            {lastResponse && (
              <Card data-testid="card-api-response">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-base">API Response</CardTitle>
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
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create {selectedResource?.resourceName}</DialogTitle>
            <DialogDescription>
              {selectedResource?.create?.[0]?.operation.description || selectedResource?.create?.[0]?.operation.summary}
            </DialogDescription>
          </DialogHeader>
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
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit {selectedResource?.resourceName}</DialogTitle>
            <DialogDescription>
              {selectedResource?.update?.[0]?.operation.description || selectedResource?.update?.[0]?.operation.summary}
            </DialogDescription>
          </DialogHeader>
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
                <div className="mt-2 p-2 bg-muted rounded-md">
                  <code className="text-xs">{JSON.stringify(selectedItem, null, 2)}</code>
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
  );
}
