<script setup lang="ts">
// 1. SEO Multilingüe: Genera hreflang, canonical y og:locale
const i18nHead = useLocaleHead({
  addDirAttribute: true,      // Agrega dir="ltr"
  identifierAttribute: 'id',  // Usa 'id' para identificar la página
  addSeoAttributes: true      // Agrega atributos SEO básicos
})

// 2. Inyección Global de Metadatos
// Usamos una función () => para asegurar reactividad total
useHead(() => ({
  htmlAttrs: {
    lang: i18nHead.value.htmlAttrs?.lang,
    dir: i18nHead.value.htmlAttrs?.dir
  },
  // Título base para todas las páginas: "%s | Northern Chile"
  titleTemplate: (titleChunk) => {
    return titleChunk 
      ? `${titleChunk} | Northern Chile` 
      : 'Northern Chile - Tours Astronómicos en Atacama'
  },
  // Inyecta los links (canonical, hreflang) generados por i18n
  link: [...(i18nHead.value.link || [])],
  // Inyecta los meta tags (og:locale, etc.) generados por i18n
  meta: [...(i18nHead.value.meta || [])]
}))
</script>

<template>
  <div>
    <LayoutTheHeader />
    <UMain>
      <slot />
    </UMain>
    <LazyLayoutTheFooter />
  </div>
</template>
