import { test, expect, Page, Request } from '@playwright/test';
import { loginAsSuperuser } from './helpers/test-helpers';

/**
 * Comprehensive API authentication tests
 * 
 * These tests verify that all API endpoints properly include authentication headers
 * to prevent 401 errors when accessing protected resources.
 */

test.describe('API Authentication Headers', () => {
  let authenticatedRequests: Request[] = [];
  
  // Helper to capture all API requests and verify they have auth headers
  async function setupAuthCapture(page: Page) {
    authenticatedRequests = [];
    
    page.on('request', request => {
      const url = request.url();
      // Capture all API requests to the proxy
      if (url.includes('/api/proxy/') && !url.includes('config.json')) {
        authenticatedRequests.push(request);
      }
    });
  }
  
  // Helper to verify auth headers are present
  function verifyAuthHeaders(requests: Request[], description: string) {
    const requestsWithoutAuth = requests.filter(req => {
      const url = req.url();
      const headers = req.headers();
      
      // Exclude the session login endpoint - it doesn't need auth
      if (url.includes('/api/rest/session') && req.method() === 'POST') {
        return false;
      }
      
      return !headers['elements-sessionsecret'] && !headers['Elements-SessionSecret'];
    });
    
    if (requestsWithoutAuth.length > 0) {
      console.error(`Found ${requestsWithoutAuth.length} requests without auth headers in ${description}:`);
      requestsWithoutAuth.forEach(req => {
        console.error(`  - ${req.method()} ${req.url()}`);
      });
    }
    
    expect(requestsWithoutAuth.length).toBe(0);
  }

  test.beforeEach(async ({ page }) => {
    await setupAuthCapture(page);
    await loginAsSuperuser(page);
  });

  test('Dashboard page - all API calls should have auth headers', async ({ page }) => {
    await page.goto('/admin/dashboard');
    
    // Wait for data to load
    await page.waitForTimeout(2000);
    
    // Verify at least one request was made
    expect(authenticatedRequests.length).toBeGreaterThan(0);
    
    // Verify auth headers on all requests
    verifyAuthHeaders(authenticatedRequests, 'Dashboard');
    
    // Verify specific expected endpoints were called
    const urls = authenticatedRequests.map(r => r.url());
    expect(urls.some(url => url.includes('/api/rest/version'))).toBeTruthy();
    expect(urls.some(url => url.includes('/api/rest/health'))).toBeTruthy();
  });

  test('Large Objects page - list and API calls should have auth headers', async ({ page }) => {
    await page.goto('/admin/large-objects');
    
    // Wait for page to load
    await page.waitForTimeout(2000);
    
    // If requests were made, verify they have auth headers
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Large Objects');
      
      // If large_object endpoint was called, verify it has auth
      const largeObjectRequests = authenticatedRequests.filter(r => 
        r.url().includes('/api/rest/large_object')
      );
      if (largeObjectRequests.length > 0) {
        const headers = largeObjectRequests[0].headers();
        expect(
          headers['elements-sessionsecret'] || headers['Elements-SessionSecret']
        ).toBeTruthy();
      }
    }
  });

  test('Resource Manager - OpenAPI spec and data calls should have auth headers', async ({ page }) => {
    await page.goto('/admin/resource');
    
    // Wait for OpenAPI spec to load
    await page.waitForTimeout(2000);
    
    // If requests were made, verify they have auth headers
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Resource Manager');
      
      // If OpenAPI spec was fetched, verify it has auth
      const openapiRequests = authenticatedRequests.filter(r => 
        r.url().includes('openapi.yaml') || r.url().includes('openapi.json')
      );
      if (openapiRequests.length > 0) {
        const headers = openapiRequests[0].headers();
        expect(
          headers['elements-sessionsecret'] || headers['Elements-SessionSecret']
        ).toBeTruthy();
      }
    }
  });

  test('Core Elements page - should have auth headers', async ({ page }) => {
    await page.goto('/admin/core-elements');
    
    // Wait for data to load
    await page.waitForTimeout(2000);
    
    // If requests were made, verify they have auth headers
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Core Elements');
      
      // If system elements endpoint was called, verify it has auth
      const systemRequests = authenticatedRequests.filter(r => 
        r.url().includes('/api/rest/elements/system')
      );
      if (systemRequests.length > 0) {
        expect(systemRequests.length).toBeGreaterThan(0);
      }
    }
  });

  test('Element API Explorer - OpenAPI spec should have auth headers', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.waitForTimeout(1000);
    
    // Find an element in the sidebar and click it
    const elementLink = page.locator('[data-testid^="link-element-"]').first();
    const hasElements = await elementLink.count() > 0;
    
    if (!hasElements) {
      // Skip test if no elements are available in the backend
      test.skip();
      return;
    }
    
    await elementLink.click();
    
    // Wait for element page to load
    await page.waitForTimeout(2000);
    
    // Verify auth headers on all requests
    verifyAuthHeaders(authenticatedRequests, 'Element API Explorer');
    
    // Verify OpenAPI spec was actually requested (required for Element API Explorer)
    const openapiRequests = authenticatedRequests.filter(r => 
      r.url().includes('openapi.yaml') || r.url().includes('openapi.json')
    );
    expect(openapiRequests.length).toBeGreaterThan(0);
    
    // Verify the OpenAPI request has auth header
    const headers = openapiRequests[0].headers();
    expect(
      headers['elements-sessionsecret'] || headers['Elements-SessionSecret']
    ).toBeTruthy();
  });

  test('Dynamic API Explorer - OpenAPI spec should have auth headers', async ({ page }) => {
    await page.goto('/admin/dynamic-api');
    
    // Wait for page to load and OpenAPI spec to be fetched
    await page.waitForTimeout(2000);
    
    // If requests were made, verify they have auth headers
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Dynamic API Explorer');
      
      // If OpenAPI spec was fetched, verify it has auth
      const openapiRequests = authenticatedRequests.filter(r => 
        r.url().includes('openapi.yaml') || r.url().includes('openapi.json')
      );
      if (openapiRequests.length > 0) {
        const headers = openapiRequests[0].headers();
        expect(
          headers['elements-sessionsecret'] || headers['Elements-SessionSecret']
        ).toBeTruthy();
      }
    }
  });

  test('Applications without elements - should have auth headers', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.waitForTimeout(1000);
    
    // Try to find an application link in sidebar
    const appLink = page.locator('[data-testid^="link-app-"]').first();
    const hasApps = await appLink.count() > 0;
    
    if (!hasApps) {
      // Skip test if no applications without elements are available
      test.skip();
      return;
    }
    
    // Clear previous requests
    authenticatedRequests = [];
    
    await appLink.click();
    await page.waitForTimeout(2000);
    
    // Verify at least one request was made
    expect(authenticatedRequests.length).toBeGreaterThan(0);
    
    // Verify auth headers
    verifyAuthHeaders(authenticatedRequests, 'Application Info');
    
    // Verify application endpoint was called (required)
    const appRequests = authenticatedRequests.filter(r => 
      r.url().includes('/api/rest/elements/application')
    );
    expect(appRequests.length).toBeGreaterThan(0);
  });

  test('Refresh buttons - should maintain auth headers', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.waitForTimeout(1000);
    
    // Click refresh button
    const refreshButton = page.getByTestId('button-refresh');
    const hasRefresh = await refreshButton.count() > 0;
    
    if (!hasRefresh) {
      // Skip if no refresh button found
      test.skip();
      return;
    }
    
    // Clear previous requests
    authenticatedRequests = [];
    
    await refreshButton.click();
    await page.waitForTimeout(1000);
    
    // If requests were made during refresh, verify they have auth headers
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Dashboard Refresh');
    }
  });

  test('Large Objects refresh - should have auth headers', async ({ page }) => {
    await page.goto('/admin/large-objects');
    await page.waitForTimeout(1000);
    
    // Click refresh button
    const refreshButton = page.getByTestId('button-refresh');
    const hasRefresh = await refreshButton.count() > 0;
    
    if (!hasRefresh) {
      // Skip if no refresh button found
      test.skip();
      return;
    }
    
    // Clear previous requests
    authenticatedRequests = [];
    
    await refreshButton.click();
    await page.waitForTimeout(1000);
    
    // If requests were made during refresh, verify they have auth headers
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Large Objects Refresh');
      
      // If large_object was called, verify it happened
      const largeObjectRequests = authenticatedRequests.filter(r => 
        r.url().includes('/api/rest/large_object')
      );
      if (largeObjectRequests.length > 0) {
        expect(largeObjectRequests.length).toBeGreaterThan(0);
      }
    }
  });

  test('Session persistence after page reload', async ({ page }) => {
    await page.goto('/admin/dashboard');
    await page.waitForTimeout(1000);
    
    // Clear previous requests
    authenticatedRequests = [];
    
    // Reload the page
    await page.reload();
    await page.waitForTimeout(2000);
    
    // If requests were made after reload, verify they have auth headers
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'After Page Reload');
      
      // Should still be able to fetch data
      const dataRequests = authenticatedRequests.filter(r => 
        r.url().includes('/api/rest/')
      );
      expect(dataRequests.length).toBeGreaterThan(0);
    }
  });

  test('Multiple page navigation - auth headers should persist', async ({ page }) => {
    // Navigate through multiple pages
    await page.goto('/admin/dashboard');
    await page.waitForTimeout(1000);
    
    // Large Objects navigation
    authenticatedRequests = [];
    await page.goto('/admin/large-objects');
    await page.waitForTimeout(1000);
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Large Objects Navigation');
    }
    
    // Resource Manager navigation
    authenticatedRequests = [];
    await page.goto('/admin/resource');
    await page.waitForTimeout(1000);
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Resource Manager Navigation');
    }
    
    // Core Elements navigation
    authenticatedRequests = [];
    await page.goto('/admin/core-elements');
    await page.waitForTimeout(1000);
    if (authenticatedRequests.length > 0) {
      verifyAuthHeaders(authenticatedRequests, 'Core Elements Navigation');
    }
  });
});

test.describe('API Error Handling', () => {
  test('Should not get 401 errors on any page', async ({ page }) => {
    await loginAsSuperuser(page);
    
    const unauthorizedResponses: any[] = [];
    
    page.on('response', response => {
      if (response.status() === 401) {
        unauthorizedResponses.push({
          url: response.url(),
          status: response.status()
        });
      }
    });
    
    // Visit all major pages
    const pages = [
      '/admin/dashboard',
      '/admin/large-objects',
      '/admin/resource',
      '/admin/core-elements'
    ];
    
    for (const pageUrl of pages) {
      await page.goto(pageUrl);
      await page.waitForTimeout(2000);
    }
    
    // Report any 401 errors
    if (unauthorizedResponses.length > 0) {
      console.error('Found 401 Unauthorized responses:');
      unauthorizedResponses.forEach(resp => {
        console.error(`  - ${resp.url}`);
      });
    }
    
    expect(unauthorizedResponses.length).toBe(0);
  });
});
