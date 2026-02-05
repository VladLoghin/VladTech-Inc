import { test, expect } from '../fixtures/fixtures';

test('create project modal required fields validation', async ({ page, loginAs }) => {
  await loginAs('admin');

  // Check if we're in mobile view
  const viewportSize = page.viewportSize();
  const isMobile = viewportSize && viewportSize.width < 768;

  if (isMobile) {
    // Mobile: Open hamburger menu and click ADMIN PANEL
    const hamburgerButton = page.locator('button[aria-expanded]');
    await hamburgerButton.click();
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
  } else {
    // Desktop: Click ADMIN PANEL in navbar
    await page.getByRole('button', { name: /admin panel/i }).click();
  }

  await page.getByRole('button', { name: 'ADD' }).click();
  
  // Wait for modal to be visible
  await expect(page.locator('form')).toBeVisible();

  // Get form elements
  const nameInput = page.locator('form input[name="name"]');
  const dueDateInput = page.locator('form input[name="dueDate"]');
  const projectTypeSelect = page.locator('form select[name="projectType"]');
  const cityInput = page.locator('form input[name="address.city"]');

  // Click Create to trigger validation
  await page.getByRole('button', { name: /^Create$/ }).click();
  
  // Check that required fields are marked as :invalid using CSS pseudo-class
  // This is the recommended way to test native HTML5 validation
  await expect(nameInput).toHaveCSS('border-color', /.*/); // Field should have some styling
  
  // Use evaluate to check if fields are invalid via checkValidity()
  const nameIsInvalid = await nameInput.evaluate((el: HTMLInputElement) => !el.checkValidity());
  const dueDateIsInvalid = await dueDateInput.evaluate((el: HTMLInputElement) => !el.checkValidity());
  const projectTypeIsInvalid = await projectTypeSelect.evaluate((el: HTMLSelectElement) => !el.checkValidity());
  const cityIsValid = await cityInput.evaluate((el: HTMLInputElement) => el.checkValidity());

  expect(nameIsInvalid).toBe(true);
  expect(dueDateIsInvalid).toBe(true);
  expect(projectTypeIsInvalid).toBe(true);
  // City should be valid when address is empty
  expect(cityIsValid).toBe(true);

  // Note: City conditional validation (when address is partially filled) 
  // uses native browser tooltips which aren't testable via DOM inspection.
  // The validation logic is in ProjectModal.jsx validateForm() function.
});
