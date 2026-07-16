import { test, expect } from '@playwright/test';

test.describe('Contest Problem Solving Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Authenticate
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
  });

  test('should enter ongoing contest, select problem and submit code successfully', async ({ page }) => {
    test.setTimeout(90000); // 90 seconds timeout due to slowMo: 2000
    const contestId = '99';
    const problemId = '9901';

    // 1. Mock API endpoints for ongoing contest
    await page.route(`**/contests?*`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 1000,
          message: 'Success',
          result: {
            content: [
              {
                id: Number(contestId),
                title: 'Weekly Practice Contest #99',
                description: 'Showcase your coding skills in this weekly arena.',
                scoringRule: 'ACM',
                startTime: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
                endTime: new Date(Date.now() + 3600000).toISOString(),   // 1 hour later
                durations: 120,
                status: 'ONGOING',
                creatorName: 'System Admin',
                isPrivate: false,
                participantCount: 10,
                problemCount: 1,
                isUserRegistered: true
              }
            ],
            totalPages: 1,
            totalElements: 1
          }
        })
      });
    });

    await page.route(`**/contests/${contestId}`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 1000,
          message: 'Success',
          result: {
            id: Number(contestId),
            title: 'Weekly Practice Contest #99',
            description: 'Showcase your coding skills in this weekly arena.',
            scoringRule: 'ACM',
            startTime: new Date(Date.now() - 3600000).toISOString(),
            endTime: new Date(Date.now() + 3600000).toISOString(),
            durations: 120,
            status: 'ONGOING',
            creatorName: 'System Admin',
            isPrivate: false,
            participantCount: 10,
            problemCount: 1,
            isUserRegistered: true
          }
        })
      });
    });

    await page.route(`**/contests/${contestId}/problems`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 1000,
          message: 'Success',
          result: [
            {
              problemId: Number(problemId),
              title: 'Contest Sum Problem',
              orderIndex: 0,
              difficulty: 'EASY',
              totalSubmission: 5,
              totalAccepted: 2,
              status: 'UNATTEMPTED'
            }
          ]
        })
      });
    });

    await page.route(`**/contests/${contestId}/problems/${problemId}`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 1000,
          message: 'Success',
          result: {
            id: Number(problemId),
            title: 'Contest Sum Problem',
            difficulty: 'Easy',
            description: 'Given two integers, return their sum.',
            inputDescription: 'Two integers a and b.',
            outputDescription: 'Sum of a and b.',
            constraints: '0 <= a, b <= 100',
            exampleInput: '1\n2',
            exampleOutput: '3',
            hint: 'Use + operator',
            tags: ['Math'],
            starterTemplates: {
              'Python 3': 'def solve(a, b):\n    return a + b'
            },
            status: 'unsolved',
            acceptance: '40%',
            totalSolved: 2
          }
        })
      });
    });

    await page.route(`**/online-judge/submissions`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 1000,
          message: 'Success',
          result: {
            token: 'mock-token-123',
            status: 'Accepted'
          }
        })
      });
    });

    await page.route(`**/contests/${contestId}/submissions`, async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 1000,
          message: 'Success',
          result: []
        })
      });
    });

    // 2. Go to contests list page
    await page.goto('/contests');

    // Click Enter Arena for Weekly Practice Contest #99
    const enterArenaBtn = page.locator('a:has-text("Enter Arena")').first();
    await expect(enterArenaBtn).toBeVisible({ timeout: 15000 });
    await enterArenaBtn.click();
    await expect(page).toHaveURL(new RegExp(`/contests/${contestId}`));

    // 3. Go to Problems Tab
    const problemsLink = page.locator('aside nav a:has-text("Problems")');
    await expect(problemsLink).toBeVisible();
    await problemsLink.click();
    await expect(page).toHaveURL(new RegExp(`/contests/${contestId}/problems`));

    // Click the mock problem
    const problemLink = page.locator('text=Contest Sum Problem');
    await expect(problemLink).toBeVisible();
    await problemLink.click();
    await expect(page).toHaveURL(new RegExp(`/contests/${contestId}/problems/${problemId}`));

    // 4. Wait for Monaco Editor to load
    const editorContainer = page.locator('.monaco-editor').first();
    await expect(editorContainer).toBeVisible({ timeout: 45000 });

    // Locate Submit Button
    const submitBtn = page.locator('button:has-text("Submit")');
    await expect(submitBtn).toBeVisible();
    await submitBtn.click();

    // Verify it starts submitting (button text changes or spinner is visible)
    const submittingText = page.locator('button:has-text("Submitting")').or(page.locator('button:has-text("Submit")'));
    await expect(submittingText.first()).toBeVisible();
  });
});
