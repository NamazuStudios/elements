# Integration Tests

This directory contains end-to-end integration tests for the Elements Admin Editor using Playwright.

## Setup

### 1. Install Playwright browsers

```bash
npx playwright install chromium
```

### 2. Configure test credentials

Create a `.env` file in the root directory with your test credentials:

```env
TEST_SUPERUSER_USERNAME=root
TEST_SUPERUSER_PASSWORD=your-password-here
ELEMENTS_BACKEND_URL=http://localhost:8080
```

### 3. Ensure Elements backend is running

The tests require a running Elements backend with test data:
- Backend should be running at the URL specified in `ELEMENTS_BACKEND_URL`
- Backend should have at least one SUPERUSER account with credentials from step 2
- For inventory/resource tests to pass, backend should have sample users and data

### 4. Add test scripts to package.json

Add these scripts to your `package.json`:

```json
{
  "scripts": {
    "test": "playwright test",
    "test:auth": "playwright test tests/auth.spec.ts",
    "test:headed": "playwright test --headed",
    "test:ui": "playwright test --ui",
    "test:debug": "playwright test --debug",
    "test:codegen": "playwright codegen http://localhost:5000/admin"
  }
}
```

## Running Tests

### Run all tests
```bash
npm test
```

### Run tests in headed mode (see the browser)
```bash
npm run test:headed
```

### Run tests with Playwright UI (interactive)
```bash
npm run test:ui
```

### Debug a specific test
```bash
npm run test:debug
```

### Generate new tests with codegen
```bash
npm run test:codegen
```

## Test Structure

### Test Files

- **`auth.spec.ts`** - Authentication flow tests
  - Verifies SUPERUSER-only access
  - Tests login/logout functionality
  - Tests "remember me" session persistence

- **`api-authentication.spec.ts`** - API authentication header tests (NEW)
  - Verifies all API requests include authentication headers
  - Tests Dashboard, Large Objects, Resource Manager, Core Elements
  - Tests Element API Explorer and Dynamic API Explorer
  - Verifies refresh button functionality maintains auth
  - Tests session persistence across page reloads
  - Prevents 401 authentication errors from regressions
  - **Critical for catching missing auth headers before production**

- **`inventory.spec.ts`** - Inventory viewer tests
  - Verifies userId query parameters are sent correctly
  - Tests fungible and distinct inventory display
  - Prevents regression of query parameter stripping bug

- **`resources.spec.ts`** - Resource management tests
  - Tests navigation to resource pages
  - Tests user detail loading
  - Verifies query parameters in API requests

### Helper Utilities

- **`helpers/test-helpers.ts`** - Reusable test utilities
  - `loginAsSuperuser()` - Helper to login before tests
  - `captureApiRequests()` - Capture and filter API requests
  - `verifyQueryParams()` - Verify URL query parameters

## Writing New Tests

### Example: Testing a new feature

```typescript
import { test, expect } from '@playwright/test';
import { loginAsSuperuser } from './helpers/test-helpers';

test.describe('My New Feature', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsSuperuser(page);
  });

  test('should do something', async ({ page }) => {
    await page.goto('/my-feature');
    await expect(page.getByTestId('my-element')).toBeVisible();
  });
});
```

### Best Practices

1. **Use data-testid attributes** for reliable element selection
2. **Login once** per test using beforeEach hook
3. **Test critical paths** that could break in production
4. **Verify API requests** to catch query parameter issues
5. **Clean up** test data when necessary

## Regression Prevention

These tests specifically prevent regressions in:

1. **API Authentication Headers** (NEW)
   - Verifies all API requests include `Elements-SessionSecret` header
   - Tests custom queryFn implementations in pages
   - Catches missing authentication on Large Objects, OpenAPI specs, and element APIs
   - Prevents 401 Unauthorized errors from reaching production
   - Tests all major pages: Dashboard, Large Objects, Resource Manager, Core Elements
   - Verifies auth persists across navigation and page reloads

2. **Query Parameter Handling**
   - Tests verify that query parameters (like `userId`) are preserved in API requests
   - Catches issues between development and production mode

3. **Authentication Security**
   - Ensures only SUPERUSER users can access the admin interface
   - Verifies proper session management

4. **Resource Navigation**
   - Tests that all resource pages load correctly
   - Verifies detail views work properly

## CI/CD Integration

To run tests in CI/CD:

```yaml
# Example GitHub Actions workflow
- name: Install dependencies
  run: npm install

- name: Install Playwright browsers
  run: npx playwright install chromium

- name: Run tests
  run: npm test
  env:
    TEST_SUPERUSER_USERNAME: ${{ secrets.TEST_SUPERUSER_USERNAME }}
    TEST_SUPERUSER_PASSWORD: ${{ secrets.TEST_SUPERUSER_PASSWORD }}
```

## Test Results

### Default State (Without Backend Data)
When run without a populated Elements backend:
- ✅ **2 tests pass**: SUPERUSER authentication and session persistence
- ⏭️ **6 tests skip gracefully**: Tests that require backend data automatically skip with clear messages
- ❌ **0 tests fail**: Test suite ships in a passing state

### Passing Tests
- ✅ **Authentication**: SUPERUSER login working correctly
- ✅ **Session Management**: Remember me functionality  
- ✅ **Path Construction**: Bug fix verified (no `/api/proxy./config.json` errors)

### Tests That Auto-Skip Without Backend Data
The following tests will skip automatically when backend data isn't available:
- ⏭️ **Inventory Viewer** (2 tests): Requires users with inventory data
- ⏭️ **Resource Management** (4 tests): Requires configured resources and user data
- ⏭️ **Non-SUPERUSER rejection**: Requires a non-SUPERUSER test account

When backend data is available, these tests will run automatically.

## Troubleshooting

### Tests fail with "element not found"
- Ensure data-testid attributes match
- Check if elements are visible before interacting
- Use `await page.waitForTimeout()` if necessary
- Verify you're navigating to `/admin` base path

### Authentication fails
- Verify .env credentials are correct
- Ensure Elements backend is running at ELEMENTS_BACKEND_URL
- Check that SUPERUSER account exists in backend
- Confirm credentials match TEST_SUPERUSER_USERNAME and TEST_SUPERUSER_PASSWORD

### Resource/inventory tests fail
- Ensure Elements backend has sample users and data
- Verify backend is properly configured and accessible
- Check that resources are visible in the admin UI manually first

### Query parameter tests fail
- These tests monitor the critical bug where query params were being stripped
- If failing, check browser console logs for path construction errors
- Rebuild production bundle: `npm run build`
- Verify latest code is deployed

## Coverage Areas

Current test coverage includes:
- ✅ Authentication (login/logout, SUPERUSER check)
- ✅ API authentication headers (all pages and endpoints) **NEW**
- ✅ Large Objects API authentication **NEW**
- ✅ OpenAPI spec fetching with auth **NEW**
- ✅ Element API Explorer authentication **NEW**
- ✅ Session persistence across reloads **NEW**
- ✅ Inventory viewer (query parameters)
- ✅ Resource navigation
- ⚠️ TODO: Form submissions
- ⚠️ TODO: API mutations (create/update/delete)
