import { test, expect } from '../fixtures/fixtures';

test('archive project', async ({ page, loginAs, createProject }) => {
    await loginAs('admin');

    // Create a project first to ensure we have one to archive
    const projectName = await createProject('Archive Test');

    // Check if we're in mobile view
    const viewportSize = page.viewportSize();
    const isMobile = viewportSize && viewportSize.width < 768;

    // Navigate to Admin Panel (createProject already navigated there, but ensure we're on Active tab)
    await page.waitForTimeout(500);

    // Ensure we're on Active tab
    await page.getByRole('button', { name: /^Active/i }).click();
    await page.waitForTimeout(500);

    // Find the project we just created and click "Mark Complete" on it
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 5000 });

    await projectCard.getByRole('button', { name: /Mark Complete/i }).click();

    // Verify success message
    try {
        await expect(page.getByText(/has been marked as complete/i)).toBeVisible({ timeout: 15000 });
    } catch (e) {
        // If success message not found, check if error message is present
        const errorMsg = page.getByText('Failed to complete project.');
        if (await errorMsg.isVisible()) {
            throw new Error('Backend failed to complete project: UI reported "Failed to complete project."');
        }
        throw e;
    }
});
