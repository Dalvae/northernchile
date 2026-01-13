<script setup lang="ts">
import '~/assets/css/main.css'

const { t } = useI18n()
const config = useRuntimeConfig()

// OG Image default configuration
const ogImageOptions = computed(() => ({
  title: t('home.meta.og_title'),
  category: t('home.meta.og_category'),
  width: 1200,
  height: 630,
  image: 'https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=1200&auto=format&fit=crop',
  price: undefined
}))

defineOgImageComponent('Tour', ogImageOptions)

// Schema.org WebSite para que Google muestre el nombre del sitio correctamente
useSchemaOrg([
  defineWebSite({
    name: 'Northern Chile Tours',
    alternateName: 'Northern Chile',
    url: config.public.baseUrl
  })
])

// SEO Meta tags globales
const seoMetaConfig: Parameters<typeof useSeoMeta>[0] & { fbAppId?: string } = {
  titleTemplate: (titleChunk: string | undefined) => {
    return titleChunk
      ? `${titleChunk} | Northern Chile`
      : `${t('home.hero.title_line1')} - Northern Chile`
  },
  ogSiteName: 'Northern Chile Tours',
  ogType: 'website',
  twitterCard: 'summary_large_image',
  ogImageWidth: 1200,
  ogImageHeight: 630
}

// Agregar fb:app_id solo si está configurado
if (config.public.fbAppId) {
  seoMetaConfig.fbAppId = config.public.fbAppId
}

useSeoMeta(seoMetaConfig)
</script>

<template>
  <div class="dark">
    <UApp>
      <NuxtLayout>
        <NuxtPage transition />
      </NuxtLayout>
    </UApp>
  </div>
</template>

<style>
.page-enter-active,
.page-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
