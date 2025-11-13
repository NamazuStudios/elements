import { Page } from '@playwright/test';

/**
 * Helper to login as SUPERUSER
 */
export async function loginAsSuperuser(page: Page) {
  await page.goto('/admin');
  await page.getByTestId('input-username').fill(process.env.TEST_SUPERUSER_USERNAME || 'root');
  await page.getByTestId('input-password').fill(process.env.TEST_SUPERUSER_PASSWORD || 'password');
  await page.getByTestId('button-login').click();
  
  // Wait for redirect
  await page.waitForURL(/\/(dashboard|resource)/);
}

/**
 * Helper to capture API requests with specific pattern
 */
export function captureApiRequests(page: Page, pattern: RegExp): Promise<string[]> {
  const requests: string[] = [];
  
  page.on('request', request => {
    const url = request.url();
    if (pattern.test(url)) {
      requests.push(url);
    }
  });
  
  return Promise.resolve(requests);
}

/**
 * Helper to verify query parameters in URL
 */
export function verifyQueryParams(url: string, expectedParams: Record<string, string>): boolean {
  const urlObj = new URL(url);
  const searchParams = new URLSearchParams(urlObj.search);
  
  for (const [key, value] of Object.entries(expectedParams)) {
    if (searchParams.get(key) !== value) {
      return false;
    }
  }
  
  return true;
}
