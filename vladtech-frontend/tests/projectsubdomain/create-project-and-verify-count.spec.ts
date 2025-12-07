import { test, expect } from '../fixtures/fixtures';

test('admin creates project and verifies count on homepage', async ({ page, loginAs }) => {
  // Step 1: Login as admin using fixtures
  await loginAs('admin');
  console.log('✅ Step 1: Logged in as admin');

  // Check if mobile view
  const viewportSize = page.viewportSize();
  const isMobile = viewportSize && viewportSize.width < 768;

  // Step 2: Navigate to Admin Panel
  if (isMobile) {
    // Mobile: Open hamburger menu and click ADMIN PANEL
    const hamburgerButton = page.locator('button svg').first();
    await hamburgerButton.click();
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
  } else {
    // Desktop: Click ADMIN PANEL in navbar
    await page.getByRole('button', { name: /admin panel/i }).click();
  }
  await page.waitForURL('http://localhost:5173/admin');
  console.log('✅ Step 2: Navigated to Admin Panel');

  // Step 3: Get current project count before creating new project
  await page.waitForTimeout(1000);
  const projectCards = page.locator('.border-2.border-black.rounded-xl').filter({ hasText: /Project ID:|ID:/i });
  const initialCount = await projectCards.count();
  console.log(`📊 Initial project count: ${initialCount}`);

  // Step 4: Click yellow "New Project" button (top right)
  await page.getByRole('button', { name: /new project/i }).click();
  await page.waitForTimeout(500);
  console.log('✅ Step 4: Clicked New Project button');

  // Wait for modal to appear
  await expect(page.getByRole('heading', { name: /new project/i })).toBeVisible({ timeout: 5000 });
  console.log('✅ Step 5: New Project modal opened');

  // Step 6: Fill out the project form
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
  
  console.log(`✅ Step 6: Filled project form with name: ${projectName}`);

  // Step 7: Click yellow "Save" button
  await page.getByRole('button', { name: /^save$/i }).click();
  console.log('✅ Step 7: Clicked Save button');

  // Wait for modal to close
  await expect(page.getByRole('heading', { name: /new project/i })).not.toBeVisible({ timeout: 10000 });
  console.log('✅ Step 8: Modal closed, project saved');

  // Step 9: Verify new project appears in the list
  await page.waitForTimeout(2000); // Give time for UI to update
  const updatedProjectCards = page.locator('.border-2.border-black.rounded-xl').filter({ hasText: /Project ID:|ID:/i });
  const newCount = await updatedProjectCards.count();
  console.log(`📊 New project count: ${newCount}`);
  
  expect(newCount).toBe(initialCount + 1);
  console.log('✅ Step 9: Verified new project appears in list');

  // Step 10: Navigate back to homepage
  await page.goto('http://localhost:5173/');
  await page.waitForLoadState('networkidle');
  console.log('✅ Step 10: Back on homepage');

  // Step 11: Click "ABOUT" button in the navbar to scroll to About section
  if (isMobile) {
    // Mobile: Open hamburger menu and click ABOUT
    const hamburgerButton = page.locator('button svg').first();
    await hamburgerButton.click();
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: 'ABOUT' }).first().click();
  } else {
    // Desktop: Click ABOUT in navbar
    await page.getByRole('button', { name: /^about$/i }).click();
  }
  await page.waitForTimeout(1500); // Wait for smooth scroll
  console.log('✅ Step 11: Clicked ABOUT, scrolled to About section');

  // Step 12: Verify project count is displayed correctly in the About section
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
    console.log(`✅ Step 12: Project count verified in About section: ${displayedCount}`);
  } else {
    console.log('⚠️ Could not parse project count from About section');
  }
  
  console.log(`\n🎉 TEST PASSED! Created project "${projectName}" and verified count: ${newCount}`);
});
