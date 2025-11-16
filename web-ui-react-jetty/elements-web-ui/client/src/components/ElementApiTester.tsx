import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Loader2, Play, FileCode, CheckCircle, XCircle, Lock } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { fixElementUri } from '@/lib/openapi-utils';
import { apiClient } from '@/lib/api-client';

interface OpenAPIPath {
  [method: string]: {
    tags?: string[];
    summary?: string;
    description?: string;
    operationId?: string;
    security?: Array<{ [key: string]: string[] }>;
    parameters?: Array<{
      name: string;
      in: string;
      required?: boolean;
      schema?: any;
    }>;
    requestBody?: {
      content?: {
        [contentType: string]: {
          schema?: any;
        };
      };
    };
    responses?: {
      [code: string]: {
        description?: string;
        content?: {
          [contentType: string]: {
            schema?: any;
          };
        };
      };
    };
  };
}

interface OpenAPISpec {
  openapi: string;
  paths: {
    [path: string]: OpenAPIPath;
  };
  components?: {
    securitySchemes?: {
      [key: string]: any;
    };
  };
  security?: Array<{ [key: string]: string[] }>;
}

interface ElementApiTesterProps {
  elementName: string;
  elementUri?: string; // Optional: Element URI to use for fetching OpenAPI spec
}

