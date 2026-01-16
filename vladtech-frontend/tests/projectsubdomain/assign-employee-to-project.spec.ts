import { test, expect } from '../fixtures/fixtures.js';

test.describe('Admin assigns employee to project', () => {
  test('assign employee to project', async ({ page, loginAs, createProject }) => {
    const targetEmployeeEmail = 'employee.vladtech@cle4rwater.ca';

    // 1) Login as admin
    await loginAs('admin');

    // 2) Create a project to assign employee to
    const projectName = await createProject('Employee Assignment Test');

    // 3) Wait for page to be ready
    await page.waitForTimeout(500);

    // 4) Find our project and open edit modal
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 5000 });
    await projectCard.getByRole('button', { name: 'Edit' }).click();

    // Wait for the ProjectModal to appear
    const projectModalTitle = page.getByRole('heading', {
      name: /Update Project|New Project/i,
    });
    await expect(projectModalTitle).toBeVisible();

    // 5) Open Employee picker
    const employeePickerButton = page.getByRole('button', {
      name: /Select employees?|Select employee/i,
    });
    await employeePickerButton.click();

    // Wait for EmployeeFinderModal
    await expect(
      page.getByRole('heading', { name: /Select Employee/i })
    ).toBeVisible();

    // 6) Click the employee row for employee.vladtech@cle4rwater.ca
    const employeeRow = page
      .getByRole('button')
      .filter({ hasText: targetEmployeeEmail })
      .first();

    await expect(employeeRow).toBeVisible();
    await employeeRow.click(); // toggles selection, modal stays open

    // 7) Click the Confirm button inside the modal
    const confirmButton = page.getByRole('button', { name: /^Confirm$/i });
    await confirmButton.click();

    // Modal should close
    await expect(
      page.getByRole('heading', { name: /Select Employee/i })
    ).toBeHidden();

    // 8) Check that the employee email now appears in the Employee field in the modal
    await expect(
      page.getByRole('button', {
        name: new RegExp(targetEmployeeEmail.replace('.', '\\.'), 'i'),
      })
    ).toBeVisible();

    // 9) Save the project
    const saveButton = page.getByRole('button', { name: 'Save' });
    await saveButton.click();

    // Wait for modal to close
    await expect(projectModalTitle).toBeHidden({ timeout: 15000 });

    // 10) Verify the assigned employee email appears in the project card
    await page.waitForTimeout(1000);
    const updatedProjectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(
      updatedProjectCard.getByText(targetEmployeeEmail, { exact: false })
    ).toBeVisible();
  });
});
