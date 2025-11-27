/**
 * Composable for managing cookie consent banner
 * Tracks user consent for analytics cookies and initializes gtag when accepted
 */
export function useCookieConsent() {
  const COOKIE_NAME = 'cookie-consent'
  const COOKIE_EXPIRY = 365 // days

  // Get consent status from cookie
  const consentGiven = useCookie<boolean | null>(COOKIE_NAME, {
    maxAge: 60 * 60 * 24 * COOKIE_EXPIRY,
    sameSite: 'lax',
    secure: true
  })

  const { initialize: initializeGtag } = useGtag()

  /**
   * Accept cookies and initialize analytics
   */
  function acceptCookies() {
    consentGiven.value = true

    // Initialize Google Analytics if on public route
    const route = useRoute()
    const excludedRoutes = ['/admin', '/profile', '/bookings', '/cart', '/checkout', '/auth', '/payment']
    const isPublicRoute = !excludedRoutes.some(excluded => route.path.startsWith(excluded))

    if (isPublicRoute) {
      initializeGtag()
    }
  }

  /**
   * Reject cookies
   */
  function rejectCookies() {
    consentGiven.value = false
  }

  /**
   * Check if banner should be shown
   */
  const shouldShowBanner = computed(() => {
    return consentGiven.value === null || consentGiven.value === undefined
  })

  return {
    consentGiven,
    shouldShowBanner,
    acceptCookies,
    rejectCookies
  }
}
