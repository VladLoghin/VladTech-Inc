import { test, expect } from '../fixtures/fixtures';

test('project priority dropdown and display', async ({ page, loginAs }) => {
    await loginAs('admin');

    // Check if we're in mobile view
    const viewportSize = page.viewportSize();
    const isMobile = viewportSize && viewportSize.width < 768;

    // Navigate to Admin Panel
    if (isMobile) {
        const hamburgerButton = page.locator('button[aria-expanded]');
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

    // Verify priority dropdown exists with MEDIUM as default
    const prioritySelect = page.locator('form select[name="priority"]');
    await expect(prioritySelect).toBeVisible();
    await expect(prioritySelect).toHaveValue('MEDIUM');

    // Verify all priority options exist
    await expect(prioritySelect.locator('option[value="LOW"]')).toHaveCount(1);
    await expect(prioritySelect.locator('option[value="MEDIUM"]')).toHaveCount(1);
    await expect(prioritySelect.locator('option[value="HIGH"]')).toHaveCount(1);
    await expect(prioritySelect.locator('option[value="URGENT"]')).toHaveCount(1);

    // Fill out the project form with HIGH priority
    const timestamp = Date.now();
    const projectName = `Priority Test ${timestamp}`;

    await page.locator('form input[name="name"]').fill(projectName);
    await page.locator('form select[name="address.country"]').selectOption('Canada');
    await page.locator('form select[name="address.province"]').selectOption('Quebec');
    await page.locator('form input[name="address.city"]').fill('Montreal');
    await page.locator('form input[name="dueDate"]').fill('2026-12-31');

    // Scroll down in the modal
    await page.evaluate(() => {
        const modal = document.querySelector('.overflow-y-auto');
        if (modal) modal.scrollTop = 400;
    });
    await page.waitForTimeout(300);

    await page.locator('form select[name="projectType"]').selectOption('SCHEDULED');
    await prioritySelect.selectOption('HIGH');
    await page.locator('form input[name="startDate"]').fill('2026-10-15');

    // Click "Create" button
    await page.getByRole('button', { name: /^create$/i }).click();

    // Wait for modal to close
    await expect(page.getByRole('heading', { name: /new project/i })).not.toBeVisible({ timeout: 10000 });

    // Wait for UI to update
    await page.waitForTimeout(2000);

    // Wait for UI to update
    await page.waitForTimeout(2000);

    // Search for project to handle pagination
    await page.locator('input[name="search"]').fill(projectName);
    await page.keyboard.press('Enter');

    // Find the newly created project card
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 5000 });

    // Verify the priority shows "HIGH" with orange styling
    const priorityBadge = projectCard.locator('span.bg-orange-100.text-orange-800:has-text("HIGH")');
    await expect(priorityBadge).toBeVisible();
});
