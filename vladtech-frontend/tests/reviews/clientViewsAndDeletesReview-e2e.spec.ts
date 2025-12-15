import { test, expect } from '../fixtures/fixtures.js';

test('admin toggles review visibility, client views reviews', async ({ page, loginAs }) => {
  // --------------------
  // ADMIN FLOW
  // --------------------
  await loginAs('admin');

  await page.getByRole('button', { name: 'VIEW ALL →' }).click();
  await page.getByRole('button', { name: 'REVIEWS' }).click();

  // Toggle visibility on a review
  await page.getByTestId('review-visibility-toggle').nth(2).check();

  // Logout admin
  await page.getByRole('button', { name: 'VLADTECH' }).click();
  await page.getByRole('button', { name: 'LOGOUT' }).click();

  // Ensure we are logged out before next login
  await expect(page.getByRole('button', { name: 'LOGIN' })).toBeVisible();

  // --------------------
  // CLIENT FLOW
  // --------------------
  await loginAs('client');

  await page.getByRole('button', { name: 'VIEW ALL →' }).click();
  await page.getByRole('button', { name: 'REVIEWS' }).click();

  // Navigate through carousel
  await page.locator('.swiper-button-next').click();
  await page.locator('.swiper-button-next').click();
  await page.locator('.swiper-button-next').click();
  await page.locator('.swiper-button-next').click();
});
