import { test, expect } from '@playwright/test';

test.describe('Legal and Contact Pages Flow', () => {

  test('should navigate to contact us page and verify elements', async ({ page }) => {
    await page.goto('/contact');
    const header = page.locator('h1:has-text("Contact")').or(page.locator('h1:has-text("Support")'));
    await expect(header.first()).toBeVisible();
    
    // Check if form elements exist
    await expect(page.locator('input[placeholder="Enter your name"]')).toBeVisible();
    await expect(page.locator('input[placeholder="name@example.com"]')).toBeVisible();
    await expect(page.locator('textarea[placeholder="How can we help you? Please describe in detail..."]')).toBeVisible();
  });

  test('should navigate to terms of service page and verify', async ({ page }) => {
    await page.goto('/terms');
    const header = page.locator('h1:has-text("Terms of Service")').or(page.locator('h1:has-text("Terms and Conditions")'));
    await expect(header).toBeVisible();
  });

  test('should navigate to privacy policy page and verify', async ({ page }) => {
    await page.goto('/privacy');
    const header = page.locator('h1:has-text("Privacy Policy")');
    await expect(header).toBeVisible();
  });

  test('should navigate to cookies policy page and verify', async ({ page }) => {
    await page.goto('/cookies');
    const header = page.locator('h1:has-text("Cookie Policy")').or(page.locator('h1:has-text("Cookies Policy")'));
    await expect(header).toBeVisible();
  });
});
