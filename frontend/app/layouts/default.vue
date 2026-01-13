<script setup lang="ts">
const i18nHead = useLocaleHead()
const { t } = useI18n()

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
})

// Define default OG Image for the whole site
defineOgImageComponent('Default', {
  siteName: 'Northern Chile Tours',
  title: 'San Pedro de Atacama',
  description: t('home.meta.description')
})
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
