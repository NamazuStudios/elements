// OpenAPI spec analyzer for dynamic UI generation

export interface OpenAPIParameter {
  name: string;
  in: 'path' | 'query' | 'header';
  required?: boolean;
  schema?: {
    type: string;
    format?: string;
    enum?: string[];
    default?: any;
  };
  description?: string;
}

export interface OpenAPIOperation {
  operationId: string;
  summary?: string;
  description?: string;
  parameters?: OpenAPIParameter[];
  requestBody?: {
    required?: boolean;
    content: {
      [contentType: string]: {
        schema: any;
      };
    };
  };
  responses?: {
    [statusCode: string]: {
      description?: string;
      content?: {
        [contentType: string]: {
          schema: any;
        };
      };
    };
  };
  security?: Array<{
    [schemeName: string]: string[];
  }>;
}

export interface ResourceOperations {
  resourceName: string;
  basePath: string;
  list?: {
    method: 'GET';
    path: string;
    operation: OpenAPIOperation;
    isPaginated: boolean;
    queryParams: OpenAPIParameter[];
  };
  get?: {
    method: 'GET';
    path: string;
    operation: OpenAPIOperation;
    pathParams: string[];
    queryParams: OpenAPIParameter[];
  };
  create?: Array<{
    method: 'POST';
    path: string;
    operation: OpenAPIOperation;
    pathParams: string[];
    queryParams: OpenAPIParameter[];
    requestSchema?: any;
  }>;
  update?: Array<{
    method: 'PUT';
    path: string;
    operation: OpenAPIOperation;
    pathParams: string[];
    queryParams: OpenAPIParameter[];
    requestSchema?: any;
  }>;
  delete?: Array<{
    method: 'DELETE';
    path: string;
    operation: OpenAPIOperation;
    pathParams: string[];
    queryParams: OpenAPIParameter[];
  }>;
}

interface OpenAPISpec {
  paths: {
    [path: string]: {
      get?: OpenAPIOperation;
      post?: OpenAPIOperation;
      put?: OpenAPIOperation;
      delete?: OpenAPIOperation;
      patch?: OpenAPIOperation;
    };
  };
  components?: {
    schemas?: {
      [schemaName: string]: any;
    };
  };
}

/**
 * Extract path parameters from a path string
 * e.g., "/user/{id}" => ["id"]
 */
function extractPathParams(path: string): string[] {
  const matches = path.match(/\{([^}]+)\}/g);
  if (!matches) return [];
  return matches.map(m => m.slice(1, -1));
}

/**
 * Extract query parameters from operation parameters
 */
function extractQueryParams(operation: OpenAPIOperation): OpenAPIParameter[] {
  if (!operation.parameters) return [];
  return operation.parameters.filter(p => p.in === 'query');
}

/**
 * Get the base resource name from a path
 * For CRUD patterns: "/application/{id}" => "application"
 * For standalone endpoints: "/helloworld" or "/hello/world" => use full path
 */
function getResourceName(path: string): string {
  // Remove leading slash and split by /
  const parts = path.slice(1).split('/');
  
  // Filter out path parameters
  const nonParamParts = parts.filter(part => !part.startsWith('{'));
  
  // If it's a single segment path without parameters, use it as-is
  // e.g., "/helloworld" => "helloworld"
  if (nonParamParts.length === 1) {
    return nonParamParts[0] || '';
  }
  
  // If it has multiple segments and no parameters, it's likely a standalone endpoint
  // Use the full path as the resource name to avoid collisions
  // e.g., "/hello/world" => "hello/world"
  if (parts.length === nonParamParts.length) {
    return nonParamParts.join('/');
  }
  
  // For CRUD patterns with parameters, use the first segment
  // e.g., "/application/{id}" => "application"
  return nonParamParts[0] || '';
}

/**
 * Determine if a path is a "list" endpoint (no path params at all)
 * This ensures we only show top-level collections that can be loaded without parent IDs
 */
function isListPath(path: string): boolean {
  return !path.includes('{');
}

/**
 * Check if an operation returns paginated results
 */
function isPaginatedOperation(operation: OpenAPIOperation): boolean {
  const params = operation.parameters || [];
  const hasOffsetOrPage = params.some(p => 
    p.name === 'offset' || p.name === 'page' || p.name === 'skip'
  );
  const hasCountOrLimit = params.some(p => 
    p.name === 'count' || p.name === 'limit' || p.name === 'size'
  );
  return hasOffsetOrPage && hasCountOrLimit;
}

