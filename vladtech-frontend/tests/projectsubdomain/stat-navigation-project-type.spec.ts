import { test, expect } from '../fixtures/fixtures';

test('stat navigation - project type filter', async ({ page, loginAs }) => {
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

  // Fill out the project form with APPOINTMENT project type
  const timestamp = Date.now();
  const projectName = `Stat Test Appointment ${timestamp}`;

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

  // Set project type to APPOINTMENT
  await page.locator('form select[name="projectType"]').selectOption('APPOINTMENT');
  await page.locator('form input[name="startDate"]').fill('2026-10-15');
  await page.locator('form textarea[name="description"]').fill('Testing stat navigation');

  // Click "Create" button
  await page.getByRole('button', { name: /^create$/i }).click();

  // Wait for modal to close
  await expect(page.getByRole('heading', { name: /new project/i })).not.toBeVisible({ timeout: 10000 });

  // Wait for UI to update
  await page.waitForTimeout(2000);

  // Switch to Project Type view by clicking the "Project Type" tab
  await page.getByRole('button', { name: 'Project Type' }).click();
  await page.waitForTimeout(500);

  // Click on the "Scheduled" stat card
  // The stat cards are buttons with aria-label "Filter by X"
  await page.getByRole('button', { name: 'Filter by Scheduled' }).click();
  await page.waitForTimeout(1000);

  // Verify that the projectType filter is set to SCHEDULED in the search/filter section
  // The filter dropdown should have the value "SCHEDULED"
  const projectTypeFilter = page.locator('select[name="projectType"]');
  await expect(projectTypeFilter).toHaveValue('SCHEDULED');

  console.log('✓ Stat navigation test passed: Project Type filter correctly set to SCHEDULED');
});
