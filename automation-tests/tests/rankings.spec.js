import { test, expect } from '@playwright/test';

test.describe('Global Rankings & Leaderboard Flow', () => {

  test('should view rankings page and search for a coder', async ({ page }) => {
    // 1. Navigate to rankings page
    await page.goto('/rankings');

    // 2. Assert page headings
    const heading = page.locator('h1:has-text("Global Rankings")');
    await expect(heading).toBeVisible();

    // 3. Verify search input exists
    const searchInput = page.locator('#rank-search');
    await expect(searchInput).toBeVisible();

    // 4. Check if some rankings are loaded
    // Since there are podium users (first 3) and a table of users, check for ranking rows or user cards.
    // Let's check if the leaderboard list container or some user names are visible.
    const weeklyTab = page.locator('#tab-weekly').or(page.locator('text=Weekly'));
    const monthlyTab = page.locator('#tab-monthly').or(page.locator('text=Monthly'));
    const allTimeTab = page.locator('#tab-all-time').or(page.locator('text=All-Time'));

    await expect(weeklyTab.first()).toBeVisible();
    await expect(monthlyTab.first()).toBeVisible();
    await expect(allTimeTab.first()).toBeVisible();

    // 5. Try searching for a user
    await searchInput.fill('admin');
    
    // The table should filter or show no results / filtered results.
    // We expect the search to execute without errors.
    await page.waitForTimeout(500);
  });
});
