// tests/projectsubdomain/upload-comment-employee.spec.ts
import { test, expect, type Page, type Locator } from '@playwright/test';

async function safeClick(locator: Locator) {
  await locator.scrollIntoViewIfNeeded();
  await locator.waitFor({ state: 'visible' });
  await locator.click({ force: true }); // bypass fixed-nav “intercepts pointer events”
}

async function clickLogin(page: Page) {
  const viewport = page.viewportSize();
  const isMobile = viewport && viewport.width < 768;

  await page.goto('http://localhost:5173/');
  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(200);

  if (isMobile) {
    // open hamburger, then click LOGIN inside menu
    const hamburgerBtn = page.locator('button:has(svg)').first();
    await safeClick(hamburgerBtn);
    await page.waitForTimeout(300);

    // In mobile menu, LOGIN is usually the last one
    const loginBtn = page.getByRole('button', { name: 'LOGIN' }).last();
    await safeClick(loginBtn);
  } else {
    const loginBtn = page.getByRole('button', { name: 'LOGIN' }).first();
    await safeClick(loginBtn);
  }
}

async function loginAuth0(page: Page, email: string, password: string) {
  // Auth0 form (labels differ depending on theme, so use robust locators)
  const emailBox = page.getByRole('textbox', { name: /email/i }).first();
  await emailBox.waitFor({ state: 'visible', timeout: 20000 });
  await emailBox.fill(email);

  const passwordBox = page.locator('input[type="password"]').first();
  await passwordBox.waitFor({ state: 'visible', timeout: 20000 });
  await passwordBox.fill(password);

  // Some themes say Continue, others say Log In, etc.
  const continueBtn = page.getByRole('button', { name: /continue|log in|login|sign in|next/i }).first();
  await safeClick(continueBtn);

  // back to app (Auth0 redirect can be slow on webkit)
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

test('employee upload info works on mobile safari', async ({ page }) => {
  // ---------------- Employee login ----------------
  await clickLogin(page);
  await loginAuth0(page, 'cunninghamemployee4399@gmail.com', 'VladTechPassword123!');

  // ---------------- Go to employee tools ----------------
  await gotoEmployeeTools(page);

  // ---------------- Open Upload Information modal ----------------
  const uploadBtn = page.getByRole('button', { name: /upload information/i }).first();
  await safeClick(uploadBtn);

  // ---------------- Fill textbox ----------------
  // Your recording used role textbox with this exact accessible name.
  // If it ever fails on WebKit, swap to locator('textarea') as a fallback.
  const infoBox = page.getByRole('textbox', { name: /write what you did today/i }).first();
  await infoBox.waitFor({ state: 'visible', timeout: 15000 });
  await infoBox.fill('Hi');

  // ---------------- Submit ----------------
  const submitBtn = page.getByRole('button', { name: /^submit$/i }).first();
  await safeClick(submitBtn);

  // Optional small wait for backend/UI update
  await page.waitForTimeout(800);

  // ---------------- View Information ----------------
  // Your recorded test used nth(1). Keep it, but make it resilient.
  const viewButtons = page.getByRole('button', { name: /view information/i });
  await expect(viewButtons.first()).toBeVisible({ timeout: 15000 });

  const count = await viewButtons.count();
  if (count >= 2) {
    await safeClick(viewButtons.nth(1));
  } else {
    await safeClick(viewButtons.first());
  }
});
