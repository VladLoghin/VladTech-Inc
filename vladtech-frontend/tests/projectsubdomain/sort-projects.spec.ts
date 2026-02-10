import { test, expect } from '../fixtures/fixtures';

// Helper function to extract text safely
async function getTextContent(element) {
  const text = await element.textContent();
  return text ? text.trim() : '';
}

// Helper to verify ascending order
function verifyAscending(values: any[], fieldName: string) {
  for (let i = 0; i < values.length - 1; i++) {
    const current = values[i];
    const next = values[i + 1];
    
    if (current > next) {
      console.log(`❌ Sort error at index ${i}: "${current}" > "${next}"`);
      throw new Error(`${fieldName} not in ascending order: "${current}" should be <= "${next}"`);
    }
  }
  console.log(`✅ Verified ${values.length} items in ascending order for ${fieldName}`);
}

// Helper to verify descending order
function verifyDescending(values: any[], fieldName: string) {
  for (let i = 0; i < values.length - 1; i++) {
    const current = values[i];
    const next = values[i + 1];
    
    if (current < next) {
      console.log(`❌ Sort error at index ${i}: "${current}" < "${next}"`);
      throw new Error(`${fieldName} not in descending order: "${current}" should be >= "${next}"`);
    }
  }
  console.log(`✅ Verified ${values.length} items in descending order for ${fieldName}`);
}

// Helper to apply sort and wait for results
async function applySortAndWait(page, sortField: string, sortOrder: string) {
  // Find and click the sort toggle - use getByRole with careful wait
  const sortHeading = page.getByRole('heading', { name: /sort/i });
  
  // Wait for the heading to be visible
  await sortHeading.waitFor({ state: 'visible', timeout: 10000 });
  await page.waitForTimeout(500);
  
  // Scroll into view and click
  await sortHeading.scrollIntoViewIfNeeded();
  await page.waitForTimeout(300);
  
  // Click on the parent section containing the sort controls
  const sortSection = sortHeading.locator('ancestor::div[@class*="rounded-xl border"]');
  await sortSection.first().click();
  await page.waitForTimeout(800);

  // Select sort field
  const sortBySelect = page.locator('select[name="sortBy"]');
  await expect(sortBySelect).toBeVisible({ timeout: 5000 });
  await sortBySelect.selectOption(sortField);
  console.log(`✅ Selected sort field: ${sortField}`);

  // Select sort order
  await page.locator('select[name="sortOrder"]').selectOption(sortOrder);
  console.log(`✅ Selected sort order: ${sortOrder}`);

  // Click apply button
  const applyButton = page.getByRole('button', { name: /apply|appliquer/i });
  await applyButton.click();
  await page.waitForTimeout(1500);
  console.log('✅ Applied sorting');
}

test.describe('Project Sorting - Project ID', () => {
  test('Sort by Project ID - Ascending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    console.log('✅ Logged in as employee');
    
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    console.log(`📊 Found ${count} projects`);

    await applySortAndWait(page, 'projectIdentifier', 'ASC');

    count = await projectCards.count();
    const projectIds: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const idContent = await card.locator('p', { hasText: /^ID:/ }).first().textContent();
      if (idContent) {
        const id = idContent.replace(/ID:\s*/i, '').trim();
        projectIds.push(id);
      }
    }

    console.log('Project IDs (ASC):', projectIds);
    if (projectIds.length >= 2) {
      verifyAscending(projectIds, 'Project ID');
    }
  });

  test('Sort by Project ID - Descending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    console.log('✅ Logged in as employee');
    
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'projectIdentifier', 'DESC');

    count = await projectCards.count();
    const projectIds: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const idContent = await card.locator('p', { hasText: /^ID:/ }).first().textContent();
      if (idContent) {
        const id = idContent.replace(/ID:\s*/i, '').trim();
        projectIds.push(id);
      }
    }

    console.log('Project IDs (DESC):', projectIds);
    if (projectIds.length >= 2) {
      verifyDescending(projectIds, 'Project ID');
    }
  });
});

