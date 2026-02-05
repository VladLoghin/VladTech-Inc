import { test, expect } from '../fixtures/fixtures';

test('admin creates and edits project with estimated cost', async ({ page, loginAs }) => {
    // 1. Login as Admin using fixture
    await loginAs('admin');

    // Check if we're in mobile view (copied from user example/fixture logic)
    const viewportSize = page.viewportSize();
    const isMobile = viewportSize && viewportSize.width < 768;

    // 2. Go to Admin Panel
    // Logic similar to fixture or user example to navigate
    if (isMobile) {
        const hamburgerButton = page.locator('button[aria-expanded]');
        await hamburgerButton.click();
        await page.waitForTimeout(500);
        await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
    } else {
        await page.getByRole('button', { name: /admin panel/i }).click();
    }

    // 3. Create Project with Cost
    const projectName = `CostTest-${Date.now()}`;
    await page.getByRole('button', { name: 'ADD' }).click();

    // Wait for modal
    await page.getByRole('heading', { name: /new project/i }).waitFor({ state: 'visible' });

    // Fill basics
    await page.locator('form input[name="name"]').fill(projectName);
    await page.locator('form input[name="address.city"]').fill('Montreal');
    await page.locator('form input[name="dueDate"]').fill('2026-12-31');

    // Select Project Type
    await page.locator('form select[name="projectType"]').selectOption('SCHEDULED');

    // Set Estimated Cost (The new feature!)
    await page.locator('form select[name="estimatedCostCurrency"]').selectOption('USD');
    await page.locator('form input[name="estimatedCost"]').fill('5000.00');

    // Submit
    await page.getByRole('button', { name: 'Create', exact: true }).click();

    // Wait for modal to close
    await page.getByRole('heading', { name: /new project/i }).waitFor({ state: 'hidden' });
    await page.waitForTimeout(1000); // Wait for list refresh

    // 4. Verify Project Card Display
    // Search for project to handle pagination
    await page.locator('input[name="search"]').fill(projectName);
    await page.keyboard.press('Enter');

    // Use specific class selector found in ProjectList.jsx (border border-black/10 rounded-lg p-4)
    // Escaping the slash in black/10 is usually safe in CSS selectors, or we can just use partial class match or other attributes.
    // Let's use the full class string but escape the slash if needed. Playwright supports 'class=' or css .class.
    // Safest is to use the exact classes we verify in other tests: div.border.rounded-lg
    const projectCard = page.locator('div.border.rounded-lg.p-4', {
        has: page.getByRole('heading', { name: projectName }),
    }).first();

    await expect(projectCard).toBeVisible({ timeout: 15000 });

    await expect(projectCard).toContainText('Final Cost');
    // Loose check for number to handle potential locale formatting differences (e.g. US$5,000.00 vs 5 000,00 $)
    await expect(projectCard).toContainText('5,000');

    // 5. Edit Project Cost
    const editBtn = projectCard.getByRole('button', { name: /edit/i });
    await editBtn.click();

    // Wait for modal
    await expect(page.getByRole('heading', { name: /edit project/i })).toBeVisible();

    // Verify auto-conversion from USD to CAD
    // Current is 5000 USD. Switching to CAD should make it 5000 * 1.4 = 7000.
    await page.locator('form select[name="estimatedCostCurrency"]').selectOption('CAD');

    // Check if input value updated (exact 7000.00)
    await expect(page.locator('form input[name="estimatedCost"]')).toHaveValue('7000.00');

    // Change to a specific value to save - e.g. 2500.50 CAD
    await page.locator('form input[name="estimatedCost"]').fill('2500.50');

    // Save
    await page.getByRole('button', { name: /save/i }).click();

    // 6. Verify Update
    await expect(page.getByRole('heading', { name: /edit project/i })).not.toBeVisible();

    // Give it a moment to update DOM
    await page.waitForTimeout(500);

    await expect(projectCard).toContainText('Final Cost');
    await expect(projectCard).toContainText('2,500.5');
});
