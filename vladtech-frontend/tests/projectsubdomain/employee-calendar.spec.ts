// tests/projectsubdomain/employee-click-jan15-calendar.spec.ts
import { test, expect } from '../fixtures/fixtures';

test('employee clicks January 15 on calendar (employee dashboard)', async ({ page, loginAs }) => {
  // Login as employee (uses your provided fixture)
  await loginAs('employee');

  // Go to employee dashboard
  await page.goto('http://localhost:5173/employee');

  await expect(page.getByRole('heading', { name: /employee area/i })).toBeVisible();

  // Calendar is FullCalendar in your app (fc-daygrid). Click the Jan 15 cell.
  // This targets the day cell by its data-date attribute.
  const jan15Cell = page.locator('[data-date="2026-01-15"]');

  await expect(jan15Cell).toBeVisible({ timeout: 10000 });

  // Click inside the day cell (more reliable than clicking "15" text which may exist multiple times)
  await jan15Cell.click();

  // Optional: assert the right-side panel updates to show the selected date.
  // If your UI prints the formatted date somewhere (like "Thursday, January 15, 2026"),
  // this should pass. If not, delete this expectation.
  await expect(page.getByText(/January\s+15,\s+2026/i)).toBeVisible({ timeout: 10000 });
});
