<script setup lang="ts">
const i18nHead = useLocaleHead({
  addSeoAttributes: true
})

useHead(() => ({
  htmlAttrs: {
    lang: i18nHead.value.htmlAttrs?.lang,
    dir: i18nHead.value.htmlAttrs?.dir as 'ltr' | 'rtl' | 'auto' | undefined
  },
  titleTemplate: (titleChunk) => {
    return titleChunk
      ? `${titleChunk} | Northern Chile`
      : 'Northern Chile - Tours Astronómicos en Atacama'
  },
  link: [...(i18nHead.value.link || [])],
  meta: [...(i18nHead.value.meta || [])]
}))

// Initialize Google Analytics only on public pages
const route = useRoute()
const { initialize } = useGtag()

// Routes that should NOT be tracked
const excludedRoutes = ['/admin', '/profile', '/bookings', '/cart', '/checkout', '/auth', '/payment']

// Initialize gtag only if we're on a public route
onMounted(() => {
  const isPublicRoute = !excludedRoutes.some(excluded => route.path.startsWith(excluded))
  if (isPublicRoute) {
    initialize()
  }
})
</script>

<template>
  <div>
    <LayoutTheHeader />

    <UMain as="main">
      <slot />
    </UMain>

    <LazyLayoutTheFooter />
  </div>
</template>
