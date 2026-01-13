<script setup lang="ts">
const i18nHead = useLocaleHead()
const { t } = useI18n()
const route = useRoute()
const { initialize } = useGtag()
const { consentGiven } = useCookieConsent()

useHead(() => ({
  htmlAttrs: {
    lang: i18nHead.value.htmlAttrs?.lang,
    dir: i18nHead.value.htmlAttrs?.dir as 'ltr' | 'rtl' | 'auto' | undefined
  },
  titleTemplate: (titleChunk: string | undefined) => {
    return titleChunk
      ? `${titleChunk} | ${t('site.name')}`
      : t('site.default_title')
  },
  link: [...(i18nHead.value.link || [])],
  meta: [...(i18nHead.value.meta || [])]
}))

// Define default OG Image for the whole site
defineOgImageComponent('Default', {
  title: t('home.hero.title_line1'),
  description: t('home.meta.description')
})

// Routes that should NOT be tracked
const excludedRoutes = ['/admin', '/profile', '/bookings', '/cart', '/checkout', '/auth', '/payment']

// Initialize Google Analytics only on public pages when user accepts cookies
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
    <DevWarningBanner />
    <LayoutTheHeader />

    <UMain as="main">
      <slot />
    </UMain>

    <LazyLayoutTheFooter />

    <ClientOnly>
      <CookieBanner />
      <DevWarningModal />
    </ClientOnly>
    <WhatsAppButton />
  </div>
</template>
