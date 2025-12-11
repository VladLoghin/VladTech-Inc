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
        await page.locator('text=VIEW ALL →').click();
        
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

test.describe('Portfolio Page E2E - Mobile', () => {
    test.beforeEach(async ({ page }) => {
        await page.setViewportSize({ width: 390, height: 844 });
        // Start from homepage
        await page.goto('http://localhost:5173/');
        await page.waitForLoadState('networkidle');
    });

    test('navigate to portfolio from homepage and click portfolio item - Mobile', async ({ page }) => {
        // Scroll down to find the View All button in the portfolio section
        await page.locator('text=VIEW ALL →').click();
        
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
