import { loginAs, expect, test } from './fixtures/fixtures';

test.describe('User Profile Menu', () => {
  test('should display user profile menu and toggle it off', async ({ page, loginAs, browserName }) => {
    test.skip(browserName !== 'chromium', 'This test only runs on Chromium');
    
    // Navigate to the home page
    await page.goto('http://localhost:5173/');
    await page.waitForLoadState('networkidle');
    await loginAs('employee', page);

    // Open user menu (desktop)
    const avatarButton = page.getByTestId('user-menu-toggle');
    await expect(avatarButton).toBeVisible();
    await avatarButton.click();

    // Verify menu is open
    const userMenu = page.getByTestId('user-menu-panel');
    await expect(userMenu).toBeVisible();

    // Click again to close
    await avatarButton.click();
    await expect(userMenu).toBeHidden();
  });
});


