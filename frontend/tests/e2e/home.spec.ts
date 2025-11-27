import { test, expect } from '@playwright/test'

test.describe('Homepage', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
  })

  test('should display hero section', async ({ page }) => {
    await expect(page.locator('h1, h2').first()).toBeVisible()
  })

  test('should display featured tours section', async ({ page }) => {
    const featuredSection = page.getByText(/experiencias destacadas|featured tours/i).first()
    await expect(featuredSection).toBeVisible()
  })

  test('should navigate to tours page', async ({ page }) => {
    const viewAllButton = page.getByRole('link', { name: /ver todos|view all/i }).first()
    await viewAllButton.click()
    await expect(page).toHaveURL(/\/tours/)
  })

  test('should navigate to contact page', async ({ page }) => {
    const contactButton = page.getByRole('link', { name: /contact/i }).first()
    await contactButton.click()
    await expect(page).toHaveURL(/\/contact/)
  })
})