/**
 * Get request schema from operation
 */
function getRequestSchema(operation: OpenAPIOperation): any | undefined {
  if (!operation.requestBody) return undefined;
  
  const content = operation.requestBody.content;
  const jsonContent = content['application/json'];
  
  return jsonContent?.schema;
}

/**
 * Analyze OpenAPI spec and group operations by resource path
 */
export function analyzeOpenAPISpec(spec: OpenAPISpec): ResourceOperations[] {
  const resourceMap = new Map<string, ResourceOperations>();
  
  // Group operations by resource
  for (const [path, pathItem] of Object.entries(spec.paths)) {
    const resourceName = getResourceName(path);
    if (!resourceName) continue;
    
    // Get or create resource operations entry
    let resource = resourceMap.get(resourceName);
    if (!resource) {
      resource = {
        resourceName,
        basePath: `/${resourceName}`,
      };
      resourceMap.set(resourceName, resource);
    }
    
    const pathParams = extractPathParams(path);
    const hasPathParams = pathParams.length > 0;
    
    // Analyze GET operation
    if (pathItem.get) {
      if (isListPath(path)) {
        // List operation (returns array or pagination)
        resource.list = {
          method: 'GET',
          path,
          operation: pathItem.get,
          isPaginated: isPaginatedOperation(pathItem.get),
          queryParams: extractQueryParams(pathItem.get),
        };
      } else if (hasPathParams) {
        // Detail/Get operation (single item by ID)
        resource.get = {
          method: 'GET',
          path,
          operation: pathItem.get,
          pathParams,
          queryParams: extractQueryParams(pathItem.get),
        };
      }
    }
    
    // Analyze POST operation (create) - can be on any path
    if (pathItem.post) {
      if (!resource.create) resource.create = [];
      resource.create.push({
        method: 'POST',
        path,
        operation: pathItem.post,
        pathParams,
        queryParams: extractQueryParams(pathItem.post),
        requestSchema: getRequestSchema(pathItem.post),
      });
    }
    
    // Analyze PUT operation (update) - can be on any path
    if (pathItem.put) {
      if (!resource.update) resource.update = [];
      resource.update.push({
        method: 'PUT',
        path,
        operation: pathItem.put,
        pathParams,
        queryParams: extractQueryParams(pathItem.put),
        requestSchema: getRequestSchema(pathItem.put),
      });
    }
    
    // Analyze DELETE operation - can be on any path
    if (pathItem.delete) {
      if (!resource.delete) resource.delete = [];
      resource.delete.push({
        method: 'DELETE',
        path,
        operation: pathItem.delete,
        pathParams,
        queryParams: extractQueryParams(pathItem.delete),
      });
    }
  }
  
  // Always return grouped resources (even if no list operations exist)
  return Array.from(resourceMap.values());
}

/**
 * Resolve a $ref reference in the OpenAPI spec
 */
export function resolveRef(spec: OpenAPISpec, ref: string): any {
  if (!ref.startsWith('#/')) return null;
  
  const parts = ref.slice(2).split('/');
  let current: any = spec;
  
  for (const part of parts) {
    current = current?.[part];
    if (!current) return null;
  }
  
  return current;
}

/**
 * Get properties from a schema, resolving $ref if needed
 */
export function getSchemaProperties(spec: OpenAPISpec, schema: any): Record<string, any> {
  if (!schema) return {};
  
  // Handle $ref
  if (schema.$ref) {
    const resolved = resolveRef(spec, schema.$ref);
    return getSchemaProperties(spec, resolved);
  }
  
  // Handle allOf (merge all properties)
  if (schema.allOf) {
    const merged: Record<string, any> = {};
    for (const subSchema of schema.allOf) {
      const props = getSchemaProperties(spec, subSchema);
      Object.assign(merged, props);
    }
    return merged;
  }
  
  return schema.properties || {};
}

/**
 * Get required fields from a schema
 */
export function getRequiredFields(spec: OpenAPISpec, schema: any): string[] {
  if (!schema) return [];
  
  // Handle $ref
  if (schema.$ref) {
    const resolved = resolveRef(spec, schema.$ref);
    return getRequiredFields(spec, resolved);
  }
  
  // Handle allOf (merge all required arrays)
  if (schema.allOf) {
    const allRequired: string[] = [];
    for (const subSchema of schema.allOf) {
      allRequired.push(...getRequiredFields(spec, subSchema));
    }
    return Array.from(new Set(allRequired));
  }
  
  return schema.required || [];
}
