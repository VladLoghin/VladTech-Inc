import { test, expect } from '../fixtures/fixtures';

test('edit project modal assign client', async ({ page, loginAs, createProject }) => {
  await loginAs('admin');

  // Create a project first
  const projectName = await createProject('Client Assignment Test');

  // Wait for page to be ready
  await page.waitForTimeout(500);

  // Find our project and open edit modal
  const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
  await expect(projectCard).toBeVisible({ timeout: 5000 });
  await projectCard.getByRole('button', { name: 'Edit' }).click();

  await expect(page.getByRole('heading', { name: 'Update Project' })).toBeVisible();

  // Clear existing client if any
  const clearButton = page.getByRole('button', { name: 'Clear' }).first();
  if (await clearButton.isVisible().catch(() => false)) {
    await clearButton.click();
  }

  // Assign client
  await page.getByRole('button', { name: 'Select a client' }).click();
  await page.getByRole('textbox', { name: 'Search by email, name, or' }).click();
  await page.getByRole('textbox', { name: 'Search by email, name, or' }).fill('client.vladtech@cle4rwater.ca');
  await page.getByRole('button', { name: 'Search' }).click();
  await page.getByRole('button', { name: 'client.vladtech@cle4rwater.ca' }).click();
  await page.getByRole('button', { name: 'Save' }).click();

  // Verify client appears in the project list
  await page.waitForTimeout(1000);
  const updatedProjectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
  await expect(updatedProjectCard.getByText('client.vladtech@cle4rwater.ca').first()).toBeVisible();
});
