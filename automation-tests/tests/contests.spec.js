import { test, expect } from '@playwright/test';

test.describe('Competitive Programming Contest Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Authenticate first
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
  });

  test('should browse contests list and enter a contest arena', async ({ page }) => {
    // Go to contests page
    await page.goto('/contests');

    // Verify page header
    const header = page.locator('h1:has-text("Contests")').or(page.locator('h1:has-text("Practice")'));
    await expect(header.first()).toBeVisible();

    // Wait for the loading spinner to disappear (handles debounce and backend fetch delay)
    const spinner = page.locator('span.material-symbols-outlined:has-text("sync")');
    if (await spinner.count() > 0) {
      await expect(spinner).toBeHidden({ timeout: 15000 });
    }

    // Check if contests list is rendered and click "Enter Arena"
    const enterArenaBtn = page.locator('a:has-text("Enter Arena")').first();
    await expect(enterArenaBtn).toBeVisible({ timeout: 15000 });
    await enterArenaBtn.click();

    // Should redirect to contest overview page
    await expect(page).toHaveURL(/\/contests\/\d+/);

    // Verify contest sidebar elements (like Contest header or Timer)
    const contestHeader = page.locator('aside h2:has-text("Contest #")');
    await expect(contestHeader).toBeVisible();

    // Check registration state
    const registerBtn = page.locator('button:has-text("Register Now")');
    const registeredBadge = page.locator('div.bg-green-50:has-text("Registered")');

    if (await registerBtn.count() > 0 && await registerBtn.isVisible()) {
      console.log('Registering user for this contest...');
      // Click register
      await registerBtn.click();
      // Wait for registered indicator to show up
      await expect(registeredBadge).toBeVisible({ timeout: 15000 });
    } else {
      console.log('User is already registered for this contest or registration is closed.');
    }

    // Attempt to navigate contest content if it is ongoing/ended
    const problemsLink = page.locator('aside nav a:has-text("Problems")');
    const rankingsLink = page.locator('aside nav a:has-text("Rankings")');

    if (await problemsLink.count() > 0 && await problemsLink.isVisible()) {
      // Click Problems link
      await problemsLink.click();
      await expect(page).toHaveURL(/\/contests\/\d+\/problems/);

      // Go back to overview and click rankings
      await rankingsLink.click();
      await expect(page).toHaveURL(/\/contests\/\d+\/ranking/);
      
      const rankingTable = page.locator('table');
      await expect(rankingTable).toBeVisible();
    } else {
      console.log('Contest content is not accessible yet (upcoming contest). Test passed on registration.');
    }
  });
});
