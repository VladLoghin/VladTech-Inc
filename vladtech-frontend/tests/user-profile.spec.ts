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
  });

  test('should edit name', async ({ page, loginAs, browserName }) => {
    test.skip(browserName !== 'chromium', 'This test only runs on Chromium');
    
    // Navigate to the home page and log in via fixture
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

    // Edit name flow
    await userMenu.getByTestId('edit-name-button').click();
    const nameInput = userMenu.getByTestId('user-menu-name-input');
    await nameInput.click();
    await nameInput.dblclick();
    await nameInput.fill('Vlad Loghin');
    await page.getByTestId('save-name-button').click();

  });
});




