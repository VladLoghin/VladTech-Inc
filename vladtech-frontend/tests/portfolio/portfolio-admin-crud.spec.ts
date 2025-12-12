import { test, expect } from '../fixtures/fixtures';
import path from 'path';

test.describe('Admin Portfolio CRUD Operations', () => {
  test('Admin can create portfolio item with file upload and navigate to portfolio gallery', async ({ 
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
    
    // Click Create Portfolio button
    await page.getByRole('button', { name: 'Create Portfolio' }).click();
    await page.waitForTimeout(500);
    
    // Fill in the form
    await page.getByRole('textbox').click();
    await page.getByRole('textbox').fill('New Kitchen Alley II');
    
    // Upload an image
    await page.getByText('Click to upload imagePNG, JPG').click();
    const imagePath = path.join(__dirname, '../fixtures/jason-briscoe-AQl-J19ocWE-unsplash.jpg');
    await page.locator('body').setInputFiles(imagePath);
    
    // Create the portfolio item
    await page.getByRole('button', { name: 'Create', exact: true }).click();
    await page.waitForTimeout(1000);
    
    // Navigate to home
    await page.getByRole('link', { name: 'Home' }).click();
    
    // Navigate to portfolio gallery
    await page.getByRole('navigation').getByRole('button', { name: 'PORTFOLIO' }).click();
    
    // Verify navigation to portfolio page
    await expect(page).toHaveURL(/.*portfolio/);
  });
});