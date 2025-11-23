<script setup lang="ts">
import '~/assets/css/main.css'

defineOgImageComponent('Tour', {
  title: 'Northern Chile Tours',
  category: 'Experiencias',
  image: '/images/hero-bg.jpg',
  price: 'San Pedro de Atacama'
})

useSeoMeta({
  titleTemplate: (titleChunk) => {
    return titleChunk
      ? `${titleChunk} | Northern Chile`
      : 'Northern Chile - Tours Astronómicos'
  },
  ogSiteName: 'Northern Chile Tours',
  ogType: 'website',
  twitterCard: 'summary_large_image'
})
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
