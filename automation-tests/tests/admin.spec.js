import { test, expect } from '@playwright/test';

test.describe('Admin Control Panel & System Management Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Log in as super administrator
    await page.goto('/login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'admin');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/admin/);
  });

  test('should navigate through admin dashboard panels and verify grids', async ({ page }) => {
    // 1. Verify general stats in Dashboard overview
    const activeUsersCard = page.locator('text=Active Users');
    const totalCoursesCard = page.locator('text=Total Courses');
    await expect(activeUsersCard.first()).toBeVisible();
    await expect(totalCoursesCard.first()).toBeVisible();

    // 2. Navigate to Courses Management
    const coursesLink = page.locator('aside nav a[href="/admin/courses"]').first();
    await expect(coursesLink).toBeVisible();
    await coursesLink.click();
    await expect(page).toHaveURL(/\/admin\/courses/);
    
    const courseManageHeader = page.locator('h1:has-text("Courses Management")').or(page.locator('h1:has-text("Course")'));
    await expect(courseManageHeader.first()).toBeVisible();

    // 3. Navigate to Problems Management
    const problemsLink = page.locator('aside nav a[href="/admin/problems"]').first();
    await expect(problemsLink).toBeVisible();
    await problemsLink.click();
    await expect(page).toHaveURL(/\/admin\/problems/);

    const problemBankHeader = page.locator('h1:has-text("Programming Problems")').or(page.locator('h1:has-text("Problem")'));
    await expect(problemBankHeader.first()).toBeVisible();

    // 4. Navigate to Contest Management
    const contestLink = page.locator('aside nav a[href="/admin/contests"]').first();
    await expect(contestLink).toBeVisible();
    await contestLink.click();
    await expect(page).toHaveURL(/\/admin\/contests/);

    const contestHeader = page.locator('h1:has-text("Competitive Contests")').or(page.locator('h1:has-text("Contest")'));
    await expect(contestHeader.first()).toBeVisible();

    // 5. Navigate to Instructor Management
    const instructorLink = page.locator('aside nav a[href="/admin/instructors"]').first();
    await expect(instructorLink).toBeVisible();
    await instructorLink.click();
    await expect(page).toHaveURL(/\/admin\/instructors/);

    const instructorHeader = page.locator('h1:has-text("Platform Instructors")').or(page.locator('h1:has-text("Instructor")'));
    await expect(instructorHeader.first()).toBeVisible();

    // 6. Navigate to Users Directory
    const usersLink = page.locator('aside nav a[href="/admin/users"]').first();
    await expect(usersLink).toBeVisible();
    await usersLink.click();
    await expect(page).toHaveURL(/\/admin\/users/);

    const usersHeader = page.locator('h1:has-text("Platform Users")').or(page.locator('h1:has-text("User")'));
    await expect(usersHeader.first()).toBeVisible();

    // 7. Navigate to Financial Dashboard
    const financialLink = page.locator('aside nav a[href="/admin/financial"]').first();
    await expect(financialLink).toBeVisible();
    await financialLink.click();
    await expect(page).toHaveURL(/\/admin\/financial/);

    // Using exact title from AdminDashboard.tsx configurations (Financial Statistics or Financial Overview)
    const financialHeader = page.locator('h1:has-text("Financial Statistics")').or(page.locator('h1:has-text("Financial Overview")'));
    await expect(financialHeader.first()).toBeVisible();
  });
});
