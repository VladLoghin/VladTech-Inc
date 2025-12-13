// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';

test.describe('Estimate Modal E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:5173/');
    await page.waitForLoadState('networkidle');

    // Open the estimate modal from the hero CTA
    const createEstimateBtn = page.getByRole('button', { name: /create estimate/i });
    await expect(createEstimateBtn).toBeVisible();
    await createEstimateBtn.click();

    // Wait for modal to appear
    await expect(page.getByRole('dialog')).toBeVisible();
  });

  test('modal loads and fields are visible', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /enter estimate details/i })).toBeVisible();
    await expect(page.getByLabel(/presets/i)).toBeVisible();
    await expect(page.getByLabel(/square feet/i)).toBeVisible();
    await expect(page.getByLabel(/average material cost per sq ft/i)).toBeVisible();
    await expect(page.getByRole('button', { name: /submit/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /close/i })).toBeVisible();
  });


  test('submits and shows result modal', async ({ page }) => {
    await page.getByLabel(/square feet/i).fill('1200');
    await page.getByLabel(/average material cost per sq ft/i).fill('8');

    const responsePromise = page.waitForResponse((resp) =>
      resp.url().includes('/api/estimates/calculate') && resp.status() === 200
    );

    await page.getByRole('button', { name: /submit/i }).click();
    await responsePromise;

    // Locate the result modal by its content instead of aria-name
    const resultModal = page.getByRole('dialog').filter({ hasText: /estimated total/i });
    const resultHeading = resultModal.getByRole('heading', { name: /estimate result/i });

    await expect(resultModal).toBeVisible({ timeout: 10000 });
    await expect(resultHeading).toBeVisible({ timeout: 10000 });

    await resultModal.getByRole('button', { name: /close/i }).click();
    await expect(resultModal).not.toBeVisible({ timeout: 5000 });
  });


  test('backdrop click closes the main modal', async ({ page }) => {
    const backdrop = page.locator('.modal').first();
    await backdrop.click({ position: { x: 5, y: 5 } });
    await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 2000 });
  });

  test('required validation keeps modal open when empty', async ({ page }) => {
    // Clear required field and submit
    await page.getByLabel(/square feet/i).fill('');
    await page.getByRole('button', { name: /submit/i }).click();

    // Modal should remain open because HTML5 validation blocks submit
    await expect(page.getByRole('dialog')).toBeVisible();
  });
});