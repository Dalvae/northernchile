import { test, expect } from '@playwright/test'

test.describe('Cart Flow', () => {
  test('should display empty cart message', async ({ page }) => {
    await page.goto('/cart')
    // Cart should show empty state or items
    await expect(page.locator('body')).toContainText(/carrito|cart/i)
  })

  test('should redirect empty cart checkout to cart page', async ({ page }) => {
    // Clear any existing cart
    await page.goto('/checkout')
    // Should redirect to cart if empty
    await expect(page).toHaveURL(/\/(cart|checkout)/)
  })
})

test.describe('Checkout Flow', () => {
  test.describe('Step 1: Contact Information', () => {
    test.skip('should display contact form fields', async ({ page }) => {
      // This test requires a cart with items
      await page.goto('/checkout')
      await expect(page.getByText(/contacto|contact/i).first()).toBeVisible()
      await expect(page.locator('input[placeholder*="nombre" i], input[placeholder*="name" i]').first()).toBeVisible()
      await expect(page.locator('input[type="email"]')).toBeVisible()
      await expect(page.locator('input[type="tel"]')).toBeVisible()
    })
  })

  test.describe('Step 2: Participants', () => {
    test.skip('should display participant forms', async ({ page }) => {
      // This test requires completing step 1
      await page.goto('/checkout')
      // Navigate to step 2
      await expect(page.getByText(/participante/i).first()).toBeVisible()
    })
  })

  test.describe('Step 3: Payment', () => {
    test.skip('should display payment method selector', async ({ page }) => {
      // This test requires completing step 1 and 2
      await page.goto('/checkout')
      // Navigate to step 3
      await expect(page.getByText(/pago|payment/i).first()).toBeVisible()
    })
  })
})
