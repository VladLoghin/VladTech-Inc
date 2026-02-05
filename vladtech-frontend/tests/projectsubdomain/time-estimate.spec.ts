import { test, expect } from '../fixtures/fixtures';

test('create project with time estimate', async ({ page, loginAs }) => {
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

    // Click \"New Project\" button
    await page.getByRole('button', { name: /add/i }).click();
    await page.waitForTimeout(500);

    // Wait for modal to appear
    await expect(page.getByRole('heading', { name: /new project/i })).toBeVisible({ timeout: 5000 });

    // Fill out the project form
    const timestamp = Date.now();
    const projectName = `Time Estimate Test ${timestamp}`;

    await page.locator('form input[name="name"]').fill(projectName);
    await page.locator('form input[name="address.city"]').fill('Toronto');
    await page.locator('form input[name="dueDate"]').fill('2026-12-31');

    // Scroll down in the modal to reach the time estimate fields
    await page.evaluate(() => {
        const modal = document.querySelector('.overflow-y-auto');
        if (modal) modal.scrollTop = 500;
    });
    await page.waitForTimeout(300);

    await page.locator('form select[name="projectType"]').selectOption('SCHEDULED');
    
    // Fill in time estimate: 2 years, 3 months, 15 days, 8 hours
    // Use label-based selection for better reliability
    await page.getByText('Years').locator('..').locator('input[type="number"]').fill('2');
    await page.getByText('Months').locator('..').locator('input[type="number"]').fill('3');
    await page.getByText('Days').locator('..').locator('input[type="number"]').fill('15');
    await page.getByText('Hours').locator('..').locator('input[type="number"]').fill('8');

    // Verify the total display is shown
    await expect(page.getByText(/Total:/)).toBeVisible();
    
    // Click \"Create\" button
    await page.getByRole('button', { name: /^create$/i }).click();

    // Wait for modal to close
    await expect(page.getByRole('heading', { name: /new project/i })).not.toBeVisible({ timeout: 10000 });

    // Wait for UI to update
    await page.waitForTimeout(2000);

    // Search for project to handle pagination
    await page.locator('input[name="search"]').fill(projectName);
    await page.keyboard.press('Enter');
    await page.waitForTimeout(1000);

    // Find the newly created project card
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 5000 });

    // Click the edit button to open the edit modal
    const editBtn = projectCard.getByRole('button', { name: /edit/i });
    await editBtn.click();
    await page.waitForTimeout(1000);

    // Wait for the edit modal to appear
    await expect(page.getByRole('heading', { name: /edit project/i })).toBeVisible({ timeout: 5000 });

    // Scroll to time estimate fields
    await page.evaluate(() => {
        const modal = document.querySelector('.overflow-y-auto');
        if (modal) modal.scrollTop = 500;
    });
    await page.waitForTimeout(300);

    // Verify time estimate values were persisted using more specific selectors
    // Find the time estimate section and get inputs within it
    const yearsInput = page.getByText('Years').locator('..').locator('input[type="number"]');
    const monthsInput = page.getByText('Months').locator('..').locator('input[type="number"]');
    const daysInput = page.getByText('Days').locator('..').locator('input[type="number"]');
    const hoursInput = page.getByText('Hours').locator('..').locator('input[type="number"]');

    await expect(yearsInput).toHaveValue('2');
    await expect(monthsInput).toHaveValue('3');
    await expect(daysInput).toHaveValue('15');
    await expect(hoursInput).toHaveValue('8');

    // Verify the total is still displayed
    await expect(page.getByText(/Total:/)).toBeVisible();
    await expect(page.getByText(/2y 3mo/)).toBeVisible();
});