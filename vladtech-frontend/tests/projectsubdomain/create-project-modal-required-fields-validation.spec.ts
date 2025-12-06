import { test, expect } from '../fixtures/fixtures';

test('validation errors appear for required fields', async ({ page, loginAs }) => {
  await loginAs('admin');
  
  // Check if we're in mobile view
  const viewportSize = page.viewportSize();
  const isMobile = viewportSize && viewportSize.width < 768;
  
  if (isMobile) {
    // Mobile: Open hamburger menu and click ADMIN PANEL
    const hamburgerButton = page.locator('button svg').first();
    await hamburgerButton.click();
    await page.waitForTimeout(500);
    await page.locator('button:has-text("ADMIN PANEL")').click();
  } else {
    // Desktop: Click ADMIN PANEL in navbar
    await page.getByRole('button', { name: /admin panel/i }).click();
  }
  
  await page.getByRole('button', { name: 'New Project' }).click();

  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page.getByText('Project name is required')).toBeVisible();
  await expect(page.getByText('City is required')).toBeVisible();
  await expect(page.getByText('Due date is required')).toBeVisible();
  await expect(page.getByText('Project type is required')).toBeVisible();
});
