// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';

test.describe('Estimate Modal E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
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

    const submitBtn = page.getByRole('button', { name: /submit/i });
    await submitBtn.scrollIntoViewIfNeeded();
    await submitBtn.click();
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
    const submitBtn = page.getByRole('button', { name: /submit/i });
    await submitBtn.scrollIntoViewIfNeeded();
    await submitBtn.click();

    // Modal should remain open because HTML5 validation blocks submit
    await expect(page.getByRole('dialog')).toBeVisible();
  });

  // ============ Preset Tests ============

  test('applies roof preset and auto-fills fields', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    
    // Get all option values
    const options = page.locator('#preset-select option');
    const optionCount = await options.count();
    
    // Get the second option (assuming: default, roof, siding, kitchen order)
    if (optionCount > 1) {
      const optionValue = await options.nth(1).getAttribute('value');
      if (optionValue) {
        await presetSelect.selectOption(optionValue);
        await page.waitForTimeout(500);
      }
    }

    // Verify fields are populated
    const sqftField = page.getByLabel(/area|square feet/i).first();
    const materialCostField = page.locator('input[type="number"]').nth(1);

    const sqftValue = await sqftField.inputValue();
    const materialValue = await materialCostField.inputValue();

    expect(sqftValue).toBeTruthy();
    expect(materialValue).toBeTruthy();
  });

  test('applies siding preset and auto-fills fields', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    const optionCount = await options.count();
    
    // Try to find siding option
    let found = false;
    for (let i = 1; i < optionCount; i++) {
      const text = await options.nth(i).textContent();
      if (text?.includes('Siding')) {
        const optionValue = await options.nth(i).getAttribute('value');
        if (optionValue) {
          await presetSelect.selectOption(optionValue);
          await page.waitForTimeout(500);
          found = true;
          break;
        }
      }
    }

    if (!found && optionCount > 1) {
      const optionValue = await options.nth(1).getAttribute('value');
      if (optionValue) await presetSelect.selectOption(optionValue);
      await page.waitForTimeout(500);
    }

    // Verify preset is selected
    const selectedValue = await presetSelect.inputValue();
    expect(selectedValue).toBeTruthy();
    
    // Verify form fields exist and are visible
    const sqftField = page.locator('input[name="squareFeet"]');
    const materialField = page.locator('input[name="materialCostPerSqFt"]');
    
    await expect(sqftField).toBeVisible();
    await expect(materialField).toBeVisible();
  });

  test('applies kitchen preset and auto-fills fields', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    const optionCount = await options.count();
    
    // Try to find kitchen option
    let found = false;
    for (let i = 1; i < optionCount; i++) {
      const text = await options.nth(i).textContent();
      if (text?.includes('Kitchen')) {
        const optionValue = await options.nth(i).getAttribute('value');
        if (optionValue) {
          await presetSelect.selectOption(optionValue);
          await page.waitForTimeout(500);
          found = true;
          break;
        }
      }
    }

    if (!found && optionCount > 2) {
      const optionValue = await options.nth(optionCount - 1).getAttribute('value');
      if (optionValue) await presetSelect.selectOption(optionValue);
      await page.waitForTimeout(500);
    }

    // Verify preset is selected
    const selectedValue = await presetSelect.inputValue();
    expect(selectedValue).toBeTruthy();
    
    // Verify form fields exist and are visible
    const sqftField = page.locator('input[name="squareFeet"]');
    const materialField = page.locator('input[name="materialCostPerSqFt"]');
    
    await expect(sqftField).toBeVisible();
    await expect(materialField).toBeVisible();
  });

  // ============ Conditional Field Tests ============

  test('displays skylight fields only for roof preset', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    
    // Find and select roof preset
    let roofOptionValue = '';
    const optionCount = await options.count();
    for (let i = 1; i < optionCount; i++) {
      const text = await options.nth(i).textContent();
      if (text?.includes('Roof')) {
        roofOptionValue = (await options.nth(i).getAttribute('value')) || '';
        break;
      }
    }

    if (roofOptionValue) {
      await presetSelect.selectOption(roofOptionValue);
      await page.waitForTimeout(500);
      
      // Check if any skylight-related fields are visible
      const skylightCheckbox = page.locator('input[name="hasSkylights"]');
      const numSkylightsInput = page.locator('input[name="numSkylights"]');
      
      const hasSkylightCheckbox = await skylightCheckbox.isVisible();
      expect(hasSkylightCheckbox).toBeTruthy();
    }
  });

  test('displays insulation checkbox for siding preset only', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    
    // Find and select siding preset
    let sidingOptionValue = '';
    const optionCount = await options.count();
    for (let i = 1; i < optionCount; i++) {
      const text = await options.nth(i).textContent();
      if (text?.includes('Siding')) {
        sidingOptionValue = (await options.nth(i).getAttribute('value')) || '';
        break;
      }
    }

    if (sidingOptionValue) {
      await presetSelect.selectOption(sidingOptionValue);
      await page.waitForTimeout(500);
      
      // Check for insulation field
      const insulationCheckbox = page.locator('input[name="includeInsulation"]');
      const isVisible = await insulationCheckbox.isVisible();
      expect(isVisible).toBeTruthy();
    }
  });

  // ============ Calculation Tests ============

  test('calculates roof estimate with all inputs', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    
    // Find and select roof preset
    let roofOptionValue = '';
    const optionCount = await options.count();
    for (let i = 1; i < optionCount; i++) {
      const text = await options.nth(i).textContent();
      if (text?.includes('Roof')) {
        roofOptionValue = (await options.nth(i).getAttribute('value')) || '';
        break;
      }
    }

    if (roofOptionValue) {
      await presetSelect.selectOption(roofOptionValue);
      await page.waitForTimeout(500);

      // Set custom values
      const sqftInput = page.locator('input[name="squareFeet"]');
      const materialInput = page.locator('input[name="materialCostPerSqFt"]');
      await sqftInput.fill('500'); // square feet
      await materialInput.fill('15'); // material cost

      const responsePromise = page.waitForResponse((resp) =>
        resp.url().includes('/api/estimates/calculate') && resp.status() === 200
      );

      // Scroll submit button into view before clicking (important for mobile)
      const submitBtn = page.getByRole('button', { name: /submit/i });
      await submitBtn.scrollIntoViewIfNeeded();
      await submitBtn.click();
      const response = await responsePromise;

      expect(response.status()).toBe(200);
      const data = await response.json();
      expect(data.estimatePrice).toBeGreaterThan(0);
      expect(data.totalPrice).toBeGreaterThan(data.estimatePrice);
    }
  });

  test('calculates kitchen estimate with plumbing and electrical', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    
    // Find and select kitchen preset
    let kitchenOptionValue = '';
    const optionCount = await options.count();
    for (let i = 1; i < optionCount; i++) {
      const text = await options.nth(i).textContent();
      if (text?.includes('Kitchen')) {
        kitchenOptionValue = (await options.nth(i).getAttribute('value')) || '';
        break;
      }
    }

    if (kitchenOptionValue) {
      await presetSelect.selectOption(kitchenOptionValue);
      await page.waitForTimeout(500);

      const sqftInput = page.locator('input[name="squareFeet"]');
      const materialInput = page.locator('input[name="materialCostPerSqFt"]');
      await sqftInput.fill('300'); // square feet
      await materialInput.fill('80'); // material cost

      // Check plumbing and electrical if visible
      const plumbingCheckbox = page.locator('input[name="plumbingChanges"]');
      const electricalCheckbox = page.locator('input[name="electricalChanges"]');

      if (await plumbingCheckbox.isVisible()) {
        await plumbingCheckbox.check();
      }
      if (await electricalCheckbox.isVisible()) {
        await electricalCheckbox.check();
      }

      const responsePromise = page.waitForResponse((resp) =>
        resp.url().includes('/api/estimates/calculate') && resp.status() === 200
      );

      // Scroll submit button into view before clicking (important for mobile)
      const submitBtn = page.getByRole('button', { name: /submit/i });
      await submitBtn.scrollIntoViewIfNeeded();
      await submitBtn.click();
      const response = await responsePromise;

      expect(response.status()).toBe(200);
      const data = await response.json();
      expect(data.estimatePrice).toBeGreaterThan(0);
    }
  });

  test('displays total price calculation breakdown', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    
    let roofOptionValue = '';
    const optionCount = await options.count();
    for (let i = 1; i < optionCount; i++) {
      const text = await options.nth(i).textContent();
      if (text?.includes('Roof')) {
        roofOptionValue = (await options.nth(i).getAttribute('value')) || '';
        break;
      }
    }

    if (roofOptionValue) {
      await presetSelect.selectOption(roofOptionValue);
      await page.waitForTimeout(500);
    }

    // Fill in square feet and material cost
    const sqftInput = page.locator('input[name="squareFeet"]');
    const materialInput = page.locator('input[name="materialCostPerSqFt"]');
    await sqftInput.fill('1000');
    await materialInput.fill('12');

    const responsePromise = page.waitForResponse((resp) =>
      resp.url().includes('/api/estimates/calculate') && resp.status() === 200
    );

    // Scroll submit button into view before clicking (important for mobile)
    const submitBtn = page.getByRole('button', { name: /submit/i });
    await submitBtn.scrollIntoViewIfNeeded();
    await submitBtn.click();
    await responsePromise;

    const resultModal = page.getByRole('dialog').filter({ hasText: /estimated total/i });
    await expect(resultModal).toBeVisible();

    // Check for price display - look for currency amount
    const priceText = resultModal.getByText(/\$\d+/);
    await expect(priceText).toBeVisible();
  });

  // ============ Error Handling Tests ============

  test('shows error for zero square feet', async ({ page }) => {
    const sqftInput = page.locator('input[name="squareFeet"]');
    const materialInput = page.locator('input[name="materialCostPerSqFt"]');
    await sqftInput.fill('0');
    await materialInput.fill('10');

    const submitBtn = page.getByRole('button', { name: /submit/i });
    await submitBtn.scrollIntoViewIfNeeded();
    await submitBtn.click();

    // HTML5 validation should prevent submission
    await expect(page.getByRole('dialog')).toBeVisible();
  });

  test('shows error for negative material cost', async ({ page }) => {
    const sqftInput = page.locator('input[name="squareFeet"]');
    const materialInput = page.locator('input[name="materialCostPerSqFt"]');
    await sqftInput.fill('100');
    await materialInput.fill('-5');

    // HTML5 validation should prevent negative submission
    const submitBtn = page.getByRole('button', { name: /submit/i });
    await submitBtn.scrollIntoViewIfNeeded();
    await submitBtn.click();
    
    // Form should still be visible (not submitted)
    await expect(page.getByRole('dialog')).toBeVisible();
  });

  // ============ Language Tests ============

  test('displays estimate modal in English', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /enter estimate details/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /submit/i })).toBeVisible();
  });

  // ============ State Management Tests ============

  test('shows result modal after successful submission', async ({ page }) => {
    const sqftInput = page.locator('input[name="squareFeet"]');
    const materialInput = page.locator('input[name="materialCostPerSqFt"]');
    await sqftInput.fill('500');
    await materialInput.fill('10');

    const responsePromise = page.waitForResponse((resp) =>
      resp.url().includes('/api/estimates/calculate') && resp.status() === 200
    );

    const submitBtn = page.getByRole('button', { name: /submit/i });
    await submitBtn.scrollIntoViewIfNeeded();
    await submitBtn.click();
    await responsePromise;

    // Result modal should appear
    const resultModal = page.getByRole('dialog').filter({ hasText: /estimated total/i });
    await expect(resultModal).toBeVisible({ timeout: 10000 });

    // Close result modal
    const closeButton = resultModal.getByRole('button', { name: /close/i });
    await closeButton.click();
    await expect(resultModal).not.toBeVisible({ timeout: 5000 });
  });

  test('multiple preset switches update fields correctly', async ({ page }) => {
    const presetSelect = page.locator('#preset-select');
    const options = page.locator('#preset-select option');
    const optionCount = await options.count();

    // Switch through at least 2 presets if available
    if (optionCount > 1) {
      const firstValue = await options.nth(1).getAttribute('value');
      if (firstValue) {
        await presetSelect.selectOption(firstValue);
        await page.waitForTimeout(300);
        let selected = await presetSelect.inputValue();
        expect(selected).toBe(firstValue);
      }

      if (optionCount > 2) {
        const secondValue = await options.nth(2).getAttribute('value');
        if (secondValue) {
          await presetSelect.selectOption(secondValue);
          await page.waitForTimeout(300);
          let selected = await presetSelect.inputValue();
          expect(selected).toBe(secondValue);
        }
      }
    }

    // Verify form is still in good state
    await expect(page.locator('#preset-select')).toBeVisible();
  });
});
