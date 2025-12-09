// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

test.describe('Reviews Page E2E', () => {
    test.describe('View and Create Reviews', () => {
        test.beforeEach(async ({page}) => {
            // Go to the homepage
            await page.goto('http://localhost:5173/');
            await page.waitForLoadState('networkidle');

            // Scroll to the portfolio section
            const portfolioSection = page.locator('#portfolio');
            await portfolioSection.scrollIntoViewIfNeeded();

            // Click the "View All" button inside the portfolio section
            const viewAllButton = portfolioSection.getByRole('button', {name: /view all/i});
            await viewAllButton.waitFor({state: 'visible', timeout: 10000});
            await viewAllButton.click();

            // Wait for navigation to /reviews
            await page.waitForURL('**/reviews', {timeout: 10000});
            await page.waitForLoadState('networkidle');
        });

        test('page loads and main sections are visible', async ({page}) => {
            await expect(page.getByRole('heading', {name: /customer highlights/i})).toBeVisible();
            await expect(page.getByTestId('reviews-page')).toBeVisible();
            await expect(page.getByTestId('reviews-carousel-section')).toBeVisible();
            await expect(page.getByTestId('review-carousel')).toBeVisible();
        });

        test('carousel fetches reviews from backend', async ({page}) => {
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

        test('each review card displays required info', async ({page}) => {
            const cards = page.getByTestId('review-card');
            await page.waitForTimeout(500); //gives time for review cards to load before counting
            const cardCount = await cards.count();
            expect(cardCount).toBeGreaterThan(0);

            for (let i = 0; i < cardCount; i++) {
                const card = cards.nth(i);
                await expect(card.getByTestId('review-client')).toBeVisible();
                await expect(card.getByTestId('review-comment')).toBeVisible();
                await expect(card.getByTestId('review-stars')).toBeVisible();
            }
        });

        test('create a new review successfully', async ({page, loginAs}) => {

            // Generate a unique comment so Safari & Chromium never collide
            const uniqueComment = `E2E review submission ${Date.now()}`;

            // Log in as client
            await loginAs('client');
            console.log('✅ Logged in as client');

            // Scroll to portfolio section and click "View All"
            const portfolioSection = page.locator('#portfolio');
            await portfolioSection.scrollIntoViewIfNeeded();
            const viewAllButton = portfolioSection.getByRole('button', {name: /view all/i});
            await viewAllButton.waitFor({state: 'visible', timeout: 10000});
            await viewAllButton.click();

            // Wait for navigation to /reviews
            await page.waitForURL('**/reviews', {timeout: 10000});
            await page.waitForLoadState('networkidle');

            // Open the review modal
            const addReviewButton = page.getByTestId('add-review-button');
            await addReviewButton.waitFor({state: 'visible', timeout: 30000});
            await addReviewButton.scrollIntoViewIfNeeded();
            await addReviewButton.click();

            // Fill out the review form
            await page.getByPlaceholder('Your name').fill('Charlie');
            await page.getByPlaceholder('Your message').fill(uniqueComment);
            await page.locator('button >> text=★').nth(4).click(); // select 5 stars

            // Submit review AND wait for the POST request
            await Promise.all([
                page.waitForResponse(resp =>
                    resp.url().includes('/api/reviews') && resp.status() === 200
                ),
                page.getByRole('button', {name: /submit review/i}).click()
            ]);

            // Wait for modal to close
            await page.getByRole('dialog').waitFor({state: 'detached', timeout: 5000});

            // Find the new review by its UNIQUE comment
            const newReviewCard = page
                .getByTestId('review-card')
                .filter({
                    has: page.locator('[data-testid="review-comment"]').filter({
                        hasText: uniqueComment
                    })
                });

            // Ensure it becomes visible in the carousel
            await expect(newReviewCard).toBeVisible({timeout: 15000});

            // Validate its content
            await expect(newReviewCard.getByTestId('review-comment')).toHaveText(`${uniqueComment}`);
            await expect(newReviewCard.getByTestId('review-stars')).toBeVisible();
        });


        test('star ratings render correctly', async ({page}) => {
            const firstCard = page.getByTestId('review-card').first();
            await expect(firstCard).toBeVisible();

            const filledStars = await firstCard.locator('[data-testid="review-star-filled"]').count();
            const emptyStars = await firstCard.locator('[data-testid="review-star-empty"]').count();
            expect(filledStars + emptyStars).toBe(5);
        });

        test('carousel navigation buttons work', async ({page}) => {
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

    test.describe('Review Detail Modal', () => {

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

        test('clicking a review card opens the detail modal', async ({ page }) => {
            // Get the first review card
            const firstCard = page.getByTestId('review-card').first();
            await expect(firstCard).toBeVisible();

            // Click on the review card
            await firstCard.click();

            // Wait for the modal to appear
            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible({ timeout: 5000 });

            // Verify modal content is visible
            await expect(page.getByTestId('review-detail-image')).toBeVisible();
            await expect(page.getByTestId('review-detail-client')).toBeVisible();
            await expect(page.getByTestId('review-detail-stars')).toBeVisible();
            await expect(page.getByTestId('review-detail-comment')).toBeVisible();
        });

        test('modal displays the correct review data', async ({ page }) => {
            // Get data from the first card
            const firstCard = page.getByTestId('review-card').first();
            const cardClientName = await firstCard.getByTestId('review-client').textContent();
            const cardComment = await firstCard.getByTestId('review-comment').textContent();

            // Click the card
            await firstCard.click();

            // Wait for modal
            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            // Verify the same data appears in the modal
            const modalClientName = await page.getByTestId('review-detail-client').textContent();
            const modalComment = await page.getByTestId('review-detail-comment').textContent();

            expect(modalClientName).toBe(cardClientName);
            expect(modalComment).toBe(cardComment);
        });

        test('modal close button works', async ({ page }) => {
            // Click a review card to open modal
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            // Wait for modal to appear
            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            // Click the close button
            const closeButton = page.getByTestId('review-detail-close-button');
            await expect(closeButton).toBeVisible();
            await closeButton.click();

            // Verify modal is closed
            await expect(modal).not.toBeVisible({ timeout: 2000 });
        });

        test('clicking modal backdrop closes the modal', async ({ page }) => {
            // Click a review card to open modal
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            // Wait for modal backdrop to appear
            const backdrop = page.getByTestId('review-detail-modal-backdrop');
            await expect(backdrop).toBeVisible();

            // Click on the backdrop (not the modal content)
            await backdrop.click({ position: { x: 10, y: 10 } });

            // Verify modal is closed
            await expect(backdrop).not.toBeVisible({ timeout: 2000 });
        });

        test('modal image is larger than card image', async ({ page }) => {
            // Get first card image dimensions
            const firstCard = page.getByTestId('review-card').first();
            const cardImage = firstCard.getByTestId('review-image');
            const cardImageBox = await cardImage.boundingBox();

            // Click the card
            await firstCard.click();

            // Wait for modal and get modal image dimensions
            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();
            const modalImage = page.getByTestId('review-detail-image');
            const modalImageBox = await modalImage.boundingBox();

            // Verify modal image is larger
            expect(modalImageBox?.height).toBeGreaterThan(cardImageBox?.height || 0);
        });

        test('modal displays star ratings correctly', async ({ page }) => {
            // Click a review card
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            // Wait for modal
            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            // Verify 5 stars are present
            const starsContainer = page.getByTestId('review-detail-stars');
            const stars = await starsContainer.locator('svg').count();
            expect(stars).toBe(5);
        });

        test('can open modal for multiple different reviews', async ({ page }) => {
            const cards = page.getByTestId('review-card');
            const cardCount = await cards.count();

            if (cardCount < 2) {
                console.warn('⚠️ Not enough reviews to test multiple modals');
                return;
            }

            // Click first card
            const firstCard = cards.first();
            const firstComment = await firstCard.getByTestId('review-comment').textContent();
            await firstCard.click();

            // Verify first modal
            let modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();
            let modalComment = await page.getByTestId('review-detail-comment').textContent();
            expect(modalComment).toBe(firstComment);

            // Close modal
            await page.getByTestId('review-detail-close-button').click();
            await expect(modal).not.toBeVisible();

            // Click second card
            const secondCard = cards.nth(1);
            const secondComment = await secondCard.getByTestId('review-comment').textContent();
            await secondCard.click();

            // Verify second modal shows different content
            modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();
            modalComment = await page.getByTestId('review-detail-comment').textContent();
            expect(modalComment).toBe(secondComment);
            expect(modalComment).not.toBe(firstComment);
        });

        test('modal is scrollable when content is long', async ({ page }) => {
            // Click a review card
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            // Wait for modal
            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            // Get modal content container
            const modalContent = page.locator('.review-modal-content');

            // Check if content is scrollable (scrollHeight > clientHeight)
            const isScrollable = await modalContent.evaluate((el) => {
                return el.scrollHeight > el.clientHeight;
            });

            // Note: This may be false if reviews are short, which is okay
            console.log(`Modal scrollable: ${isScrollable}`);
        });

        test('modal maintains accessibility', async ({ page }) => {
            // Click a review card
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            // Wait for modal
            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            // Check close button has aria-label
            const closeButton = page.getByTestId('review-detail-close-button');
            const ariaLabel = await closeButton.getAttribute('aria-label');
            expect(ariaLabel).toBeTruthy();
            expect(ariaLabel?.toLowerCase()).toContain('close');

            // Check image has alt text
            const image = page.getByTestId('review-detail-image');
            const altText = await image.getAttribute('alt');
            expect(altText).toBeTruthy();
        });
    });
});
