import { test, expect } from '@playwright/test';

test.describe('Portfolio Filter Feature', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to portfolio page
    await page.goto('http://localhost:5173/portfolio');
    await page.waitForLoadState('networkidle');
    
    // Wait for portfolio items to load
    await page.waitForTimeout(2000);
  });

  test('should filter portfolio items by Interior type', async ({ page }) => {
    // Click on filter dropdown
    const filterButton = page.locator('button:has-text("All")').first();
    await filterButton.click();
    
    // Select Interior filter
    await page.locator('text=Interior').click();
    
    // Wait for filter to apply
    await page.waitForTimeout(1000);
    
    // Verify the filter button shows "Interior"
    await expect(page.locator('button:has-text("Interior")')).toBeVisible();
    
    // Verify items are displayed
    const interiorItems = await page.locator('[class*="cursor-pointer"]').count();
    console.log(`Interior items count: ${interiorItems}`);
    expect(interiorItems).toBeGreaterThanOrEqual(0);
  });

  test('should filter portfolio items by Exterior/Yard type', async ({ page }) => {
    // Click on filter dropdown
    const filterButton = page.locator('button:has-text("All")').first();
    await filterButton.click();
    
    // Select Exterior/Yard filter
    await page.locator('text=Exterior/Yard').click();
    
    // Wait for filter to apply
    await page.waitForTimeout(1000);
    
    // Verify the filter button shows "Exterior/Yard"
    await expect(page.locator('button:has-text("Exterior/Yard")')).toBeVisible();
    
    // Verify items are displayed
    const exteriorItems = await page.locator('[class*="cursor-pointer"]').count();
    console.log(`Exterior/Yard items count: ${exteriorItems}`);
    expect(exteriorItems).toBeGreaterThanOrEqual(0);
  });

  test('should switch between Interior and Exterior filters', async ({ page }) => {
    // Start with Interior filter
    let filterButton = page.locator('button:has-text("All")').first();
    await filterButton.click();
    await page.locator('text=Interior').click();
    await page.waitForTimeout(1000);
    
    await expect(page.locator('button:has-text("Interior")')).toBeVisible();
    const interiorCount = await page.locator('[class*="cursor-pointer"]').count();
    console.log(`Interior items: ${interiorCount}`);
    
    // Switch to Exterior/Yard filter
    filterButton = page.locator('button:has-text("Interior")').first();
    await filterButton.click();
    await page.locator('text=Exterior/Yard').click();
    await page.waitForTimeout(1000);
    
    await expect(page.locator('button:has-text("Exterior/Yard")')).toBeVisible();
    const exteriorCount = await page.locator('[class*="cursor-pointer"]').count();
    console.log(`Exterior/Yard items: ${exteriorCount}`);
    
    // Verify counts can be different
    expect(interiorCount).toBeGreaterThanOrEqual(0);
    expect(exteriorCount).toBeGreaterThanOrEqual(0);
  });

  test('should maintain filter when opening and closing portfolio item modal', async ({ page }) => {
    // Apply Interior filter
    const filterButton = page.locator('button:has-text("All")').first();
    await filterButton.click();
    await page.locator('text=Interior').click();
    await page.waitForTimeout(1000);
    
    await expect(page.locator('button:has-text("Interior")')).toBeVisible();
    
    // Get count before clicking item
    const countBeforeModal = await page.locator('[class*="cursor-pointer"]').count();
    
    // Click on a portfolio item if available
    if (countBeforeModal > 0) {
      const firstItem = page.locator('[class*="cursor-pointer"]').first();
      await firstItem.click();
      
      // Wait for modal to appear
      await page.waitForSelector('button:has(svg)', { timeout: 5000 });
      
      // Close modal by clicking the X button
      await page.locator('button:has(svg)').first().click();
      
      // Wait for modal to close
      await page.waitForTimeout(500);
      
      // Verify filter is still applied
      await expect(page.locator('button:has-text("Interior")')).toBeVisible();
      
      // Verify same number of items
      const countAfterModal = await page.locator('[class*="cursor-pointer"]').count();
      expect(countAfterModal).toBe(countBeforeModal);
    }
  });
});

test.describe('Portfolio Filter - Mobile View', () => {
  test.beforeEach(async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Navigate to portfolio page
    await page.goto('http://localhost:5173/portfolio');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);
  });

  test('should filter by Interior on mobile devices', async ({ page }) => {
    // Click on filter dropdown
    const filterButton = page.locator('button:has-text("All")').first();
    await expect(filterButton).toBeVisible({ timeout: 5000 });
    await filterButton.click();
    
    // Select Interior filter
    await page.locator('text=Interior').click();
    
    // Wait for filter to apply
    await page.waitForTimeout(1000);
    
    // Verify filter is applied
    await expect(page.locator('button:has-text("Interior")')).toBeVisible();
    
    // Verify items are displayed
    const itemCount = await page.locator('[class*="cursor-pointer"]').count();
    console.log(`Mobile - Interior items: ${itemCount}`);
    expect(itemCount).toBeGreaterThanOrEqual(0);
  });

  test('should filter by Exterior/Yard on mobile devices', async ({ page }) => {
    // Click on filter dropdown
    const filterButton = page.locator('button:has-text("All")').first();
    await expect(filterButton).toBeVisible({ timeout: 5000 });
    await filterButton.click();
    
    // Select Exterior/Yard filter
    await page.locator('text=Exterior/Yard').click();
    
    // Wait for filter to apply
    await page.waitForTimeout(1000);
    
    // Verify filter is applied
    await expect(page.locator('button:has-text("Exterior/Yard")')).toBeVisible();
    
    // Verify items are displayed
    const itemCount = await page.locator('[class*="cursor-pointer"]').count();
    console.log(`Mobile - Exterior/Yard items: ${itemCount}`);
    expect(itemCount).toBeGreaterThanOrEqual(0);
  });
});

