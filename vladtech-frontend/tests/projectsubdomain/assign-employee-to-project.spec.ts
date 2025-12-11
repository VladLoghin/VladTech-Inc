import { test, expect } from '../fixtures/fixtures.js';

test.describe('Admin assigns employee to project', () => {
  test('admin can assign an employee to a project', async ({ page, loginAs }) => {
    const targetEmployeeEmail = 'employee.vladtech@cle4rwater.ca';

    // 1) Login as admin
    await loginAs('admin');

    // 2) Go to admin panel
    await page.goto('http://localhost:5173/admin');

    await expect(
      page.getByRole('heading', { name: /Admin Area - Only for Admin Role/i })
    ).toBeVisible();

    // 3) Open first project via Edit button in "All Projects"
    const firstEditButton = page.getByRole('button', { name: 'Edit' }).first();
    await firstEditButton.click();

    // Wait for the ProjectModal to appear
    const projectModalTitle = page.getByRole('heading', {
      name: /Update Project|New Project/i,
    });
    await expect(projectModalTitle).toBeVisible();

    // 4) Open Employee picker
    const employeePickerButton = page.getByRole('button', {
      name: /Select employees?|Select employee/i,
    });
    await employeePickerButton.click();

    // Wait for EmployeeFinderModal
    await expect(
      page.getByRole('heading', { name: /Select Employee/i })
    ).toBeVisible();

    // 5) Click the employee row for employee.vladtech@cle4rwater.ca
    const employeeRow = page
      .getByRole('button')
      .filter({ hasText: targetEmployeeEmail })
      .first();

    await expect(employeeRow).toBeVisible();
    await employeeRow.click(); // toggles selection, modal stays open

    // 6) Click the Confirm button inside the modal
    const confirmButton = page.getByRole('button', { name: /^Confirm$/i });
    await confirmButton.click();

    // Modal should close
    await expect(
      page.getByRole('heading', { name: /Select Employee/i })
    ).toBeHidden();

    // 7) Check that the employee email now appears in the Employee field in the modal
    await expect(
      page.getByRole('button', {
        name: new RegExp(targetEmployeeEmail.replace('.', '\\.'), 'i'),
      })
    ).toBeVisible();

    // 8) Save the project
    const saveButton = page.getByRole('button', { name: /Save|Create/i });
    await saveButton.click();

    // Wait for modal to close
    await expect(projectModalTitle).toBeHidden();

    // 9) Verify the assigned employee email appears in "All Projects" list
    const allProjectsSection = page
      .getByRole('heading', { name: /All Projects/i })
      .locator('..'); // parent container

    await expect(
      allProjectsSection.getByText(targetEmployeeEmail, { exact: false })
    ).toBeVisible();
  });
});
