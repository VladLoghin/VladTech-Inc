// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

test.describe('Reviews Page E2E', () => {

    test.beforeEach(async ({ page }) => {
        // Go to the homepage
        await page.goto('http://localhost:5173/');
        await page.waitForLoadState('networkidle');

        // Scroll to the portfolio section
        const portfolioSection = page.locator('#portfolio');
        await portfolioSection.scrollIntoViewIfNeeded();

        // Click the "View All" button inside the portfolio section
        const viewAllButton = portfolioSection.getByRole('button', { name: /view all/i });
        await viewAllButton.waitFor({ state: 'visible', timeout: 10000 });
        await viewAllButton.click();

        // Wait for navigation to /reviews
        await page.waitForURL('**/reviews', { timeout: 10000 });
        await page.waitForLoadState('networkidle');
    });

    test('page loads and main sections are visible', async ({ page }) => {
        await expect(page.getByRole('heading', { name: /customer highlights/i })).toBeVisible();
        await expect(page.getByTestId('reviews-page')).toBeVisible();
        await expect(page.getByTestId('reviews-carousel-section')).toBeVisible();
        await expect(page.getByTestId('review-carousel')).toBeVisible();
    });

    test('carousel fetches reviews from backend', async ({ page }) => {
        const requests: Array<import('playwright').Request> = [];
        page.on('request', req => {
            if (req.url().includes('/api/reviews') && req.method() === 'GET') {
                requests.push(req);
            }
        });

        await page.reload();

        expect(requests.length).toBeGreaterThan(0);
        const firstRequest = requests[0]!;
        expect(firstRequest.method()).toBe('GET');
    });

    test('each review card displays required info', async ({ page }) => {
        const cards = page.getByTestId('review-card');
        const cardCount = await cards.count();
        expect(cardCount).toBeGreaterThan(0);

        for (let i = 0; i < cardCount; i++) {
            const card = cards.nth(i);
            await expect(card.getByTestId('review-client')).toBeVisible();
            await expect(card.getByTestId('review-comment')).toBeVisible();
            await expect(card.getByTestId('review-stars')).toBeVisible();
        }
    });
    const __filename = fileURLToPath(import.meta.url);
    const __dirname = path.dirname(__filename);

    test('create a new review successfully', async ({ page, loginAs }) => {

        // Generate a unique comment so Safari & Chromium never collide
        const uniqueComment = `E2E review submission ${Date.now()}`;

        // Log in as client
        await loginAs('client');
        console.log('✅ Logged in as client');

        // Scroll to portfolio section and click "View All"
        const portfolioSection = page.locator('#portfolio');
        await portfolioSection.scrollIntoViewIfNeeded();
        const viewAllButton = portfolioSection.getByRole('button', { name: /view all/i });
        await viewAllButton.waitFor({ state: 'visible', timeout: 10000 });
        await viewAllButton.click();

        // Wait for navigation to /reviews
        await page.waitForURL('**/reviews', { timeout: 10000 });
        await page.waitForLoadState('networkidle');

        // Open the review modal
        const addReviewButton = page.getByTestId('add-review-button');
        await addReviewButton.waitFor({ state: 'visible', timeout: 30000 });
        await addReviewButton.scrollIntoViewIfNeeded();
        await addReviewButton.click();

        // Fill out the review form
        await page.getByPlaceholder('Your name').fill('Charlie');
        await page.getByPlaceholder('Your message').fill(uniqueComment);
        await page.locator('button >> text=★').nth(4).click(); // select 5 stars

        // Attach image if exists
        const filePath = path.resolve(__dirname, 'test-image.jpg');
        if (fs.existsSync(filePath)) {
            await page.locator('input[type="file"]').setInputFiles(filePath);
        } else {
            console.warn(`⚠️ File not found: ${filePath}. Skipping file upload.`);
        }

        // Submit review AND wait for the POST request
        await Promise.all([
            page.waitForResponse(resp =>
                resp.url().includes('/api/reviews') && resp.status() === 200
            ),
            page.getByRole('button', { name: /submit review/i }).click()
        ]);

        // Wait for modal to close
        await page.getByRole('dialog').waitFor({ state: 'detached', timeout: 5000 });

        // Find the new review by its UNIQUE comment
        const newReviewCard = page
            .getByTestId('review-card')
            .filter({
                has: page.locator('[data-testid="review-comment"]').filter({
                    hasText: uniqueComment
                })
            });

        // Ensure it becomes visible in the carousel
        await expect(newReviewCard).toBeVisible({ timeout: 15000 });

        // Validate its content
        await expect(newReviewCard.getByTestId('review-comment')).toHaveText(`${uniqueComment}`);
        await expect(newReviewCard.getByTestId('review-stars')).toBeVisible();
    });



    test('star ratings render correctly', async ({ page }) => {
        const firstCard = page.getByTestId('review-card').first();
        await expect(firstCard).toBeVisible();

        const filledStars = await firstCard.locator('[data-testid="review-star-filled"]').count();
        const emptyStars = await firstCard.locator('[data-testid="review-star-empty"]').count();
        expect(filledStars + emptyStars).toBe(5);
    });

    test('carousel navigation buttons work', async ({ page }) => {
        const nextButton = page.locator('.swiper-button-next');
        const prevButton = page.locator('.swiper-button-prev');

        await expect(nextButton).toBeVisible();
        await expect(prevButton).toBeVisible();

        await nextButton.click();
        await page.waitForTimeout(300);

        await prevButton.click();
        await page.waitForTimeout(300);

        expect(true).toBeTruthy(); // no errors
    });

});
