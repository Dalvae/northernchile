<script setup lang="ts">
import { useTheme } from '~/composables/useTheme'

const { themes, setTheme } = useTheme()

const options = computed(() => [
  themes.map((key) => {
    if (key === 'atacama-nocturna') {
      return {
        label: 'Atacama Nocturna',
        icon: 'i-lucide-moon-star',
        onSelect: () => setTheme('atacama-nocturna')
      }
    }
    if (key === 'atacama-cobre-lunar') {
      return {
        label: 'Atacama Cobre Lunar',
        icon: 'i-lucide-sparkles',
        onSelect: () => setTheme('atacama-cobre-lunar')
      }
    }
    if (key === 'atacama-cosmic-desert') {
      return {
        label: 'Atacama Cosmic Desert',
        icon: 'i-lucide-orbit',
        onSelect: () => setTheme('atacama-cosmic-desert')
      }
    }
    if (key === 'aurora') {
      return {
        label: 'Aurora',
        icon: 'i-lucide-sun-medium',
        onSelect: () => setTheme('aurora')
      }
    }
    if (key === 'atacama-classic') {
      return {
        label: 'Atacama Classic',
        icon: 'i-lucide-mountain',
        onSelect: () => setTheme('atacama-classic')
      }
    }
    if (key === 'cosmic') {
      return {
        label: 'Cosmic',
        icon: 'i-lucide-sparkles',
        onSelect: () => setTheme('cosmic')
      }
    }

    return {
      label: key,
      icon: 'i-lucide-swatch-book',
      onSelect: () => setTheme(key)
    }
  })
])
</script>

<template>
  <UDropdownMenu :items="options">
    <UButton
      color="neutral"
      variant="ghost"
      icon="i-lucide-swatch-book"
      aria-label="Theme switcher"
      class="hover:bg-white/10 text-white"
    />
  </UDropdownMenu>
</template>
