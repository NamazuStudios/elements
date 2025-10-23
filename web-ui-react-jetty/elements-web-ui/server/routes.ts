import type { Express, Request, Response } from "express";
import { createServer, type Server } from "http";
import { storage } from "./storage";
import * as yaml from 'js-yaml';
import { requireAuth, optionalAuth } from "./middleware/auth";
import { loginLimiter, proxyLimiter } from "./middleware/rate-limit";
import { getBackendUrl } from "./config";

// Helper to sanitize error messages
function sanitizeError(error: any): string {
  if (error?.message) {
    // Remove stack traces and internal details
    return error.message.split('\n')[0];
  }
  return 'An error occurred';
}

export async function registerRoutes(app: Express): Promise<Server> {
  // Login endpoint with rate limiting
  app.post('/api/auth/login', loginLimiter, async (req: Request, res: Response) => {
    try {
      const { username, password, rememberMe } = req.body;
      
      const isDev = process.env.NODE_ENV === 'development' && !process.env.REPLIT_DEPLOYMENT;
      
      if (isDev) {
        console.log('[LOGIN] Client request - username:', username, 'rememberMe:', rememberMe);
      }

      if (!username || !password) {
        return res.status(400).json({ error: 'Username and password required' });
      }

      const requestBody = {
        userId: username,
        password: password,
      };

      const ELEMENTS_BACKEND_URL = getBackendUrl();
      
      if (isDev) {
        console.log('[LOGIN] Sending to:', `${ELEMENTS_BACKEND_URL}/api/rest/session`);
      }

      // Call Elements backend to authenticate
      const response = await fetch(`${ELEMENTS_BACKEND_URL}/api/rest/session`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      const responseText = await response.text();
      if (isDev) {
        console.log('[LOGIN] Response status:', response.status);
      }

      if (!response.ok) {
        return res.status(401).json({ error: 'Invalid credentials' });
      }

      const sessionData = JSON.parse(responseText);
      const sessionSecret = sessionData.sessionSecret;
      const sessionExpiry = sessionData.session?.expiry;

      if (!sessionSecret) {
        if (isDev) console.error('[LOGIN] No sessionSecret in response');
        return res.status(500).json({ error: 'Login failed' });
      }

      // Calculate maxAge from backend's session expiry timestamp
      // If rememberMe is true, use the backend's expiry
      // If rememberMe is false, use a shorter duration (1 day) or backend expiry, whichever is shorter
      let maxAge: number;
      if (sessionExpiry) {
        const backendMaxAge = sessionExpiry - Date.now();
        if (rememberMe) {
          // Use full backend expiry
          maxAge = backendMaxAge;
        } else {
          // Use shorter of 1 day or backend expiry
          maxAge = Math.min(backendMaxAge, 24 * 60 * 60 * 1000);
        }
      } else {
        // Fallback if backend doesn't provide expiry
        maxAge = rememberMe ? 30 * 24 * 60 * 60 * 1000 : 24 * 60 * 60 * 1000;
      }

      const cookieOptions = {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'strict' as const,
        maxAge,
      };
      
      if (isDev) {
        console.log('[LOGIN] Backend expiry:', sessionExpiry ? new Date(sessionExpiry).toISOString() : 'none');
        console.log('[LOGIN] Setting cookie with maxAge:', maxAge, 'ms (', (maxAge / (24 * 60 * 60 * 1000)).toFixed(2), 'days)');
      }

      res.cookie('elements-session', sessionSecret, cookieOptions);

      // Don't return sessionSecret in response - it's in the HTTP-only cookie
      // This prevents it from being logged or accessible to JavaScript
      res.json({
        success: true,
        session: {
          userId: sessionData.session?.user?.name || username,
          level: sessionData.session?.user?.level || 'SUPERUSER',
        },
      });
    } catch (error) {
      console.error('Login error:', error);
      res.status(500).json({ error: 'Login failed' });
    }
  });

  // Logout endpoint
  app.post('/api/auth/logout', (req: Request, res: Response) => {
    // Clear cookie with same options used when setting it
    res.clearCookie('elements-session', {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'strict',
    });
    res.json({ success: true });
  });

  // Session verification endpoint
  app.get('/api/auth/verify', async (req: Request, res: Response) => {
    try {
      const sessionToken = req.cookies?.['elements-session'] || req.headers['elements-sessionsecret'] as string;
      const isDev = process.env.NODE_ENV === 'development' && !process.env.REPLIT_DEPLOYMENT;
      
      if (isDev) {
        console.log('[VERIFY] Session token from cookie:', sessionToken ? 'present' : 'missing');
      }
      
      if (!sessionToken) {
        if (isDev) console.log('[VERIFY] No session token found');
        return res.status(401).json({ error: 'No session token provided' });
      }

      if (isDev) {
        console.log('[VERIFY] Calling backend to verify session');
      }
      
      const ELEMENTS_BACKEND_URL = getBackendUrl();
      
      // Verify session by calling the Elements backend session endpoint
      const response = await fetch(`${ELEMENTS_BACKEND_URL}/api/rest/session`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Elements-SessionSecret': sessionToken,
        },
        body: JSON.stringify({}), // Send empty object, not null
      });

      const responseText = await response.text();
      
      if (isDev) {
        console.log('[VERIFY] Backend response status:', response.status);
      }

      if (!response.ok) {
        if (isDev) console.log('[VERIFY] Session invalid');
        return res.status(401).json({ error: 'Invalid session' });
      }

      // The session endpoint returns user info including username and level
      const sessionData = JSON.parse(responseText);
      
      if (isDev) {
        console.log('[VERIFY] Session valid for user:', sessionData.session?.user?.name);
      }
      res.json({
        level: sessionData.session?.user?.level || 'SUPERUSER',
        username: sessionData.session?.user?.name || 'admin',
      });
    } catch (error) {
      console.error('[VERIFY] Session verification error:', error);
      res.status(500).json({ error: 'Session verification failed' });
    }
  });

  // Serve config.json as static file (public endpoint)
  // Available at both /config.json and /admin/config.json
  const serveConfig = (req: Request, res: Response) => {
    res.json({
      api: {
        url: `${getBackendUrl()}/api/rest`
      }
    });
  };
  app.get('/config.json', serveConfig);
  app.get('/admin/config.json', serveConfig);

  // Version endpoint - returns Elements backend version (public endpoint)
  app.get('/api/version', async (req: Request, res: Response) => {
    try {
      const ELEMENTS_BACKEND_URL = getBackendUrl();
      
      const response = await fetch(`${ELEMENTS_BACKEND_URL}/api/rest/version`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        return res.json({ version: 'Unknown' });
      }

      const versionData = await response.json();
      res.json({ version: versionData.version || versionData });
    } catch (error) {
      res.json({ version: 'Unknown' });
    }
  });

  // Mock endpoint for installed elements (returns HelloWorld demo element) - requires auth
  app.get('/api/rest/elements/application', requireAuth, (req: Request, res: Response) => {
    const mockApplicationStatuses = [
      {
        id: 'app-demo',
        name: 'Demo Application',
        elements: [
          {
            id: 'elem-hello-world',
            applicationId: 'app-demo',
            element: {
              type: 'DemoElement',
              definition: {
                name: 'HelloWorld',
                recursive: false,
                additionalPackages: [
                  { name: 'demo-core', version: '1.0.0' }
                ],
                loader: 'StandardLoader'
              },
              services: [
                {
                  name: 'GreetingService',
                  type: 'dev.getelements.demo.GreetingService',
                  description: 'Service for managing greetings'
                }
              ],
              producedEvents: [
                {
                  name: 'GreetingCreated',
                  type: 'dev.getelements.demo.events.GreetingCreated',
                  description: 'Fired when a new greeting is created'
                }
              ],
              consumedEvents: [],
              dependencies: [],
              attributes: { 
                version: '1.0.0',
                author: 'Elements Team',
                description: 'A simple HelloWorld element for demonstration'
              },
              defaultAttributes: [
                {
                  name: 'defaultLanguage',
                  value: 'en',
                  description: 'Default language for greetings'
                }
              ]
            }
          }
        ]
      }
    ];
    
    res.json(mockApplicationStatuses);
  });

  // In-memory storage for demo greetings
  let demoGreetings = [
    { id: 'greeting-1', message: 'Hello, World!', language: 'en', createdAt: new Date().toISOString() },
    { id: 'greeting-2', message: 'Bonjour, Monde!', language: 'fr', createdAt: new Date().toISOString() },
    { id: 'greeting-3', message: 'Hola, Mundo!', language: 'es', createdAt: new Date().toISOString() },
  ];

  // Mock API endpoints for HelloWorld element - require auth
  app.get('/api/proxy/app/rest/HelloWorld/greetings', requireAuth, (req: Request, res: Response) => {
    const { language, limit } = req.query;
    let results = [...demoGreetings];
    
    if (language) {
      results = results.filter(g => g.language === language);
    }
    
    if (limit) {
      results = results.slice(0, parseInt(limit as string));
    }
    
    res.json(results);
  });

  app.post('/api/proxy/app/rest/HelloWorld/greetings', requireAuth, (req: Request, res: Response) => {
    const { message, language } = req.body;
    
    if (!message || !language) {
      return res.status(400).json({ error: 'message and language are required' });
    }
    
    const newGreeting = {
      id: `greeting-${Date.now()}`,
      message,
      language,
      createdAt: new Date().toISOString(),
    };
    
    demoGreetings.push(newGreeting);
    res.status(201).json(newGreeting);
  });

  app.get('/api/proxy/app/rest/HelloWorld/greetings/:id', requireAuth, (req: Request, res: Response) => {
    const greeting = demoGreetings.find(g => g.id === req.params.id);
    
    if (!greeting) {
      return res.status(404).json({ error: 'Greeting not found' });
    }
    
    res.json(greeting);
  });

  app.delete('/api/proxy/app/rest/HelloWorld/greetings/:id', requireAuth, (req: Request, res: Response) => {
    const index = demoGreetings.findIndex(g => g.id === req.params.id);
    
    if (index === -1) {
      return res.status(404).json({ error: 'Greeting not found' });
    }
    
    demoGreetings.splice(index, 1);
    res.status(204).send();
  });

  // Mock OpenAPI spec for HelloWorld element (demo/testing) - optional auth
  app.get('/api/proxy/app/rest/HelloWorld/openapi.json', optionalAuth, (req: Request, res: Response) => {
    const mockSpec = {
      openapi: '3.0.1',
      info: {
        title: 'HelloWorld Element API',
        description: 'Demo REST API for the HelloWorld element',
        version: '1.0.0',
      },
      paths: {
        '/greetings': {
          get: {
            tags: ['Greetings'],
            summary: 'List all greetings',
            description: 'Retrieve a list of all stored greetings',
            operationId: 'listGreetings',
            parameters: [
              {
                name: 'language',
                in: 'query',
                required: false,
                schema: { type: 'string', default: 'en' },
                description: 'Filter greetings by language code',
              },
              {
                name: 'limit',
                in: 'query',
                required: false,
                schema: { type: 'integer', default: 10 },
                description: 'Maximum number of greetings to return',
              },
            ],
            responses: {
              '200': {
                description: 'Successful response',
                content: {
                  'application/json': {
                    schema: {
                      type: 'array',
                      items: {
                        type: 'object',
                        properties: {
                          id: { type: 'string' },
                          message: { type: 'string' },
                          language: { type: 'string' },
                          createdAt: { type: 'string', format: 'date-time' },
                        },
                      },
                    },
                  },
                },
              },
            },
          },
          post: {
            tags: ['Greetings'],
            summary: 'Create a new greeting',
            description: 'Add a new greeting to the system',
            operationId: 'createGreeting',
            requestBody: {
              required: true,
              content: {
                'application/json': {
                  schema: {
                    type: 'object',
                    required: ['message', 'language'],
                    properties: {
                      message: {
                        type: 'string',
                        description: 'The greeting message',
                        example: 'Hello, World!',
                      },
                      language: {
                        type: 'string',
                        description: 'Language code for the greeting',
                        example: 'en',
                      },
                    },
                  },
                },
              },
            },
            responses: {
              '201': {
                description: 'Greeting created successfully',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        id: { type: 'string' },
                        message: { type: 'string' },
                        language: { type: 'string' },
                        createdAt: { type: 'string', format: 'date-time' },
                      },
                    },
                  },
                },
              },
              '400': {
                description: 'Bad request - missing required fields',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        error: {
                          type: 'string',
                          example: 'message and language are required',
                        },
                      },
                    },
                  },
                },
              },
            },
          },
        },
        '/greetings/{id}': {
          get: {
            tags: ['Greetings'],
            summary: 'Get a greeting by ID',
            description: 'Retrieve a specific greeting using its unique identifier',
            operationId: 'getGreeting',
            parameters: [
              {
                name: 'id',
                in: 'path',
                required: true,
                schema: { type: 'string' },
                description: 'The greeting ID',
              },
            ],
            responses: {
              '200': {
                description: 'Successful response',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        id: { type: 'string' },
                        message: { type: 'string' },
                        language: { type: 'string' },
                        createdAt: { type: 'string', format: 'date-time' },
                      },
                    },
                  },
                },
              },
              '404': {
                description: 'Greeting not found',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        error: {
                          type: 'string',
                          example: 'Greeting not found',
                        },
                      },
                    },
                  },
                },
              },
            },
          },
          delete: {
            tags: ['Greetings'],
            summary: 'Delete a greeting',
            description: 'Remove a greeting from the system',
            operationId: 'deleteGreeting',
            parameters: [
              {
                name: 'id',
                in: 'path',
                required: true,
                schema: { type: 'string' },
                description: 'The greeting ID',
              },
            ],
            responses: {
              '204': {
                description: 'Greeting deleted successfully',
              },
              '404': {
                description: 'Greeting not found',
                content: {
                  'application/json': {
                    schema: {
                      type: 'object',
                      properties: {
                        error: {
                          type: 'string',
                          example: 'Greeting not found',
                        },
                      },
                    },
                  },
                },
              },
            },
          },
        },
      },
    };

    res.json(mockSpec);
  });

  // Proxy all /api/proxy/* requests to the Elements backend
  app.all('/api/proxy/*', optionalAuth, proxyLimiter, async (req: Request, res: Response) => {
    try {
      const ELEMENTS_BACKEND_URL = getBackendUrl();
      
      // Extract the path after /api/proxy
      const elementsPath = req.path.replace('/api/proxy', '');
      
      // Build query string from req.query
      const queryString = new URLSearchParams(req.query as Record<string, string>).toString();
      const targetUrl = `${ELEMENTS_BACKEND_URL}${elementsPath}${queryString ? `?${queryString}` : ''}`;

      // Check if this is an auth endpoint that should not require/include session header
      const authEndpoints = ['/api/rest/auth/oauth2', '/api/rest/auth/oidc', '/api/rest/session', '/api/rest/signup'];
      const isAuthEndpoint = req.method === 'POST' && authEndpoints.some(endpoint => elementsPath === endpoint);

      // Check for custom session token in header (from frontend override) or use cookie session
      const customToken = req.headers['elements-sessionsecret'] as string;
      const sessionToken = customToken || (req as any).sessionToken;
      const isDev = process.env.NODE_ENV === 'development' && !process.env.REPLIT_DEPLOYMENT;
      
      if (isDev) {
        console.log(`[PROXY] ${req.method} ${elementsPath}`);
        console.log(`[PROXY] Custom token: ${customToken ? 'present' : 'none'}`);
        console.log(`[PROXY] Cookie token: ${(req as any).sessionToken ? 'present' : 'none'}`);
        console.log(`[PROXY] Using token: ${sessionToken ? 'present' : 'none'}`);
      }
      
      if (!isAuthEndpoint && !sessionToken) {
        return res.status(401).json({ error: 'Authentication required' });
      }

      // Forward headers with session token (except for auth endpoints)
      const headers: HeadersInit = {};
      
      if (!isAuthEndpoint && sessionToken) {
        headers['Elements-SessionSecret'] = sessionToken;
      }
      
      // Only set Content-Type for requests with a body
      if (req.method !== 'GET' && req.method !== 'HEAD') {
        headers['Content-Type'] = 'application/json';
      }
      
      // Forward Accept header if present
      if (req.headers.accept) {
        headers['Accept'] = req.headers.accept;
      }

      // Make request to Elements backend
      const response = await fetch(targetUrl, {
        method: req.method,
        headers,
        body: req.method !== 'GET' && req.method !== 'HEAD' ? JSON.stringify(req.body) : undefined,
      });

      // Forward response status
      res.status(response.status);
      
      // Get response body
      const contentType = response.headers.get('content-type');
      
      // Check if this is a YAML file based on path or content-type
      const isYamlEndpoint = elementsPath.endsWith('.yaml') || elementsPath.endsWith('.yml');
      
      if (contentType?.includes('application/json')) {
        const data = await response.json();
        
        // Sanitize error responses to avoid leaking internal details
        if (!response.ok) {
          console.error(`Backend error (${response.status}) for ${req.method} ${elementsPath}`);
          console.error('Full error response:', JSON.stringify(data, null, 2));
          // Forward error info, including validation details if present
          const sanitizedError = {
            error: data.error || data.message || 'Request failed',
            message: data.message,
            details: data.details || data.errors || data.validationErrors,
            status: response.status,
          };
          return res.json(sanitizedError);
        }
        
        res.json(data);
      } else if (contentType?.includes('yaml') || contentType?.includes('yml') || isYamlEndpoint) {
        // Handle YAML responses (like OpenAPI specs)
        const text = await response.text();
        
        try {
          const data = yaml.load(text);
          res.json(data);
        } catch (yamlError) {
          console.error(`Failed to parse YAML from ${elementsPath}`);
          res.status(500).json({ error: 'Invalid YAML response' });
        }
      } else {
        const text = await response.text();
        
        if (!response.ok) {
          console.error(`Backend error (${response.status}) for ${req.method} ${elementsPath}`);
          return res.status(response.status).json({ error: 'Request failed' });
        }
        
        res.send(text);
      }
    } catch (error) {
      console.error('Proxy error:', sanitizeError(error));
      res.status(500).json({ error: 'Proxy request failed' });
    }
  });

  const httpServer = createServer(app);

  return httpServer;
}
