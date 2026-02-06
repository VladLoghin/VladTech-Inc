import { test as base, expect } from '@playwright/test'; 

type UserRole = 'admin' | 'employee' | 'client' | 'realClient' | 'realAdmin';

const USERS: Record<UserRole, { email: string; password: string }> = {
  admin: {
    email: 'admin.vladtech@cle4rwater.ca',
    password: 'Oshawott24!'
  },
  employee: {
    email: 'employee.vladtech@cle4rwater.ca',
    password: 'Oshawott24!'
  },
  client: {
    email: 'client.vladtech@cle4rwater.ca',
    password: 'Oshawott24!'
  },
  realClient: {
    email: 'hitswave@gmail.com',
    password: '30_BoomBap_30'
  },
  realAdmin: {
    email: '30boombap300@gmail.com',
    password: '30_BoomBap_30'
  }
};

export const test = base.extend<{
  loginAs: (role: UserRole) => Promise<void>;
  createProject: (projectNamePrefix?: string) => Promise<string>;
}>({
  loginAs: async ({ page }, use) => {
    // Provide a helper that logs in as ANY role (works for both mobile and desktop)
    async function loginAs(role: UserRole) {
      const user = USERS[role];

      await page.goto('http://localhost:5173/');

      // Check if we're in mobile view by checking viewport width
      const viewportSize = page.viewportSize();
      const isMobile = viewportSize && viewportSize.width < 768;

      if (isMobile) {
        // Mobile view: Open hamburger menu first
        const hamburgerButton = page.locator('button[aria-expanded]');
        await hamburgerButton.waitFor({ state: 'visible', timeout: 5000 });
        await hamburgerButton.click();
        await page.waitForTimeout(500); // Wait for menu animation to complete

        // Click LOGIN button inside mobile dropdown menu (now visible after animation)
        const loginButton = page.getByRole('button', { name: 'LOGIN' }).last();
        await loginButton.waitFor({ state: 'visible', timeout: 5000 });
        await loginButton.click();
      } else {
        // Desktop view: Click LOGIN button in desktop nav
        const loginButton = page.getByRole('button', { name: 'LOGIN' }).first();
        await loginButton.click();
      }

      // Fill Auth0 login form
      await page.getByLabel('Email').fill(user.email);
      await page.locator('input[type="password"]').fill(user.password);
      await page.getByRole('button', { name: 'Continue', exact: true }).click();

      // Wait for redirect back to app
      await page.waitForURL('http://localhost:5173/', { timeout: 10000 });
    }

    await use(loginAs);
  },

  createProject: async ({ page }, use) => {
    // Helper to create a project via the UI and return the project name
    async function createProject(projectNamePrefix: string = 'Test Project'): Promise<string> {
      const viewportSize = page.viewportSize();
      const isMobile = viewportSize && viewportSize.width < 768;

      // Navigate to Admin Panel if not already there
      const currentUrl = page.url();
      if (!currentUrl.includes('/admin')) {
        if (isMobile) {
          const hamburgerButton = page.locator('button[aria-expanded]');
          await hamburgerButton.waitFor({ state: 'visible', timeout: 5000 });
          await hamburgerButton.click();
          await page.waitForTimeout(500);
          await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
        } else {
          await page.getByRole('button', { name: /admin panel/i }).click();
        }
        await page.waitForURL('http://localhost:5173/admin', { timeout: 5000 });
      }

      await page.waitForTimeout(500);

      // Click "New Project" button
      await page.getByRole('button', { name: /add/i }).click();
      await page.waitForTimeout(500);

      // Wait for modal to appear
      await page.getByRole('heading', { name: /new project/i }).waitFor({ state: 'visible', timeout: 5000 });

      // Fill out the project form with unique name
      const timestamp = Date.now();
      const projectName = `${projectNamePrefix} ${timestamp}`;

      await page.locator('form input[name="name"]').fill(projectName);
      await page.locator('form select[name="address.country"]').selectOption('Canada');
      await page.locator('form select[name="address.province"]').selectOption('Quebec');
      await page.locator('form input[name="address.city"]').fill('Montreal');
      await page.locator('form input[name="dueDate"]').fill('2026-12-31');

      // Scroll down in the modal
      await page.evaluate(() => {
        const modal = document.querySelector('.overflow-y-auto');
        if (modal) modal.scrollTop = 400;
      });
      await page.waitForTimeout(300);

      await page.locator('form select[name="projectType"]').selectOption('SCHEDULED');
      await page.locator('form input[name="startDate"]').fill('2026-01-15');
      await page.locator('form textarea[name="description"]').fill('Auto-created by Playwright test');

      // Click "Create" button
      await page.getByRole('button', { name: /^create$/i }).click();

      // Wait for modal to close
      await page.getByRole('heading', { name: /new project/i }).waitFor({ state: 'hidden', timeout: 10000 });

      // Wait for UI to update
      await page.waitForTimeout(1000);

      // Search for the project to ensure it is visible (handling pagination)
      await page.waitForTimeout(500);
      await page.locator('input[name="search"]').fill(projectName);
      await page.keyboard.press('Enter');
      // Also click the button just in case
      // await page.getByRole('button', { name: /search projects/i }).click();

      // Wait for filtered results to load and find the specific project card
      const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
      await expect(projectCard).toBeVisible({ timeout: 10000 });

      return projectName;
    }

    await use(createProject);
  }
});

export { expect };
