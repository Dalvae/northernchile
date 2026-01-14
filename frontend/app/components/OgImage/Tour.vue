<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  title?: string
  price?: string
  image?: string
  category?: string
  description?: string
}>()

// Truncate description to ~120 chars for OG image
const truncatedDescription = computed(() => {
  if (!props.description) return ''
  if (props.description.length <= 120) return props.description
  return props.description.substring(0, 117) + '...'
})
</script>

<template>
  <div class="flex flex-row w-[1200px] h-[630px]">
    <!-- Left: Tour Image with gradient overlay -->
    <div
      class="flex w-[480px] h-[630px] relative"
      style="overflow: hidden;"
    >
      <!-- Tour image or fallback -->
      <img
        v-if="image"
        :src="image"
        class="w-[630px] h-[630px]"
        style="object-fit: cover; position: absolute; left: 0; top: 0;"
      >
      <!-- Fallback if no image -->
      <img
        v-else
        src="/images/private-tours-hero.png"
        class="w-[630px] h-[630px]"
        style="object-fit: cover; position: absolute; left: 0; top: 0;"
      >
      <!-- Gradient overlay to blend with right side -->
      <div
        class="absolute w-[480px] h-[630px]"
        style="background: linear-gradient(90deg, transparent 0%, transparent 50%, #0a1628 100%); left: 0; top: 0;"
      />
    </div>

    <!-- Right: Content -->
    <div
      class="flex flex-col w-[720px] h-[630px] p-[40px] justify-between"
      style="background: linear-gradient(135deg, #0a1628 0%, #0f172a 50%, #1a1f35 100%);"
    >
      <!-- Header: Logo + Brand -->
      <div class="flex flex-row items-center gap-[12px]">
        <!-- Telescopio icon -->
        <svg
          width="32"
          height="32"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#fbaa62"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="m10.065 12.493-6.18 1.318a.934.934 0 0 1-1.108-.702l-.537-2.15a1.07 1.07 0 0 1 .691-1.265l13.504-4.44" />
          <path d="m13.56 11.747 4.332-.924" />
          <path d="m16 21-3.105-6.21" />
          <path d="M16.485 5.94a2 2 0 0 1 1.455-2.425l1.09-.272a1 1 0 0 1 1.212.727l1.515 6.06a1 1 0 0 1-.727 1.213l-1.09.272a2 2 0 0 1-2.425-1.455z" />
          <path d="m6.158 8.633 1.114 4.456" />
          <path d="m8 21 3.105-6.21" />
          <circle
            cx="12"
            cy="13"
            r="2"
          />
        </svg>
        <span
          class="text-[20px] font-bold tracking-wide"
          style="font-family: 'Playfair Display', serif; color: #ffffff;"
        >
          Northern Chile Tours
        </span>
        <div
          class="h-[1px] flex-1 ml-[12px]"
          style="background: linear-gradient(to right, rgba(251, 170, 98, 0.5) 0%, transparent 100%);"
        />
      </div>

      <!-- Center: Tour Info (flex-1 to take remaining space, overflow hidden) -->
      <div
        class="flex flex-col gap-[12px] flex-1 justify-center"
        style="overflow: hidden;"
      >
        <!-- Category Badge -->
        <div
          v-if="category"
          class="flex flex-row items-center gap-[8px] px-[14px] py-[6px] rounded-full w-fit"
          style="background: rgba(251, 170, 98, 0.15); border: 1px solid rgba(251, 170, 98, 0.4);"
        >
          <svg
            width="12"
            height="12"
            viewBox="0 0 24 24"
            fill="#fbaa62"
          >
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" />
          </svg>
          <span
            class="text-[11px] font-semibold uppercase tracking-wider"
            style="color: #fbaa62;"
          >
            {{ category }}
          </span>
        </div>

        <!-- Tour Title -->
        <div
          class="text-[40px] font-bold text-white"
          style="font-family: 'Playfair Display', serif; line-height: 1.15;"
        >
          {{ title || t('tours.og.default_title') }}
        </div>

        <!-- Description (truncated) -->
        <div
          v-if="truncatedDescription"
          class="text-[15px]"
          style="color: rgba(255, 255, 255, 0.7); line-height: 1.4;"
        >
          {{ truncatedDescription }}
        </div>
      </div>

      <!-- Footer: Location + Price -->
      <div
        class="flex flex-row items-end justify-between pt-[16px]"
        style="border-top: 1px solid rgba(255,255,255,0.1);"
      >
        <!-- Location -->
        <div class="flex flex-row items-center gap-[8px]">
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#fbaa62"
            stroke-width="2"
          >
            <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
            <circle
              cx="12"
              cy="10"
              r="3"
            />
          </svg>
          <span
            class="text-[16px]"
            style="color: rgba(255, 255, 255, 0.7);"
          >{{ t('tours.location') }}</span>
        </div>

        <!-- Price -->
        <div
          v-if="price"
          class="flex flex-row items-baseline gap-[8px]"
        >
          <span
            class="text-[14px]"
            style="color: rgba(255,255,255,0.5);"
          >{{ t('tours.price_from') }}</span>
          <span
            class="text-[36px] font-bold"
            style="color: #fbaa62;"
          >{{ price }}</span>
        </div>
        <div
          v-else
          class="text-[16px] font-semibold"
          style="color: #fbaa62;"
        >
          www.northernchile.com
        </div>
      </div>
    </div>
  </div>
</template>
