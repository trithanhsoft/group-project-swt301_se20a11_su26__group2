import { test, expect } from '@playwright/test';

test.describe('Role-Based Access Control (RBAC) Security Flow', () => {

  test('should redirect unauthenticated guest to login page when accessing protected admin route', async ({ page }) => {
    await page.goto('/admin');
    await expect(page).toHaveURL(/\/login/);
  });

  test('should redirect unauthenticated guest to home page when accessing protected instructor route', async ({ page }) => {
    page.on('pageerror', err => console.log('Browser Page Error (instructor):', err.message));
    await page.goto('/instructor');
    await expect(page).toHaveURL(/http:\/\/localhost:\d+\/$/);
  });

  test('should redirect unauthenticated guest to home page when accessing protected dashboard route', async ({ page }) => {
    page.on('pageerror', err => console.log('Browser Page Error (dashboard):', err.message));
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/http:\/\/localhost:\d+\/$/);
  });

  test('should render Access Denied page when a normal student attempts to access admin route', async ({ page }) => {
    // 1. Log in as a normal student
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);

    // 2. Try loading /admin route directly
    await page.goto('/admin');

    // 3. Expect "Access Denied" text to be visible
    const deniedHeader = page.locator('h3:has-text("Access Denied")');
    await expect(deniedHeader).toBeVisible();

    const deniedText = page.locator('text=Your current account does not have sufficient permissions');
    await expect(deniedText).toBeVisible();
  });
});
