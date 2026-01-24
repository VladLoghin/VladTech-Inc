// tests/projectsubdomain/update-work-status.spec.ts
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
  await emailBox.waitFor({ state: 'visible', timeout: 15000 });
  await emailBox.fill(email);

  const passwordBox = page.locator('input[type="password"]').first();
  await passwordBox.fill(password);

  const continueBtn = page.getByRole('button', { name: /continue/i }).first();
  await safeClick(continueBtn);

  // back to app (Auth0 redirect can be slow on webkit)
  await page.waitForURL(/http:\/\/localhost:5173\/?$/, { timeout: 30000 });
}

async function gotoAdminPanel(page: Page) {
  const viewport = page.viewportSize();
  const isMobile = viewport && viewport.width < 768;

  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(200);

  if (isMobile) {
    const hamburgerBtn = page.locator('button:has(svg)').first();
    await safeClick(hamburgerBtn);
    await page.waitForTimeout(300);

    const adminBtn = page.getByRole('button', { name: /admin panel/i }).first();
    await safeClick(adminBtn);
  } else {
    const adminBtn = page.getByRole('button', { name: /admin panel/i }).first();
    await safeClick(adminBtn);
  }

  await expect(page.getByRole('heading', { name: /admin area/i })).toBeVisible({ timeout: 15000 });
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

async function clickLogout(page: Page) {
  const viewport = page.viewportSize();
  const isMobile = viewport && viewport.width < 768;

  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(200);

  if (isMobile) {
    const hamburgerBtn = page.locator('button:has(svg)').first();
    await safeClick(hamburgerBtn);
    await page.waitForTimeout(300);

    const logoutBtn = page.getByRole('button', { name: /logout/i }).first();
    await safeClick(logoutBtn);
  } else {
    const logoutBtn = page.getByRole('button', { name: /logout/i }).first();
    await safeClick(logoutBtn);
  }

  // Wait until we’re back at home and LOGIN exists again
  await page.waitForURL(/http:\/\/localhost:5173\/?$/, { timeout: 30000 }).catch(() => { });
  await page.waitForTimeout(800);
}

test('admin creates project, assigns employee, employee updates status', async ({ page }) => {
  // ---------------- Admin login ----------------
  await clickLogin(page);
  await loginAuth0(page, 'admin@dragoshosting.ca', 'Potts#1083');

  // ---------------- Go to admin ----------------
  await gotoAdminPanel(page);

  // ---------------- Create project ----------------
  const projectName = `Testing-${Date.now()}`;

  await safeClick(page.getByRole('button', { name: 'ADD' }));

  // Fill modal (based on your recorded locators)
  await page.locator('form input[name="name"]').fill(projectName);

  // Select employees
  await safeClick(page.getByRole('button', { name: /select employees/i }));
  await safeClick(page.getByRole('button', { name: /cunninghamemployee4399@gmail/i }));
  await safeClick(page.getByRole('button', { name: /confirm/i }));

  await page.locator('form input[name="address.streetAddress"]').fill('test');
  await page.locator('form input[name="address.city"]').fill('test');

  // due date (your recording uses dueDate only)
  await page.locator('form input[name="dueDate"]').fill('2026-01-31');

  // project type dropdown (first combobox in modal)
  await page.getByRole('combobox').first().selectOption('SCHEDULED');

  await safeClick(page.getByRole('button', { name: 'Create', exact: true }));

  // Search for project to handle pagination
  await page.waitForTimeout(1000);
  await page.locator('input[name="search"]').fill(projectName);
  await page.keyboard.press('Enter');

  // Optional: ensure the created project appears (avoid strict mode issue by using heading)
  await expect(page.getByRole('heading', { name: projectName }).first()).toBeVisible({ timeout: 15000 });

  // ---------------- Logout admin ----------------
  await clickLogout(page);

  // ---------------- Employee login ----------------
  await clickLogin(page);
  await loginAuth0(page, 'cunninghamemployee4399@gmail.com', 'VladTechPassword123!');

  // ---------------- Go to employee tools ----------------
  await gotoEmployeeTools(page);

  // ---------------- Update status on the created project card ----------------
  // Find the card that contains the project heading, then the select inside it
  const projectCard = page.locator('div', {
    has: page.getByRole('heading', { name: projectName }).first(),
  }).first();

  await expect(projectCard).toBeVisible({ timeout: 15000 });

  const statusSelect = projectCard.locator('select').first();
  await statusSelect.waitFor({ state: 'visible', timeout: 15000 });

  // Change to IN_PROGRESS
  await statusSelect.selectOption('IN_PROGRESS');

  // IMPORTANT: your UI might not persist immediately; verify value changed
  await expect(statusSelect).toHaveValue('IN_PROGRESS', { timeout: 15000 });
});
