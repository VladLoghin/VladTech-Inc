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
    await page.locator('button:has-text("CONTACT")').click();
  } else {
    // Desktop: Click CONTACT in navbar
    await page.locator('.hidden.md\\:flex button:has-text("CONTACT")').click();
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

  // 5) Click the CONTACT US button in modal and wait for the backend call
  const [request] = await Promise.all([
    page.waitForRequest(req =>
      req.url().includes('/api/contact') && req.method() === 'POST'
    ),
    page.getByRole('button', { name: /contact us/i }).click()
  ]);

  // 6) Basic sanity check on the request
  expect(request.url()).toContain('/api/contact');
  expect(request.method()).toBe('POST');
});
