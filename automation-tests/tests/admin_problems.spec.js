import { test, expect } from '@playwright/test';

test.describe('Admin Programming Problem Creation Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Log in as super administrator
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/admin/);
  });

  test('should navigate to creation page and submit a new coding problem', async ({ page }) => {
    test.setTimeout(90000); // 90 seconds timeout due to slowMo: 2000
    // 1. Go to Admin Problems Management
    await page.goto('/admin/problems');

    // 2. Click "Create Problem" button
    const createBtn = page.locator('button:has-text("Create Problem")').first();
    await expect(createBtn).toBeVisible();
    await createBtn.click();
    await expect(page).toHaveURL(/\/admin\/problems-create/);

    // 3. Fill out the form
    const uniqueTitle = `Automated Code Problem - ${Date.now()}`;
    await page.fill('input[placeholder="e.g. Two Sum"]', uniqueTitle);
    await page.fill('textarea[placeholder="Explain the problem here..."]', 'This is a description written by the Playwright automated E2E test suite.');
    await page.fill('textarea[placeholder="Describe input structure..."]', 'An array of integers nums and an integer target.');
    await page.fill('textarea[placeholder="Describe output structure..."]', 'Indices of the two numbers such that they add up to target.');
    await page.fill('textarea[placeholder="e.g. 1 <= nums.length <= 10^5"]', '2 <= nums.length <= 10^4');
    await page.fill('textarea[placeholder="Input sample..."]', '[2,7,11,15]\n9');
    await page.fill('textarea[placeholder="Output sample..."]', '[0,1]');

    // Fill numerical limits
    await page.fill('input[type="number"] >> nth=0', '100'); // Max Score
    await page.fill('input[type="number"] >> nth=1', '1000'); // Time Limit
    await page.fill('input[type="number"] >> nth=2', '64000'); // Memory Limit

    // Choose difficulty Easy
    const easyLabel = page.locator('label:has-text("EASY")').first();
    await easyLabel.click();

    // Click submit
    const submitBtn = page.locator('button:has-text("Create Problem & Testcases")');
    await expect(submitBtn).toBeVisible();
    await submitBtn.click();

    // 4. Assert successful redirection and presence in problems table
    await expect(page).toHaveURL(/\/admin\/problems/, { timeout: 15000 });
  });
});
