import { test, expect } from '../fixtures/fixtures';

test('modal closes when hitting cancel', async ({ page, loginAs }) => {
  await loginAs('admin');
  await page.getByRole('link', { name: 'Admin Panel' }).click();
  await page.getByRole('button', { name: 'New Project' }).click();

  await page.getByRole('button', { name: 'Cancel' }).click();
  await expect(page.getByRole('heading', { name: 'New Project' })).not.toBeVisible();
});
