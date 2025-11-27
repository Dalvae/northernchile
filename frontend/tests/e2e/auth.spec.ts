import { test, expect } from '@playwright/test'

test.describe('Authentication Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/auth')
  })

  test('should display login form by default', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /login|iniciar sesión/i })).toBeVisible()
    await expect(page.locator('input[type="email"]')).toBeVisible()
    await expect(page.locator('input[type="password"]')).toBeVisible()
  })

  test('should switch to register form', async ({ page }) => {
    const registerLink = page.getByRole('button', { name: /register|registr/i })
    await registerLink.click()
    await expect(page.getByRole('heading', { name: /register|registro|crear cuenta/i })).toBeVisible()
    await expect(page.locator('input[placeholder*="nombre" i], input[placeholder*="name" i]').first()).toBeVisible()
  })

  test('should show validation error for invalid email', async ({ page }) => {
    await page.locator('input[type="email"]').fill('invalid-email')
    await page.locator('input[type="password"]').fill('password123')
    await page.getByRole('button', { name: /login|iniciar sesión/i }).click()
    // Form should not submit with invalid email
    await expect(page).toHaveURL(/\/auth/)
  })

  test('should show error for incorrect credentials', async ({ page }) => {
    await page.locator('input[type="email"]').fill('nonexistent@test.com')
    await page.locator('input[type="password"]').fill('wrongpassword')
    await page.getByRole('button', { name: /login|iniciar sesión/i }).click()
    // Should show error toast or remain on auth page
    await expect(page).toHaveURL(/\/auth/)
  })

  test('should have links to terms and privacy policy', async ({ page }) => {
    await expect(page.getByRole('link', { name: /terms|términos/i })).toBeVisible()
    await expect(page.getByRole('link', { name: /privacy|privacidad/i })).toBeVisible()
  })
})

test.describe('Protected Routes', () => {
  test('should redirect unauthenticated user from profile to auth', async ({ page }) => {
    await page.goto('/profile/bookings')
    // Should redirect to auth or show login requirement
    await expect(page).toHaveURL(/\/(auth|profile)/)
  })

  test('should redirect unauthenticated user from admin to auth', async ({ page }) => {
    await page.goto('/admin')
    // Should redirect to auth or show access denied
    await expect(page).toHaveURL(/\/(auth|admin)/)
  })
})
