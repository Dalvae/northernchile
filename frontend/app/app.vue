<script setup lang="ts">
import '~/assets/css/main.css'

const { getSocialImage } = useSocialMeta()

useSeoMeta({
  titleTemplate: (titleChunk) => {
    return titleChunk
      ? `${titleChunk} | Northern Chile`
      : 'Northern Chile - Tours Astronómicos'
  },
  ogSiteName: 'Northern Chile Tours',
  ogType: 'website',
  twitterCard: 'summary_large_image',
  ogImage: getSocialImage(),
  twitterImage: getSocialImage()
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
