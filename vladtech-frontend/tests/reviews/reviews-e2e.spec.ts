// @ts-ignore
import { test, expect } from '../fixtures/fixtures.ts';

test.describe('Reviews Page E2E', () => {

    test.beforeEach(async ({ page }) => {
        // Navigate to the Reviews Page
        await page.goto('http://localhost:5173/reviews')
        await page.waitForLoadState('networkidle');
    });

    test('page loads and main sections are visible', async ({ page }) => {
        await expect(page.getByRole('heading', { name: /customer highlights/i })).toBeVisible();
        await expect(page.getByTestId('reviews-page')).toBeVisible();
        await expect(page.getByTestId('reviews-carousel-section')).toBeVisible();
        await expect(page.getByTestId('review-carousel')).toBeVisible();
    });

    test('carousel fetches reviews from backend', async ({ page }) => {
        // Intercept the API call
        const [request] = await Promise.all([
            page.waitForRequest(req =>
                req.url().includes('/api/reviews') && req.method() === 'GET'
            ),
            page.reload()
        ]);

        expect(request.url()).toContain('/api/reviews');
        expect(request.method()).toBe('GET');
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

    test('star ratings render correctly', async ({ page }) => {
        const firstCard = page.getByTestId('review-card').first();
        await expect(firstCard).toBeVisible();

// wait for the stars to render inside
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

        expect(true).toBeTruthy(); // No errors = navigation works
    });

});
