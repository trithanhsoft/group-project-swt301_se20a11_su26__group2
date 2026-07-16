import { test, expect } from '@playwright/test';

test.describe('Wallet Deposit & Course Checkout Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Login first
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
  });

  test('should mock wallet balance and complete checkout successfully', async ({ page }) => {
    test.setTimeout(90000); // 90 seconds timeout due to slowMo: 2000
    // 1. Clean cart first
    await page.goto('/shopping-cart');
    
    const removeAllBtn = page.locator('button:has-text("Remove All")');
    if (await removeAllBtn.count() > 0 && await removeAllBtn.isVisible()) {
      await removeAllBtn.click();
      await expect(page.locator('text=Your cart is empty')).toBeVisible();
    }

    // 2. Go to courses and add a paid course to cart
    await page.goto('/courses');
    const addToCartBtn = page.locator('button[title="Add to cart"]').first();
    await expect(addToCartBtn).toBeVisible({ timeout: 10000 });
    await addToCartBtn.click();

    // Verify cart count badge
    const cartBadge = page.locator('header a[href="/shopping-cart"] span.bg-primary');
    await expect(cartBadge).toBeVisible();

    // 3. Set up mock routes for balance and checkout before going to cart
    await page.route('**/payment/balance', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 1000, message: 'Success', result: 2000000 })
      });
    });

    await page.route('**/orders/checkout', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ code: 1000, message: 'Success', result: true })
      });
    });

    // 4. Go to shopping cart page
    await page.goto('/shopping-cart');

    // Verify mock wallet balance shows up (format: 2.000.000đ or similar depending on locales, let's search text "2.000.000")
    const walletBalanceText = page.locator('text=2.000.000');
    await expect(walletBalanceText.first()).toBeVisible({ timeout: 10000 });

    // 5. Perform checkout
    const checkoutBtn = page.locator('button:has-text("Checkout")');
    await expect(checkoutBtn).toBeVisible();
    await checkoutBtn.click();

    // Verify success feedback message and redirection to dashboard
    const successMsg = page.locator('text=Checkout successful!');
    await expect(successMsg).toBeVisible({ timeout: 10000 });
    await expect(page).toHaveURL(/\/dashboard/, { timeout: 15000 });
  });
});
