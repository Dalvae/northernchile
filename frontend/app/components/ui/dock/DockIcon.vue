<template>
  <div
    ref="iconRef"
    class="relative group"
    @mouseenter="handleMouseEnter"
    @mouseleave="isHovered = false"
    @mousemove="handleMouseMove"
    @click="handleClick"
  >
    <!-- Animated Tooltip -->
    <Motion
      v-if="tooltip && (isHovered || showTooltipMobile)"
      :initial="{
        opacity: 0,
        y: 20,
        scale: 0.6,
      }"
      :animate="{
        opacity: 1,
        y: 0,
        scale: 1,
      }"
      :transition="{
        type: 'spring',
        stiffness: 260,
        damping: 10,
      }"
      :exit="{
        opacity: 0,
        y: 20,
        scale: 0.6,
      }"
      class="absolute left-1/2 -translate-x-1/2 -top-16 z-50 flex flex-col items-center justify-center whitespace-nowrap rounded-lg bg-neutral-900 dark:bg-neutral-800 px-4 py-2.5 text-sm shadow-xl border border-neutral-700"
    >
      <div
        class="absolute inset-x-10 -bottom-px z-30 h-px w-1/5 bg-gradient-to-r from-transparent via-primary-500 to-transparent"
      />
      <div class="relative z-30 text-sm font-semibold text-white">
        {{ tooltip }}
      </div>
    </Motion>

    <!-- Icon Container -->
    <div
      class="flex aspect-square cursor-pointer items-center justify-center rounded-full transition-all duration-200 ease-out"
      :style="{
        width: `${iconWidth}px`,
        height: `${iconWidth}px`,
        marginLeft: `${margin}px`,
        marginRight: `${margin}px`,
      }"
    >
      <slot :size="iconWidth" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, inject, computed, watch } from "vue";
import { Motion } from "motion-v";
import {
  MOUSE_X_INJECTION_KEY,
  MOUSE_Y_INJECTION_KEY,
  MAGNIFICATION_INJECTION_KEY,
  DISTANCE_INJECTION_KEY,
  ORIENTATION_INJECTION_KEY,
  ACTIVE_TOOLTIP_INJECTION_KEY,
} from "./injectionKeys";

interface DockIconProps {
  tooltip?: string;
}

const props = defineProps<DockIconProps>();

// Generate unique ID for this icon
const iconId = `dock-icon-${Math.random().toString(36).substr(2, 9)}`;

const iconRef = ref<HTMLDivElement | null>(null);
const isHovered = ref(false);
const showTooltipMobile = ref(false);

const mouseX = inject(MOUSE_X_INJECTION_KEY, ref(Infinity));
const mouseY = inject(MOUSE_Y_INJECTION_KEY, ref(Infinity));
const distance = inject(DISTANCE_INJECTION_KEY);
const orientation = inject(ORIENTATION_INJECTION_KEY, "vertical");
const magnification = inject(MAGNIFICATION_INJECTION_KEY);
const activeTooltip = inject(ACTIVE_TOOLTIP_INJECTION_KEY, ref<string | null>(null));
const isVertical = computed(() => orientation === "vertical");

const margin = ref(0);

// Watch activeTooltip to close this tooltip when another opens
watch(activeTooltip, (newActiveId) => {
  if (newActiveId !== iconId && showTooltipMobile.value) {
    showTooltipMobile.value = false;
  }
});

function calculateDistance(val: number) {
  if (isVertical.value) {
    const bounds = iconRef.value?.getBoundingClientRect() || {
      y: 0,
      height: 0,
    };
    return val - bounds.y - bounds.height / 2;
  }
  const bounds = iconRef.value?.getBoundingClientRect() || { x: 0, width: 0 };
  return val - bounds.x - bounds.width / 2;
}

const iconWidth = computed(() => {
  const distanceCalc = isVertical.value
    ? calculateDistance(mouseY.value)
    : calculateDistance(mouseX.value);
  if (!distance?.value || !magnification?.value) return 40;
  if (Math.abs(distanceCalc) < distance?.value) {
    return (
      (1 - Math.abs(distanceCalc) / distance?.value) * magnification?.value + 40
    );
  }

  return 40;
});

// Handle mouse enter
function handleMouseEnter() {
  isHovered.value = true;
}

// Handle mouse movement (mantener para no romper el binding)
function handleMouseMove() {
  // No necesitamos hacer nada aquí ahora
}

// Handle click for mobile
function handleClick() {
  if (showTooltipMobile.value) {
    // Close this tooltip
    showTooltipMobile.value = false;
    activeTooltip.value = null;
  } else {
    // Open this tooltip and close others
    showTooltipMobile.value = true;
    activeTooltip.value = iconId;

    // Auto-hide after 3 seconds on mobile
    setTimeout(() => {
      if (activeTooltip.value === iconId) {
        showTooltipMobile.value = false;
        activeTooltip.value = null;
      }
    }, 3000);
  }
}
</script>
