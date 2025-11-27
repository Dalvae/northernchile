<script setup lang="ts">
const { t } = useI18n()
const { shouldShowBanner, acceptCookies, rejectCookies } = useCookieConsent()

// Show banner after user scrolls or after 3 seconds
const showBanner = ref(false)
const hasInteracted = ref(false)

// Detect scroll interaction
function handleScroll() {
  if (!hasInteracted.value && window.scrollY > 100) {
    hasInteracted.value = true
    showBanner.value = true
    window.removeEventListener('scroll', handleScroll)
  }
}

// Show after timeout if no interaction
onMounted(() => {
  if (shouldShowBanner.value) {
    // Listen for scroll
    window.addEventListener('scroll', handleScroll)

    // Fallback: show after 3 seconds if no scroll
    setTimeout(() => {
      if (!hasInteracted.value) {
        showBanner.value = true
        window.removeEventListener('scroll', handleScroll)
      }
    }, 3000)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

function handleAccept() {
  acceptCookies()
  showBanner.value = false
}

function handleReject() {
  rejectCookies()
  showBanner.value = false
}
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="translate-y-full opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-full opacity-0"
    >
      <div
        v-if="shouldShowBanner && showBanner"
        class="fixed bottom-0 left-0 right-0 z-50 p-4 sm:p-6"
      >
        <div class="mx-auto max-w-7xl">
          <UCard
            class="shadow-xl bg-neutral-50 dark:bg-neutral-900 ring-1 ring-neutral-200 dark:ring-neutral-800"
          >
            <div class="flex flex-col gap-3 sm:gap-4 sm:flex-row sm:items-center sm:justify-between">
              <!-- Content -->
              <div class="flex-1">
                <div class="flex items-start gap-2 sm:gap-3">
                  <UIcon
                    name="i-heroicons-information-circle"
                    class="size-5 flex-shrink-0 text-info-500 mt-0.5 hidden sm:block"
                  />
                  <div class="flex-1 min-w-0">
                    <p class="text-xs sm:text-sm text-neutral-700 dark:text-neutral-300">
                      {{ t('cookie_banner.message') }}
                      <NuxtLink
                        to="/privacy"
                        class="text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 underline"
                      >
                        {{ t('cookie_banner.learn_more') }}
                      </NuxtLink>
                    </p>
                  </div>
                </div>
              </div>

              <!-- Actions -->
              <div class="flex items-center gap-2 sm:flex-shrink-0">
                <UButton
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  class="flex-1 sm:flex-none"
                  @click="handleReject"
                >
                  {{ t('cookie_banner.reject') }}
                </UButton>
                <UButton
                  color="primary"
                  size="xs"
                  class="flex-1 sm:flex-none"
                  @click="handleAccept"
                >
                  {{ t('cookie_banner.accept') }}
                </UButton>
              </div>
            </div>
          </UCard>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
