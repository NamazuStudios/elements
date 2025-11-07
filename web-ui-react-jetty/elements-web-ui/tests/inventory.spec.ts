import { test, expect } from '@playwright/test';

test.describe('Inventory Viewer', () => {
  test.beforeEach(async ({ page }) => {
    // Login as SUPERUSER before each test
    await page.goto('/admin');
    await page.getByTestId('input-username').fill(process.env.TEST_SUPERUSER_USERNAME || 'root');
    await page.getByTestId('input-password').fill(process.env.TEST_SUPERUSER_PASSWORD || 'password');
    await page.getByTestId('button-login').click();
    await expect(page).toHaveURL(/\/admin\/(dashboard|resource)/);
  });

  test('should send userId query parameter when viewing user inventory', async ({ page }) => {
    // Navigate to users page
    await page.goto('/admin/resource/users');
    
    // Skip test if users resource doesn't exist or no data
    const hasUsers = await page.locator('[data-testid^="row-user-"]').first().isVisible({ timeout: 5000 }).catch(() => false);
    if (!hasUsers) {
      test.skip(true, 'No users data available - backend needs to be populated with test data');
      return;
    }
    
    // Wait for users to load
    await page.waitForTimeout(1000);
    
    // Click on the first user row to view details
    const firstUserRow = page.locator('[data-testid^="row-user-"]').first();
    await firstUserRow.click();
    
    // Wait for inventory tab to be visible
    await expect(page.getByText(/inventory/i)).toBeVisible();
    
    // Click on inventory tab
    await page.getByRole('tab', { name: /inventory/i }).click();
    
    // Listen for network requests to verify query parameters
    const inventoryRequests: string[] = [];
    page.on('request', request => {
      const url = request.url();
      if (url.includes('/inventory/advanced') || url.includes('/inventory/distinct')) {
        inventoryRequests.push(url);
        console.log('[TEST] Inventory request URL:', url);
      }
    });
    
    // Wait for inventory to load
    await page.waitForTimeout(2000);
    
    // Verify that at least one inventory request was made with userId parameter
    const hasUserIdParam = inventoryRequests.some(url => url.includes('userId='));
    expect(hasUserIdParam).toBeTruthy();
  });

  test('should display fungible and distinct inventory items', async ({ page }) => {
    // Navigate to users page
    await page.goto('/admin/resource/users');
    
    // Skip test if users resource doesn't exist or no data
    const hasUsers = await page.locator('[data-testid^="row-user-"]').first().isVisible({ timeout: 5000 }).catch(() => false);
    if (!hasUsers) {
      test.skip(true, 'No users data available - backend needs to be populated with test data');
      return;
    }
    
    // Click on a user with inventory (you may need to adjust based on your test data)
    const userRow = page.locator('[data-testid^="row-user-"]').first();
    await userRow.click();
    
    // Click inventory tab
    await page.getByRole('tab', { name: /inventory/i }).click();
    
    // Wait for inventory content
    await page.waitForTimeout(1000);
    
    // Should show fungible and distinct tabs
    await expect(page.getByRole('tab', { name: /fungible/i })).toBeVisible();
    await expect(page.getByRole('tab', { name: /distinct/i })).toBeVisible();
  });
});
