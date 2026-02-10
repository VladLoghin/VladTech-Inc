import { test, expect } from '../fixtures/fixtures.js';

async function gotoEmployeeTools(page) {
  await page.goto('http://localhost:5173/employee');
  await page.evaluate(() => window.scrollTo(0, 0));
  await expect(page.getByRole('heading', { name: /employee tools/i })).toBeVisible({
    timeout: 15000,
  });
}

async function gotoProjectsList(page) {
  const myProjectsHeading = page.getByRole('heading', { name: /my projects/i });
  await expect(myProjectsHeading).toBeVisible({ timeout: 15000 });
  await page.evaluate(() => window.scrollBy(0, 600));
  await page.waitForTimeout(300);
}

test('employee can upload comment for assigned project', async ({ page, loginAs }) => {
  await loginAs('employee');
  await gotoEmployeeTools(page);
  await gotoProjectsList(page);

  const uploadButton = page.getByRole('button', { name: /upload information/i }).first();
  await expect(uploadButton).toBeVisible({ timeout: 15000 });
  await uploadButton.click();

  const uploadModalTitle = page.getByRole('heading', { name: /upload information/i });
  await expect(uploadModalTitle).toBeVisible({ timeout: 10000 });

  const commentText = `Employee update ${Date.now()}`;
  await page.getByPlaceholder(/write what you did today/i).fill(commentText);

  const submitButton = page.getByRole('button', { name: /^submit$/i });
  await expect(submitButton).toBeEnabled({ timeout: 10000 });
  await submitButton.click();

  await expect(uploadModalTitle).toBeHidden({ timeout: 15000 });
  await page.waitForTimeout(1500);

  const viewButton = page.getByRole('button', { name: /view information/i }).first();
  await viewButton.click();

  const viewModalTitle = page.getByRole('heading', { name: /view information/i });
  await expect(viewModalTitle).toBeVisible({ timeout: 10000 });
  await expect(page.getByText(commentText)).toBeVisible({ timeout: 15000 });
});
