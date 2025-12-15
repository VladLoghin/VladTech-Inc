import { test, expect } from '../fixtures/fixtures';

test('test', async ({ page, loginAs }) => {
  // 🔐 LOGIN via fixture (choose role here)
  await loginAs('client'); // or 'admin' / 'employee' as needed

  // --- original test steps (unchanged) ---
  await page.getByRole('button', { name: 'VIEW ALL →' }).click();
  await page.getByRole('button', { name: 'REVIEWS' }).click();

  await page.getByRole('button', { name: 'Add Review' }).click();

  await page.getByRole('textbox', { name: 'Your name' }).fill('Spider-Man');
  await page.getByRole('textbox', { name: 'Your message' }).fill('Peter Parker');

  await page.getByRole('button', { name: 'Submit Review' }).click();

  await page.getByRole('switch').click();
  await page.getByTestId('review-delete-button').click();
});
