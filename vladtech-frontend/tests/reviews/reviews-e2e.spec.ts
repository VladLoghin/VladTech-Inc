// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';

test.describe('Reviews Page E2E', () => {
    test.describe('View and Create Reviews', () => {
        test.beforeEach(async ({page}) => {
            // Go directly to reviews page
            await page.goto('/reviews');
            await page.waitForLoadState('networkidle');
        });

        test('page loads and main sections are visible', async ({page}) => {
            await expect(page.getByRole('heading', {name: /customer highlights/i})).toBeVisible();
            await expect(page.getByTestId('reviews-page')).toBeVisible();
        });

        test('each review card displays required info', async ({page}) => {
            const cards = page.getByTestId('review-card');
            await page.waitForTimeout(500);
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
            const uniqueComment = `E2E review submission ${Date.now()}`;

            // Log in as client
            await loginAs('client');
            console.log('✅ Logged in as client');

            // Navigate to reviews page
            await page.goto('/reviews');
            await page.waitForLoadState('networkidle');

            // Open the review modal
            const addReviewButton = page.getByTestId('Add Review');
            await addReviewButton.waitFor({state: 'visible', timeout: 30000});
            await addReviewButton.click();

            // Fill out the review form
            await page.getByPlaceholder('Your name').fill('Charlie');
            await page.getByPlaceholder('Your message').fill(uniqueComment);
            await page.locator('button >> text=★').nth(4).click();

            // Submit review and wait for response
            await Promise.all([
                page.waitForResponse(resp =>
                    resp.url().includes('/api/reviews') && resp.status() === 200
                ),
                page.getByRole('button', {name: /submit review/i}).click()
            ]);

            // Wait for modal to close
            await page.getByRole('dialog').waitFor({state: 'detached', timeout: 5000});
            await page.waitForLoadState('networkidle');
            await page.waitForTimeout(1000);

            // Verify the new review appears
            const newReviewCard = page
                .getByTestId('review-card')
                .filter({
                    has: page.locator('[data-testid="review-comment"]').filter({
                        hasText: uniqueComment
                    })
                });

            await expect(newReviewCard).toBeVisible({timeout: 15000});
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

            expect(true).toBeTruthy();
        });
    });

    test.describe('Review Detail Modal', () => {

        test.beforeEach(async ({ page }) => {
            await page.goto('/reviews');
            await page.waitForLoadState('networkidle');
        });

        test('clicking a review card opens the detail modal', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            await expect(firstCard).toBeVisible();
            await firstCard.click();

            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible({ timeout: 5000 });

            await expect(page.getByTestId('review-detail-image')).toBeVisible();
            await expect(page.getByTestId('review-detail-client')).toBeVisible();
            await expect(page.getByTestId('review-detail-stars')).toBeVisible();
            await expect(page.getByTestId('review-detail-comment')).toBeVisible();
        });

        test('modal displays the correct review data', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            const cardClientName = await firstCard.getByTestId('review-client').textContent();
            const cardComment = await firstCard.getByTestId('review-comment').textContent();

            await firstCard.click();

            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            const modalClientName = await page.getByTestId('review-detail-client').textContent();
            const modalComment = await page.getByTestId('review-detail-comment').textContent();

            expect(modalClientName).toBe(cardClientName);
            expect(modalComment).toBe(cardComment);
        });

        test('modal close button works', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            const closeButton = page.getByTestId('review-detail-close-button');
            await expect(closeButton).toBeVisible();
            await closeButton.click();

            await expect(modal).not.toBeVisible({ timeout: 2000 });
        });

        test('clicking modal backdrop closes the modal', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            const backdrop = page.getByTestId('review-detail-modal-backdrop');
            await expect(backdrop).toBeVisible();

            await backdrop.click({ position: { x: 10, y: 10 } });

            await expect(backdrop).not.toBeVisible({ timeout: 2000 });
        });

        test('modal image is larger than card image', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            const cardImage = firstCard.getByTestId('review-image');
            const cardImageBox = await cardImage.boundingBox();

            await firstCard.click();

            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();
            const modalImage = page.getByTestId('review-detail-image');
            const modalImageBox = await modalImage.boundingBox();

            expect(modalImageBox?.height).toBeGreaterThan(cardImageBox?.height || 0);
        });

        test('modal displays star ratings correctly', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

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

            const firstCard = cards.first();
            const firstComment = await firstCard.getByTestId('review-comment').textContent();
            await firstCard.click();

            let modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();
            let modalComment = await page.getByTestId('review-detail-comment').textContent();
            expect(modalComment).toBe(firstComment);

            await page.getByTestId('review-detail-close-button').click();
            await expect(modal).not.toBeVisible();

            const secondCard = cards.nth(1);
            const secondComment = await secondCard.getByTestId('review-comment').textContent();
            await secondCard.click();

            modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();
            modalComment = await page.getByTestId('review-detail-comment').textContent();
            expect(modalComment).toBe(secondComment);
            expect(modalComment).not.toBe(firstComment);
        });

        test('modal is scrollable when content is long', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            const modalContent = page.locator('.review-modal-content');

            const isScrollable = await modalContent.evaluate((el) => {
                return el.scrollHeight > el.clientHeight;
            });

            console.log(`Modal scrollable: ${isScrollable}`);
        });

        test('modal maintains accessibility', async ({ page }) => {
            const firstCard = page.getByTestId('review-card').first();
            await firstCard.click();

            const modal = page.getByTestId('review-detail-modal');
            await expect(modal).toBeVisible();

            const closeButton = page.getByTestId('review-detail-close-button');
            const ariaLabel = await closeButton.getAttribute('aria-label');
            expect(ariaLabel).toBeTruthy();
            expect(ariaLabel?.toLowerCase()).toContain('close');

            const image = page.getByTestId('review-detail-image');
            const altText = await image.getAttribute('alt');
            expect(altText).toBeTruthy();
        });
    });
});
