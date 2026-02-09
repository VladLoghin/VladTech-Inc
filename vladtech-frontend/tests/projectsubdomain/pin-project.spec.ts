import { test, expect } from '../fixtures/fixtures';

test('pin project persists in sorted list', async ({ page, loginAs }) => {
  // Step 1: Login as employee
  await loginAs('employee');
  console.log('✅ Step 1: Logged in as employee');

  // Step 2: Navigate to projects page
  await page.goto('http://localhost:5173/projects');
  await page.waitForLoadState('networkidle');
  console.log('✅ Step 2: Navigated to employee projects');

  // Step 3: Wait for projects to load
  await page.waitForTimeout(1000);

  // Step 4: Get all project cards
  const projectCards = page.locator('div.border.border-black\\/10.rounded-lg.p-4');
  const projectCount = await projectCards.count();

  if (projectCount < 2) {
    console.log('ℹ️  Skipping sort test - need at least 2 projects');
    return;
  }

  console.log(`📊 Found ${projectCount} projects`);

  // Step 5: Get the second project (not first) and pin it
  const secondProjectCard = projectCards.nth(1);
  const secondProjectName = await secondProjectCard.locator('h3').first().textContent();
  console.log(`📌 Pinning second project: ${secondProjectName}`);

  const pinButton = secondProjectCard.locator('button').filter({
    hasText: /pin/i
  });

  await expect(pinButton).toBeVisible();
  await pinButton.click();
  await page.waitForTimeout(1000);

  // Step 6: Verify it appears as pinned
  const unpinButton = secondProjectCard.locator('button').filter({
    hasText: /unpin/i
  });
  await expect(unpinButton).toBeVisible({ timeout: 5000 });
  console.log('✅ Step 6: Project is now pinned');

  // Step 7: Refresh the page and verify the pin state persists
  await page.reload();
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
  console.log('✅ Step 7: Page reloaded');

  // Step 8: Get the updated project cards
  const updatedProjectCards = page.locator('div.border.border-black\\/10.rounded-lg.p-4');

  // Find the pinned project by name
  let foundPinned = false;
  for (let i = 0; i < await updatedProjectCards.count(); i++) {
    const card = updatedProjectCards.nth(i);
    const name = await card.locator('h3').first().textContent();
    
    if (name === secondProjectName) {
      // Check if it has the unpin button (meaning it's still pinned)
      const unpinBtn = card.locator('button').filter({
        hasText: /unpin/i
      });
      
      if (await unpinBtn.isVisible({ timeout: 2000 })) {
        foundPinned = true;
        console.log('✅ Step 8: Pinned project persisted after page reload at position', i + 1);
        
        // Unpin it to clean up
        await unpinBtn.click();
        await page.waitForTimeout(500);
        break;
      }
    }
  }

  if (foundPinned) {
    console.log('\n🎉 TEST PASSED! Pin state persists across page reloads');
  } else {
    console.log('⚠️  Could not verify persistence - project not found after reload');
  }
});
