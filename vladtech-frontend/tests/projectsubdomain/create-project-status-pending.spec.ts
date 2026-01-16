import { test, expect } from '../fixtures/fixtures';

test('create project status pending', async ({ page, loginAs }) => {
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

    // Click "New Project" button
    await page.getByRole('button', { name: /add/i }).click();
    await page.waitForTimeout(500);

    // Wait for modal to appear
    await expect(page.getByRole('heading', { name: /new project/i })).toBeVisible({ timeout: 5000 });

    // Fill out the project form
    const timestamp = Date.now();
    const projectName = `Status Test ${timestamp}`;

    await page.locator('input[name="name"]').fill(projectName);
    await page.locator('input[name="address.city"]').fill('Montreal');
    await page.locator('input[name="dueDate"]').fill('2025-12-31');

    // Scroll down in the modal
    await page.evaluate(() => {
        const modal = document.querySelector('.overflow-y-auto');
        if (modal) modal.scrollTop = 400;
    });
    await page.waitForTimeout(300);

    await page.locator('select[name="projectType"]').selectOption('SCHEDULED');
    await page.locator('input[name="startDate"]').fill('2025-01-15');
    await page.locator('textarea[name="description"]').fill('Testing PENDING status');

    // Click "Create" button
    await page.getByRole('button', { name: /^create$/i }).click();

    // Wait for modal to close
    await expect(page.getByRole('heading', { name: /new project/i })).not.toBeVisible({ timeout: 10000 });

    // Wait for UI to update
    await page.waitForTimeout(2000);

    // Find the newly created project card
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 5000 });

    // Verify the status shows "PENDING" with yellow styling
    const pendingBadge = projectCard.locator('span.bg-yellow-100.text-yellow-800:has-text("PENDING")');
    await expect(pendingBadge).toBeVisible();
});
