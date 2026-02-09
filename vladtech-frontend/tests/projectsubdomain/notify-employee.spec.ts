import { test, expect } from '../fixtures/fixtures.js';

test.describe('Notify employee when assigned to project', () => {
  test('send email notification when employee is assigned to project', async ({ page, loginAs, createProject }) => {
    const targetEmployeeEmail = 'employee.vladtech@cle4rwater.ca';

    // 1) Login as admin
    await loginAs('admin');

    // 2) Create a project to assign employee to
    const projectName = await createProject('Notify Employee Test');

    // 3) Wait for page to be ready
    await page.waitForTimeout(500);

    // 4) Find our project and open edit modal
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 5000 });
    await projectCard.getByRole('button', { name: 'Edit' }).click();

    // Wait for the ProjectModal to appear
    const projectModalTitle = page.getByRole('heading', {
      name: /Edit Project|New Project/i,
    });
    await expect(projectModalTitle).toBeVisible();

    // 5) Scroll down to find the employee assignment field
    await page.evaluate(() => {
      const modal = document.querySelector('.overflow-y-auto');
      if (modal) modal.scrollTop = 400;
    });
    await page.waitForTimeout(300);

    // 6) Open Employee picker
    const employeePickerButton = page.getByRole('button', {
      name: /Select employees?|Select employee/i,
    });
    await employeePickerButton.click();

    // Wait for EmployeeFinderModal
    await expect(
      page.getByRole('heading', { name: /Select Employee/i })
    ).toBeVisible();

    // 7) Click the employee row for employee.vladtech@cle4rwater.ca
    const employeeRow = page
      .getByRole('button')
      .filter({ hasText: targetEmployeeEmail })
      .first();

    await expect(employeeRow).toBeVisible({ timeout: 15000 });
    await employeeRow.click(); // toggles selection, modal stays open

    // 8) Click the Confirm button inside the modal
    const confirmButton = page.getByRole('button', { name: /^Confirm$/i });
    await confirmButton.click();

    // Modal should close
    await expect(
      page.getByRole('heading', { name: /Select Employee/i })
    ).toBeHidden();

    // 9) Check that the employee email now appears in the Employee field in the modal
    await expect(
      page.getByRole('button', {
        name: new RegExp(targetEmployeeEmail.replace('.', '\\.'), 'i'),
      })
    ).toBeVisible();

    // 10) Save the project
    const saveButton = page.getByRole('button', { name: 'Save' });
    await saveButton.click();

    // Wait for modal to close
    try {
      await expect(projectModalTitle).toBeHidden({ timeout: 15000 });
    } catch (e) {
      // If modal is still open, check for error message
      const errorAlert = page.locator('div.bg-red-100.border-red-400');
      if (await errorAlert.isVisible()) {
        const errorText = await errorAlert.innerText();
        throw new Error(`Project update failed with error: ${errorText}`);
      }
      throw e;
    }

    // 11) Wait a moment to ensure backend processes the assignment and sends email
    await page.waitForTimeout(2000);

    // 12) Check mailhog for the notification email
    // Navigate to mailhog API and fetch recent messages
    const mailhogResponse = await page.request.get('http://localhost:8025/api/v2/messages');
    expect(mailhogResponse.ok()).toBeTruthy();

    const mailhogData = await mailhogResponse.json();
    console.log('Mailhog response:', JSON.stringify(mailhogData, null, 2));
    
    // Look for an email sent to the target employee
    const emailToEmployee = mailhogData.items?.find((item: any) => {
      // Try different recipient formats
      const recipients = item.To || [];
      const rawRecipients = item.Raw?.To || [];
      
      // Check both structured and raw recipient formats
      const foundInStructured = recipients.some((recipient: any) => {
        const email = typeof recipient === 'string' ? recipient : recipient.Mailbox + '@' + recipient.Domain;
        return email.includes(targetEmployeeEmail);
      });
      
      const foundInRaw = rawRecipients.some((recipient: any) => {
        const email = typeof recipient === 'string' ? recipient : recipient;
        return email.includes(targetEmployeeEmail);
      });
      
      return foundInStructured || foundInRaw;
    });

    // Log all email recipients for debugging
    if (!emailToEmployee) {
      console.log('All emails in mailhog:');
      mailhogData.items?.forEach((item: any, index: number) => {
        console.log(`Email ${index}:`, {
          To: item.To,
          From: item.From,
          Subject: item.Content?.Headers?.Subject?.[0],
        });
      });
    }

    // Assert that the email was found
    expect(emailToEmployee).toBeDefined();
    expect(emailToEmployee).not.toBeNull();

    // 13) Verify email contains relevant project information
    const emailBody = emailToEmployee.Content?.Body || emailToEmployee.Raw?.Data || '';
    expect(emailBody).toContain(projectName);
    
    // 14) Verify the email subject mentions project assignment
    const emailSubject = emailToEmployee.Content?.Headers?.Subject?.[0] || '';
    expect(emailSubject.toLowerCase()).toContain('project');
  });
});
