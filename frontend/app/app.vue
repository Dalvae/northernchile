<script setup lang="ts">
// Preload critical fonts to avoid render-blocking
useHead({
  link: [
    // Preload critical Playfair Display font (headings)
    {
      rel: 'preload',
      as: 'font',
      type: 'font/woff2',
      href: '/_fonts/a0pq7nGAs5PnCDc-r_b7.woff2',
      crossorigin: 'anonymous'
    },
    // Preload critical Inter font (body text)
    {
      rel: 'preload',
      as: 'font',
      type: 'font/woff2',
      href: '/_fonts/HgJel4iASKShcI4f-gY.woff2',
      crossorigin: 'anonymous'
    }
  ]
})
</script>

<template>
  <div class="dark">
    <UApp>
      <NuxtLayout>
        <NuxtPage />
      </NuxtLayout>
    </UApp>
  </div>
</template>