test.describe('Project Sorting - Project Name', () => {
  test('Sort by Project Name - Ascending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'name', 'ASC');

    count = await projectCards.count();
    const names: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const name = await card.locator('h3').first().textContent();
      if (name) {
        names.push(name.toLowerCase().trim());
      }
    }

    console.log('Project Names (ASC):', names);
    if (names.length >= 2) {
      verifyAscending(names, 'Project Name');
    }
  });

  test('Sort by Project Name - Descending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'name', 'DESC');

    count = await projectCards.count();
    const names: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const name = await card.locator('h3').first().textContent();
      if (name) {
        names.push(name.toLowerCase().trim());
      }
    }

    console.log('Project Names (DESC):', names);
    if (names.length >= 2) {
      verifyDescending(names, 'Project Name');
    }
  });
});

test.describe('Project Sorting - Client Name', () => {
  test('Sort by Client Name - Ascending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'clientName', 'ASC');

    count = await projectCards.count();
    const clientNames: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const clientContent = await card.locator('p:has-text("Client")').first().textContent();
      if (clientContent) {
        const client = clientContent.replace(/Client.*:\s*/i, '').trim().toLowerCase();
        if (client) clientNames.push(client);
      }
    }

    console.log('Client Names (ASC):', clientNames);
    if (clientNames.length >= 2) {
      verifyAscending(clientNames, 'Client Name');
    }
  });

  test('Sort by Client Name - Descending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'clientName', 'DESC');

    count = await projectCards.count();
    const clientNames: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const clientContent = await card.locator('p:has-text("Client")').first().textContent();
      if (clientContent) {
        const client = clientContent.replace(/Client.*:\s*/i, '').trim().toLowerCase();
        if (client) clientNames.push(client);
      }
    }

    console.log('Client Names (DESC):', clientNames);
    if (clientNames.length >= 2) {
      verifyDescending(clientNames, 'Client Name');
    }
  });
});

test.describe('Project Sorting - Due Date', () => {
  test('Sort by Due Date - Ascending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'dueDate', 'ASC');

    count = await projectCards.count();
    const dueDates: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const dateContent = await card.locator('p:has-text("Due Date")').first().textContent();
      if (dateContent) {
        const date = dateContent.replace(/Due Date.*:\s*/i, '').trim();
        if (date) dueDates.push(date);
      }
    }

    console.log('Due Dates (ASC):', dueDates);
    if (dueDates.length >= 2) {
      verifyAscending(dueDates, 'Due Date');
    }
  });

  test('Sort by Due Date - Descending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'dueDate', 'DESC');

    count = await projectCards.count();
    const dueDates: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const dateContent = await card.locator('p:has-text("Due Date")').first().textContent();
      if (dateContent) {
        const date = dateContent.replace(/Due Date.*:\s*/i, '').trim();
        if (date) dueDates.push(date);
      }
    }

    console.log('Due Dates (DESC):', dueDates);
    if (dueDates.length >= 2) {
      verifyDescending(dueDates, 'Due Date');
    }
  });
});

test.describe('Project Sorting - Start Date', () => {
  test('Sort by Start Date - Ascending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'startDate', 'ASC');

    count = await projectCards.count();
    const startDates: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const dateContent = await card.locator('p:has-text("Start Date")').first().textContent();
      if (dateContent) {
        const date = dateContent.replace(/Start Date.*:\s*/i, '').trim();
        if (date) startDates.push(date);
      }
    }

    console.log('Start Dates (ASC):', startDates);
    if (startDates.length >= 2) {
      verifyAscending(startDates, 'Start Date');
    }
  });

  test('Sort by Start Date - Descending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'startDate', 'DESC');

    count = await projectCards.count();
    const startDates: string[] = [];
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const dateContent = await card.locator('p:has-text("Start Date")').first().textContent();
      if (dateContent) {
        const date = dateContent.replace(/Start Date.*:\s*/i, '').trim();
        if (date) startDates.push(date);
      }
    }

    console.log('Start Dates (DESC):', startDates);
    if (startDates.length >= 2) {
      verifyDescending(startDates, 'Start Date');
    }
  });
});

