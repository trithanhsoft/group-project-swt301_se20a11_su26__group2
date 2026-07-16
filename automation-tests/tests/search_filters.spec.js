import { test, expect } from '@playwright/test';

test.describe('Advanced Catalog Search and Filters Flow', () => {

  test('should filter problems by difficulty correctly', async ({ page }) => {
    // 1. Go to problems page
    await page.goto('/problems');

    // 2. Select Easy difficulty option
    // The second select element is for difficulty filter
    const difficultySelect = page.locator('select').nth(1);
    await expect(difficultySelect).toBeVisible();
    await difficultySelect.selectOption('easy');

    // 3. Verify that only Easy problems are rendered in the table rows
    // Wait for the UI state to update after option selection
    await page.waitForTimeout(500);

    const difficultyBadges = page.locator('tbody tr td span.text-brand-green');
    const badgeCount = await difficultyBadges.count();
    
    // Check that we have elements and all of them are Easy difficulty
    expect(badgeCount).toBeGreaterThan(0);
    
    for (let i = 0; i < badgeCount; i++) {
      const text = await difficultyBadges.nth(i).innerText();
      expect(text.trim()).toBe('Easy');
    }
  });

  test('should search for problems using the search input', async ({ page }) => {
    // 1. Go to problems page
    await page.goto('/problems');

    // 2. Type "Sum" in the search input
    const searchInput = page.locator('input[placeholder="Search problems, topics..."]');
    await expect(searchInput).toBeVisible();
    await searchInput.fill('Sum');

    // Submit or trigger search
    await page.press('input[placeholder="Search problems, topics..."]', 'Enter');
    await page.waitForTimeout(500);

    // 3. Assert that all matching problem titles contain the word "Sum"
    const problemLinks = page.locator('tbody tr td a');
    const count = await problemLinks.count();
    
    // We expect some matches (e.g. "Two Sum")
    expect(count).toBeGreaterThan(0);

    for (let i = 0; i < count; i++) {
      const title = await problemLinks.nth(i).innerText();
      expect(title.toLowerCase()).toContain('sum');
    }
  });
});
