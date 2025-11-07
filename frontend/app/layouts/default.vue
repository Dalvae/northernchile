<script setup>
// 1. SEO Multilingüe (hreflang, etc.)
const i18nHead = useLocaleHead({
  addDirAttribute: true,
  identifierAttribute: 'id',
  addSeoAttributes: true
})

// 2. Plantilla para los títulos y meta-etiquetas por defecto para todo el sitio
useHead({
  htmlAttrs: {
    lang: i18nHead.value.htmlAttrs.lang
  },
  // titleTemplate es la clave: '%s' será reemplazado por el título de cada página
  titleTemplate: (titleChunk) => {
    return titleChunk ? `${titleChunk} | Northern Chile` : 'Northern Chile - Tours Astronómicos en Atacama'
  },
  link: i18nHead.value.link,
  meta: i18nHead.value.meta
})
</script>

<template>
  <div>
    <LayoutTheHeader />
    <UMain>
      <slot />
    </UMain>
    <LayoutTheFooter />
  </div>
</template>