export function ElementApiTester({ elementName, elementUri }: ElementApiTesterProps) {
  console.log('[ElementApiTester] ✓ LOADED - Build 4');
  const { toast } = useToast();
  const [selectedPath, setSelectedPath] = useState<string>('');
  const [selectedMethod, setSelectedMethod] = useState<string>('');
  const [requestBody, setRequestBody] = useState<string>('');
  const [queryParams, setQueryParams] = useState<Record<string, string>>({});
  const [pathParams, setPathParams] = useState<Record<string, string>>({});
  const [response, setResponse] = useState<{ status: number; data: any } | null>(null);
  const [useCustomToken, setUseCustomToken] = useState(false);
  const [customToken, setCustomToken] = useState('');

  // Fetch OpenAPI spec for this element
  const { data: openApiSpec, isLoading: isLoadingSpec } = useQuery<OpenAPISpec>({
    queryKey: ['/api/rest/openapi', elementName, elementUri],
    queryFn: async () => {
      // Determine the OpenAPI spec URL
      // Note: If elementUri is provided, it should point to the Element's base URL
      // (e.g., http://localhost:8080/app/rest/example-element)
      // We need to replace localhost with actual deployment URL and try fetching OpenAPI spec
      
      let specUrl: string | undefined;
      let lastError: Error | null = null;
      
      if (elementUri) {
        // Use provided URI and fix localhost URL
        const fixedUri = fixElementUri(elementUri);
        console.log(`Element URI: ${elementUri} -> Fixed URI: ${fixedUri}`);
        
        // Extract path from the fixed URI for proxying
        // The proxy expects paths like /app/rest/example-element/openapi.json
        try {
          const urlObj = new URL(fixedUri);
          const basePath = urlObj.pathname; // e.g., /app/rest/example-element
          
          // Try both .json and .yaml extensions
          const extensions = ['openapi.json', 'openapi.yaml'];
          for (const ext of extensions) {
            try {
              const proxyPath = `${basePath}/${ext}`;
              const testResponse = await fetch(`/api/proxy${proxyPath}`);
              
              if (testResponse.ok) {
                specUrl = `/api/proxy${proxyPath}`;
                console.log(`Found OpenAPI spec at: ${specUrl}`);
                break;
              }
            } catch (e) {
              lastError = e as Error;
              console.warn(`Failed to fetch ${ext}:`, e);
            }
          }
        } catch (e) {
          lastError = e as Error;
          console.error('Failed to parse element URI:', e);
        }
        
        if (!specUrl) {
          throw new Error(`Could not find OpenAPI spec for this element. ${lastError?.message || ''}`);
        }
      } else {
        // Fallback to default path
        specUrl = `/api/proxy/app/rest/${elementName}/openapi.json`;
      }
      
      // Fetch the OpenAPI spec through the proxy
      // The proxy automatically converts YAML to JSON, so we can always use .json()
      const response = await fetch(specUrl);
      
      if (!response.ok) {
        throw new Error(`Failed to fetch OpenAPI spec: ${response.status} ${response.statusText}`);
      }
      
      // Proxy converts YAML to JSON, so we can always parse as JSON
      return response.json();
    },
  });

  // Execute API request mutation
  const executeRequestMutation = useMutation({
    mutationFn: async () => {
      // Build the URL with path and query parameters
      let url = `/api/proxy/app/rest/${elementName}${selectedPath}`;
      
      // Replace path parameters
      Object.entries(pathParams).forEach(([key, value]) => {
        url = url.replace(`{${key}}`, encodeURIComponent(value));
      });
      
      // Add query parameters
      const queryString = Object.entries(queryParams)
        .filter(([_, value]) => value)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&');
      
      if (queryString) {
        url += `?${queryString}`;
      }

      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
      };

      // Always send auth token if available (backend may require auth even if not in spec)
      const token = useCustomToken ? customToken : apiClient.getSessionToken();
      if (token) {
        headers['Elements-SessionSecret'] = token;
      }

      const options: RequestInit = {
        method: selectedMethod.toUpperCase(),
        headers,
      };

      // Add request body for POST, PUT, PATCH
      if (['POST', 'PUT', 'PATCH'].includes(selectedMethod.toUpperCase()) && requestBody) {
        try {
          JSON.parse(requestBody); // Validate JSON
          options.body = requestBody;
        } catch (e) {
          throw new Error('Invalid JSON in request body');
        }
      }

      const response = await fetch(url, options);
      const contentType = response.headers.get('content-type');
      
      let data;
      if (contentType?.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      return { status: response.status, data };
    },
    onSuccess: (data) => {
      setResponse(data);
      toast({
        title: 'Request Successful',
        description: `Status: ${data.status}`,
      });
    },
    onError: (error: any) => {
      toast({
        title: 'Request Failed',
        description: error.message || 'An error occurred',
        variant: 'destructive',
      });
    },
  });

  // Get available paths and methods
  const paths = openApiSpec ? Object.keys(openApiSpec.paths) : [];
  const methods = selectedPath && openApiSpec 
    ? Object.keys(openApiSpec.paths[selectedPath]).filter(m => ['get', 'post', 'put', 'patch', 'delete'].includes(m.toLowerCase()))
    : [];

  const currentOperation = selectedPath && selectedMethod && openApiSpec
    ? openApiSpec.paths[selectedPath]?.[selectedMethod.toLowerCase()]
    : null;

  // Check if the current operation requires authentication
  // For element endpoints, only check operation-level security (ignore global security)
  // This allows elements to have global security but only enforce it on specific operations
  const requiresAuth = currentOperation?.security?.some((secReq: { [key: string]: string[] }) => 
    Object.keys(secReq).some(schemeName => schemeName === 'session_secret')
  ) ?? false;
  
  // Debug logging
  if (selectedPath && selectedMethod) {
    console.log('[ElementApiTester] Path:', selectedPath, 'Method:', selectedMethod);
    console.log('[ElementApiTester] Operation security:', currentOperation?.security);
    console.log('[ElementApiTester] requiresAuth:', requiresAuth);
  }

  // Extract parameters
  const pathParameters = currentOperation?.parameters?.filter(p => p.in === 'path') || [];
  const queryParameters = currentOperation?.parameters?.filter(p => p.in === 'query') || [];
  
  const hasRequestBody = currentOperation?.requestBody && ['POST', 'PUT', 'PATCH'].includes(selectedMethod.toUpperCase());

  if (isLoadingSpec) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!openApiSpec || paths.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileCode className="w-5 h-5" />
            No API Endpoints
          </CardTitle>
          <CardDescription>
            No OpenAPI specification found for this element.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            API Endpoint Selection
            <Badge variant="destructive" className="text-xs font-normal">Build 4</Badge>
          </CardTitle>
          <CardDescription>Choose an endpoint to test</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label htmlFor="path-select">Endpoint Path</Label>
            <Select
              value={selectedPath}
              onValueChange={(value) => {
                setSelectedPath(value);
                setSelectedMethod('');
                setQueryParams({});
                setPathParams({});
                setRequestBody('');
                setResponse(null);
              }}
            >
              <SelectTrigger id="path-select" data-testid="select-path">
                <SelectValue placeholder="Select an endpoint" />
              </SelectTrigger>
              <SelectContent>
                {paths.map((path) => (
                  <SelectItem key={path} value={path}>
                    {path}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {methods.length > 0 && (
            <div>
              <Label htmlFor="method-select">HTTP Method</Label>
              <Select
                value={selectedMethod}
                onValueChange={(value) => {
                  setSelectedMethod(value);
                  setRequestBody('');
                  setResponse(null);
                }}
              >
                <SelectTrigger id="method-select" data-testid="select-method">
                  <SelectValue placeholder="Select a method" />
                </SelectTrigger>
                <SelectContent>
                  {methods.map((method) => (
                    <SelectItem key={method} value={method.toUpperCase()}>
                      <Badge variant={method.toLowerCase() === 'get' ? 'default' : 'secondary'}>
                        {method.toUpperCase()}
                      </Badge>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}

          {currentOperation && (
            <div className="space-y-2">
              <div>
                <p className="text-sm font-medium">Summary</p>
                <p className="text-sm text-muted-foreground">{currentOperation.summary || 'No summary available'}</p>
              </div>
              {currentOperation.description && (
                <div>
                  <p className="text-sm font-medium">Description</p>
                  <p className="text-sm text-muted-foreground">{currentOperation.description}</p>
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {currentOperation && (
        <>
          {pathParameters.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Path Parameters</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {pathParameters.map((param) => (
                  <div key={param.name}>
                    <Label htmlFor={`path-${param.name}`}>
                      {param.name} {param.required && <span className="text-destructive">*</span>}
                    </Label>
                    <Input
                      id={`path-${param.name}`}
                      value={pathParams[param.name] || ''}
                      onChange={(e) => setPathParams({ ...pathParams, [param.name]: e.target.value })}
                      placeholder={`Enter ${param.name}`}
                      data-testid={`input-path-${param.name}`}
                    />
                  </div>
                ))}
              </CardContent>
            </Card>
          )}

          {queryParameters.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Query Parameters</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {queryParameters.map((param) => (
                  <div key={param.name}>
                    <Label htmlFor={`query-${param.name}`}>
                      {param.name} {param.required && <span className="text-destructive">*</span>}
                    </Label>
                    <Input
                      id={`query-${param.name}`}
                      value={queryParams[param.name] || ''}
                      onChange={(e) => setQueryParams({ ...queryParams, [param.name]: e.target.value })}
                      placeholder={`Enter ${param.name}`}
                      data-testid={`input-query-${param.name}`}
                    />
                  </div>
                ))}
              </CardContent>
            </Card>
          )}

          {hasRequestBody && (
            <Card>
              <CardHeader>
                <CardTitle>Request Body (JSON)</CardTitle>
              </CardHeader>
              <CardContent>
                <Textarea
                  value={requestBody}
                  onChange={(e) => setRequestBody(e.target.value)}
                  placeholder='{"key": "value"}'
                  className="font-mono text-sm min-h-[120px]"
                  data-testid="textarea-request-body"
                />
              </CardContent>
            </Card>
          )}

          {requiresAuth && (
            <Card>
              <CardHeader>
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
                      id="use-current-session-tester"
                      checked={!useCustomToken}
                      onChange={() => setUseCustomToken(false)}
                      className="cursor-pointer"
                      data-testid="radio-use-current-session"
                    />
                    <Label htmlFor="use-current-session-tester" className="cursor-pointer flex items-center gap-1 text-sm">
                      Use Current Session
                    </Label>
                  </div>
                  <div className="flex items-center gap-2">
                    <input
                      type="radio"
                      id="use-custom-token-tester"
                      checked={useCustomToken}
                      onChange={() => setUseCustomToken(true)}
                      className="cursor-pointer"
                      data-testid="radio-use-custom-token"
                    />
                    <Label htmlFor="use-custom-token-tester" className="cursor-pointer text-sm">
                      Override with Custom Token
                    </Label>
                  </div>
                </div>
                
                {useCustomToken && (
                  <div className="space-y-2">
                    <Label htmlFor="custom-token-tester" className="text-xs">Session Token</Label>
                    <Input
                      id="custom-token-tester"
                      type="text"
                      placeholder="Enter session token..."
                      value={customToken}
                      onChange={(e) => setCustomToken(e.target.value)}
                      className="font-mono text-xs"
                      data-testid="input-custom-token"
                    />
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          <Button
            onClick={() => executeRequestMutation.mutate()}
            disabled={executeRequestMutation.isPending}
            className="w-full"
            data-testid="button-execute"
          >
            {executeRequestMutation.isPending ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Executing...
              </>
            ) : (
              <>
                <Play className="w-4 h-4 mr-2" />
                Execute Request
              </>
            )}
          </Button>

          {response && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  {response.status >= 200 && response.status < 300 ? (
                    <CheckCircle className="w-5 h-5 text-green-500" />
                  ) : (
                    <XCircle className="w-5 h-5 text-red-500" />
                  )}
                  Response
                  <Badge variant={response.status >= 200 && response.status < 300 ? 'default' : 'destructive'}>
                    {response.status}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-[300px] w-full">
                  <pre className="text-sm bg-muted p-4 rounded-md overflow-auto">
                    {typeof response.data === 'string' 
                      ? response.data 
                      : JSON.stringify(response.data, null, 2)}
                  </pre>
                </ScrollArea>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </div>
  );
}
