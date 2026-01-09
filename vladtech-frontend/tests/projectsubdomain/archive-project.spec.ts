import { test, expect } from '../fixtures/fixtures';

test('admin can mark a project as complete', async ({ page, loginAs }) => {
    await loginAs('admin');

    // Check if we're in mobile view
    const viewportSize = page.viewportSize();
    const isMobile = viewportSize && viewportSize.width < 768;

    // Navigate to Admin Panel
    if (isMobile) {
        const hamburgerButton = page.locator('button svg').first();
        await hamburgerButton.click();
        await page.waitForTimeout(500);
        await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
    } else {
        await page.getByRole('button', { name: /admin panel/i }).click();
    }

    // Wait for page to load
    await page.waitForTimeout(1000);

    // Ensure we're on Active tab
    await page.getByRole('button', { name: /^Active/i }).click();
    await page.waitForTimeout(500);

    // Click "Mark Complete" button on first project
    await page.getByRole('button', { name: /Mark Complete/i }).first().click();

    // Verify success message
    await expect(page.getByText(/has been marked as complete/i)).toBeVisible({ timeout: 5000 });
});
