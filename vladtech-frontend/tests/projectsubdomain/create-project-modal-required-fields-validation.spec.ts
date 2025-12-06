import { test, expect } from '../fixtures/fixtures';

test('validation errors appear for required fields', async ({ page, loginAs }) => {
  await loginAs('admin');
  await page.getByRole('link', { name: 'Admin Panel' }).click();
  await page.getByRole('button', { name: 'New Project' }).click();

  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page.getByText('Project name is required')).toBeVisible();
  await expect(page.getByText('City is required')).toBeVisible();
  await expect(page.getByText('Due date is required')).toBeVisible();
  await expect(page.getByText('Project type is required')).toBeVisible();
});
