<script setup lang="ts">
const { t } = useI18n()

withDefaults(defineProps<{
  title?: string
  description?: string
}>(), {
  title: '',
  description: ''
})
</script>

<template>
  <div class="flex flex-row w-[1200px] h-[630px]">
    <!-- Left: Hero Image with gradient overlay -->
    <div
      class="flex w-[480px] h-[630px] relative"
      style="overflow: hidden;"
    >
      <img
        src="/images/private-tours-hero.png"
        class="w-[630px] h-[630px]"
        style="object-fit: cover; position: absolute; left: 0; top: 0;"
      >
      <!-- Gradient overlay to blend with right side -->
      <div
        class="absolute w-[480px] h-[630px]"
        style="background: linear-gradient(90deg, transparent 0%, transparent 60%, #0a1628 100%); left: 0; top: 0;"
      />
    </div>

    <!-- Right: Content -->
    <div
      class="flex flex-col w-[720px] h-[630px] p-[50px] justify-between"
      style="background: linear-gradient(135deg, #0a1628 0%, #0f172a 50%, #1a1f35 100%);"
    >
      <!-- Header: Logo + Badge (centered) -->
      <div class="flex flex-col items-center w-full gap-[12px]">
        <!-- Telescopio icon -->
        <svg
          width="48"
          height="48"
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

        <!-- Badge -->
        <div
          class="flex flex-row items-center gap-[8px] px-[16px] py-[8px] rounded-full"
          style="background: rgba(251, 170, 98, 0.2); border: 1px solid rgba(251, 170, 98, 0.5);"
        >
          <svg
            width="14"
            height="14"
            viewBox="0 0 24 24"
            fill="#fbaa62"
          >
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" />
          </svg>
          <span
            class="text-[11px] font-bold uppercase tracking-widest"
            style="color: #fbaa62;"
          >
            {{ t('home.hero.badge') }}
          </span>
        </div>
      </div>

      <!-- Center: Titles -->
      <div class="flex flex-col items-center w-full gap-[8px]">
        <!-- Main title: Northern Chile Tours -->
        <div
          class="text-[52px] font-bold text-white text-center"
          style="font-family: 'Playfair Display', serif; line-height: 1.1;"
        >
          {{ title || t('home.hero.title_line1') }}
        </div>

        <!-- Subtitle: San Pedro de Atacama -->
        <div
          class="text-[32px] font-bold text-center"
          style="font-family: 'Playfair Display', serif; color: #fbaa62; line-height: 1.2;"
        >
          {{ t('home.hero.title_line2') }}
        </div>

        <!-- Description -->
        <div
          class="text-[18px] mt-[8px] text-center"
          style="color: rgba(255, 255, 255, 0.8); max-width: 580px; line-height: 1.5;"
        >
          {{ description || t('home.hero.subtitle') }}
        </div>
      </div>

      <!-- Footer: Features + URL (centered) -->
      <div class="flex flex-col items-center w-full gap-[12px]">
        <div class="flex flex-row items-center gap-[24px]">
          <div
            class="flex flex-row items-center gap-[6px]"
            style="color: rgba(255, 255, 255, 0.8);"
          >
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" />
            </svg>
            <span class="text-[14px]">{{ t('home.hero.tooltips.clearSkies') }}</span>
          </div>
          <div
            class="flex flex-row items-center gap-[6px]"
            style="color: rgba(255, 255, 255, 0.8);"
          >
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
              <circle
                cx="9"
                cy="7"
                r="4"
              />
              <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
              <path d="M16 3.13a4 4 0 0 1 0 7.75" />
            </svg>
            <span class="text-[14px]">{{ t('home.hero.tooltips.expertGuides') }}</span>
          </div>
          <div
            class="flex flex-row items-center gap-[6px]"
            style="color: rgba(255, 255, 255, 0.8);"
          >
            <svg
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10" />
              <path d="m9 12 2 2 4-4" />
            </svg>
            <span class="text-[14px]">{{ t('home.hero.tooltips.freeCancellation') }}</span>
          </div>
        </div>

        <!-- URL -->
        <span
          class="text-[16px] font-semibold text-center"
          style="color: #fbaa62;"
        >
          www.northernchile.com
        </span>
      </div>
    </div>
  </div>
</template>
