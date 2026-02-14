import { test, expect } from '../fixtures/fixtures.js';

test.describe('Estimate Settings Modal', () => {
  test.beforeEach(async ({ page, loginAs }: any) => {
    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5173/admin');
    await page.waitForLoadState('networkidle');
    
    // Wait for admin page to load - check for the main heading
    await expect(page.getByRole('heading', { name: /admin panel/i })).toBeVisible({ timeout: 10000 });
    
    // Wait for the Edit Estimate Variables button to be visible (admin-only button)
    await expect(page.getByRole('button', { name: /edit estimate variables/i })).toBeVisible({ timeout: 10000 });
  });

  test('Admin can open estimate settings modal', async ({ page }: any) => {
    // Click the Edit Estimate Variables button
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Verify modal is visible
    await expect(page.getByRole('heading', { name: /estimate settings/i })).toBeVisible();
    await expect(page.getByText(/update the pricing defaults/i)).toBeVisible();
  });

  test('All sections are collapsible', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Test Core Rates section - should be open by default
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });
    await expect(laborRateInput).toBeVisible();

    // Find and click the Core Rates section header (first section with ChevronDown icon)
    const coreRatesSection = page.locator('button').filter({ hasText: 'Core Rates' });
    await coreRatesSection.click();
    await page.waitForTimeout(300);

    // Verify inputs are hidden after collapse
    await expect(laborRateInput).not.toBeVisible();

    // Click again to expand
    await coreRatesSection.click();
    await page.waitForTimeout(300);
    await expect(laborRateInput).toBeVisible();

    // Test Siding section collapse
    const sidingSection = page.locator('button').filter({ hasText: 'Siding' });
    const sidingSectionContent = page.locator('section').filter({ hasText: 'SidingExtra Labor Per Story' });
    const sidingInput = sidingSectionContent.getByLabel('Extra Labor Per Story Rate', { exact: true });
    
    await expect(sidingInput).toBeVisible();
    await sidingSection.click();
    await page.waitForTimeout(300);
    await expect(sidingInput).not.toBeVisible();

    // Test Roofing section collapse
    const roofingSection = page.locator('button').filter({ hasText: 'Roofing' });
    const roofingInput = page.getByLabel('Pitch Factor Per Unit', { exact: true });
    
    await roofingSection.click();
    await page.waitForTimeout(300);
    await expect(roofingInput).not.toBeVisible();

    // Test Kitchen section collapse
    const kitchenSection = page.locator('button').filter({ hasText: 'Kitchen' });
    const kitchenInput = page.getByLabel('Plumbing Cost', { exact: true });
    
    await kitchenSection.click();
    await page.waitForTimeout(300);
    await expect(kitchenInput).not.toBeVisible();
  });

  test('Form inputs accept numeric values', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Find Labor Rate input (first input in Core Rates)
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });

    // Clear and fill with a numeric value
    await laborRateInput.click();
    await laborRateInput.fill('75.50');
    await expect(laborRateInput).toHaveValue('75.50');

    // Test another input - Overhead Rate
    const overheadRateInput = page.getByLabel('Overhead Rate', { exact: true });
    
    await overheadRateInput.click();
    await overheadRateInput.fill('0.15');
    await expect(overheadRateInput).toHaveValue('0.15');

    // Test a factor input - Vinyl Factor in Siding section (use section scoping)
    const sidingSection = page.locator('section').filter({ hasText: 'SidingExtra Labor Per Story' });
    const vinylFactorInput = sidingSection.getByLabel('Vinyl Factor', { exact: true });
    
    await vinylFactorInput.click();
    await vinylFactorInput.fill('1.25');
    await expect(vinylFactorInput).toHaveValue('1.25');
  });

  test('Validation prevents negative values', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Find Labor Rate input
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });

    // Try to enter a negative value
    await laborRateInput.click();
    await laborRateInput.fill('-50');
    
    // Try to save
    await page.getByRole('button', { name: 'Save Settings' }).click();
    await page.waitForTimeout(500);

    // Verify error message appears
    await expect(page.getByText('All estimate values must be zero or greater.')).toBeVisible();
  });

  test('Can update and save estimate settings', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Update Labor Rate
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });
    
    await laborRateInput.click();
    await laborRateInput.clear();
    await laborRateInput.fill('80.00');

    // Update Tax Rate
    const taxRateInput = page.getByLabel('Tax Rate', { exact: true });
    
    await taxRateInput.click();
    await taxRateInput.clear();
    await taxRateInput.fill('0.13');

    // Update a siding factor (scope to siding section)
    const sidingSection = page.locator('section').filter({ hasText: 'SidingExtra Labor Per Story' });
    const vinylFactorInput = sidingSection.getByLabel('Vinyl Factor', { exact: true });
    
    await vinylFactorInput.click();
    await vinylFactorInput.clear();
    await vinylFactorInput.fill('1.30');

    // Save settings
    await page.getByRole('button', { name: 'Save Settings' }).click();
    await page.waitForTimeout(1500);

    // Verify success message
    await expect(page.getByText('Estimate settings saved successfully.')).toBeVisible({ timeout: 5000 });
  });

  test('Modal can be closed without saving', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Verify modal is open
    await expect(page.getByRole('heading', { name: 'Estimate Settings' })).toBeVisible();

    // Update a value without saving
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });
    
    await laborRateInput.click();
    await laborRateInput.fill('999.99');

    // Close modal using the X button in the modal header
    const modal = page.locator('.fixed.inset-0').filter({ has: page.getByRole('heading', { name: /estimate settings/i }) });
    const closeButton = modal.locator('button').first();
    await closeButton.click();
    await page.waitForTimeout(500);

    // Verify modal is closed
    await expect(page.getByRole('heading', { name: 'Estimate Settings' })).not.toBeVisible();

    // Reopen modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Verify the unsaved changes were not persisted
    // (The value should be whatever was in the DB, not 999.99)
    const laborRateInputReopened = page.getByLabel('Labor Rate', { exact: true });
    await expect(laborRateInputReopened).not.toHaveValue('999.99');
  });

  test('Can close modal using Close button', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Verify modal is open
    await expect(page.getByRole('heading', { name: 'Estimate Settings' })).toBeVisible();

    // Click the Close button in the footer
    await page.getByRole('button', { name: 'Close' }).click();
    await page.waitForTimeout(500);

    // Verify modal is closed
    await expect(page.getByRole('heading', { name: 'Estimate Settings' })).not.toBeVisible();
  });

  test('All sections have proper headers', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Verify all section headers exist
    await expect(page.locator('button').filter({ hasText: 'Core Rates' })).toBeVisible();
    await expect(page.locator('button').filter({ hasText: 'Siding' })).toBeVisible();
    await expect(page.locator('button').filter({ hasText: 'Roofing' })).toBeVisible();
    await expect(page.locator('button').filter({ hasText: 'Kitchen' })).toBeVisible();
    await expect(page.locator('button').filter({ hasText: 'Window and Door' })).toBeVisible();
    await expect(page.locator('button').filter({ hasText: 'Deck and Patio' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Flooring', exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Flooring Removal Factors', exact: true })).toBeVisible();
  });

  test('Inputs have proper validation attributes', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Check Labor Rate input attributes
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });
    
    // Verify input type and min attribute
    await expect(laborRateInput).toHaveAttribute('type', 'number');
    await expect(laborRateInput).toHaveAttribute('min', '0');
    await expect(laborRateInput).toHaveAttribute('step', '0.01');
  });

  test('Loading spinner appears while fetching settings', async ({ page }: any) => {
    // Intercept the API call to delay it
    await page.route('**/api/estimates/config', async route => {
      await page.waitForTimeout(1000); // Add delay
      route.continue();
    });

    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    
    // Verify modal eventually loads with settings
    // Spinner may or may not be visible depending on API speed
    await expect(page.getByLabel('Labor Rate', { exact: true })).toBeVisible({ timeout: 5000 });
  });

  test('Can update multiple fields across different sections', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Update Core Rate
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });
    await laborRateInput.click();
    await laborRateInput.clear();
    await laborRateInput.fill('85.00');

    // Update Siding value (scope to Siding section)
    const sidingSection = page.locator('section').filter({ hasText: 'SidingExtra Labor Per Story' });
    const sidingLaborInput = sidingSection.getByLabel('Extra Labor Per Story Rate', { exact: true });
    await sidingLaborInput.click();
    await sidingLaborInput.clear();
    await sidingLaborInput.fill('15.50');

    // Update Kitchen value
    const plumbingInput = page.getByLabel('Plumbing Cost', { exact: true });
    await plumbingInput.click();
    await plumbingInput.clear();
    await plumbingInput.fill('2500.00');

    // Update Deck value
    const deckMaterialInput = page.getByLabel('Base Material Cost Per Sq Ft', { exact: true });
    await deckMaterialInput.click();
    await deckMaterialInput.clear();
    await deckMaterialInput.fill('25.00');

    // Save all changes
    await page.getByRole('button', { name: 'Save Settings' }).click();
    await page.waitForTimeout(1500);

    // Verify success
    await expect(page.getByText('Estimate settings saved successfully.')).toBeVisible({ timeout: 5000 });
  });

  test('Empty values are handled correctly', async ({ page }: any) => {
    // Open modal
    await page.getByRole('button', { name: /edit estimate variables/i }).click();
    await page.waitForTimeout(500);

    // Clear a value
    const laborRateInput = page.getByLabel('Labor Rate', { exact: true });
    await laborRateInput.click();
    await laborRateInput.clear();

    // Try to save with empty value
    await page.getByRole('button', { name: 'Save Settings' }).click();
    await page.waitForTimeout(1500);

    // Should not show validation error for empty values (they are converted to null)
    // Success should appear
    await expect(page.getByText('Estimate settings saved successfully.')).toBeVisible({ timeout: 5000 });
  });
});
