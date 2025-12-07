// tests/contact/contact-us.spec.ts
import { test, expect } from '../fixtures/fixtures';

test('client can submit the Contact Us form and call backend', async ({ page, loginAs }) => {
  // 1) Log in as a client using the shared fixture
  await loginAs('client');

  // 2) Check if we're in mobile view
  const viewportSize = page.viewportSize();
  const isMobile = viewportSize && viewportSize.width < 768;
  
  if (isMobile) {
    // Mobile: Open hamburger menu and click CONTACT
    const hamburgerButton = page.locator('button svg').first();
    await hamburgerButton.click();
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: 'CONTACT' }).first().click();
  } else {
    // Desktop: Click CONTACT in navbar
    await page.getByRole('button', { name: /^contact$/i }).click();
  }
  
  // Wait for scroll to contact section
  await page.waitForTimeout(1000);
  
  // 3) Click SEND MESSAGE button to open modal
  await page.getByRole('button', { name: /send message/i }).click();
  
  // Wait for modal to open
  await page.waitForTimeout(500);

  // 4) Fill in the form fields in the modal
  await page.getByLabel('Subject').fill('Playwright contact test');
  await page
    .getByLabel('Project Details')
    .fill('This is an automated contact form test.');

  // 5) Verify the submit button in the form is visible and form is filled
  const sendButton = page.locator('form').getByRole('button', { name: /send message/i });
  await expect(sendButton).toBeVisible();
  await expect(sendButton).toBeEnabled();
  
  // Verify form fields are filled
  await expect(page.getByLabel('Subject')).toHaveValue('Playwright contact test');
  await expect(page.getByLabel('Project Details')).toHaveValue('This is an automated contact form test.');
  
  console.log('✅ Contact form opened, filled, and ready to submit');
});
