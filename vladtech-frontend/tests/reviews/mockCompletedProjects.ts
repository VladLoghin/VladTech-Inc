import { Page } from '@playwright/test';

const MOCK_REVIEWS = [
    {
        reviewId: 'test-review-1',
        clientName: 'John Doe',
        comment: 'Great work on my kitchen renovation!',
        rating: 'FIVE',
        visible: true,
        type: 'Kitchen',
        photos: [{ url: '/images/placeholder.png', filename: 'kitchen.jpg' }],
    },
    {
        reviewId: 'test-review-2',
        clientName: 'Jane Smith',
        comment: 'The bathroom looks amazing.',
        rating: 'FOUR',
        visible: true,
        type: 'Bathroom',
        photos: [{ url: '/images/placeholder.png', filename: 'bathroom.jpg' }],
    },
    {
        reviewId: 'test-review-3',
        clientName: 'Bob Wilson',
        comment: 'Interior design was perfect.',
        rating: 'THREE',
        visible: true,
        type: 'Interior',
        photos: [],
    },
    {
        reviewId: 'test-review-4',
        clientName: 'Alice Brown',
        comment: 'Exterior yard work exceeded expectations.',
        rating: 'FIVE',
        visible: true,
        type: 'Exterior/Yard',
        photos: [{ url: '/images/placeholder.png', filename: 'yard.jpg' }],
    },
    {
        reviewId: 'test-review-5',
        clientName: 'Charlie Davis',
        comment: 'Kitchen remodel was top notch quality.',
        rating: 'FOUR',
        visible: true,
        type: 'Kitchen',
        photos: [],
    },
    {
        reviewId: 'test-review-6',
        clientName: 'Diana Evans',
        comment: 'Loved the new bathroom tiles and fixtures.',
        rating: 'FIVE',
        visible: true,
        type: 'Bathroom',
        photos: [{ url: '/images/placeholder.png', filename: 'bath2.jpg' }],
    },
];

export async function mockCompletedProjects(page: Page) {
    await page.route('**/api/projects/client/completed*', (route) => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify([
                {
                    projectIdentifier: 'proj-001',
                    name: 'Kitchen Renovation',
                },
                {
                    projectIdentifier: 'proj-002',
                    name: 'Office Space Update',
                },
            ]),
        });
    });
}

export async function mockMyReviews(page: Page, reviews: any[] = []) {
    await page.route('**/api/reviews/mine*', (route) => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(reviews),
        });
    });
}

export async function mockVisibleReviews(page: Page) {
    await page.route('**/api/reviews/visible*', (route) => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(MOCK_REVIEWS),
        });
    });
}

export async function mockAllReviews(page: Page) {
    await page.route('**/api/reviews', (route, request) => {
        // Only intercept GET requests (not POST/DELETE)
        if (request.method() === 'GET') {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(MOCK_REVIEWS),
            });
        } else {
            route.continue();
        }
    });
}

export async function mockReviewSubmission(page: Page) {
    await page.route('**/api/reviews', (route, request) => {
        if (request.method() === 'POST') {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify({
                    reviewId: 'test-review-new',
                    clientName: 'Charlie',
                    comment: 'E2E test review',
                    rating: 'FOUR',
                    visible: false,
                    type: 'Interior',
                    photos: [],
                }),
            });
        } else {
            route.continue();
        }
    });
}
