import { test, expect } from '@playwright/test';

test.describe('Student Profile Management Flow', () => {

  test.beforeEach(async ({ page }) => {
    // Log in
    await page.goto('/login');
    await page.fill('input[name="username"]', 'user1');
    await page.fill('input[name="password"]', 'user1');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/\/dashboard|\/instructor/);
  });

  test('should navigate to profile settings, update Display Name, and verify header update', async ({ page }) => {
    // 1. Go to dashboard and navigate to my-profile tab
    await page.goto('/dashboard#my-profile');

    // 2. Locate the Display Name input field
    const displayNameInput = page.locator('input[placeholder="Enter display name"]');
    await expect(displayNameInput).toBeVisible();

    // 3. Clear and enter a new unique display name
    const newName = `Playwright User - ${Date.now()}`;
    await displayNameInput.fill(newName);

    // 4. Click Save Changes
    const saveBtn = page.locator('button:has-text("Save Changes")');
    await expect(saveBtn).toBeVisible();
    await saveBtn.click();

    // 5. Verify success alert message
    const successAlert = page.locator('text=Profile details updated successfully!');
    await expect(successAlert).toBeVisible();

    // 6. Verify header displays updated name
    // Since header shows the user avatar dropdown or name, let's hover the avatar group to see if name matches
    const avatarGroup = page.locator('header div.group').first();
    await avatarGroup.hover();
    
    // Check if the new name is rendered in the user profile menu/popover
    const profileName = page.locator(`text=${newName}`);
    await expect(profileName.first()).toBeVisible();
  });
});
