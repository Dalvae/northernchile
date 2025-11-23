<script setup lang="ts">
import "~/assets/css/main.css";

defineOgImageComponent("Tour", {
  title: "Northern Chile",
  category: "San Pedro de Atacama",
  image:
    "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=1200&auto=format&fit=crop",
  price: undefined,
});

useSeoMeta({
  titleTemplate: (titleChunk) => {
    return titleChunk
      ? `${titleChunk} | Northern Chile`
      : "Northern Chile - Tours Astronómicos";
  },
  ogSiteName: "Northern Chile Tours",
  ogType: "website",
  twitterCard: "summary_large_image",
});
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