test.describe('Project Sorting - Priority', () => {
  test('Sort by Priority - Ascending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'priority', 'ASC');

    count = await projectCards.count();
    const priorityValues: number[] = [];
    const priorityMap = { 'LOW': 1, 'MEDIUM': 2, 'HIGH': 3, 'URGENT': 4 };
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const priorityContent = await card.locator('p:has-text("Priority")').first().textContent();
      if (priorityContent) {
        const priority = priorityContent.toUpperCase();
        let priorityKey = 'MEDIUM';
        if (priority.includes('URGENT')) priorityKey = 'URGENT';
        else if (priority.includes('HIGH')) priorityKey = 'HIGH';
        else if (priority.includes('LOW')) priorityKey = 'LOW';
        
        priorityValues.push(priorityMap[priorityKey] || 2);
      }
    }

    console.log('Priority Values (ASC):', priorityValues);
    if (priorityValues.length >= 2) {
      verifyAscending(priorityValues, 'Priority');
    }
  });

  test('Sort by Priority - Descending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'priority', 'DESC');

    count = await projectCards.count();
    const priorityValues: number[] = [];
    const priorityMap = { 'LOW': 1, 'MEDIUM': 2, 'HIGH': 3, 'URGENT': 4 };
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const priorityContent = await card.locator('p:has-text("Priority")').first().textContent();
      if (priorityContent) {
        const priority = priorityContent.toUpperCase();
        let priorityKey = 'MEDIUM';
        if (priority.includes('URGENT')) priorityKey = 'URGENT';
        else if (priority.includes('HIGH')) priorityKey = 'HIGH';
        else if (priority.includes('LOW')) priorityKey = 'LOW';
        
        priorityValues.push(priorityMap[priorityKey] || 2);
      }
    }

    console.log('Priority Values (DESC):', priorityValues);
    if (priorityValues.length >= 2) {
      verifyDescending(priorityValues, 'Priority');
    }
  });
});

test.describe('Project Sorting - Status', () => {
  test('Sort by Status - Ascending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'status', 'ASC');

    count = await projectCards.count();
    const statusValues: number[] = [];
    const statusMap = { 'PENDING': 1, 'IN_PROGRESS': 2, 'COMPLETED': 3 };
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const statusContent = await card.locator('p:has-text("Status")').first().textContent();
      if (statusContent) {
        const status = statusContent.toUpperCase();
        let statusKey = 'PENDING';
        if (status.includes('COMPLETED')) statusKey = 'COMPLETED';
        else if (status.includes('IN_PROGRESS') || status.includes('IN PROGRESS')) statusKey = 'IN_PROGRESS';
        
        statusValues.push(statusMap[statusKey] || 1);
      }
    }

    console.log('Status Values (ASC):', statusValues);
    if (statusValues.length >= 2) {
      verifyAscending(statusValues, 'Status');
    }
  });

  test('Sort by Status - Descending', async ({ page, loginAs }) => {
    test.setTimeout(90000);
    
    await loginAs('employee');
    await page.goto('http://localhost:5173/projects');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    const projectCards = page.locator('div.border.rounded-lg.p-4');
    let count = await projectCards.count();
    
    if (count < 2) {
      console.log('⚠️  Skipping test - need at least 2 projects');
      return;
    }

    await applySortAndWait(page, 'status', 'DESC');

    count = await projectCards.count();
    const statusValues: number[] = [];
    const statusMap = { 'PENDING': 1, 'IN_PROGRESS': 2, 'COMPLETED': 3 };
    
    for (let i = 0; i < count; i++) {
      const card = projectCards.nth(i);
      const statusContent = await card.locator('p:has-text("Status")').first().textContent();
      if (statusContent) {
        const status = statusContent.toUpperCase();
        let statusKey = 'PENDING';
        if (status.includes('COMPLETED')) statusKey = 'COMPLETED';
        else if (status.includes('IN_PROGRESS') || status.includes('IN PROGRESS')) statusKey = 'IN_PROGRESS';
        
        statusValues.push(statusMap[statusKey] || 1);
      }
    }

    console.log('Status Values (DESC):', statusValues);
    if (statusValues.length >= 2) {
      verifyDescending(statusValues, 'Status');
    }
  });
});
