import { test, expect } from '@playwright/test';

test.describe('AI Learning Assistant / Tutor Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Intercept visualizer API requests and mock them for fast, reliable testing
    await page.route('**/api/v1/ai/visualizer/**', async (route) => {
      const url = route.request().url();
      if (url.includes('/generate')) {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            status: 200,
            code: 2000,
            message: 'Success (from cache)',
            result: {
              detectedAlgorithm: 'Binary Search',
              timeComplexity: 'O(log n)',
              htmlContent: '<html><body><h1>Binary Search Simulation</h1></body></html>',
              fromCache: true
            }
          })
        });
      } else {
        // Cache check: return null to force simulator button to appear
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            status: 200,
            code: 2000,
            message: 'No cache found',
            result: null
          })
        });
      }
    });

    // Authenticate first
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    // Support redirect to either student dashboard or instructor panel (since user1 is registered as instructor)
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
  });

  test('should ask AI Tutor to simulate algorithm and time complexity', async ({ page }) => {
    // Go to solve problem page 1
    await page.goto('/problems/1');

    // Wait for editor container to be visible to ensure page is loaded
    await expect(page.locator('.monaco-editor').first()).toBeVisible({ timeout: 45000 });

    // Locate the AI Tutor tab button
    const aiTutorTab = page.locator('button:has-text("AI Tutor")');
    await expect(aiTutorTab).toBeVisible();
    await aiTutorTab.click();

    // Verify AI Tutor Panel is shown
    const customInputLabel = page.locator('label:has-text("Custom Input")');
    await expect(customInputLabel).toBeVisible();

    // Check if the simulation button is present
    const askAiBtn = page.locator('button:has-text("Ask AI")');
    await expect(askAiBtn).toBeVisible();
    
    // Trigger AI generation (which is mocked above)
    await askAiBtn.click();

    // Verify visualizer iframe and badges become visible instantly due to mocking
    const complexityBadge = page.locator('text=⏱ O-Big:');
    const algorithmBadge = page.locator('text=🚀 Algorithm:');
    const visualizerIframe = page.locator('iframe[title="AI Visualizer"]');

    await expect(visualizerIframe).toBeVisible({ timeout: 15000 });
    await expect(complexityBadge).toBeVisible();
    await expect(algorithmBadge).toBeVisible();

    const algoName = await algorithmBadge.innerText();
    console.log(`AI Tutor detected algorithm: ${algoName}`);
  });
});
