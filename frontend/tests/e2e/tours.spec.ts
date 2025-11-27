import { test, expect } from '@playwright/test'

test.describe('Tours Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/tours')
  })

  test('should display tours listing page', async ({ page }) => {
    await expect(page.locator('h1')).toContainText(/tours|experiencias/i)
  })

  test('should display tour cards', async ({ page }) => {
    const tourCards = page.locator('[class*="tour"], [class*="card"]').first()
    await expect(tourCards).toBeVisible({ timeout: 10000 })
  })

  test('should navigate to tour detail when clicking view details', async ({ page }) => {
    const viewDetailsButton = page.getByRole('link', { name: /ver detalles|view details/i }).first()
    await viewDetailsButton.click()
    await expect(page).toHaveURL(/\/tours\//)
  })

  test('should display calendar section when scrolled', async ({ page }) => {
    const calendarSection = page.getByText(/calendario|calendar/i).first()
    await calendarSection.scrollIntoViewIfNeeded()
    await expect(calendarSection).toBeVisible()
  })

  test('should display tour price', async ({ page }) => {
    const priceElement = page.getByText(/\$|desde|from/i).first()
    await expect(priceElement).toBeVisible()
  })
})
