import { test, expect } from '@playwright/test';

test('admin creates project and verifies count on homepage', async ({ page }) => {
  // Step 1: Navigate to the homepage
  await page.goto('http://localhost:5173/');
  console.log('✅ Step 1: Navigated to homepage');

  // Step 2: Click the LOGIN button (top right, in navbar)
  await page.getByRole('button', { name: /^LOGIN$/i }).click();
  console.log('✅ Step 2: Clicked LOGIN button');

  // Step 3: Fill in Auth0 credentials
  await page.waitForSelector('input[type="email"], input[name="username"]', { timeout: 10000 });
  await page.getByLabel(/email/i).fill('30boombap300@gmail.com');
  await page.getByLabel(/password/i).fill('30_BoomBap_30');
  console.log('✅ Step 3: Filled in credentials');

  // Step 4: Click the blue "LOG IN" button in Auth0 modal
  await page.getByRole('button', { name: /log in/i }).click();
  console.log('✅ Step 4: Clicked Auth0 LOG IN button');

  // Wait for successful login and redirect back to homepage
  await page.waitForURL('http://localhost:5173/', { timeout: 15000 });
  await page.waitForLoadState('networkidle');
  console.log('✅ Step 5: Logged in successfully, back on homepage');

  // Step 6: Click "ADMIN PANEL" button in the navbar
  await page.getByRole('button', { name: /admin panel/i }).click();
  await page.waitForURL('http://localhost:5173/admin');
  console.log('✅ Step 6: Navigated to Admin Panel');

  // Step 7: Get current project count before creating new project
  await page.waitForTimeout(1000);
  const projectCards = page.locator('.border-2.border-black.rounded-xl').filter({ hasText: /Project ID:|ID:/i });
  const initialCount = await projectCards.count();
  console.log(`📊 Initial project count: ${initialCount}`);

  // Step 8: Click yellow "New Project" button (top right)
  await page.getByRole('button', { name: /new project/i }).click();
  await page.waitForTimeout(500);
  console.log('✅ Step 8: Clicked New Project button');

  // Wait for modal to appear
  await expect(page.getByRole('heading', { name: /new project/i })).toBeVisible({ timeout: 5000 });
  console.log('✅ Step 9: New Project modal opened');

  // Step 10: Fill out the project form
  const timestamp = Date.now();
  const projectName = `Playwright Test ${timestamp}`;
  
  await page.locator('input[name="name"]').fill(projectName);
  await page.locator('input[name="address.city"]').fill('Montreal');
  await page.locator('input[name="dueDate"]').fill('2025-12-31');
  
  // Scroll down in the modal to see more fields
  await page.evaluate(() => {
    const modal = document.querySelector('.overflow-y-auto');
    if (modal) modal.scrollTop = 400;
  });
  await page.waitForTimeout(300);
  
  await page.locator('select[name="projectType"]').selectOption('SCHEDULED');
  await page.locator('input[name="startDate"]').fill('2025-01-15');
  await page.locator('textarea[name="description"]').fill('Automated test project created by Playwright');
  
  console.log(`✅ Step 10: Filled project form with name: ${projectName}`);

  // Step 11: Click yellow "Save" button
  await page.getByRole('button', { name: /^save$/i }).click();
  console.log('✅ Step 11: Clicked Save button');

  // Wait for modal to close
  await expect(page.getByRole('heading', { name: /new project/i })).not.toBeVisible({ timeout: 10000 });
  console.log('✅ Step 12: Modal closed, project saved');

  // Step 13: Verify new project appears in the list
  await page.waitForTimeout(2000); // Give time for UI to update
  const updatedProjectCards = page.locator('.border-2.border-black.rounded-xl').filter({ hasText: /Project ID:|ID:/i });
  const newCount = await updatedProjectCards.count();
  console.log(`📊 New project count: ${newCount}`);
  
  expect(newCount).toBe(initialCount + 1);
  console.log('✅ Step 13: Verified new project appears in list');

  // Step 14: Navigate back to homepage by clicking Home in navbar or going directly
  await page.goto('http://localhost:5173/');
  await page.waitForLoadState('networkidle');
  console.log('✅ Step 14: Back on homepage');

  // Step 15: Click "ABOUT" button in the navbar to scroll to About section
  await page.getByRole('button', { name: /^about$/i }).click();
  await page.waitForTimeout(1500); // Wait for smooth scroll
  console.log('✅ Step 15: Clicked ABOUT, scrolled to About section');

  // Step 16: Verify project count is displayed correctly in the About section
  // Look for the stats section with "PROJECTS" text
  const projectsLabel = page.locator('text=/PROJECTS/i').first();
  await expect(projectsLabel).toBeVisible({ timeout: 5000 });
  
  // Get the project count number (the yellow text above "PROJECTS")
  const projectCountElement = page.locator('.text-5xl.text-yellow-400').filter({ hasText: /^\d+\+?$/ }).first();
  await expect(projectCountElement).toBeVisible();
  
  const displayedText = await projectCountElement.textContent();
  console.log(`📊 Displayed project count text: ${displayedText}`);
  
  // Extract the number from the text (e.g., "5+" -> 5)
  const countMatch = displayedText?.match(/(\d+)/);
  if (countMatch) {
    const displayedCount = parseInt(countMatch[1]);
    console.log(`📊 Parsed project count: ${displayedCount}`);
    expect(displayedCount).toBe(newCount);
    console.log(`✅ Step 16: Project count verified in About section: ${displayedCount}`);
  } else {
    console.log('⚠️ Could not parse project count from About section');
  }
  
  console.log(`\n🎉 TEST PASSED! Created project "${projectName}" and verified count: ${newCount}`);
});
