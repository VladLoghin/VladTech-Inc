// @ts-ignore
import { test, expect, devices } from '@playwright/test';

test.describe('Portfolio Page E2E - Desktop', () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 1920, height: 1080 });
        // Start from homepage
        await page.goto('http://localhost:5173/');
        await page.waitForLoadState('networkidle');
    });

    test('navigate to portfolio from homepage and click portfolio item', async ({ page }) => {
        // Scroll down to find the View All button in the portfolio section
        await page.getByRole('button', { name: /View Full Gallery/i }).click();
        
        // Wait for navigation to portfolio page
        await page.waitForURL('**/portfolio', { timeout: 5000 });
        expect(page.url()).toContain('/portfolio');
        
        // Wait for portfolio items to load
        await page.waitForSelector('img[alt]', { timeout: 10000 });
        
        // Click on the first portfolio item (click the parent container, not the image)
        const firstItem = page.locator('[class*="cursor-pointer"]').first();
        await firstItem.click();

        // Wait for modal to appear by checking for the close button
        await page.waitForSelector('button:has(svg)', { timeout: 5000 });

        // Verify the title is visible in the modal
        await expect(page.locator('text=Modern Kitchen Counter').last()).toBeVisible();
    });
});

test.describe('Portfolio Page - Cross Navigation', () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 1920, height: 1080 });
    });

    test('navigate from portfolio to reviews via navbar REVIEWS link', async ({ page }) => {
        await page.goto('http://localhost:5173/portfolio');
        await page.waitForLoadState('networkidle');

        // Click REVIEWS in the global navbar
        await page.getByRole('button', { name: 'REVIEWS' }).click();

        // Verify navigation to reviews page
        await page.waitForURL('**/reviews', { timeout: 5000 });
        expect(page.url()).toContain('/reviews');
    });

    test('navigate from reviews to portfolio via navbar home then portfolio', async ({ page }) => {
        await page.goto('http://localhost:5173/reviews');
        await page.waitForLoadState('networkidle');

        // Click VLADTECH to go home
        await page.getByRole('button', { name: 'VLADTECH' }).click();
        await page.waitForURL('http://localhost:5173/', { timeout: 5000 });

        // Scroll to and click "View Full Gallery" to navigate to portfolio
        const viewGalleryButton = page.getByRole('button', { name: /View Full Gallery/i });
        await viewGalleryButton.scrollIntoViewIfNeeded();
        await viewGalleryButton.click();
        await page.waitForURL('**/portfolio', { timeout: 5000 });
        expect(page.url()).toContain('/portfolio');
    });

    test('portfolio filter dropdown remains functional with global navbar', async ({ page }) => {
        await page.goto('http://localhost:5173/portfolio');
        await page.waitForLoadState('networkidle');
        await page.waitForTimeout(1000);

        // Click the filter dropdown inside the navbar
        const filterButton = page.locator('button:has-text("All")').first();
        await filterButton.click();

        // Select Interior
        await page.locator('button:has-text("Interior")').last().click();
        await page.waitForTimeout(1000);

        // Verify filter applied
        await expect(page.locator('button:has-text("Interior")').first()).toBeVisible();

        // REVIEWS link should still be accessible in the navbar
        await expect(page.getByRole('button', { name: 'REVIEWS' })).toBeVisible();
    });
});

test.describe('Portfolio Page E2E - Mobile', () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        // Start from homepage
        await page.goto('http://localhost:5173/');
        await page.waitForLoadState('networkidle');
    });

    test('navigate to portfolio from homepage and click portfolio item - Mobile', async ({ page }) => {
        // Scroll down to find the View All button in the portfolio section
        await page.getByRole('button', { name: /View Full Gallery/i }).click();
        
        // Wait for navigation to portfolio page
        await page.waitForURL('**/portfolio', { timeout: 5000 });
        expect(page.url()).toContain('/portfolio');
        
        // Wait for portfolio items to load
        await page.waitForSelector('img[alt]', { timeout: 10000 });
        
        // Click on the first portfolio item (click the parent container, not the image)
        const firstItem = page.locator('[class*="cursor-pointer"]').first();
        await firstItem.click();

        // Wait for modal to appear by checking for the close button
        await page.waitForSelector('button:has(svg)', { timeout: 5000 });

        // Verify the title is visible in the modal
        await expect(page.locator('text=Modern Kitchen Counter').last()).toBeVisible();
    });
});
