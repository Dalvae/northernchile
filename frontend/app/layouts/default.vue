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
