// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';
// @ts-ignore
import { mockAllReviews, mockVisibleReviews, mockMyReviews } from './mockCompletedProjects.ts';

// Auth0 login flow can be slow — increase timeout for this multi-step test
test.setTimeout(60000);

test('admin toggles review visibility, client views reviews', async ({ page, loginAs }) => {
  // --------------------
  // ADMIN FLOW
  // --------------------
  // Mock the admin reviews endpoint so there is data to toggle
  await mockAllReviews(page);
  await mockVisibleReviews(page);
  await mockMyReviews(page);

  await loginAs('admin');

  // Navigate to reviews page
  await page.goto('http://localhost:5173/reviews');
  await page.waitForLoadState('networkidle');

  // Wait for visibility toggles (admin sees all reviews)
  await page.getByTestId('review-visibility-toggle').first().waitFor({ state: 'visible', timeout: 15000 });

  // Toggle visibility on a review
  const toggles = page.getByTestId('review-visibility-toggle');
  const toggleCount = await toggles.count();
  if (toggleCount > 2) {
    await toggles.nth(2).check();
  } else if (toggleCount > 0) {
    await toggles.first().check();
  }

  // Logout admin via global navbar
  await page.getByRole('button', { name: /logout/i }).click();

  // Ensure we are logged out before next login
  await expect(page.getByRole('button', { name: /login/i })).toBeVisible({ timeout: 15000 });

  // --------------------
  // CLIENT FLOW
  // --------------------
  await loginAs('client');

  // Navigate to reviews page
  await page.goto('http://localhost:5173/reviews');
  await page.waitForLoadState('networkidle');

  // Wait for reviews to load
  await page.waitForTimeout(1000);

  // Navigate through carousel if buttons are available
  const nextButton = page.locator('.swiper-button-next');
  if (await nextButton.isVisible({ timeout: 5000 })) {
    await nextButton.click();
    await page.waitForTimeout(300);
    await nextButton.click();
    await page.waitForTimeout(300);
  }
});
