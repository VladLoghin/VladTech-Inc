import { test, expect } from '../fixtures/fixtures';

test('test auth page navigation admin', async ({ page, loginAs }) => {
  await loginAs('admin');
  console.log('Logged in as admin');

  const viewport = page.viewportSize();
  const isMobile = viewport && viewport.width < 768;

  if (isMobile) {
    const hamburger = page.locator('button svg').first();
    await hamburger.click();
    await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
  } else {
    await page.getByRole('button', { name: /admin panel/i }).click();
  }
});
