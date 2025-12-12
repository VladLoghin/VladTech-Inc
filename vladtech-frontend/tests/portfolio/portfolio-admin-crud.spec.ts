import { test, expect } from '../fixtures/fixtures';
import path from 'path';

test.describe('Admin Portfolio CRUD Operations', () => {
  test('Admin can create portfolio item with file upload and then delete it', async ({ 
    page, 
    loginAs 
  }) => {
    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5173/admin');
    await page.waitForLoadState('networkidle');
    
    // Wait for admin page to load
    await expect(page.getByText('Admin Area')).toBeVisible({ timeout: 10000 });

    // === CREATE PORTFOLIO ITEM ===
    await page.getByRole('button', { name: 'Create Portfolio' }).click();
    await expect(page.getByText('Create Portfolio Item')).toBeVisible({ timeout: 5000 });

    const timestamp = Date.now();
    const portfolioTitle = `Test Portfolio ${timestamp}`;
    
    // Fill title
    await page.getByLabel('Title').fill(portfolioTitle);
    
    // Upload a test image file
    // Create a simple test image path (you can use any image from your project)
    const testImagePath = path.join(__dirname, '../fixtures/test-image.jpg');
    
    // Set the file input
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(testImagePath);
    
    // Wait for preview to load
    await page.waitForTimeout(1000);

    // Fill rating
    await page.getByLabel('Rating').fill('4.5');

    // Submit form
    await page.locator('button:has-text("Create Portfolio Item")').last().click();
    
    // Wait for upload and creation to complete
    await expect(page.getByText('Portfolio item created successfully!')).toBeVisible({ timeout: 15000 });

    // === VERIFY ITEM EXISTS ===
    await page.goto('http://localhost:5173/portfolio');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Check if the new portfolio item is visible
    const portfolioItem = page.locator(`[alt="${portfolioTitle}"]`).first();
    await expect(portfolioItem).toBeVisible({ timeout: 10000 });

    // Verify image loaded
    const imageNaturalWidth = await portfolioItem.evaluate((img: HTMLImageElement) => img.naturalWidth);
    expect(imageNaturalWidth).toBeGreaterThan(0);

    console.log('✅ Portfolio item created with uploaded image!');

    // === DELETE THE CREATED ITEM ===
    await page.goto('http://localhost:5173/admin');
    await page.waitForLoadState('networkidle');
    
    await page.getByRole('button', { name: 'Delete Portfolio' }).click();
    await expect(page.getByText('Delete Portfolio Items')).toBeVisible({ timeout: 5000 });
    await page.waitForTimeout(2000);

    // Find the item we just created
    const itemToDelete = page.locator(`text=${portfolioTitle}`).first();
    await expect(itemToDelete).toBeVisible({ timeout: 5000 });

    // Click the delete button for this item
    const deleteButton = page.locator(`text=${portfolioTitle}`).locator('xpath=ancestor::div[contains(@class, "border")]//button:has-text("Delete")').first();
    
    // Handle confirmation dialog
    page.once('dialog', async dialog => {
      expect(dialog.message()).toContain('delete');
      await dialog.accept();
    });

    await deleteButton.click();
    await page.waitForTimeout(2000);

    // Verify item is removed from list
    await expect(page.locator(`text=${portfolioTitle}`)).not.toBeVisible({ timeout: 5000 });

    console.log('✅ Portfolio item created and deleted successfully!');
  });
});
