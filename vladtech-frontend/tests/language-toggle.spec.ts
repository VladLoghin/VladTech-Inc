import { test, expect } from '@playwright/test';

test.describe('Language Toggle', () => {
  test('should toggle language from English to French on home page', async ({ page }) => {
    // Navigate to the home page
    await page.goto('http://localhost:5173/');

    // Wait for the page to load
    await page.waitForLoadState('networkidle');

    // Find the language toggle button using its aria-label
    const languageToggle = page.locator('[aria-label="Toggle language"]');
    await expect(languageToggle).toBeVisible();

    // Verify initial state is English (aria-checked should be false when English is selected)
    await expect(languageToggle).toHaveAttribute('aria-checked', 'false');

    // Verify the toggle indicator is on the left (English position)
    const toggleIndicator = languageToggle.locator('div').last();
    await expect(toggleIndicator).toHaveClass(/left-1/);

    // Click the language toggle to switch to French
    await languageToggle.click();

    // Verify the state changed to French (aria-checked should be true when French is selected)
    await expect(languageToggle).toHaveAttribute('aria-checked', 'true');

    // Verify the toggle indicator moved to the right (French position)
    await expect(toggleIndicator).toHaveClass(/left-\[calc\(100%-1\.75rem\)\]/);

    // Optional: Verify that the language actually changed by checking some translated text
    // You can uncomment and modify this if you want to verify actual content changes
    // const welcomeText = page.locator('text=Bienvenue'); // Example French text
    // await expect(welcomeText).toBeVisible();
  });

  test('should toggle language from English to French and back to English', async ({ page }) => {
    // Navigate to the home page
    await page.goto('http://localhost:5173/');
    await page.waitForLoadState('networkidle');

    const languageToggle = page.locator('[aria-label="Toggle language"]');

    // Initial state: English
    await expect(languageToggle).toHaveAttribute('aria-checked', 'false');

    // Toggle to French
    await languageToggle.click();
    await expect(languageToggle).toHaveAttribute('aria-checked', 'true');

    // Toggle back to English
    await languageToggle.click();
    await expect(languageToggle).toHaveAttribute('aria-checked', 'false');
  });
});
