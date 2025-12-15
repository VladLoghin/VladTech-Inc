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
        const hamburgerButton = page.locator('button svg').first();
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
      await page.getByRole('button', { name: 'Continue' }).click();
      
      // Wait for redirect back to app
      await page.waitForURL('http://localhost:5173/', { timeout: 10000 });
    }

    await use(loginAs);
  }
});

export { expect };
