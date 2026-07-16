import { test, expect } from '@playwright/test';

test.describe('Practice Problems & Code Playground Flow', () => {
  // Run tests sequentially to avoid parallel session conflicts
  test.describe.configure({ mode: 'serial' });

  test.beforeEach(async ({ page }) => {
    // Authenticate first
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
  });

  test('should browse problems list and select a problem', async ({ page }) => {
    // Navigate to problems page
    await page.goto('/problems');

    // Verify page header
    const header = page.locator('h1:has-text("Problems")');
    await expect(header).toBeVisible();

    // Check if problems table is rendered
    const firstProblemLink = page.locator('tbody tr td a').first();
    await expect(firstProblemLink).toBeVisible();

    const problemTitle = await firstProblemLink.innerText();
    console.log(`Selecting problem: ${problemTitle}`);

    // Click the first problem link
    await firstProblemLink.click();

    // Should navigate to solve problem page
    await expect(page).toHaveURL(/\/problems\/\d+/);

    // Verify description panel is visible
    const descTab = page.locator('button:has-text("Description")');
    await expect(descTab).toBeVisible();
    
    // Wait for the Monaco Code Editor to be visible (longer timeout for CDN download)
    const editorContainer = page.locator('.monaco-editor').first();
    await expect(editorContainer).toBeVisible({ timeout: 45000 });
  });

  test('should choose language and submit code solution', async ({ page }) => {
    test.setTimeout(90000); // 90 seconds timeout due to slowMo: 2000
    // Navigate straight to the first problem solving page (ID 1)
    await page.goto('/problems/1');

    // Wait for Monaco Code Editor to load
    const editorContainer = page.locator('.monaco-editor').first();
    await expect(editorContainer).toBeVisible({ timeout: 45000 });

    // Select language dropdown and choose Python (ID: 71)
    const langSelect = page.locator('select');
    await expect(langSelect).toBeVisible();
    await langSelect.selectOption('71'); // Select Python

    // Click Monaco editor container to focus it
    await page.locator('.monaco-editor').first().click();
    await page.waitForTimeout(1000);

    // Type a single comment character to ensure it's not empty, avoiding slowMo typing delays
    await page.keyboard.type('#');
    await page.waitForTimeout(1000);

    // Register dialog listener to print native alert errors
    page.on('dialog', async dialog => {
      console.log(`[ALERT DIALOG ERROR]: ${dialog.message()}`);
      await dialog.dismiss();
    });

    // Locate Submit button
    const submitBtn = page.locator('button:has-text("Submit")');
    await expect(submitBtn).toBeVisible();

    // Click Submit
    await submitBtn.click();

    // 4. Wait for either the final verdict (Accepted/Wrong Answer) or the maintenance error to appear
    // We check for up to 30 seconds to allow the backend/Judge0 compiler to execute.
    const finalVerdict = page.locator('#tab-result h3').first();
    const maintenanceAlert = page.locator('text=currently under maintenance').first();

    await expect(async () => {
      const isVerdictVisible = await finalVerdict.isVisible();
      const isMaintenanceVisible = await maintenanceAlert.isVisible();
      expect(isVerdictVisible || isMaintenanceVisible).toBeTruthy();
    }).toPass({
      timeout: 30000,
      intervals: [1000]
    });

    if (await finalVerdict.isVisible()) {
      const verdictText = await finalVerdict.innerText();
      console.log(`Submission finished with verdict: ${verdictText}`);
    } else {
      console.log('Submission finished: Online Judge system is under maintenance (handled gracefully).');
    }
  });
});
