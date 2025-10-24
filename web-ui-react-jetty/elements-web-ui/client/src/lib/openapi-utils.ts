/**
 * Utilities for working with OpenAPI specifications from Elements
 */

// Get the actual public-facing URL for the Elements backend
export function getActualElementsUrl(): string {
  // In production, this would be the actual deployment URL
  // For now, we detect it from window.location
  if (typeof window !== 'undefined') {
    const protocol = window.location.protocol;
    const hostname = window.location.hostname;
    const port = window.location.port;
    
    // If we're on localhost with a different port, assume Elements backend is on 8080
    if (hostname === 'localhost' && port !== '8080') {
      return 'http://localhost:8080';
    }
    
    // Otherwise, use the same protocol and hostname (Elements backend is at root)
    return `${protocol}//${hostname}`;
  }
  
  return 'http://localhost:8080'; // Fallback
}

/**
 * Replace the backend root URL in element URIs
 * Converts http://localhost:8080/app/rest/... to https://actual-deployment.com/app/rest/...
 */
export function fixElementUri(uri: string): string {
  if (!uri) return uri;
  
  // Common localhost patterns to replace
  const localhostPatterns = [
    'http://localhost:8080',
    'http://localhost',
    'https://localhost:8080',
    'https://localhost',
  ];
  
  const actualUrl = getActualElementsUrl();
  
  for (const pattern of localhostPatterns) {
    if (uri.startsWith(pattern)) {
      return uri.replace(pattern, actualUrl);
    }
  }
  
  return uri;
}

/**
 * Fetch OpenAPI spec for an element
 * Tries both .yaml and .json extensions
 */
export async function fetchOpenApiSpec(elementBaseUrl: string): Promise<any | null> {
  const fixedUrl = fixElementUri(elementBaseUrl);
  
  // Try .yaml first, then .json
  const extensions = ['openapi.yaml', 'openapi.json'];
  
  for (const ext of extensions) {
    try {
      const specUrl = `${fixedUrl}/${ext}`;
      console.log(`Attempting to fetch OpenAPI spec from: ${specUrl}`);
      
      // Fetch through our proxy to handle authentication
      const proxyUrl = `/api/proxy${new URL(specUrl).pathname}`;
      const response = await fetch(proxyUrl, {
        credentials: 'include',
      });
      
      if (response.ok) {
        const spec = await response.json();
        console.log(`Successfully fetched OpenAPI spec from ${ext}`);
        return spec;
      }
    } catch (error) {
      console.warn(`Failed to fetch OpenAPI spec with ${ext}:`, error);
    }
  }
  
  console.error(`Could not fetch OpenAPI spec from ${fixedUrl}`);
  return null;
}

/**
 * Parse OpenAPI spec to extract endpoint information
 */
export interface OpenApiEndpoint {
  path: string;
  method: string;
  summary?: string;
  description?: string;
  operationId?: string;
  parameters?: any[];
  requestBody?: any;
  responses?: any;
}

export function parseOpenApiSpec(spec: any): OpenApiEndpoint[] {
  if (!spec || !spec.paths) {
    return [];
  }
  
  const endpoints: OpenApiEndpoint[] = [];
  
  Object.entries(spec.paths).forEach(([path, pathItem]: [string, any]) => {
    const methods = ['get', 'post', 'put', 'patch', 'delete', 'options', 'head'];
    
    methods.forEach(method => {
      if (pathItem[method]) {
        const operation = pathItem[method];
        endpoints.push({
          path,
          method: method.toUpperCase(),
          summary: operation.summary,
          description: operation.description,
          operationId: operation.operationId,
          parameters: operation.parameters,
          requestBody: operation.requestBody,
          responses: operation.responses,
        });
      }
    });
  });
  
  return endpoints;
}
