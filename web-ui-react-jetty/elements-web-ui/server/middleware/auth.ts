import type { Request, Response, NextFunction } from 'express';

const ELEMENTS_BACKEND_URL = process.env.ELEMENTS_BACKEND_URL || 'http://localhost:8080';

export async function requireAuth(req: Request, res: Response, next: NextFunction) {
  try {
    // Get session token from cookie or header
    const sessionToken = req.cookies?.['elements-session'] || req.headers['elements-sessionsecret'] as string;
    
    if (!sessionToken) {
      return res.status(401).json({ error: 'Authentication required' });
    }

    // Attach session token to request for proxy to use
    // Don't verify here - let the backend endpoints validate when they're called
    (req as any).sessionToken = sessionToken;
    
    next();
  } catch (error) {
    console.error('Authentication middleware error:', error);
    return res.status(500).json({ error: 'Authentication check failed' });
  }
}

// Optional auth - doesn't reject if no token, but attaches it if present
export async function optionalAuth(req: Request, res: Response, next: NextFunction) {
  try {
    const sessionToken = req.cookies?.['elements-session'] || req.headers['elements-sessionsecret'] as string;
    
    if (sessionToken) {
      (req as any).sessionToken = sessionToken;
    }
    
    next();
  } catch (error) {
    // Continue without auth if any error
    next();
  }
}
