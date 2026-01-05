<template>
  <UModal
    v-model:open="isOpen"
    :dismissible="false"
    :ui="{
      content: 'sm:max-w-lg'
    }"
  >
    <template #content>
      <UCard>
        <template #header>
          <div class="flex items-center gap-3">
            <UIcon
              name="i-lucide-triangle-alert"
              class="size-8 text-warning"
            />
            <h2 class="text-xl font-bold text-warning">
              {{ t('dev_warning.title') }}
            </h2>
          </div>
        </template>

        <div class="space-y-4">
          <p class="text-neutral-300">
            {{ t('dev_warning.description') }}
          </p>

          <ul class="space-y-2 text-neutral-300">
            <li class="flex items-start gap-2">
              <UIcon
                name="i-lucide-calendar-x"
                class="size-5 text-warning shrink-0 mt-0.5"
              />
              <span>{{ t('dev_warning.schedules_fake') }}</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon
                name="i-lucide-credit-card"
                class="size-5 text-warning shrink-0 mt-0.5"
              />
              <span>{{ t('dev_warning.payments_test') }}</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon
                name="i-lucide-ban"
                class="size-5 text-warning shrink-0 mt-0.5"
              />
              <span>{{ t('dev_warning.no_charges') }}</span>
            </li>
            <li class="flex items-start gap-2">
              <UIcon
                name="i-lucide-file-x"
                class="size-5 text-warning shrink-0 mt-0.5"
              />
              <span>{{ t('dev_warning.bookings_invalid') }}</span>
            </li>
          </ul>

          <p class="text-neutral-400 text-sm">
            {{ t('dev_warning.contact_us') }}
          </p>
        </div>

        <template #footer>
          <div class="flex justify-end">
            <UButton
              color="warning"
              size="lg"
              @click="handleAccept"
            >
              {{ t('dev_warning.accept') }}
            </UButton>
          </div>
        </template>
      </UCard>
    </template>
  </UModal>
</template>

<script setup lang="ts">
const { t } = useI18n()

const STORAGE_KEY = 'dev_warning_accepted'

const isOpen = ref(false)

onMounted(() => {
  // Check if user already accepted
  const accepted = localStorage.getItem(STORAGE_KEY)
  if (!accepted) {
    isOpen.value = true
  }
})

function handleAccept() {
  localStorage.setItem(STORAGE_KEY, 'true')
  isOpen.value = false
}
</script>
