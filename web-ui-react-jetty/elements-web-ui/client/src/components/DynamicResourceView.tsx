import { useState, useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Loader2, Plus, Pencil, Trash2, ExternalLink, Info, Lock, User } from 'lucide-react';
import { ResourceOperations } from '@/lib/openapi-analyzer';
import { getApiPath, apiClient } from '@/lib/api-client';

interface DynamicResourceViewProps {
  resource: ResourceOperations;
  spec: any;
  baseUrl?: string; // Optional base URL for element APIs (defaults to /api/proxy/api/rest for core API)
  onCreateClick?: () => void;
  onEditClick?: (item: any) => void;
  onDeleteClick?: (item: any) => void;
}

export function DynamicResourceView({
  resource,
  spec,
  baseUrl = '/api/proxy/api/rest', // Default to core API
  onCreateClick,
  onEditClick,
  onDeleteClick,
}: DynamicResourceViewProps) {
  const [page, setPage] = useState(0);
  const [useCustomToken, setUseCustomToken] = useState(false);
  const [customToken, setCustomToken] = useState('');
  const [requestedPath, setRequestedPath] = useState<string | null>(null);
  
  // Read page size from settings (localStorage)
  const getPageSize = () => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  };
  const [pageSize, setPageSize] = useState(getPageSize());
  
  // Update page size if settings change
  useEffect(() => {
    const handleStorageChange = () => {
      const newPageSize = getPageSize();
      if (newPageSize !== pageSize) {
        setPageSize(newPageSize);
        setPage(0); // Reset to first page when page size changes
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [pageSize]);

  // Build query parameters for list endpoint
  const queryParams = new URLSearchParams();
  if (resource.list?.isPaginated) {
    queryParams.set('offset', (page * pageSize).toString());
    queryParams.set('count', pageSize.toString());
  }

  // Build the base path (without environment-specific prefix yet)
  const basePath = resource.list?.path ? `${baseUrl}${resource.list.path}` : '';
  const pathWithQuery = `${basePath}${queryParams.toString() ? `?${queryParams.toString()}` : ''}`;

  // Determine expected response content type from OpenAPI spec
  const getResponseContentType = () => {
    if (!resource.list?.operation?.responses) return 'application/json';
    
    const responses = resource.list.operation.responses;
    const successResponse = responses['200'] || responses['default'];
    
    if (!successResponse?.content) return 'application/json';
    
    const contentTypes = Object.keys(successResponse.content);
    return contentTypes[0] || 'application/json';
  };

  const responseContentType = getResponseContentType();
  const isJsonResponse = responseContentType.includes('json');

  // Analyze security requirements from OpenAPI spec
  const getSecurityRequirements = () => {
    // For element endpoints, ONLY check operation-level security (ignore global security)
    // This allows elements to have global security definitions while selectively
    // enforcing auth on specific operations
    const operationSecurity = resource.list?.operation?.security;
    
    // If operation has empty security array, explicitly no auth required
    if (operationSecurity && operationSecurity.length === 0) return null;
    
    // ONLY use operation-level security, do NOT fall back to global spec security
    const securityToUse = operationSecurity;
    
    // If no security at all, treat as no auth required
    if (!securityToUse || securityToUse.length === 0) return null;
    
    // Get security schemes from components
    const securitySchemes = spec?.components?.securitySchemes || {};
    
    // Parse security requirements
    const requirements = securityToUse.flatMap((secReq: { [key: string]: string[] }) => 
      Object.keys(secReq).map(schemeName => ({
        name: schemeName,
        scheme: securitySchemes[schemeName],
        scopes: secReq[schemeName]
      }))
    );
    
    return requirements.length > 0 ? requirements : null;
  };

  const securityRequirements = getSecurityRequirements();
  
  // Check if auth is required - look for any security scheme with apiKey type
  // This handles both Elements-SessionSecret and session_secret naming variations
  const requiresAuth = securityRequirements?.some((req: { name: string; scheme: any; scopes: string[] }) => {
    // If we have a scheme definition, check its properties
    if (req.scheme) {
      return req.scheme.type === 'apiKey' && 
             (req.scheme.name === 'Elements-SessionSecret' || req.name === 'session_secret');
    }
    // If no scheme definition found, but the security requirement is named session_secret, treat as auth required
    return req.name === 'session_secret';
  }) ?? false; // Default to false if securityRequirements is null/undefined

  // Reset requestedPath when token settings change (user must click Send Request again)
  useEffect(() => {
    setRequestedPath(null);
  }, [useCustomToken, customToken]);

  // Determine which token to use
  const getAuthToken = () => {
    // Always send token if we have one (backend may require auth even if not in spec)
    // Use custom token if explicitly enabled, otherwise use current session token
    if (useCustomToken) return customToken;
    return apiClient.getSessionToken() || '';
  };

  // Fetch data from list endpoint - use basePath in queryKey for consistent cache invalidation
  // Note: We don't include useCustomToken/customToken in queryKey because we manually control fetching
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: [basePath, page, pageSize],
    queryFn: async () => {
      // Use getApiPath to handle production vs development mode
      const fullUrl = await getApiPath(pathWithQuery);
      
      const headers: Record<string, string> = {};
      const token = getAuthToken();
      if (token) {
        headers['Elements-SessionSecret'] = token;
      }
      
      // Set Accept header based on expected response type
      headers['Accept'] = responseContentType;
      
      const response = await fetch(fullUrl, { headers });
      if (!response.ok) throw new Error(`Failed to fetch: ${response.statusText}`);
      
      // Handle different response content types
      if (isJsonResponse) {
        return response.json();
      } else if (responseContentType.includes('text')) {
        return response.text();
      } else {
        return response.blob();
      }
    },
    enabled: !!resource.list && requestedPath === basePath,
    retry: false, // Don't auto-retry on error - let user manually retry
  });

  // Handle non-JSON responses (text, blob, etc.)
  const isTextResponse = typeof data === 'string';
  const isBlobResponse = data instanceof Blob;

  // Extract items from response (handle different pagination formats)
  let items: any[] = [];
  if (!isJsonResponse) {
    // For non-JSON responses, we don't have items to display in a table
    items = [];
  } else if (Array.isArray(data)) {
    // Data is already an array
    items = data;
  } else if (data && typeof data === 'object') {
    // Check for paginated response formats
    if (Array.isArray((data as any).content)) {
      items = (data as any).content;
    } else if (Array.isArray((data as any).objects)) {
      items = (data as any).objects;
    } else {
      // Single object response (like health check) - wrap in array
      items = [data];
    }
  }
  
  const total = (data as any)?.total || items.length;
  const totalPages = Math.ceil(total / pageSize);

  // Get displayable column names from the first item
  const getColumns = () => {
    if (items.length === 0) return [];
    const firstItem = items[0];
    
    // Prioritize certain common fields
    const priorityFields = ['id', 'name', 'displayName', 'email', 'type', 'status'];
    const allKeys = Object.keys(firstItem);
    
    // Filter out complex objects and arrays for display
    const simpleKeys = allKeys.filter(key => {
      const value = firstItem[key];
      return value === null || value === undefined || 
             typeof value === 'string' || 
             typeof value === 'number' || 
             typeof value === 'boolean';
    });
    
    // Sort: priority fields first, then alphabetically
    return simpleKeys.sort((a, b) => {
      const aPriority = priorityFields.indexOf(a);
      const bPriority = priorityFields.indexOf(b);
      
      if (aPriority !== -1 && bPriority !== -1) {
        return aPriority - bPriority;
      }
      if (aPriority !== -1) return -1;
      if (bPriority !== -1) return 1;
      
      return a.localeCompare(b);
    }).slice(0, 6); // Limit to 6 columns
  };

  const columns = getColumns();

  // Render cell value
  const renderCellValue = (value: any) => {
    if (value === null || value === undefined) {
      return <span className="text-muted-foreground">—</span>;
    }
    if (typeof value === 'boolean') {
      return <Badge variant={value ? 'default' : 'secondary'}>{value ? 'Yes' : 'No'}</Badge>;
    }
    if (typeof value === 'string' && value.length > 50) {
      return <span className="truncate block max-w-xs" title={value}>{value}</span>;
    }
    return <span>{String(value)}</span>;
  };

  const getResourceDisplayName = () => {
    const name = resource.resourceName;
    // Convert kebab-case or snake_case to Title Case
    return name
      .split(/[-_]/)
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  };

  // Get any available operation for displaying info when there's no list operation
  const getAnyOperation = () => {
    return resource.list?.operation || 
           (resource.create && resource.create.length > 0 ? resource.create[0].operation : undefined) || 
           (resource.update && resource.update.length > 0 ? resource.update[0].operation : undefined) || 
           (resource.delete && resource.delete.length > 0 ? resource.delete[0].operation : undefined);
  };

  const anyOperation = getAnyOperation();
  
  // Handler for sending request
  const handleSendRequest = () => {
    setRequestedPath(basePath);
    // Force refetch even if data is cached
    refetch();
  };

  return (
    <div className="space-y-4">
      {/* OpenAPI Info Section */}
      {spec?.info && (
        <Alert>
          <Info className="h-4 w-4" />
          <AlertDescription>
            <div className="space-y-2">
              {spec.info.title && (
                <div>
                  <strong className="font-semibold">{spec.info.title}</strong>
                  {spec.info.version && <span className="text-xs text-muted-foreground ml-2">v{spec.info.version}</span>}
                </div>
              )}
              {spec.info.description && (
                <p className="text-sm">{spec.info.description}</p>
              )}
              {spec.info.contact && (
                <div className="flex items-center gap-4 text-xs text-muted-foreground flex-wrap">
                  {spec.info.contact.name && <span>{spec.info.contact.name}</span>}
                  {spec.info.contact.email && (
                    <a href={`mailto:${spec.info.contact.email}`} className="hover:text-foreground">
                      {spec.info.contact.email}
                    </a>
                  )}
                  {spec.info.contact.url && (
                    <a 
                      href={spec.info.contact.url} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="hover:text-foreground flex items-center gap-1"
                    >
                      Website <ExternalLink className="w-3 h-3" />
                    </a>
                  )}
                </div>
              )}
            </div>
          </AlertDescription>
        </Alert>
      )}

      {/* External Documentation */}
      {spec?.externalDocs && (
        <Alert>
          <ExternalLink className="h-4 w-4" />
          <AlertDescription>
            {spec.externalDocs.description && (
              <p className="text-sm mb-1">{spec.externalDocs.description}</p>
            )}
            {spec.externalDocs.url && (
              <a 
                href={spec.externalDocs.url} 
                target="_blank" 
                rel="noopener noreferrer"
                className="text-sm text-primary hover:underline flex items-center gap-1"
              >
                {spec.externalDocs.url} <ExternalLink className="w-3 h-3" />
              </a>
            )}
          </AlertDescription>
        </Alert>
      )}

      {/* Authentication Options */}
      {requiresAuth && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium flex items-center gap-2">
              <Lock className="w-4 h-4" />
              Authentication Required
            </CardTitle>
            <CardDescription className="text-xs">
              This endpoint requires authentication via Elements-SessionSecret header
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <input
                  type="radio"
                  id="use-current-session"
                  checked={!useCustomToken}
                  onChange={() => setUseCustomToken(false)}
                  className="cursor-pointer"
                  data-testid="radio-use-current-session"
                />
                <Label htmlFor="use-current-session" className="cursor-pointer flex items-center gap-1 text-sm">
                  <User className="w-3 h-3" />
                  Use Current Session
                </Label>
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="radio"
                  id="use-custom-token"
                  checked={useCustomToken}
                  onChange={() => setUseCustomToken(true)}
                  className="cursor-pointer"
                  data-testid="radio-use-custom-token"
                />
                <Label htmlFor="use-custom-token" className="cursor-pointer text-sm">
                  Override with Custom Token
                </Label>
              </div>
            </div>
            
            {useCustomToken && (
              <div className="space-y-2">
                <Label htmlFor="custom-token" className="text-xs">Session Token</Label>
                <Input
                  id="custom-token"
                  type="text"
                  placeholder="Enter session token..."
                  value={customToken}
                  onChange={(e) => setCustomToken(e.target.value)}
                  className="font-mono text-xs"
                  data-testid="input-custom-token"
                />
                <p className="text-xs text-muted-foreground">
                  Use the Core API explorer to create a session for a different user if needed
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Request Controls */}
      {resource.list && (
        <Card>
          <CardContent className="pt-6">
            <div className="space-y-3">
              <div className="flex items-center gap-2 text-sm">
                <span className="text-muted-foreground">Request:</span>
                <Badge variant="outline" className="font-mono text-xs">
                  {resource.list.method} {resource.list.path}
                </Badge>
              </div>
              <div className="flex items-center gap-3">
                <Button
                  onClick={handleSendRequest}
                  disabled={isLoading}
                  data-testid="button-send-request"
                >
                  {isLoading ? (
                    <>
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                      Loading...
                    </>
                  ) : (
                    'Send Request'
                  )}
                </Button>
                {data && !error && (
                  <Badge variant="secondary" className="text-xs">
                    Request successful
                  </Badge>
                )}
              </div>
            </div>
            
            {/* Error Display */}
            {error && (
              <Alert className="mt-4 border-destructive">
                <AlertDescription className="text-destructive">
                  <div className="flex items-start gap-2">
                    <span className="font-semibold">Error:</span>
                    <span>{error instanceof Error ? error.message : 'Failed to load data'}</span>
                  </div>
                </AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>
      )}

      <div className="flex items-center justify-between">
        <div className="flex-1">
          <h2 className="text-2xl font-bold" data-testid={`text-${resource.resourceName}-title`}>
            {getResourceDisplayName()}
          </h2>
          {anyOperation?.summary && (
            <p className="text-sm font-medium text-foreground mt-1">
              {anyOperation.summary}
            </p>
          )}
          {anyOperation?.description && (
            <p className="text-sm text-muted-foreground mt-1">
              {anyOperation.description}
            </p>
          )}
          <div className="flex items-center gap-2 mt-2 flex-wrap">
            {resource.list && (
              <>
                <Badge variant="outline" className="text-xs font-mono">
                  {resource.list.method} {resource.list.path}
                </Badge>
                <Badge variant="secondary" className="text-xs">
                  {resource.list ? 'List' : 'Endpoint'}
                </Badge>
              </>
            )}
            {!resource.list && resource.create && resource.create.length > 0 && (
              <Badge variant="outline" className="text-xs font-mono">
                {resource.create[0].method} {resource.create[0].path}
              </Badge>
            )}
            <Badge variant="secondary" className="text-xs">
              {responseContentType}
            </Badge>
            {anyOperation?.operationId && (
              <Badge variant="outline" className="text-xs text-muted-foreground">
                {anyOperation.operationId}
              </Badge>
            )}
          </div>
        </div>
        
        {resource.create && resource.create.length > 0 && onCreateClick && (
          <Button
            onClick={onCreateClick}
            data-testid={`button-create-${resource.resourceName}`}
          >
            <Plus className="w-4 h-4 mr-2" />
            Create {getResourceDisplayName()}
          </Button>
        )}
      </div>

      {/* Available Endpoints Section - show all operations */}
      {((resource.create && resource.create.length > 0) || 
        (resource.update && resource.update.length > 0) || 
        (resource.delete && resource.delete.length > 0) ||
        resource.get) && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Available Endpoints</CardTitle>
            <CardDescription>
              {resource.list 
                ? 'Test these endpoints directly or use the table actions below' 
                : 'Click the buttons below to test each endpoint'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {resource.create?.map((createOp, idx) => onCreateClick && (
                <Button 
                  key={`create-${idx}`}
                  variant="outline"
                  size="sm"
                  onClick={onCreateClick}
                  data-testid={`button-test-create-${resource.resourceName}-${idx}`}
                  className="font-mono text-xs"
                >
                  POST {createOp.path}
                </Button>
              ))}
              {resource.get && (
                <Badge variant="outline" className="text-xs font-mono px-3 py-1.5">
                  GET {resource.get.path}
                </Badge>
              )}
              {resource.update?.map((updateOp, idx) => onEditClick && (
                <Button 
                  key={`update-${idx}`}
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    // Create a mock item with path params for the user to fill
                    const mockItem: any = { _operationIndex: idx };
                    updateOp.pathParams.forEach(param => {
                      mockItem[param] = '';
                    });
                    onEditClick(mockItem);
                  }}
                  data-testid={`button-test-update-${resource.resourceName}-${idx}`}
                  className="font-mono text-xs"
                >
                  PUT {updateOp.path}
                </Button>
              ))}
              {resource.delete?.map((deleteOp, idx) => onDeleteClick && (
                <Button 
                  key={`delete-${idx}`}
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    // Create a mock item with path params for the user to fill
                    const mockItem: any = { _operationIndex: idx };
                    deleteOp.pathParams.forEach(param => {
                      mockItem[param] = '';
                    });
                    onDeleteClick(mockItem);
                  }}
                  data-testid={`button-test-delete-${resource.resourceName}-${idx}`}
                  className="font-mono text-xs"
                >
                  DELETE {deleteOp.path}
                </Button>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {resource.list && (
        <Card>
          <CardContent className="p-0">
            {isTextResponse ? (
              <div className="p-6">
                <div className="bg-muted rounded-lg p-4 font-mono text-sm whitespace-pre-wrap break-words">
                  {data}
                </div>
              </div>
            ) : isBlobResponse ? (
              <div className="p-6">
                <p className="text-muted-foreground">Binary response (download to view)</p>
              </div>
            ) : items.length === 0 ? (
              <div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
                <p className="text-lg font-medium">No {getResourceDisplayName()} Found</p>
                <p className="text-sm mt-2">
                  {resource.create && resource.create.length > 0 ? `Click "Create ${getResourceDisplayName()}" to add one.` : 'No items available.'}
                </p>
              </div>
            ) : (
              <>
                <Table>
                <TableHeader>
                  <TableRow>
                    {columns.map(column => (
                      <TableHead key={column} className="font-semibold">
                        {column.charAt(0).toUpperCase() + column.slice(1).replace(/([A-Z])/g, ' $1')}
                      </TableHead>
                    ))}
                    {((resource.update && resource.update.length > 0) || (resource.delete && resource.delete.length > 0)) && (
                      <TableHead className="text-right">Actions</TableHead>
                    )}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map((item: any, index: number) => (
                    <TableRow key={item.id || index} data-testid={`row-${resource.resourceName}-${index}`}>
                      {columns.map(column => (
                        <TableCell key={column}>
                          {renderCellValue(item[column])}
                        </TableCell>
                      ))}
                      {((resource.update && resource.update.length > 0) || (resource.delete && resource.delete.length > 0)) && (
                        <TableCell className="text-right">
                          <div className="flex items-center justify-end gap-2">
                            {resource.update && resource.update.length > 0 && onEditClick && (
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => onEditClick(item)}
                                data-testid={`button-edit-${resource.resourceName}-${index}`}
                              >
                                <Pencil className="w-3 h-3" />
                              </Button>
                            )}
                            {resource.delete && resource.delete.length > 0 && onDeleteClick && (
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => onDeleteClick(item)}
                                data-testid={`button-delete-${resource.resourceName}-${index}`}
                              >
                                <Trash2 className="w-3 h-3" />
                              </Button>
                            )}
                          </div>
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {/* Pagination Controls - show when paginated OR when there are results */}
              {(resource.list?.isPaginated || items.length > 0) && (
                <div className="flex items-center justify-between px-4 py-3 border-t">
                  <div className="text-sm text-muted-foreground">
                    {total > 0 ? (
                      <>
                        Showing {page * pageSize + 1} to {Math.min((page + 1) * pageSize, total)} of {total} results
                      </>
                    ) : (
                      <>No results</>
                    )}
                  </div>
                  {totalPages > 1 && (
                    <div className="flex items-center gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          setPage(p => Math.max(0, p - 1));
                          setRequestedPath(basePath);
                          refetch();
                        }}
                        disabled={page === 0 || isLoading}
                        data-testid="button-prev-page"
                      >
                        Previous
                      </Button>
                      <div className="text-sm font-medium">
                        Page {page + 1} of {totalPages}
                      </div>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          setPage(p => Math.min(totalPages - 1, p + 1));
                          setRequestedPath(basePath);
                          refetch();
                        }}
                        disabled={page >= totalPages - 1 || isLoading}
                        data-testid="button-next-page"
                      >
                        Next
                      </Button>
                    </div>
                  )}
                </div>
              )}
              </>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
