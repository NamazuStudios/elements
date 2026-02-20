import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Loader2, Pencil, Trash2, ExternalLink, Info, Filter, ChevronDown, ChevronUp } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { ResourceOperations, OpenAPIOperation, OpenAPIParameter } from '@/lib/openapi-analyzer';
import { getApiPath, apiClient } from '@/lib/api-client';

class HttpError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

interface DynamicResourceViewProps {
  resource: ResourceOperations;
  spec: any;
  baseUrl?: string;
  customHeaders?: Record<string, string>;
  onCreateClick?: () => void;
  onEditClick?: (item: any) => void;
  onDeleteClick?: (item: any) => void;
  onGetClick?: () => void;
}

export function DynamicResourceView({
  resource,
  spec,
  baseUrl = '/api/proxy/api/rest',
  customHeaders,
  onCreateClick,
  onEditClick,
  onDeleteClick,
  onGetClick,
}: DynamicResourceViewProps) {
  const [page, setPage] = useState(0);
  const [requestedPath, setRequestedPath] = useState<string | null>(null);
  const [filtersExpanded, setFiltersExpanded] = useState(false);
  const [filterValues, setFilterValues] = useState<Record<string, string>>({});
  
  const paginationParamNames = ['offset', 'count', 'limit', 'page', 'size', 'pagesize'];
  const listQueryParams: OpenAPIParameter[] = (resource.list?.queryParams || [])
    .filter(p => !paginationParamNames.includes(p.name.toLowerCase()));

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
        setPage(0);
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [pageSize]);

  // Reset filters when switching resources
  useEffect(() => {
    setFilterValues({});
    setFiltersExpanded(false);
    setPage(0);
  }, [resource.resourceName]);

  // Build query parameters for list endpoint
  const queryParams = new URLSearchParams();
  if (resource.list?.isPaginated) {
    queryParams.set('offset', (page * pageSize).toString());
    queryParams.set('count', pageSize.toString());
  }
  for (const [key, value] of Object.entries(filterValues)) {
    if (value !== '' && value !== undefined) {
      queryParams.set(key, value);
    }
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

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: [basePath, page, pageSize, filterValues],
    queryFn: async () => {
      const fullUrl = await getApiPath(pathWithQuery);
      
      const headers: Record<string, string> = {};
      const token = customHeaders?.['Elements-SessionSecret'] || apiClient.getSessionToken() || '';
      if (token) {
        headers['Elements-SessionSecret'] = token;
      }
      if (customHeaders?.['Elements-ProfileId']) {
        headers['Elements-ProfileId'] = customHeaders['Elements-ProfileId'];
      }
      
      headers['Accept'] = responseContentType;
      
      const response = await fetch(fullUrl, { headers });
      if (!response.ok) {
        let errorDetail = '';
        try {
          const errorBody = await response.text();
          try {
            const parsed = JSON.parse(errorBody);
            errorDetail = parsed.message || parsed.error || parsed.detail || errorBody;
          } catch {
            errorDetail = errorBody;
          }
        } catch {
          errorDetail = response.statusText;
        }
        throw new HttpError(response.status, errorDetail || `${response.status} ${response.statusText}`);
      }
      
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

  const getOperationResponseType = (operation?: OpenAPIOperation) => {
    if (!operation?.responses) return null;
    const successResponse = operation.responses['200'] || operation.responses['201'] || operation.responses['default'];
    if (!successResponse?.content) return null;
    return Object.keys(successResponse.content)[0] || null;
  };

  const getOperationRequestType = (operation?: OpenAPIOperation) => {
    if (!operation?.requestBody?.content) return null;
    return Object.keys(operation.requestBody.content)[0] || null;
  };
  
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

      <div>
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
      </div>

      {/* Available Endpoints Section - show all operations */}
      {((resource.create && resource.create.length > 0) || 
        (resource.update && resource.update.length > 0) || 
        (resource.delete && resource.delete.length > 0) ||
        resource.get ||
        resource.list) && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Available Endpoints</CardTitle>
            <CardDescription>
              Click an endpoint to test it
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {resource.list && (
                <div className="space-y-2">
                  <div className="flex items-center gap-3 p-2 rounded-md border border-border/50 flex-wrap">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleSendRequest}
                      disabled={isLoading}
                      data-testid={`button-test-list-${resource.resourceName}`}
                      className="font-mono text-xs shrink-0"
                    >
                      {isLoading ? <Loader2 className="w-3 h-3 mr-1 animate-spin" /> : null}
                      GET {resource.list.path}
                    </Button>
                    <div className="flex items-center gap-2 flex-wrap">
                      {resource.list.operation.operationId && (
                        <span className="text-xs text-muted-foreground">{resource.list.operation.operationId}</span>
                      )}
                      {getOperationResponseType(resource.list.operation) && (
                        <Badge variant="secondary" className="text-[10px]">{getOperationResponseType(resource.list.operation)}</Badge>
                      )}
                      {resource.list.isPaginated && (
                        <Badge variant="secondary" className="text-[10px]">paginated</Badge>
                      )}
                      {data && !error && requestedPath && (
                        <Badge
                          variant="outline"
                          className="text-[10px] text-green-600 dark:text-green-400 cursor-pointer"
                          onClick={() => document.getElementById('list-results-section')?.scrollIntoView({ behavior: 'smooth' })}
                          data-testid="badge-success-status"
                        >
                          200
                        </Badge>
                      )}
                      {error && (
                        <Badge
                          variant="outline"
                          className="text-[10px] text-destructive cursor-pointer"
                          onClick={() => document.getElementById('list-error-section')?.scrollIntoView({ behavior: 'smooth' })}
                          data-testid="badge-error-status"
                        >
                          {error instanceof HttpError ? error.status : 'error'}
                        </Badge>
                      )}
                      {listQueryParams.length > 0 && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setFiltersExpanded(!filtersExpanded)}
                          className="text-xs gap-1"
                          data-testid="button-toggle-filters"
                        >
                          <Filter className="w-3 h-3" />
                          Filters ({listQueryParams.length})
                          {filtersExpanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
                        </Button>
                      )}
                    </div>
                  </div>
                  {filtersExpanded && listQueryParams.length > 0 && (
                    <div className="ml-4 p-3 rounded-md border border-border/50 space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-muted-foreground">Query Parameters</span>
                        {Object.values(filterValues).some(v => v !== '') && (
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => { setFilterValues({}); setPage(0); }}
                            className="text-xs"
                            data-testid="button-clear-filters"
                          >
                            Clear all
                          </Button>
                        )}
                      </div>
                      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                        {listQueryParams.map(param => {
                          const paramSchema = param.schema;
                          return (
                            <div key={param.name} className="space-y-1">
                              <Label htmlFor={`filter-${param.name}`} className="text-xs flex items-center gap-1">
                                {param.name}
                                {param.required && <span className="text-destructive">*</span>}
                                {paramSchema?.type && (
                                  <span className="text-muted-foreground font-normal">({paramSchema.type})</span>
                                )}
                              </Label>
                              {paramSchema?.enum ? (
                                <Select
                                  value={filterValues[param.name] || ''}
                                  onValueChange={(val) => {
                                    setFilterValues(prev => ({ ...prev, [param.name]: val === '__clear__' ? '' : val }));
                                    setPage(0);
                                  }}
                                >
                                  <SelectTrigger className="text-xs" data-testid={`select-filter-${param.name}`}>
                                    <SelectValue placeholder={param.description || `Select ${param.name}`} />
                                  </SelectTrigger>
                                  <SelectContent>
                                    <SelectItem value="__clear__">-- None --</SelectItem>
                                    {paramSchema.enum.map((option: string) => (
                                      <SelectItem key={option} value={option}>{option}</SelectItem>
                                    ))}
                                  </SelectContent>
                                </Select>
                              ) : paramSchema?.type === 'boolean' ? (
                                <div className="flex items-center gap-2">
                                  <Checkbox
                                    id={`filter-${param.name}`}
                                    checked={filterValues[param.name] === 'true'}
                                    onCheckedChange={(checked) => {
                                      setFilterValues(prev => ({ ...prev, [param.name]: checked ? 'true' : '' }));
                                      setPage(0);
                                    }}
                                    data-testid={`checkbox-filter-${param.name}`}
                                  />
                                </div>
                              ) : (
                                <Input
                                  id={`filter-${param.name}`}
                                  type={paramSchema?.type === 'integer' || paramSchema?.type === 'number' ? 'number' : 'text'}
                                  placeholder={param.description || (paramSchema?.default !== undefined ? `Default: ${paramSchema.default}` : `Enter ${param.name}`)}
                                  value={filterValues[param.name] || ''}
                                  onChange={(e) => {
                                    setFilterValues(prev => ({ ...prev, [param.name]: e.target.value }));
                                    setPage(0);
                                  }}
                                  className="text-xs"
                                  data-testid={`input-filter-${param.name}`}
                                />
                              )}
                              {param.description && (
                                <p className="text-[10px] text-muted-foreground">{param.description}</p>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}
                </div>
              )}
              {resource.create?.map((createOp, idx) => (
                <div key={`create-${idx}`} className="flex items-center gap-3 p-2 rounded-md border border-border/50 flex-wrap">
                  {onCreateClick ? (
                    <Button 
                      variant="outline"
                      size="sm"
                      onClick={onCreateClick}
                      data-testid={`button-test-create-${resource.resourceName}-${idx}`}
                      className="font-mono text-xs shrink-0"
                    >
                      POST {createOp.path}
                    </Button>
                  ) : (
                    <Badge variant="outline" className="text-xs font-mono px-3 py-1.5 shrink-0">
                      POST {createOp.path}
                    </Badge>
                  )}
                  <div className="flex items-center gap-2 flex-wrap">
                    {createOp.operation.operationId && (
                      <span className="text-xs text-muted-foreground">{createOp.operation.operationId}</span>
                    )}
                    {getOperationRequestType(createOp.operation) && (
                      <Badge variant="secondary" className="text-[10px]">{getOperationRequestType(createOp.operation)}</Badge>
                    )}
                    {getOperationResponseType(createOp.operation) && getOperationResponseType(createOp.operation) !== getOperationRequestType(createOp.operation) && (
                      <Badge variant="secondary" className="text-[10px]">{getOperationResponseType(createOp.operation)}</Badge>
                    )}
                  </div>
                </div>
              ))}
              {resource.get && (
                <div className="flex items-center gap-3 p-2 rounded-md border border-border/50 flex-wrap">
                  {onGetClick ? (
                    <Button 
                      variant="outline"
                      size="sm"
                      onClick={onGetClick}
                      data-testid={`button-test-get-${resource.resourceName}`}
                      className="font-mono text-xs shrink-0"
                    >
                      GET {resource.get.path}
                    </Button>
                  ) : (
                    <Badge variant="outline" className="text-xs font-mono px-3 py-1.5 shrink-0">
                      GET {resource.get.path}
                    </Badge>
                  )}
                  <div className="flex items-center gap-2 flex-wrap">
                    {resource.get.operation.operationId && (
                      <span className="text-xs text-muted-foreground">{resource.get.operation.operationId}</span>
                    )}
                    {getOperationResponseType(resource.get.operation) && (
                      <Badge variant="secondary" className="text-[10px]">{getOperationResponseType(resource.get.operation)}</Badge>
                    )}
                  </div>
                </div>
              )}
              {resource.update?.map((updateOp, idx) => (
                <div key={`update-${idx}`} className="flex items-center gap-3 p-2 rounded-md border border-border/50 flex-wrap">
                  {onEditClick ? (
                    <Button 
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        const mockItem: any = { _operationIndex: idx };
                        updateOp.pathParams.forEach(param => {
                          mockItem[param] = '';
                        });
                        onEditClick(mockItem);
                      }}
                      data-testid={`button-test-update-${resource.resourceName}-${idx}`}
                      className="font-mono text-xs shrink-0"
                    >
                      PUT {updateOp.path}
                    </Button>
                  ) : (
                    <Badge variant="outline" className="text-xs font-mono px-3 py-1.5 shrink-0">
                      PUT {updateOp.path}
                    </Badge>
                  )}
                  <div className="flex items-center gap-2 flex-wrap">
                    {updateOp.operation.operationId && (
                      <span className="text-xs text-muted-foreground">{updateOp.operation.operationId}</span>
                    )}
                    {getOperationRequestType(updateOp.operation) && (
                      <Badge variant="secondary" className="text-[10px]">{getOperationRequestType(updateOp.operation)}</Badge>
                    )}
                    {getOperationResponseType(updateOp.operation) && getOperationResponseType(updateOp.operation) !== getOperationRequestType(updateOp.operation) && (
                      <Badge variant="secondary" className="text-[10px]">{getOperationResponseType(updateOp.operation)}</Badge>
                    )}
                  </div>
                </div>
              ))}
              {resource.delete?.map((deleteOp, idx) => (
                <div key={`delete-${idx}`} className="flex items-center gap-3 p-2 rounded-md border border-border/50 flex-wrap">
                  {onDeleteClick ? (
                    <Button 
                      variant="outline"
                      size="sm"
                      onClick={() => {
                        const mockItem: any = { _operationIndex: idx };
                        deleteOp.pathParams.forEach(param => {
                          mockItem[param] = '';
                        });
                        onDeleteClick(mockItem);
                      }}
                      data-testid={`button-test-delete-${resource.resourceName}-${idx}`}
                      className="font-mono text-xs shrink-0"
                    >
                      DELETE {deleteOp.path}
                    </Button>
                  ) : (
                    <Badge variant="outline" className="text-xs font-mono px-3 py-1.5 shrink-0">
                      DELETE {deleteOp.path}
                    </Badge>
                  )}
                  <div className="flex items-center gap-2 flex-wrap">
                    {deleteOp.operation.operationId && (
                      <span className="text-xs text-muted-foreground">{deleteOp.operation.operationId}</span>
                    )}
                    {getOperationResponseType(deleteOp.operation) && (
                      <Badge variant="secondary" className="text-[10px]">{getOperationResponseType(deleteOp.operation)}</Badge>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {resource.list && error && (
        <Alert id="list-error-section" className="border-destructive">
          <AlertDescription className="text-destructive">
            <div className="flex items-start gap-2">
              <span className="font-semibold">Error:</span>
              <span>{error instanceof Error ? error.message : 'Failed to load data'}</span>
            </div>
          </AlertDescription>
        </Alert>
      )}

      {resource.list && (
        <Card id="list-results-section">
          <Tabs defaultValue="table">
          <CardHeader className="pb-0">
            <TabsList>
              <TabsTrigger value="table" data-testid="tab-results-table">Table</TabsTrigger>
              <TabsTrigger value="raw-json" data-testid="tab-results-raw-json">Raw JSON</TabsTrigger>
            </TabsList>
          </CardHeader>
          <TabsContent value="raw-json" className="m-0">
            <CardContent>
              <pre className="text-xs font-mono bg-muted p-4 rounded-md overflow-auto max-h-[600px]">
                <code>{data !== undefined ? JSON.stringify(data, null, 2) : 'No data — send a request first'}</code>
              </pre>
            </CardContent>
          </TabsContent>
          <TabsContent value="table" className="m-0">
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
          </TabsContent>
          </Tabs>
        </Card>
      )}
    </div>
  );
}
