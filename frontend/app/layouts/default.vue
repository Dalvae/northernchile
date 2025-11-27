<script setup lang="ts">
const i18nHead = useLocaleHead()

useHead(() => ({
  htmlAttrs: {
    lang: i18nHead.value.htmlAttrs?.lang,
    dir: i18nHead.value.htmlAttrs?.dir as 'ltr' | 'rtl' | 'auto' | undefined
  },
  titleTemplate: (titleChunk: string | undefined) => {
    return titleChunk
      ? `${titleChunk} | Northern Chile`
      : 'Northern Chile - Tours Astronómicos en Atacama'
  },
  link: [...(i18nHead.value.link || [])],
  meta: [...(i18nHead.value.meta || [])]
}))

// Initialize Google Analytics only on public pages when user accepts cookies
const route = useRoute()
const { initialize } = useGtag()
const { consentGiven } = useCookieConsent()

// Routes that should NOT be tracked
const excludedRoutes = ['/admin', '/profile', '/bookings', '/cart', '/checkout', '/auth', '/payment']

// Watch for consent changes and initialize gtag accordingly
watch(() => consentGiven.value, (hasConsent) => {
  if (hasConsent === true) {
    const isPublicRoute = !excludedRoutes.some(excluded => route.path.startsWith(excluded))
    if (isPublicRoute) {
      initialize()
    }
  }
}, { immediate: true })
</script>

<template>
  <div>
    <LayoutTheHeader />

    <UMain as="main">
      <slot />
    </UMain>

    <LazyLayoutTheFooter />

    <ClientOnly>
      <CookieBanner />
    </ClientOnly>
  </div>
</template>
