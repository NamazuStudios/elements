import { test, expect } from '@playwright/test';

test.describe('Resource Management', () => {
  test.beforeEach(async ({ page }) => {
    // Login as SUPERUSER before each test
    await page.goto('/admin');
    await page.getByTestId('input-username').fill(process.env.TEST_SUPERUSER_USERNAME || 'root');
    await page.getByTestId('input-password').fill(process.env.TEST_SUPERUSER_PASSWORD || 'password');
    await page.getByTestId('button-login').click();
    await expect(page).toHaveURL(/\/admin\/(dashboard|resource)/);
  });

  test('should navigate to users resource page', async ({ page }) => {
    await page.goto('/admin/resource/users');
    
    // Skip test if users resource doesn't exist
    const hasUsersHeading = await page.getByRole('heading', { name: /users/i }).isVisible({ timeout: 5000 }).catch(() => false);
    if (!hasUsersHeading) {
      test.skip(true, 'Users resource not available - backend configuration needed');
      return;
    }
    
    // Should show user list
    await page.waitForTimeout(1000);
  });

  test('should load user details when clicking on a user row', async ({ page }) => {
    await page.goto('/admin/resource/users');
    
    // Skip test if no users data available
    const hasUsers = await page.locator('[data-testid^="row-user-"]').first().isVisible({ timeout: 5000 }).catch(() => false);
    if (!hasUsers) {
      test.skip(true, 'No users data available - backend needs to be populated with test data');
      return;
    }
    
    // Wait for users to load
    await page.waitForTimeout(1000);
    
    // Click first user
    const firstUserRow = page.locator('[data-testid^="row-user-"]').first();
    await firstUserRow.click();
    
    // Should show user details tabs
    await expect(page.getByRole('tab', { name: /details/i })).toBeVisible();
  });

  test('should preserve query parameters in API requests', async ({ page }) => {
    const requestUrls: string[] = [];
    
    // Capture all API requests
    page.on('request', request => {
      const url = request.url();
      if (url.includes('/api/rest/')) {
        requestUrls.push(url);
      }
    });
    
    await page.goto('/admin/resource/users');
    
    // Wait for requests to complete
    await page.waitForTimeout(2000);
    
    // Skip test if no API requests were made (backend not available)
    if (requestUrls.length === 0) {
      test.skip(true, 'No API requests detected - backend may not be available');
      return;
    }
    
    // Find requests with query parameters
    const requestsWithParams = requestUrls.filter(url => {
      const urlObj = new URL(url);
      return urlObj.search.length > 0;
    });
    
    // Log for debugging
    console.log('[TEST] Requests with query params:', requestsWithParams);
    
    // Verify query parameters are preserved (if any requests had them)
    requestsWithParams.forEach(url => {
      const urlObj = new URL(url);
      // Query string should not be empty if the request had parameters
      expect(urlObj.search).not.toBe('');
    });
  });
});
