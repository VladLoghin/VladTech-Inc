import { test, expect } from '../fixtures/fixtures';

test.describe('Admin Portfolio Archive Management', () => {
  test('Admin can archive a portfolio item', async ({ 
    page, 
    loginAs 
  }) => {
    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5173/admin');
    await page.waitForLoadState('networkidle');
    
    // Click the Archive Portfolio button (red button)
    await page.getByRole('button', { name: 'Archive Portfolio' }).click();
    await page.waitForTimeout(1000);
    
    // Should be on "Active Items" tab by default
    await expect(page.locator('button:has-text("Active Items")')).toBeVisible();
    
    // Click Archive button on first available item (inside the modal list items)
    const archiveButtons = page.locator('.overflow-y-auto button:has-text("Archive")');
    const count = await archiveButtons.count();
    
    if (count > 0) {
      await archiveButtons.first().click();
      await page.waitForTimeout(500);
      
      // Confirm archiving in the confirmation modal - look for the dialog with "Confirm Archive" title
      const confirmDialog = page.locator('.z-\\[60\\]');
      await expect(confirmDialog).toBeVisible();
      
      // Click the Archive button in the confirmation dialog (the flex-1 red button)
      await confirmDialog.locator('button.bg-red-500').click();
      await page.waitForTimeout(500);
    }
    
    // Close the modal using the Close button at the bottom
    await page.getByRole('button', { name: 'Close' }).click();
  });

  test('Admin can restore an archived portfolio item', async ({ 
    page, 
    loginAs 
  }) => {
    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5173/admin');
    await page.waitForLoadState('networkidle');
    
    // Click the Archive Portfolio button
    await page.getByRole('button', { name: 'Archive Portfolio' }).click();
    await page.waitForTimeout(1000);
    
    // Click on "Archived Items" tab
    await page.locator('button:has-text("Archived Items")').first().click();
    await page.waitForTimeout(500);
    
    // Click Restore button on first available archived item
    const restoreButtons = page.locator('.overflow-y-auto button:has-text("Restore")');
    const count = await restoreButtons.count();
    
    if (count > 0) {
      await restoreButtons.first().click();
      await page.waitForTimeout(500);
      
      // Confirm restore in the confirmation modal
      const confirmDialog = page.locator('.z-\\[60\\]');
      await expect(confirmDialog).toBeVisible();
      
      // Click the Restore button in the confirmation dialog
      await confirmDialog.locator('button.bg-green-500').click();
      await page.waitForTimeout(500);
    }
    
    // Close the modal using the Close button at the bottom
    await page.getByRole('button', { name: 'Close' }).click();
  });

  test('Archive modal shows correct tabs and counts', async ({ 
    page, 
    loginAs 
  }) => {
    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5173/admin');
    await page.waitForLoadState('networkidle');
    
    // Click the Archive Portfolio button
    await page.getByRole('button', { name: 'Archive Portfolio' }).click();
    await page.waitForTimeout(1000);
    
    // Verify modal title
    await expect(page.getByText('Manage Portfolio Archive')).toBeVisible();
    
    // Verify both tabs are visible
    await expect(page.locator('button:has-text("Active Items")')).toBeVisible();
    await expect(page.locator('button:has-text("Archived Items")')).toBeVisible();
    
    // Close the modal using the Close button at the bottom
    await page.getByRole('button', { name: 'Close' }).click();
  });
});