import { test, expect } from '../fixtures/fixtures';

test.describe('Portfolio Comment - Client - Desktop', () => {
  test.beforeEach(async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
  });

  test('client can add comment on portfolio item', async ({ page, loginAs }) => {
    // Login as client
    await loginAs('realClient');
    
    // Navigate to portfolio
    await page.goto('http://localhost:5173/portfolio');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    
    // Wait for portfolio items to load
    await page.waitForSelector('[class*="cursor-pointer"]', { timeout: 15000 });
    await page.waitForTimeout(1000);
    
    // Click on the first portfolio item
    const firstItem = page.locator('[class*="cursor-pointer"]').first();
    await firstItem.click();
    await page.waitForTimeout(1000);
    
    // Wait for modal to appear
    await page.waitForSelector('button:has(svg)', { timeout: 10000 });
    await page.waitForTimeout(500);
    
    // Generate unique comment text
    const commentText = `Client test comment ${Date.now()}`;
    
    // Find the textarea and fill in comment
    const textarea = page.locator('textarea[placeholder="Add a comment..."]');
    await textarea.waitFor({ state: 'visible', timeout: 10000 });
    await textarea.fill(commentText);
    await page.waitForTimeout(500);
    
    // Click the send button
    const sendButton = page.locator('button:has(svg):visible').last();
    await sendButton.click();
    
    // Wait for comment to appear in the list
    await page.waitForTimeout(3000); // Give time for API call
    
    // Verify the comment appears with the client's name
    await expect(page.locator(`text=${commentText}`)).toBeVisible();
    
    // Verify the username appears near the new comment (check the most recent comment)
    const comments = page.locator('div:has(> div > span.text-white)');
    const lastComment = comments.last();
    await expect(lastComment.locator('text=hitswave')).toBeVisible();
  });
});

test.describe('Portfolio Comment - Client - Mobile', () => {
  test.beforeEach(async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
  });

  test('client can add comment on portfolio item - Mobile', async ({ page, loginAs }) => {
    // Login as client
    await loginAs('realClient');
    
    // Navigate to portfolio
    await page.goto('http://localhost:5173/portfolio');
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    
    // Wait for portfolio items to load
    await page.waitForSelector('[class*="cursor-pointer"]', { timeout: 15000 });
    await page.waitForTimeout(1000);
    
    // Click on the first portfolio item
    const firstItem = page.locator('[class*="cursor-pointer"]').first();
    await firstItem.click();
    await page.waitForTimeout(1000);
    
    // Wait for modal to appear
    await page.waitForSelector('button:has(svg)', { timeout: 10000 });
    await page.waitForTimeout(500);
    
    // Generate unique comment text
    const commentText = `Client mobile test ${Date.now()}`;
    
    // Scroll to textarea if needed
    const textarea = page.locator('textarea[placeholder="Add a comment..."]');
    await textarea.waitFor({ state: 'visible', timeout: 10000 });
    await textarea.scrollIntoViewIfNeeded();
    await page.waitForTimeout(500);
    await textarea.fill(commentText);
    await page.waitForTimeout(500);
    
    // Click the send button
    const sendButton = page.locator('button:has(svg):visible').last();
    await sendButton.click();
    
    // Wait for comment to appear
    await page.waitForTimeout(3000);
    
    // Verify the comment appears
    await expect(page.locator(`text=${commentText}`)).toBeVisible();
    
    // Verify the username appears near the new comment (check the most recent comment)
    const comments = page.locator('div:has(> div > span.text-white)');
    const lastComment = comments.last();
    await expect(lastComment.locator('text=hitswave')).toBeVisible();
  });
});
