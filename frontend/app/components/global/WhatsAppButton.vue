<script setup lang="ts">
const { t } = useI18n()

const WHATSAPP_NUMBER = '56942693271'

const whatsappUrl = computed(() => {
  const message = encodeURIComponent(t('whatsapp.default_message'))
  return `https://wa.me/${WHATSAPP_NUMBER}?text=${message}`
})

const isVisible = ref(false)

onMounted(() => {
  // Show button after a small delay for smoother UX
  setTimeout(() => {
    isVisible.value = true
  }, 1000)
})
</script>

<template>
  <Transition name="whatsapp-bounce">
    <a
      v-if="isVisible"
      :href="whatsappUrl"
      target="_blank"
      rel="noopener noreferrer"
      class="whatsapp-button"
      :aria-label="t('whatsapp.aria_label')"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="currentColor"
        class="w-7 h-7"
      >
        <path
          d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413z"
        />
      </svg>
    </a>
  </Transition>
</template>

<style scoped>
.whatsapp-button {
  position: fixed;
  bottom: 1.5rem;
  right: 1.5rem;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 3.5rem;
  height: 3.5rem;
  background-color: #25d366;
  color: white;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(37, 211, 102, 0.4);
  transition: all 0.3s ease;
}

.whatsapp-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(37, 211, 102, 0.5);
}

.whatsapp-button:active {
  transform: scale(0.95);
}

/* Bounce animation on entry */
.whatsapp-bounce-enter-active {
  animation: whatsapp-bounce-in 0.6s ease-out;
}

.whatsapp-bounce-leave-active {
  animation: whatsapp-bounce-in 0.3s ease-in reverse;
}

@keyframes whatsapp-bounce-in {
  0% {
    opacity: 0;
    transform: scale(0) translateY(20px);
  }
  50% {
    transform: scale(1.2) translateY(-5px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* Subtle pulse animation when idle */
@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(37, 211, 102, 0.4);
  }
  50% {
    box-shadow: 0 4px 20px rgba(37, 211, 102, 0.6);
  }
}

.whatsapp-button {
  animation: pulse 2s infinite;
}

.whatsapp-button:hover {
  animation: none;
}

/* Mobile adjustments */
@media (max-width: 640px) {
  .whatsapp-button {
    bottom: 1rem;
    right: 1rem;
    width: 3rem;
    height: 3rem;
  }

  .whatsapp-button svg {
    width: 1.5rem;
    height: 1.5rem;
  }
}
</style>
