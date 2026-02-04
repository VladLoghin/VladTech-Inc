import { test, expect } from '../fixtures/fixtures.ts';

test('create project and verify count', async ({ page, loginAs }) => {
  // Step 1: Login as admin using fixtures
  await loginAs('admin');
  console.log('✅ Step 1: Logged in as admin');

  // Check if mobile view
  const viewportSize = page.viewportSize();
  const isMobile = viewportSize && viewportSize.width < 768;

  // Helper function to get project count from About section
  const getProjectCountFromHome = async () => {
    // Navigate to homepage
    await page.goto('http://localhost:5173/');
    await page.waitForLoadState('networkidle');

    // Scroll to About section and wait
    if (isMobile) {
      const hamburgerButton = page.locator('button svg').first();
      if (await hamburgerButton.isVisible()) {
        await hamburgerButton.click();
        await page.waitForTimeout(500);
        await page.getByRole('button', { name: 'ABOUT' }).first().click();
      }
    } else {
      await page.getByRole('button', { name: /^about$/i }).click();
    }
    await page.waitForTimeout(1500);

    // Get count text
    const projectCountElement = page.locator('.text-5xl.text-yellow-400').filter({ hasText: /^\d+\+?$/ }).first();
    await expect(projectCountElement).toBeVisible();
    const text = await projectCountElement.textContent();
    const match = text?.match(/(\d+)/);
    return match ? parseInt(match[1]) : 0;
  };

  // Step 2: Get initial count from Homepage
  const initialHomeCount = await getProjectCountFromHome();
  console.log(`📊 Initial Homepage project count: ${initialHomeCount}`);

  // Step 3: Navigate to Admin Panel
  if (isMobile) {
    const hamburgerButton = page.locator('button svg').first();
    await hamburgerButton.click();
    await page.waitForTimeout(500);
    await page.getByRole('button', { name: 'ADMIN PANEL' }).first().click();
  } else {
    await page.getByRole('button', { name: /admin panel/i }).click();
  }
  await page.waitForURL('http://localhost:5173/admin');
  console.log('✅ Step 3: Navigated to Admin Panel');

  // Step 4: Click yellow "New Project" button
  await page.getByRole('button', { name: /add/i }).click();
  await page.waitForTimeout(500);

  // Wait for modal to appear
  await expect(page.getByRole('heading', { name: /new project/i })).toBeVisible({ timeout: 5000 });

  // Step 5: Fill out the project form
  const timestamp = Date.now();
  const projectName = `Playwright Test ${timestamp}`;

  await page.locator('form input[name="name"]').fill(projectName);
  await page.locator('form input[name="address.city"]').fill('Montreal');
  await page.locator('form input[name="dueDate"]').fill('2026-12-31');

  await page.evaluate(() => {
    const modal = document.querySelector('.overflow-y-auto');
    if (modal) modal.scrollTop = 400;
  });
  await page.waitForTimeout(300);

  await page.locator('form select[name="projectType"]').selectOption('SCHEDULED');
  await page.locator('form input[name="startDate"]').fill('2026-10-15');
  await page.locator('form textarea[name="description"]').fill('Automated test project created by Playwright');

  // Step 6: Click create
  await page.getByRole('button', { name: /^create$/i }).click();

  // Wait for modal to close
  await expect(page.getByRole('heading', { name: /add/i })).not.toBeVisible({ timeout: 10000 });
  console.log('✅ Step 6: Project created');

  // Step 7: Verify new project appears in Admin List (sanity check)
  // Instead of just counting, we check if our specific project is in the list
  await page.locator('input[name="search"]').fill(projectName);
  await page.keyboard.press('Enter');

  await expect(page.locator('body')).toContainText(projectName, { timeout: 10000 });
  console.log('✅ Step 7: Verified new project is present in Admin List');

  // Step 8: Get final count from Homepage
  const finalHomeCount = await getProjectCountFromHome();
  console.log(`📊 Final Homepage project count: ${finalHomeCount}`);

  // We relax this to be >= because other tests might have added projects in parallel
  expect(finalHomeCount).toBeGreaterThanOrEqual(initialHomeCount + 1);
  console.log(`\n🎉 TEST PASSED! Homepage count incremented from ${initialHomeCount} to ${finalHomeCount} (>= expected)`);
});
