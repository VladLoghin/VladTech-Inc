import { test, expect } from '../fixtures/fixtures';

test('admin can open new project modal', async ({ page, loginAs }) => {
  await loginAs('admin');

  await page.getByRole('link', { name: 'Admin Panel' }).click();
  await page.getByRole('button', { name: 'New Project' }).click();

  await expect(page.getByRole('heading', { name: 'New Project' })).toBeVisible();
  await expect(page.getByText('New ProjectProject Name *')).toBeVisible();
});
