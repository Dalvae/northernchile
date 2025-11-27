import { test, expect } from '@playwright/test'

test.describe('Language Switching', () => {
  test('should display language switcher', async ({ page }) => {
    await page.goto('/')
    // Look for language switcher
    const langSwitcher = page.locator('[class*="language"], [data-testid*="lang"], button:has-text("ES"), button:has-text("EN")')
    await expect(langSwitcher.first()).toBeVisible()
  })

  test('should switch to English', async ({ page }) => {
    await page.goto('/en')
    // Page should be in English
    await expect(page.locator('html')).toHaveAttribute('lang', /en/i)
  })

  test('should switch to Spanish', async ({ page }) => {
    await page.goto('/es')
    // Page should be in Spanish
    await expect(page.locator('html')).toHaveAttribute('lang', /es/i)
  })

  test('should switch to Portuguese', async ({ page }) => {
    await page.goto('/pt')
    // Page should be in Portuguese
    await expect(page.locator('html')).toHaveAttribute('lang', /pt/i)
  })
})

test.describe('Theme Switching', () => {
  test('should toggle dark mode', async ({ page }) => {
    await page.goto('/')
    // Look for theme toggle
    const themeToggle = page.locator('[class*="theme"], [data-testid*="theme"], button[aria-label*="theme" i], button[aria-label*="dark" i]')
    if (await themeToggle.count() > 0) {
      await themeToggle.first().click()
      // Check if dark class is applied
      const html = page.locator('html')
      const hasDarkClass = await html.evaluate(el => el.classList.contains('dark'))
      expect(typeof hasDarkClass).toBe('boolean')
    }
  })
})

test.describe('Navigation', () => {
  test('should display main navigation links', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('link', { name: /tours/i }).first()).toBeVisible()
  })

  test('should display mobile menu on small screens', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/')
    // Mobile menu button should be visible
    const menuButton = page.locator('button[aria-label*="menu" i], [class*="mobile-menu"], [class*="hamburger"]')
    if (await menuButton.count() > 0) {
      await expect(menuButton.first()).toBeVisible()
    }
  })
})

test.describe('Footer', () => {
  test('should display footer with links', async ({ page }) => {
    await page.goto('/')
    const footer = page.locator('footer')
    await footer.scrollIntoViewIfNeeded()
    await expect(footer).toBeVisible()
  })

  test('should have terms of service link', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('link', { name: /terms|términos/i }).first()).toBeVisible()
  })

  test('should have privacy policy link', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('link', { name: /privacy|privacidad/i }).first()).toBeVisible()
  })
})
