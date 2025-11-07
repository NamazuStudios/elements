import { test, expect } from '@playwright/test';

test.describe('Debug Tests', () => {
  test('should load the app homepage', async ({ page }) => {
    console.log('[DEBUG] Navigating to homepage...');
    await page.goto('/admin', { waitUntil: 'networkidle' });
    
    console.log('[DEBUG] Current URL:', page.url());
    console.log('[DEBUG] Page title:', await page.title());
    
    // Take a screenshot for debugging
    await page.screenshot({ path: 'test-results/debug-homepage.png', fullPage: true });
    
    // Log the page HTML
    const html = await page.content();
    console.log('[DEBUG] Page HTML length:', html.length);
    console.log('[DEBUG] Page HTML preview:', html.substring(0, 500));
    
    // Check if login form exists
    const usernameInput = page.getByTestId('input-username');
    const isVisible = await usernameInput.isVisible().catch(() => false);
    console.log('[DEBUG] Username input visible:', isVisible);
    
    if (!isVisible) {
      console.log('[DEBUG] Looking for any input elements...');
      const inputs = await page.locator('input').count();
      console.log('[DEBUG] Found', inputs, 'input elements');
      
      console.log('[DEBUG] Looking for login-related text...');
      const loginText = await page.locator('text=/login|sign in/i').count();
      console.log('[DEBUG] Found', loginText, 'login-related text elements');
    }
  });
});
