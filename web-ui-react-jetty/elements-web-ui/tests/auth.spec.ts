import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test.skip('should reject non-SUPERUSER login attempts', async ({ page }) => {
    // Skip this test by default as it requires a non-SUPERUSER test account
    // To enable: create a USER level account and update credentials below
    await page.goto('/admin');
    
    // Wait for login page
    await expect(page.getByTestId('input-username')).toBeVisible();
    
    // Try to login with a USER level account (assuming you have test credentials)
    await page.getByTestId('input-username').fill('testuser');
    await page.getByTestId('input-password').fill('testpassword');
    await page.getByTestId('button-login').click();
    
    // Should show error message about access denied
    await expect(page.locator('text=Access denied')).toBeVisible();
  });

  test('should allow SUPERUSER login', async ({ page }) => {
    await page.goto('/admin');
    
    // Fill in SUPERUSER credentials
    await page.getByTestId('input-username').fill(process.env.TEST_SUPERUSER_USERNAME || 'root');
    await page.getByTestId('input-password').fill(process.env.TEST_SUPERUSER_PASSWORD || 'password');
    await page.getByTestId('button-login').click();
    
    // Should redirect to dashboard
    await expect(page).toHaveURL(/\/(dashboard|resource)/);
  });

  test('should preserve session on page reload when remember me is checked', async ({ page }) => {
    await page.goto('/admin');
    
    // Login with remember me
    await page.getByTestId('input-username').fill(process.env.TEST_SUPERUSER_USERNAME || 'root');
    await page.getByTestId('input-password').fill(process.env.TEST_SUPERUSER_PASSWORD || 'password');
    await page.getByTestId('checkbox-remember-me').check();
    await page.getByTestId('button-login').click();
    
    // Wait for dashboard
    await expect(page).toHaveURL(/\/(dashboard|resource)/);
    
    // Reload page
    await page.reload();
    
    // Should still be logged in
    await expect(page).toHaveURL(/\/(dashboard|resource)/);
  });
});
