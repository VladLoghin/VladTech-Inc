import { test, expect } from '../fixtures/fixtures';

test('test auth page navigation employee', async ({ page, loginAs }) => {
  await loginAs('employee');
  console.log('Logged in as employee');

  await page.getByRole('button', { name: 'EMPLOYEE TOOLS' }).click();
});

