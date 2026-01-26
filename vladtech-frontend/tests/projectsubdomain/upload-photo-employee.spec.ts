// tests/projectsubdomain/upload-photo-only-employee.spec.ts
import { test, expect, type Page, type Locator } from '@playwright/test';
import path from 'path';

async function safeClick(locator: Locator) {
  await locator.scrollIntoViewIfNeeded();
  await locator.waitFor({ state: 'visible' });
  await locator.click({ force: true });
}

async function clickLogin(page: Page) {
  const viewport = page.viewportSize();
  const isMobile = viewport && viewport.width < 768;

  await page.goto('http://localhost:5173/');
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(200);

  if (isMobile) {
    const hamburgerBtn = page.locator('button:has(svg)').first();
    await safeClick(hamburgerBtn);
    await page.waitForTimeout(300);

    const loginBtn = page.getByRole('button', { name: 'LOGIN' }).last();
    await safeClick(loginBtn);
  } else {
    const loginBtn = page.getByRole('button', { name: 'LOGIN' }).first();
    await safeClick(loginBtn);
  }
}

async function loginAuth0(page: Page, email: string, password: string) {
  const emailBox = page.getByRole('textbox', { name: /email/i }).first();
  await emailBox.waitFor({ state: 'visible', timeout: 20000 });
  await emailBox.fill(email);

  const passwordBox = page.locator('input[type="password"]').first();
  await passwordBox.waitFor({ state: 'visible', timeout: 20000 });
  await passwordBox.fill(password);

  const continueBtn = page.getByRole('button', { name: /continue|log in|login|sign in|next/i }).first();
  await safeClick(continueBtn);

  await page.waitForURL(/http:\/\/localhost:5173\/?$/, { timeout: 30000 });
  await page.waitForTimeout(500);
}

async function gotoEmployeeTools(page: Page) {
  const viewport = page.viewportSize();
  const isMobile = viewport && viewport.width < 768;

  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(200);

  if (isMobile) {
    const hamburgerBtn = page.locator('button:has(svg)').first();
    await safeClick(hamburgerBtn);
    await page.waitForTimeout(300);

    const employeeBtn = page.getByRole('button', { name: /employee tools/i }).first();
    await safeClick(employeeBtn);
  } else {
    const employeeBtn = page.getByRole('button', { name: /employee tools/i }).first();
    await safeClick(employeeBtn);
  }

  await expect(page.getByRole('heading', { name: /employee tools/i })).toBeVisible({ timeout: 15000 });
}

async function attachFileInModal(page: Page, absoluteFilePath: string) {
  // Find a usable file input:
  // - must exist
  // - must not be disabled
  // - should be attached to DOM
  // It might be hidden, and that's OK for setInputFiles.
  const inputs = page.locator('input[type="file"]');
  const count = await inputs.count();
  if (count === 0) {
    throw new Error('No input[type="file"] found in the Upload Information modal.');
  }

  // Pick the first enabled input
  let chosen: Locator | null = null;
  for (let i = 0; i < count; i++) {
    const candidate = inputs.nth(i);
    const disabled = await candidate.isDisabled().catch(() => false);
    if (!disabled) {
      chosen = candidate;
      break;
    }
  }

  if (!chosen) {
    throw new Error('Found file inputs, but all appear disabled.');
  }

  // Attach the file
  await chosen.setInputFiles(absoluteFilePath);

  // PROVE the file is attached (this avoids “it hung but actually no file got set”)
  const filesLen = await chosen.evaluate((el: HTMLInputElement) => el.files?.length ?? 0);
  if (filesLen !== 1) {
    throw new Error(`File did not attach to input. files.length=${filesLen}`);
  }

  // Optional: if your UI shows the filename somewhere, this is a good assert.
  // Keep it loose (some UIs don’t show it).
  await page.waitForTimeout(300);
}

test('employee uploads photo only', async ({ page }) => {
  await clickLogin(page);
  await loginAuth0(page, 'cunninghamemployee4399@gmail.com', 'VladTechPassword123!');
  await gotoEmployeeTools(page);

  const uploadBtn = page.getByRole('button', { name: /upload information/i }).first();
  await safeClick(uploadBtn);

  // Use an absolute path (fixes a lot of “works on my machine” issues)
  const imagePath = path.resolve('tests', 'fixtures', 'test-image.jpg');

  // Optional but very useful: if the upload triggers an API request, log failures
  page.on('response', async (res) => {
    const url = res.url();
    if (url.includes('/upload') || url.includes('/image') || url.includes('/media')) {
      const status = res.status();
      if (status >= 400) {
        // This shows in the terminal and instantly tells you if backend rejects the upload.
        console.log('Upload API failed:', status, url);
      }
    }
  });

  await attachFileInModal(page, imagePath);

  // Some UIs disable Submit until upload finishes or until a preview renders.
  const submitBtn = page.getByRole('button', { name: /^submit$/i }).first();
  await submitBtn.waitFor({ state: 'visible', timeout: 15000 });

  // If Submit is disabled, that means your UI is still waiting for something.
  // This will give you a clear error instead of “stuck”.
  await expect(submitBtn).toBeEnabled({ timeout: 15000 });

  await safeClick(submitBtn);

  // If your app shows any toast or confirmation, assert it here.
  // Example (adjust to your real message):
  // await expect(page.getByText(/uploaded|success/i)).toBeVisible({ timeout: 15000 });

  await page.waitForTimeout(1500);
});
