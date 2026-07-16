import { test, expect } from '@playwright/test';

test.describe('Account Registration Flow', () => {

  test('should show validation error when passwords do not match', async ({ page }) => {
    await page.goto('/register');

    // Fill form with mismatched passwords
    await page.fill('input[name="fullname"]', 'Mismatched User');
    await page.fill('input[name="username"]', 'mismatch_user');
    await page.fill('input[name="email"]', 'mismatch@example.com');
    await page.fill('input[name="password"]', 'password123');
    await page.fill('input[name="confirm-password"]', 'different123');
    
    // Check agree terms
    await page.check('input[name="terms"]');

    // Click submit
    await page.click('button[type="submit"]');

    // Verify error alert
    const errorAlert = page.locator('div.bg-red-50');
    await expect(errorAlert).toBeVisible();
    await expect(errorAlert).toContainText('Password and confirm password do not match!');
  });

  test('should register a new account successfully with unique username', async ({ page }) => {
    await page.goto('/register');

    const uniqueId = Date.now();
    const username = `user_${uniqueId}`;
    const email = `user_${uniqueId}@example.com`;

    // Fill valid details
    await page.fill('input[name="fullname"]', `Fullname ${uniqueId}`);
    await page.fill('input[name="username"]', username);
    await page.fill('input[name="email"]', email);
    await page.fill('input[name="password"]', 'password123');
    await page.fill('input[name="confirm-password"]', 'password123');
    
    // Check agree terms
    await page.check('input[name="terms"]');

    // Click submit
    await page.click('button[type="submit"]');

    // Assert redirection to dashboard
    await expect(page).toHaveURL(/\/dashboard/);
  });
});
