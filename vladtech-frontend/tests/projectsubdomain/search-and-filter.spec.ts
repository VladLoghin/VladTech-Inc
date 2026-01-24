import { test, expect } from '../fixtures/fixtures';

test('search and filter projects by name and priority', async ({ page, loginAs, createProject }) => {
    // Step 1: Login as admin
    await loginAs('admin');

    // Step 2: Create a project with a unique name
    const uniquePrefix = 'SearchFilterTest';
    const projectName = await createProject(uniquePrefix);
    console.log(`✅ Created project: ${projectName}`);

    // Step 3: Navigate to the project and edit it to set HIGH priority
    // First, ensure we're on the admin page and can see the project
    const viewportSize = page.viewportSize();
    const isMobile = viewportSize && viewportSize.width < 768;

    // Find the project card and click edit
    const projectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectCard).toBeVisible({ timeout: 10000 });

    // Click the edit button on the project card
    const editButton = projectCard.getByRole('button', { name: /edit/i });
    await editButton.click();
    await page.waitForTimeout(500);

    // Wait for the edit modal to appear
    await expect(page.getByRole('heading', { name: /edit project/i })).toBeVisible({ timeout: 5000 });

    // Change priority to HIGH
    const prioritySelect = page.locator('form select[name="priority"]');
    await prioritySelect.selectOption('HIGH');
    console.log('✅ Changed priority to HIGH');

    // Click Update button to save
    await page.getByRole('button', { name: /save/i }).click();

    // Wait for modal to close
    await expect(page.getByRole('heading', { name: /edit project/i })).not.toBeVisible({ timeout: 10000 });
    console.log('✅ Project updated successfully');

    // Step 4: Clear results and prepare for combined search
    await page.waitForTimeout(1000);

    // Open the search and filter panel
    const filtersPanelToggle = page.locator('text=Search & Filter').or(page.locator('text=Recherche et filtres'));
    await filtersPanelToggle.click();
    await page.waitForTimeout(500);

    // Step 5: Use combined search (name + priority filter)
    // First clear any existing filters
    await page.getByRole('button', { name: /clear filters|effacer/i }).click();
    await page.waitForTimeout(500);

    // Re-open the filter panel if it closed
    const filtersPanel = page.locator('.transition-all.duration-300.ease-in-out').filter({ has: page.locator('select[name="priority"]') });
    if (!await filtersPanel.isVisible()) {
        await filtersPanelToggle.click();
        await page.waitForTimeout(500);
    }

    // Set the search field to search by name
    await page.locator('select[name="searchField"]').selectOption('name');

    // Enter the full project name in the search input
    await page.locator('input[name="search"]').fill(projectName);
    console.log(`✅ Entered search term: ${projectName}`);

    // Set priority filter to HIGH
    await page.locator('select[name="priority"]').selectOption('HIGH');
    console.log('✅ Set priority filter to HIGH');

    // Click the Search Projects button
    await page.getByRole('button', { name: /search projects|rechercher des projets/i }).click();
    await page.waitForTimeout(1500); // Wait for search results to load
    console.log('✅ Submitted combined search');

    // Step 6: Verify the project appears in the filtered results
    const filteredProjectCard = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(filteredProjectCard).toBeVisible({ timeout: 10000 });
    console.log('✅ Project found in filtered results');

    // Verify the priority badge shows HIGH
    const priorityBadge = filteredProjectCard.locator('span.bg-orange-100.text-orange-800:has-text("HIGH")');
    await expect(priorityBadge).toBeVisible();
    console.log('✅ Priority badge shows HIGH');

    // Step 7: Additional verification - clear priority filter and search should still find the project
    await page.locator('select[name="priority"]').selectOption('');
    await page.getByRole('button', { name: /search projects|rechercher des projets/i }).click();
    await page.waitForTimeout(1500);

    // Project should still be visible (searching by name only now)
    await expect(filteredProjectCard).toBeVisible({ timeout: 10000 });
    console.log('✅ Project still found when searching by name only');

    // Step 8: Now search with only priority filter (clear name search)
    await page.locator('input[name="search"]').fill('');
    await page.locator('select[name="priority"]').selectOption('HIGH');
    await page.getByRole('button', { name: /search projects|rechercher des projets/i }).click();
    await page.waitForTimeout(1500);

    // Project should be visible in the HIGH priority results
    const projectInPriorityResults = page.locator('div.border.border-black\\/10.rounded-lg.p-4').filter({ hasText: projectName });
    await expect(projectInPriorityResults).toBeVisible({ timeout: 10000 });
    console.log('✅ Project found when filtering by HIGH priority only');

    console.log('\n🎉 TEST PASSED! Search and filter functionality works correctly with combined filters!');
});
