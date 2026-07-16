import { test, expect } from '@playwright/test';

test.describe('Instructor Dashboard & Course Management Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Authenticate student user1 first
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);

    // Apply to become an instructor or navigate directly if already approved
    await page.goto('/apply-instructor');
    
    const approvedDashboardBtn = page.locator('button:has-text("Go to Instructor Dashboard")');
    const registerBtn = page.locator('button:has-text("Register as Instructor")');

    if (await approvedDashboardBtn.count() > 0 && await approvedDashboardBtn.isVisible()) {
      // User is already an approved instructor
      console.log('User is already an approved instructor. Navigating to panel.');
      await approvedDashboardBtn.click();
    } else if (await registerBtn.count() > 0 && await registerBtn.isVisible()) {
      // User needs to submit the application
      console.log('Submitting application to become instructor...');
      await page.fill('input[name="fullName"]', 'Instructor User One');
      await page.selectOption('select[name="major"]', 'Full Stack Developer');
      await page.fill('textarea[name="bio"]', 'Hello! I am a senior software developer with 10 years of industry experience. I love teaching web technologies like React, Node.js and Java Spring Boot.');
      await registerBtn.click();

      // Verify success card and go to dashboard
      const successDashboardBtn = page.locator('button:has-text("Go to Instructor Dashboard")');
      await expect(successDashboardBtn).toBeVisible({ timeout: 15000 });
      await successDashboardBtn.click();
    } else {
      // Direct navigation
      await page.goto('/instructor');
    }

    // Verify we are at the instructor panel
    await expect(page).toHaveURL(/\/instructor/);
  });

  test('should navigate through dashboard statistics, my courses and revenue tabs', async ({ page }) => {
    // 1. Verify General Dashboard Overview (hash: #dashboard)
    await page.goto('/instructor#dashboard');
    
    // Using specific first() resolution for the KPI labels to prevent strict mode errors with other helper descriptions
    const totalStudentsCard = page.locator('span:has-text("Total Students")').first();
    const activeCoursesCard = page.locator('span:has-text("Active Courses")').first();
    const walletCard = page.locator('span:has-text("Total Revenue")').or(page.locator('text=Revenue Wallet')).first();
    
    await expect(totalStudentsCard).toBeVisible();
    await expect(activeCoursesCard).toBeVisible();
    await expect(walletCard).toBeVisible();

    // 2. Verify My Authored Courses Tab (hash: #my-courses)
    await page.goto('/instructor#my-courses');
    const coursesHeader = page.locator('h1:has-text("My Authored Courses")');
    await expect(coursesHeader).toBeVisible();

    // Verify course creation modal interaction
    const createCourseBtn = page.locator('button:has-text("Create Course")');
    await expect(createCourseBtn).toBeVisible();
    await createCourseBtn.click();

    // Verify Create Course Modal opens
    const modalHeader = page.locator('h3:has-text("Create New Learning Course")');
    await expect(modalHeader).toBeVisible();

    // Fill out Title and select Category
    await page.fill('input[placeholder="e.g. Mastering Full-Stack React & Node.js"]', 'Automated Playwright Course');
    
    // Select category dropdown triggers
    const categorySelector = page.locator('div:has-text("Select one or more topics...")').first();
    await expect(categorySelector).toBeVisible();
    await categorySelector.click();
    
    // Select the first category checkbox from dropdown
    const firstCategoryCheckbox = page.locator('div[id="modal-create-course"] div div div:has-text("Database")').or(page.locator('div[id="modal-create-course"] div div div:has-text("Java")')).first();
    if (await firstCategoryCheckbox.count() > 0) {
      await firstCategoryCheckbox.click();
    }

    // Close Modal without submitting
    const closeModalBtn = page.locator('button:has-text("close")').or(page.locator('#modal-create-course button span:has-text("close")')).first();
    await closeModalBtn.click();
    await expect(modalHeader).toBeHidden();

    // 3. Verify Revenue Tab (hash: #revenue)
    await page.goto('/instructor#revenue');
    
    // Exact title from InstructorDashboard.tsx: "Instructor Revenue" and badge "Revenue Analytics"
    const revenueHeader = page.locator('h1:has-text("Instructor Revenue")');
    await expect(revenueHeader).toBeVisible();
    
    const earningsOverviewText = page.locator('h3:has-text("Earnings Overview")');
    await expect(earningsOverviewText).toBeVisible();
  });
});
