import { test, expect } from './fixtures/fixtures.ts';

test('create project and send to portfolio', async ({ page, loginAs, createProject }) => {
  // Login as admin
  await loginAs('admin');
  console.log('✅ Logged in as admin');

  // Create a new project
  const projectName = await createProject('Portfolio Test');
  console.log(`✅ Created project: ${projectName}`);

  // Find the specific project card we just created
  const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
  await expect(projectCard).toBeVisible({ timeout: 5000 });

  // Find and click the "Send to Portfolio" button within this specific project card
  const sendButton = projectCard.getByRole('button', { name: /send to portfolio/i });
  await sendButton.click();
  await page.waitForTimeout(500);
  console.log('✅ Opened Send to Portfolio modal');

  // Verify modal opened
  await expect(page.getByRole('heading', { name: /send to portfolio/i })).toBeVisible();

  // Select portfolio type
  const typeSelect = page.locator('select').first();
  await typeSelect.selectOption('Kitchen');
  console.log('✅ Selected Kitchen type');

  // Wait for user to manually add image
  console.log('⏳ Waiting 8 seconds for you to manually add the image...');
  await page.waitForTimeout(8000);
  
  // Find the yellow submit button inside the modal
  const submitButton = page.locator('button.bg-yellow-400').filter({ hasText: /send to portfolio/i });
  
  // Verify button is visible and enabled
  await expect(submitButton).toBeVisible({ timeout: 5000 });
  await expect(submitButton).toBeEnabled({ timeout: 5000 });
  console.log('✅ Submit button found and enabled');

  // Click submit
  await submitButton.click();
  console.log('🚀 Clicked Send to Portfolio button');

  // Wait for modal to close (indicates success)
  await expect(page.getByRole('heading', { name: /send to portfolio/i })).not.toBeVisible({ timeout: 10000 });
  console.log('✅ Modal closed - project sent to portfolio successfully');
});
