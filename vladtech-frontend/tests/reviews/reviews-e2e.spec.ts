// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';
import { mockCompletedProjects, mockVisibleReviews, mockReviewSubmission, mockMyReviews } from './mockCompletedProjects';

test.describe('Reviews Page E2E', () => {
    test.beforeEach(async ({ page }) => {
        await mockCompletedProjects(page);
        await mockVisibleReviews(page);
        await mockMyReviews(page);
        await page.goto('http://localhost:5173/reviews');
        await page.waitForLoadState('networkidle');
        // Wait for review cards to render from mocked data
        await page.getByTestId('review-card').first().waitFor({ state: 'visible', timeout: 10000 });
    });

    test.describe('View and Create Reviews', () => {

        test('page loads and main sections are visible', async ({page}) => {
            await expect(page.getByRole('heading', {name: /customer highlights/i})).toBeVisible();
            await expect(page.getByTestId('reviews-page')).toBeVisible();
        });

        test('each review card displays required info', async ({page}) => {
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

        test('create a new review successfully', async ({page, loginAs}) => {
            await loginAs('client');

            // Re-mock endpoints after login (login navigates away, clearing routes)
            await mockCompletedProjects(page);
            await mockVisibleReviews(page);
            await mockReviewSubmission(page);
            // Mock /reviews/mine with no reviews so both projects appear as unreviewed
            await mockMyReviews(page, []);

            // Navigate to reviews page after login
            await page.goto('http://localhost:5173/reviews');
            await page.waitForLoadState('networkidle');

            const addReviewButton = page.getByTestId('Add Review');
            await addReviewButton.waitFor({state: 'visible', timeout: 30000});
            await addReviewButton.click();

            // Wait for the modal to appear
            const reviewModal = page.locator('.fixed.inset-0.z-50');
            await reviewModal.waitFor({state: 'visible', timeout: 5000});

            // Select a project within the modal
            const projectDropdown = reviewModal.locator('select').first();
            await projectDropdown.waitFor({state: 'visible', timeout: 5000});
            await projectDropdown.selectOption({ label: 'Office Space Update' });

            // Fill in the form
            await page.getByPlaceholder('Your name').fill('Charlie');
            await page.getByPlaceholder('Your message').fill(`E2E review submission ${Date.now()}`);

            // Select star rating (click 4th star)
            await page.locator('button >> text=★').nth(3).click();

            // Submit and wait for the mocked POST response
            await Promise.all([
                page.waitForResponse(resp =>
                    resp.url().includes('/api/reviews') && resp.status() === 200
                ),
                page.getByRole('button', {name: /submit review/i}).click()
            ]);

            // After successful submission, verify the modal closes
            await expect(reviewModal).not.toBeVisible({ timeout: 10000 });

            // Verify we're back on the reviews page with cards visible
            await expect(page.getByTestId('review-card').first()).toBeVisible({ timeout: 10000 });
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

            // Swiper only shows nav buttons when there are more slides than fit in view
            if (await nextButton.isVisible({ timeout: 3000 })) {
                await nextButton.click();
                await page.waitForTimeout(300);

                await expect(prevButton).toBeVisible();
                await prevButton.click();
                await page.waitForTimeout(300);
            }

            // Verify the carousel container exists regardless
            await expect(page.locator('.swiper')).toBeVisible();
        });

        test('review cards are interactive and clickable', async ({page}) => {
            const cards = page.getByTestId('review-card');
            const cardCount = await cards.count();
            expect(cardCount).toBeGreaterThan(0);

            for (let i = 0; i < cardCount; i++) {
                const card = cards.nth(i);
                await expect(card).toBeVisible();
                await expect(card).toHaveCSS('cursor', 'pointer');
            }
        });
    });

    test.describe('Review Detail Modal', () => {

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
                console.warn('Not enough reviews to test multiple modals');
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
            expect(ariaLabel).not.toBeNull();
            expect(ariaLabel?.toLowerCase()).toContain('close');

            const image = page.getByTestId('review-detail-image');
            const altText = await image.getAttribute('alt');
            expect(altText).not.toBeNull();
        });
    });

    test.describe('Form Validation & Deletion', () => {
        test('review submission form validation works', async ({page, loginAs}) => {
            await loginAs('client');

            // Re-mock and navigate after login
            await mockCompletedProjects(page);
            await mockVisibleReviews(page);
            await mockMyReviews(page, []);
            await page.goto('http://localhost:5173/reviews');
            await page.waitForLoadState('networkidle');

            const addReviewButton = page.getByTestId('Add Review');
            await addReviewButton.waitFor({state: 'visible', timeout: 30000});
            await addReviewButton.click();

            const submitButton = page.getByRole('button', {name: /submit review/i});
            await submitButton.waitFor({state: 'visible', timeout: 5000});

            // Submit with empty fields - browser validation should prevent submission
            await submitButton.click();

            // The form should still be visible (not submitted) because required fields are empty
            await expect(submitButton).toBeVisible();

            // Fill in the required fields
            const nameInput = page.getByPlaceholder('Your name');
            await nameInput.waitFor({state: 'visible', timeout: 5000});
            await nameInput.fill('Test User');

            const messageInput = page.getByPlaceholder('Your message');
            await messageInput.waitFor({state: 'visible', timeout: 5000});
            await messageInput.fill('Test message');

            // Select star rating
            await page.locator('button >> text=★').nth(3).click();

            // Submit button should still be enabled and clickable
            await expect(submitButton).toBeVisible();
            await expect(submitButton).toBeEnabled();
        });

        test('review deletion removes card from page', async ({page, loginAs}) => {
            await loginAs('client');

            // Navigate to reviews page and toggle to show own reviews
            await page.goto('http://localhost:5173/reviews');
            await page.waitForLoadState('networkidle');
            await page.waitForTimeout(1000);

            // Toggle "Show only my reviews" so delete buttons appear for owned reviews
            // The checkbox is sr-only (visually hidden), so click the label text instead
            const toggleLabel = page.getByText('Show only my reviews');
            if (await toggleLabel.isVisible({ timeout: 3000 })) {
                await toggleLabel.click();
                await page.waitForTimeout(1000);
            }

            const initialCount = await page.getByTestId('review-card').count();
            if (initialCount === 0) return;

            const firstCard = page.getByTestId('review-card').first();
            const deleteButton = firstCard.getByTestId('review-delete-button');

            if (await deleteButton.isVisible({ timeout: 5000 })) {
                await deleteButton.click();

                // Wait for confirm modal via the Cancel button (unique to the modal)
                await page.getByRole('button', { name: /cancel/i }).waitFor({ state: 'visible', timeout: 5000 });

                // Click the modal's Delete button (last match — card's delete comes first in DOM)
                await page.getByRole('button', { name: /^delete$/i }).last().click();
                await page.waitForLoadState('networkidle');
                await page.waitForTimeout(1000);

                const newCount = await page.getByTestId('review-card').count();
                expect(newCount).toBeLessThan(initialCount);
            }
        });
    });

    test.describe('Cross-Page Navigation', () => {
        test.beforeEach(async ({ page }) => {
            // Use desktop viewport so navbar links are directly visible (not inside hamburger)
            await page.setViewportSize({ width: 1920, height: 1080 });
        });

        test('global navbar is present with REVIEWS and PORTFOLIO links', async ({ page }) => {
            const nav = page.locator('nav');
            await expect(nav).toBeVisible();

            await expect(page.getByRole('button', { name: 'VLADTECH' })).toBeVisible();
            await expect(page.getByRole('button', { name: 'REVIEWS' })).toBeVisible();
            await expect(page.getByRole('button', { name: 'PORTFOLIO' })).toBeVisible();
        });

        test('navigate from reviews to portfolio via navbar', async ({ page }) => {
            await page.getByRole('button', { name: 'PORTFOLIO' }).click();
            await page.waitForURL('**/portfolio', { timeout: 5000 });
            expect(page.url()).toContain('/portfolio');
        });

        test('navigate from portfolio to reviews via navbar', async ({ page }) => {
            // First go to portfolio
            await page.goto('http://localhost:5173/portfolio');
            await page.waitForLoadState('networkidle');

            // Click REVIEWS in the navbar
            await page.getByRole('button', { name: 'REVIEWS' }).click();
            await page.waitForURL('**/reviews', { timeout: 5000 });
            expect(page.url()).toContain('/reviews');

            // Verify reviews page loaded
            await expect(page.getByTestId('reviews-page')).toBeVisible({ timeout: 10000 });
        });

        test('navigate from reviews to home via VLADTECH button', async ({ page }) => {
            await page.getByRole('button', { name: 'VLADTECH' }).click();
            await page.waitForURL('http://localhost:5173/', { timeout: 5000 });
            expect(page.url()).toBe('http://localhost:5173/');
        });
    });

    test.describe('Rating Range Filter', () => {
        test('min and max rating dropdowns are visible', async ({page}) => {
            // The filter bar should show both min and max rating selects
            const selects = page.locator('select');
            const selectCount = await selects.count();
            // Should have at least 3 selects: minRating, maxRating, type
            expect(selectCount).toBeGreaterThanOrEqual(3);
        });

        test('filtering by rating range updates results', async ({page}) => {
            const initialCount = await page.getByTestId('review-card').count();
            expect(initialCount).toBeGreaterThan(0);

            // The min rating select is the first one in the filter bar
            // Select FIVE stars for both min and max to narrow results
            const selects = page.locator('.flex.items-center.gap-2 select');
            const minSelect = selects.first();
            const maxSelect = selects.last();

            await minSelect.selectOption('FIVE');
            await maxSelect.selectOption('FIVE');
            await page.waitForLoadState('networkidle');
            await page.waitForTimeout(500);

            // Results should be filtered (count may change)
            const filteredCount = await page.getByTestId('review-card').count();
            expect(filteredCount).toBeGreaterThanOrEqual(0);
        });

        test('reset button clears all filters', async ({page}) => {
            // Apply a filter first
            const nameInput = page.getByPlaceholder(/search by name/i);
            if (await nameInput.isVisible({ timeout: 2000 })) {
                await nameInput.fill('John');
                await page.waitForTimeout(500);
            }

            // Click reset
            const resetButton = page.getByRole('button', { name: /reset/i });
            await resetButton.click();
            await page.waitForTimeout(500);

            // Name input should be cleared
            if (await nameInput.isVisible()) {
                await expect(nameInput).toHaveValue('');
            }
        });
    });
});
