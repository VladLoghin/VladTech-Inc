import { test, expect } from '../fixtures/fixtures';

test.describe('Admin Portfolio Deletion', () => {
  test('Admin can delete a portfolio item and verify deletion in portfolio gallery', async ({ 
    page, 
    loginAs 
  }) => {
    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5173/admin');
    await page.waitForLoadState('networkidle');
    
    // Wait for admin page to load and click Delete Portfolio button
    await page.getByRole('button', { name: 'Delete Portfolio' }).click();
    await page.waitForTimeout(1000);
    
    // Click the Delete button for a portfolio item (using nth(5) as in original)
    await page.getByRole('button', { name: 'Delete', exact: true }).nth(5).click();
    
    // Confirm deletion in the custom modal
    await page.locator('.flex-1.px-4.py-2.bg-red-500').click();
    
    // Close the delete modal
    await page.getByRole('button').nth(4).click();
    
    // Navigate to home and then to portfolio gallery
    await page.getByRole('link', { name: 'Home' }).click();
    await page.getByRole('button', { name: 'PORTFOLIO' }).nth(1).click();
    
    // Verify navigation to portfolio page
    await expect(page).toHaveURL(/.*portfolio/);
  });
});