import { test, expect } from '../fixtures/fixtures';

test('admin can assign client to project', async ({ page, loginAs }) => {
  await loginAs('admin');

  // Check if we're in mobile view
  const viewportSize = page.viewportSize();
  const isMobile = viewportSize && viewportSize.width < 768;
  
  if (isMobile) {
    // Mobile: Open hamburger menu and click ADMIN PANEL
    const hamburgerButton = page.locator('button svg').first();
    await hamburgerButton.click();
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
  } else {
    // Desktop: Click ADMIN PANEL in navbar
    await page.getByRole('button', { name: /admin panel/i }).click();
  }
  
  await page.getByRole('button', { name: 'Edit' }).first().click();
  await expect(page.getByRole('heading', { name: 'Update Project' })).toBeVisible();
  const clearButton = page.getByRole('button', { name: 'Clear' }).first();
  if (await clearButton.isVisible().catch(() => false)) {
    await clearButton.click();
  }
  await page.getByRole('button', { name: 'Select a client' }).click();
  await page.getByRole('textbox', { name: 'Search by email, name, or' }).click();
  await page.getByRole('textbox', { name: 'Search by email, name, or' }).fill('client.vladtech@cle4rwater.ca');
  await page.getByRole('button', { name: 'Search' }).click();
  await page.getByRole('button', { name: 'client.vladtech@cle4rwater.ca' }).click();
  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page.getByText('client.vladtech@cle4rwater.ca').first()).toBeVisible();
});
