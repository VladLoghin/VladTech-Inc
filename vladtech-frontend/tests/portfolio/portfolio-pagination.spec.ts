import { test, expect } from '@playwright/test';

test.describe('Portfolio Pagination Feature', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to portfolio page
    await page.goto('http://localhost:5173/portfolio');
    await page.waitForLoadState('networkidle');
    
    // Wait for portfolio items to load
    await page.waitForTimeout(2000);
  });

  test('should display exactly 6 items per page', async ({ page }) => {
    // Count portfolio items on the page
    const itemCount = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').count();
    
    console.log(`Items displayed: ${itemCount}`);
    expect(itemCount).toBe(6);
  });

  test('should show pagination controls when there are more than 6 items', async ({ page }) => {
    // Check if pagination controls are visible
    const paginationExists = await page.locator('button:has(svg)').filter({ hasText: '' }).count() > 0;
    
    if (paginationExists) {
      // Verify next button exists
      await expect(page.locator('button svg').last()).toBeVisible();
      
      // Verify page number buttons exist
      const pageButtons = await page.locator('button').filter({ hasText: /^[0-9]+$/ }).count();
      expect(pageButtons).toBeGreaterThan(0);
    }
  });

  test('should highlight the current page number', async ({ page }) => {
    // Find the active page button (should have yellow background)
    const activePageButton = page.locator('button').filter({ hasText: /^1$/ }).first();
    
    // Check if page 1 button has the active styling
    const classes = await activePageButton.getAttribute('class');
    expect(classes).toContain('bg-yellow-400');
  });

  test('should navigate to next page when clicking next button', async ({ page }) => {
    // Get initial item titles
    const firstPageFirstItem = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').first().locator('h3').textContent();
    
    // Click next button (chevron right)
    const nextButton = page.locator('button').filter({ has: page.locator('svg') }).last();
    await nextButton.click();
    
    // Wait for page change
    await page.waitForTimeout(1000);
    
    // Get new first item title
    const secondPageFirstItem = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').first().locator('h3').textContent();
    
    // Verify items changed
    expect(firstPageFirstItem).not.toBe(secondPageFirstItem);
    
    // Verify page 2 is now active
    const activePage = page.locator('button').filter({ hasText: /^2$/ }).first();
    const classes = await activePage.getAttribute('class');
    expect(classes).toContain('bg-yellow-400');
  });

  test('should navigate to previous page when clicking previous button', async ({ page }) => {
    // First go to page 2
    const nextButton = page.locator('button').filter({ has: page.locator('svg') }).last();
    await nextButton.click();
    await page.waitForTimeout(1000);
    
    // Get second page first item
    const secondPageFirstItem = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').first().locator('h3').textContent();
    
    // Click previous button (chevron left)
    const prevButton = page.locator('button').filter({ has: page.locator('svg') }).first();
    await prevButton.click();
    await page.waitForTimeout(1000);
    
    // Get first page first item
    const firstPageFirstItem = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').first().locator('h3').textContent();
    
    // Verify items changed back
    expect(firstPageFirstItem).not.toBe(secondPageFirstItem);
    
    // Verify page 1 is now active
    const activePage = page.locator('button').filter({ hasText: /^1$/ }).first();
    const classes = await activePage.getAttribute('class');
    expect(classes).toContain('bg-yellow-400');
  });

  test('should navigate to specific page when clicking page number', async ({ page }) => {
    // Click on page 2 button
    const page2Button = page.locator('button').filter({ hasText: /^2$/ }).first();
    await page2Button.click();
    
    // Wait for page change
    await page.waitForTimeout(1000);
    
    // Verify page 2 is now active
    const classes = await page2Button.getAttribute('class');
    expect(classes).toContain('bg-yellow-400');
    
    // Verify 6 items are still displayed
    const itemCount = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').count();
    expect(itemCount).toBe(6);
  });

  test('should disable previous button on first page', async ({ page }) => {
    // Get previous button
    const prevButton = page.locator('button').filter({ has: page.locator('svg') }).first();
    
    // Check if it's disabled
    const isDisabled = await prevButton.isDisabled();
    expect(isDisabled).toBe(true);
    
    // Verify it has disabled styling
    const classes = await prevButton.getAttribute('class');
    expect(classes).toContain('disabled:opacity-30');
  });

  test('should disable next button on last page', async ({ page }) => {
    // Count total pages
    const pageButtons = await page.locator('button').filter({ hasText: /^[0-9]+$/ }).count();
    
    if (pageButtons > 1) {
      // Click to last page
      const lastPageButton = page.locator('button').filter({ hasText: new RegExp(`^${pageButtons}$`) }).first();
      await lastPageButton.click();
      await page.waitForTimeout(1000);
      
      // Get next button
      const nextButton = page.locator('button').filter({ has: page.locator('svg') }).last();
      
      // Check if it's disabled
      const isDisabled = await nextButton.isDisabled();
      expect(isDisabled).toBe(true);
    }
  });

  test('should maintain item count consistency across pages', async ({ page }) => {
    // Count items on page 1
    const page1Count = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').count();
    expect(page1Count).toBe(6);
    
    // Go to page 2
    const nextButton = page.locator('button').filter({ has: page.locator('svg') }).last();
    await nextButton.click();
    await page.waitForTimeout(1000);
    
    // Count items on page 2 (should be 6 or less if it's the last page)
    const page2Count = await page.locator('[class*="cursor-pointer"][class*="aspect-square"]').count();
    expect(page2Count).toBeGreaterThan(0);
    expect(page2Count).toBeLessThanOrEqual(6);
  });
});

test.describe('Portfolio Pagination - Mobile View', () => {
  test.beforeEach(async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Navigate to portfolio page
    await page.goto('http://localhost:5173/portfolio');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);
  });

  test('should show pagination on mobile devices', async ({ page }) => {
    // Verify pagination controls are visible on mobile
    const pageButtons = await page.locator('button').filter({ hasText: /^[0-9]+$/ }).count();
    
    if (pageButtons > 0) {
      // Check if page number buttons are visible
      await expect(page.locator('button').filter({ hasText: /^1$/ }).first()).toBeVisible();
    }
  });

  test('should navigate pages on mobile', async ({ page }) => {
    // Click next button on mobile
    const nextButton = page.locator('button').filter({ has: page.locator('svg') }).last();
    await nextButton.click();
    
    // Wait for page change
    await page.waitForTimeout(1000);
    
    // Verify page 2 is active
    const page2Button = page.locator('button').filter({ hasText: /^2$/ }).first();
    const classes = await page2Button.getAttribute('class');
    expect(classes).toContain('bg-yellow-400');
  });
});
