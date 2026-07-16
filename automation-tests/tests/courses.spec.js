import { test, expect } from '@playwright/test';

test.describe('Courses & Shopping Cart Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Login first
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
  });

  test('should browse courses catalog and view course detail', async ({ page }) => {
    // Go to courses page
    await page.goto('/courses');

    // Verify course catalog page header
    const header = page.locator('h1:has-text("Courses")');
    await expect(header).toBeVisible();

    // Check if courses are rendered
    const firstCourseCard = page.locator('a.course-card').first();
    await expect(firstCourseCard).toBeVisible();

    // Click the first course card to view detail
    await firstCourseCard.click();

    // Should redirect to course detail page
    await expect(page).toHaveURL(/\/courses\/\d+/);

    // Verify course action button/link is visible (can be Add to Cart, Go to Cart, or Continue Learning)
    const actionBtn = page.locator('button:has-text("Add to Cart")')
      .or(page.locator('button:has-text("Continue Learning")'))
      .or(page.locator('a:has-text("Go to Cart")'))
      .first();
    
    await expect(actionBtn).toBeVisible({ timeout: 10000 });
  });

  test('should add a paid course to cart from list', async ({ page }) => {
    // Clean state: clear shopping cart first to ensure we can add courses
    await page.goto('/shopping-cart');
    
    const removeAllBtn = page.locator('button:has-text("Remove All")');
    if (await removeAllBtn.count() > 0 && await removeAllBtn.isVisible()) {
      await removeAllBtn.click();
      // Wait for cart empty message
      await expect(page.locator('text=Your cart is empty')).toBeVisible();
    }

    // Go to courses page
    await page.goto('/courses');

    // Find the first course card that has an "Add to Cart" button
    const addToCartBtn = page.locator('button[title="Add to cart"]').first();
    await expect(addToCartBtn).toBeVisible({ timeout: 10000 });

    // Click Add to Cart
    await addToCartBtn.click();

    // Verify the header shopping cart badge counter shows a count greater than 0
    // We use the specific class .bg-primary of the count badge to avoid strict-mode duplication with the cart icon
    const cartBadge = page.locator('header a[href="/shopping-cart"] span.bg-primary');
    await expect(cartBadge).toBeVisible();
    
    const countText = await cartBadge.innerText();
    const count = parseInt(countText, 10);
    console.log(`Cart badge count: ${count}`);
    expect(count).toBeGreaterThan(0);
  });
});
