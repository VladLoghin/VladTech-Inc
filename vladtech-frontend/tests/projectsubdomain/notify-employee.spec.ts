// tests/projectsubdomain/admin-assign-sends-email.mailhog.spec.ts
import { test, expect, Page, Locator } from '@playwright/test';

const MAILHOG_BASE = 'http://localhost:8025';

// ---------- tiny helpers (same idea as your other file) ----------
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
    await safeClick(page.locator('button:has(svg)').first());
    await page.waitForTimeout(300);
    await safeClick(page.getByRole('button', { name: 'LOGIN' }).last());
  } else {
    await safeClick(page.getByRole('button', { name: 'LOGIN' }).first());
  }
}

async function loginAuth0(page: Page, email: string, password: string) {
  const emailBox = page.getByRole('textbox', { name: /email/i }).first();
  await emailBox.waitFor({ state: 'visible', timeout: 20000 });
  await emailBox.fill(email);

  await page.locator('input[type="password"]').first().fill(password);

  await safeClick(page.getByRole('button', { name: /continue/i }).first());

  await page.waitForURL(/http:\/\/localhost:5173\/?$/, { timeout: 30000 });
}

async function gotoAdminPanel(page: Page) {
  const viewport = page.viewportSize();
  const isMobile = viewport && viewport.width < 768;

  await page.evaluate(() => window.scrollTo(0, 0));
  await page.waitForTimeout(200);

  if (isMobile) {
    await safeClick(page.locator('button:has(svg)').first());
    await page.waitForTimeout(300);
    await safeClick(page.getByRole('button', { name: /admin panel/i }).first());
  } else {
    await safeClick(page.getByRole('button', { name: /admin panel/i }).first());
  }

  await expect(page.getByRole('heading', { name: /admin area/i })).toBeVisible({ timeout: 20000 });
}

// ---------- MailHog helpers ----------
async function mailhogClear(request: any) {
  // MailHog supports DELETE /api/v1/messages
  await request.delete(`${MAILHOG_BASE}/api/v1/messages`).catch(() => { });
}

async function mailhogWaitForMessage(
  request: any,
  opts: { toEmail: string; subjectIncludes?: string; timeoutMs?: number }
) {
  const timeoutMs = opts.timeoutMs ?? 20000;
  const deadline = Date.now() + timeoutMs;

  while (Date.now() < deadline) {
    const res = await request.get(`${MAILHOG_BASE}/api/v2/messages`);
    if (res.ok()) {
      const data = await res.json();

      // v2 format: { total, count, items: [...] }
      const items = (data?.items ?? []) as any[];

      const found = items.find((msg) => {
        const headers = msg?.Content?.Headers ?? {};
        const toList: string[] = (headers?.To ?? []).map((x: any) => String(x));
        const subjectList: string[] = (headers?.Subject ?? []).map((x: any) => String(x));

        const toMatch = toList.some((t) => t.toLowerCase().includes(opts.toEmail.toLowerCase()));
        const subjectMatch = opts.subjectIncludes
          ? subjectList.some((s) => s.toLowerCase().includes(opts.subjectIncludes!.toLowerCase()))
          : true;

        return toMatch && subjectMatch;
      });

      if (found) return found;
    }

    await new Promise((r) => setTimeout(r, 750));
  }

  throw new Error(
    `Timed out waiting for MailHog email to "${opts.toEmail}"` +
    (opts.subjectIncludes ? ` with subject containing "${opts.subjectIncludes}"` : '')
  );
}

test('admin creates project, assigns employee, MailHog receives assignment email', async ({ page, request }) => {
  // If MailHog isn’t running, fail with a clear error
  const ping = await request.get(`${MAILHOG_BASE}/api/v2/messages`).catch(() => null);
  if (!ping || !ping.ok()) {
    throw new Error(
      `MailHog not reachable at ${MAILHOG_BASE}. Start it in docker compose (port 8025).`
    );
  }

  await mailhogClear(request);

  // ---------------- Admin login ----------------
  await clickLogin(page);
  await loginAuth0(page, 'admin@dragoshosting.ca', 'Potts#1083');

  // ---------------- Go to admin ----------------
  await gotoAdminPanel(page);

  // ---------------- Create project + assign employee in modal (based on your recorded flow) ----------------
  const projectName = `Testing-${Date.now()}`;
  const employeeEmail = 'cunninghamemployee4399@gmail.com';

  await safeClick(page.getByRole('button', { name: 'ADD' }));

  await page.locator('form input[name="name"]').fill(projectName);

  // Assign employee (your modal has this)
  await safeClick(page.getByRole('button', { name: /select employees/i }));
  await safeClick(page.getByRole('button', { name: /cunninghamemployee4399@gmail/i }));
  await safeClick(page.getByRole('button', { name: /confirm/i }));

  await page.locator('form input[name="address.streetAddress"]').fill('test');
  await page.locator('form input[name="address.city"]').fill('test');

  await page.locator('form input[name="dueDate"]').fill('2026-01-31');
  await page.getByRole('combobox').first().selectOption('SCHEDULED');

  await safeClick(page.getByRole('button', { name: 'Create', exact: true }));

  // Wait for modal to close before searching
  await expect(page.getByRole('heading', { name: /new project/i })).toBeHidden();
  await page.waitForTimeout(1000); // Allow list refresh

  // Search for project to handle pagination
  await page.locator('input[name="search"]').fill(projectName);
  await page.keyboard.press('Enter');

  // Ensure it appears
  await expect(page.getByRole('heading', { name: projectName }).first()).toBeVisible({ timeout: 20000 });

  // ---------------- Verify email in MailHog ----------------
  // Adjust subjectIncludes if you know the exact subject text
  const msg = await mailhogWaitForMessage(request, {
    toEmail: employeeEmail,
    subjectIncludes: 'assigned', // change to whatever your subject contains, or remove this line
    timeoutMs: 25000,
  });

  // Basic assertions (MailHog JSON structure)
  const headers = msg?.Content?.Headers ?? {};
  const toHeader = (headers.To ?? []).join(' ');
  const subjectHeader = (headers.Subject ?? []).join(' ');

  expect(toHeader.toLowerCase()).toContain(employeeEmail.toLowerCase());
  // If your subject is different, change this assertion
  expect(subjectHeader.toLowerCase()).toContain('assigned');
});
