import { test, expect } from '@playwright/test';

test.describe('Instructor Course Builder Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Log in as user1 (already an approved instructor)
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
    
    // Navigate to instructor panel and activate My Courses tab via URL hash
    await page.goto('/instructor#my-courses');
    await expect(page).toHaveURL(/\/instructor#my-courses/);
  });

  test('should open creation modal, fill inputs, and save a draft course', async ({ page }) => {
    test.setTimeout(90000); // 90 seconds timeout due to slowMo: 2000
    // 1. Click "Create Course" button
    const createBtn = page.locator('button:has-text("Create Course")').first();
    await expect(createBtn).toBeVisible();
    await createBtn.click();

    // 2. Mock uploadMedia API call to return a mock CDN URL
    await page.route('**/instructor/upload', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          code: 1000,
          message: 'Success',
          result: { secureUrl: 'http://localhost:5173/LOGO.png' }
        })
      });
    });

    // Mock createCourse POST request
    await page.route('**/instructor/courses', async (route) => {
      if (route.request().method() === 'POST') {
        const payload = JSON.parse(route.request().postData() || '{}');
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            code: 1000,
            message: 'Success',
            result: {
              id: 'mock-course-123',
              title: payload.title || 'Automated React Course',
              topic: payload.topic || 'Lập Trình Cơ Bản',
              price: String(payload.price || 0),
              studentsCount: 0,
              rating: 5.0,
              reviewsCount: 0,
              status: 'DRAFT',
              icon: 'code',
              gradient: 'from-blue-600 to-indigo-700',
              description: payload.description || 'Short desc',
              thumbnailUrl: payload.thumbnailUrl || 'http://localhost:5173/LOGO.png'
            }
          })
        });
      } else {
        await route.continue();
      }
    });

    const modal = page.locator('div:has(h3:has-text("Create New Learning Course"))');

    // 3. Fill out the course title
    const uniqueTitle = `Automated React Course - ${Date.now()}`;
    await modal.locator('input[placeholder="e.g. Mastering Full-Stack React & Node.js"]').fill(uniqueTitle);

    // Select course topics/categories
    await modal.locator('text=Select one or more topics...').click();
    // Click the first category option in the dropdown list
    const firstTopic = modal.locator('div.max-h-60 div.cursor-pointer').first();
    await expect(firstTopic).toBeVisible();
    await firstTopic.click();
    // Close category dropdown by clicking heading or label
    await modal.locator('label:has-text("Course Topic")').click();

    // Fill Price for Paid Course
    await modal.locator('input[type="number"]').fill('100000');

    // Upload course thumbnail using Playwright setInputFiles
    await modal.locator('input[type="file"]').setInputFiles({
      name: 'thumbnail.png',
      mimeType: 'image/png',
      buffer: Buffer.from('fake-image-content')
    });

    // Fill descriptions
    await modal.locator('textarea[placeholder="Build scalable, production-ready web applications from scratch..."]').fill('This is an automated course description created by Playwright E2E tests.');
    await modal.locator('textarea[placeholder*="The course is a transformative journey"]').fill('Learn intermediate to advanced topics about front-end and back-end integration.');

    // Save Course
    const saveBtn = modal.locator('button:has-text("Submit Course")').first();
    await expect(saveBtn).toBeVisible();
    await saveBtn.click();

    // 4. Assert success notification toast
    await expect(page.locator('text=successfully created').first()).toBeVisible({ timeout: 15000 });
  });
});
