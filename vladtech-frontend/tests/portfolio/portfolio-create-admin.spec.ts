import { test, expect } from '../fixtures/fixtures';
import path from 'path';

test.describe('Admin Portfolio Creation', () => {
  test('Admin can create portfolio item with file upload and image displays correctly', async ({ 
    page, 
    loginAs 
  }) => {
    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5174/admin');
    await page.waitForLoadState('networkidle');
    
    // Wait for admin page to load
    await expect(page.getByText('Admin Area')).toBeVisible({ timeout: 10000 });

    // Click Create Portfolio button
    await page.getByRole('button', { name: 'Create Portfolio' }).click();
    
    // Wait for modal to appear
    await expect(page.getByText('Create Portfolio Item')).toBeVisible({ timeout: 5000 });

    // Generate unique title to avoid duplicates
    const timestamp = Date.now();
    const portfolioTitle = `E2E Test Kitchen ${timestamp}`;
    
    // Fill in the form with file upload
    const titleInput = page.locator('input[type="text"]').first();
    await titleInput.fill(portfolioTitle);
    
    // Upload test image file
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(path.join(__dirname, '../fixtures/test-image.jpg'));
    
    const ratingInput = page.locator('input[type="number"]');
    await ratingInput.fill('4.8');

    // Submit the form
    await page.locator('button[type="submit"]').click();

    // Wait for success message
    await expect(page.getByText('Portfolio item created successfully!')).toBeVisible({ timeout: 15000 });

    // Navigate to portfolio page
    await page.goto('http://localhost:5174/portfolio');
    await page.waitForLoadState('networkidle');

    // Wait for portfolio items to load
    await page.waitForTimeout(2000);

    // Check if the new portfolio item is visible by title
    const portfolioItem = page.locator(`[alt="${portfolioTitle}"]`).first();
    await expect(portfolioItem).toBeVisible({ timeout: 10000 });

    // Verify the image loaded successfully (check if it has a valid src)
    const imageSrc = await portfolioItem.getAttribute('src');
    console.log('Image src:', imageSrc);
    
    // Should be a backend URL with uploads path
    expect(imageSrc).toContain('localhost:8080/uploads/portfolio/');

    // Verify image actually loaded (not broken)
    await page.waitForTimeout(1000);
    const imageNaturalWidth = await portfolioItem.evaluate((img: HTMLImageElement) => img.naturalWidth);
    console.log('Image natural width:', imageNaturalWidth);
    expect(imageNaturalWidth).toBeGreaterThan(0);

    // Click on the portfolio item to open modal
    await portfolioItem.click();

    // Wait for modal to appear
    await expect(page.getByText(portfolioTitle)).toBeVisible({ timeout: 5000 });
    
    // Verify rating is displayed
    await expect(page.getByText('4.8 / 5.0')).toBeVisible();

    // Verify the modal image also loads correctly
    const modalImage = page.locator('.relative.bg-black img').first();
    const modalImageSrc = await modalImage.getAttribute('src');
    console.log('Modal image src:', modalImageSrc);
    
    expect(modalImageSrc).toContain('localhost:8080/uploads/portfolio/');
    
    const modalImageNaturalWidth = await modalImage.evaluate((img: HTMLImageElement) => img.naturalWidth);
    expect(modalImageNaturalWidth).toBeGreaterThan(0);

    console.log('✅ Portfolio item created and image displayed successfully!');
  });

  test('Admin can create portfolio item with file upload - Mobile Safari', async ({ 
    page, 
    loginAs,
    isMobile 
  }) => {
    test.skip(!isMobile, 'This test is only for mobile');

    // Login as admin
    await loginAs('realAdmin');

    // Navigate to admin page
    await page.goto('http://localhost:5174/admin');
    await page.waitForLoadState('networkidle');
    
    // Wait for admin page to load
    await expect(page.getByText('Admin Area')).toBeVisible({ timeout: 10000 });

    // Click Create Portfolio button
    await page.getByRole('button', { name: 'Create Portfolio' }).click();
    
    // Wait for modal to appear
    await expect(page.getByText('Create Portfolio Item')).toBeVisible({ timeout: 5000 });

    // Generate unique title
    const timestamp = Date.now();
    const portfolioTitle = `E2E Mobile Test ${timestamp}`;
    
    // Fill in the form with file upload
    const titleInput = page.locator('input[type="text"]').first();
    await titleInput.fill(portfolioTitle);
    
    // Upload test image file
    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(path.join(__dirname, '../fixtures/test-image.jpg'));
    
    const ratingInput = page.locator('input[type="number"]');
    await ratingInput.fill('4.5');

    // Submit the form
    await page.locator('button[type="submit"]').click();

    // Wait for success message
    await expect(page.getByText('Portfolio item created successfully!')).toBeVisible({ timeout: 15000 });

    // Navigate to portfolio page
    await page.goto('http://localhost:5174/portfolio');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Check if the new portfolio item is visible
    const portfolioItem = page.locator(`[alt="${portfolioTitle}"]`).first();
    await expect(portfolioItem).toBeVisible({ timeout: 10000 });

    // Verify image loaded
    const imageSrc = await portfolioItem.getAttribute('src');
    expect(imageSrc).toContain('localhost:8080/uploads/portfolio/');
    
    const imageNaturalWidth = await portfolioItem.evaluate((img: HTMLImageElement) => img.naturalWidth);
    expect(imageNaturalWidth).toBeGreaterThan(0);

    console.log('✅ Mobile: Portfolio item created and image displayed successfully!');
  });
});
