# Elements Admin Editor

A modern, production-ready admin dashboard for managing and exploring Elements backend resources through dynamically generated forms and API interfaces.

## Overview

Elements Admin Editor provides a comprehensive web interface for interacting with the Elements platform backend. The application features dynamic API exploration with auto-generated forms based on OpenAPI specifications, making it easy to test and manage any Elements-based service without writing custom admin interfaces.

## Key Features

### Dynamic API Explorer
- **OpenAPI-Driven Forms**: Automatically generates interactive forms from OpenAPI specifications
- **Multi-Operation Support**: Handles multiple operations of the same HTTP method on different paths
- **Parameter Separation**: Independently manages path parameters, query parameters, and request body fields
- **Real-time Testing**: Execute API calls directly from the interface with instant response debugging
- **WebSocket Support**: Displays metadata and URIs for WebSocket-based elements

### Element Discovery
- **Automatic Discovery**: Discovers all installed Elements from the backend
- **Application Grouping**: Organizes elements by application with visual status indicators (CLEAN, UNSTABLE, FAILED)
- **Metadata Display**: Shows element attributes, services, URIs, and configuration when OpenAPI specs aren't available
- **Protocol Detection**: Automatically identifies HTTP vs WebSocket elements with appropriate icons

### Authentication & Security
- **Cookie-Based Auth**: Secure HTTP-only cookie authentication
- **Custom Token Override**: Test with custom authentication tokens (component-state only, non-persistent)
- **Rate Limiting**: Protection against brute force and abuse
- **Proxy Architecture**: All requests proxied through authenticated middleware

## Architecture

### Frontend
- **React 18** with TypeScript
- **Vite** for fast development and optimized builds
- **Wouter** for lightweight routing
- **TanStack Query** for server state management and caching
- **shadcn/ui** component library with Tailwind CSS
- **Dark mode** as primary theme

### Backend
- **Express.js** with TypeScript
- **Proxy pattern** for secure backend communication
- **In-memory storage** for development flexibility
- **Rate limiting** on all endpoints
- **Environment-based debug logging**

## Configuration

### Environment Variables

Required environment variables:

```bash
# Backend URL (required)
ELEMENTS_BACKEND_URL=https://your-elements-backend.com

# For frontend (if different from backend URL)
VITE_ELEMENTS_BACKEND_URL=https://your-elements-backend.com
```

### Backend Connection

The application acts as a proxy to your Elements backend service:
- All API requests go through `/api/proxy/*` routes
- Authentication is managed via cookies
- The backend must support the Elements REST API structure

## Development

### Prerequisites
- Node.js 20+
- npm or compatible package manager

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The application will be available at `http://localhost:5000`

### Build for Production

```bash
# Create optimized production build
npm run build

# Start production server
npm run start
```

## Project Structure

```
├── client/              # Frontend React application
│   ├── src/
│   │   ├── components/  # Reusable UI components
│   │   ├── contexts/    # React Context providers (Auth, Resources, Theme)
│   │   ├── lib/         # Utilities and helpers (OpenAPI analyzer, query client)
│   │   └── pages/       # Route components
├── server/              # Backend Express application
│   ├── routes.ts        # API routes and proxy logic
│   ├── storage.ts       # Storage interface (in-memory)
│   └── index.ts         # Server entry point
├── shared/              # Shared types and schemas
└── attached_assets/     # Static assets (logo)
```

## Key Components

### OpenAPI Analyzer (`client/src/lib/openapi-analyzer.ts`)
Parses OpenAPI specifications and extracts:
- Path parameters with types and validation
- Query parameters with proper type coercion
- Request body schemas
- Available operations per resource

### Dynamic Form Generator (`client/src/components/DynamicFormGenerator.tsx`)
Renders forms with three distinct sections:
- Path Parameters
- Query Parameters  
- Request Body Fields

Each parameter type is independently managed to prevent naming conflicts.

### Authentication Flow
1. User enters credentials on login page
2. Backend proxies auth request to Elements backend
3. Session token stored in HTTP-only secure cookie
4. Cookie automatically included in all subsequent requests
5. Middleware validates session before proxying to Elements backend

## API Endpoints

### Authentication
- `POST /api/auth/login` - Authenticate with Elements backend
- `POST /api/auth/logout` - Clear authentication session

### Proxy
- `/api/proxy/*` - Proxy all requests to Elements backend (requires authentication)

### Utility
- `GET /api/version` - Get Elements backend version

## Security Features

- **HTTP-only cookies**: Prevents XSS token theft
- **Rate limiting**: 5 login attempts per 15 minutes per IP
- **Secure cookies**: In production (httpOnly, secure, sameSite: strict)
- **Request sanitization**: Error messages sanitized to prevent information disclosure
- **Environment-based logging**: Debug logs disabled in production

## Browser Support

Modern browsers with ES2020+ support:
- Chrome/Edge 90+
- Firefox 88+
- Safari 14+

## License

See your organization's license terms.

## Support

For issues or questions related to the Elements Admin Editor, contact your Elements platform team.
