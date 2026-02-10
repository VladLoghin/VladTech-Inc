import { test, expect } from '../fixtures/fixtures';

test('reactivate project', async ({ page, loginAs, createProject }) => {
    await loginAs('admin');

    // Create a project first
    const projectName = await createProject('Reactivate Test');

    // Check if we're in mobile view
    const viewportSize = page.viewportSize();
    const isMobile = viewportSize && viewportSize.width < 768;

    // Ensure we're on Active tab
    await page.getByRole('button', { name: /^Active/i }).click();
    await page.waitForTimeout(500);

    // Find the project we just created and archive it first
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 5000 });

    await projectCard.getByRole('button', { name: /Archive/i }).click();

    // Wait for archive action to complete
    await expect(page.getByText(/has been archived/i)).toBeVisible({ timeout: 5000 });
    await page.waitForTimeout(1000);

    // Switch to Archived tab
    await page.getByRole('button', { name: /^Archived/i }).click();
    await page.waitForTimeout(500);

    // Find our archived project and click "Reactivate"
    const archivedProjectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(archivedProjectCard).toBeVisible({ timeout: 5000 });

    await archivedProjectCard.getByRole('button', { name: /Reactivate/i }).click();

    // Verify success message
    await expect(page.getByText(/has been reactivated/i)).toBeVisible({ timeout: 5000 });
});
