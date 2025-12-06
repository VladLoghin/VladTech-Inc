// tests/contact/contact-us.spec.ts
import { test, expect } from '../fixtures/fixtures';

test('client can submit the Contact Us form and call backend', async ({ page, loginAs }) => {
  // 1) Log in as a client using the shared fixture
  await loginAs('client');

  // 2) Wait until the CONTACT US section is on the page
  const contactHeading = page.getByRole('heading', { name: /contact us/i });
  await contactHeading.scrollIntoViewIfNeeded();
  await expect(contactHeading).toBeVisible();

  // 3) Fill in the form fields
  await page.getByLabel('Subject').fill('Playwright contact test');
  await page
    .getByLabel('Project Details')
    .fill('This is an automated contact form test.');

  // 4) Click the CONTACT US button and wait for the backend call
  const [request] = await Promise.all([
    page.waitForRequest(req =>
      req.url().includes('/api/contact') && req.method() === 'POST'
    ),
    page.getByRole('button', { name: /contact us/i }).click()
  ]);

  // 5) Basic sanity check on the request
  expect(request.url()).toContain('/api/contact');
  expect(request.method()).toBe('POST');
});
