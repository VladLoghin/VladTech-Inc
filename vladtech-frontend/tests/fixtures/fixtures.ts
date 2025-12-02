import { test as base, expect } from '@playwright/test';

type UserRole = 'admin' | 'employee' | 'client';

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
  }
};

export const test = base.extend<{
  loginAs: (role: UserRole) => Promise<void>;
}>({
  loginAs: async ({ page }, use) => {
    // Provide a helper that logs in as ANY role
    async function loginAs(role: UserRole) {
      const user = USERS[role];

      await page.goto('http://localhost:5173/');
      await page.getByRole('button', { name: 'Log In' }).nth(1).click();
      await page.getByLabel('Email').fill(user.email);
      await page.getByLabel('Password').fill(user.password);
      await page.getByRole('button', { name: 'Log In' }).click();
    }

    await use(loginAs);
  }
});

export { expect };
