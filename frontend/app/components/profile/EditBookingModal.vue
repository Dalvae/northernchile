<script setup lang="ts">
import type { BookingRes, ParticipantRes, ParticipantUpdateReq } from 'api-client'

const props = defineProps<{
  booking: BookingRes
}>()

const emit = defineEmits<{
  saved: []
}>()

const { t } = useI18n()
const toast = useToast()

const isOpen = ref(false)

interface ParticipantFormState extends ParticipantUpdateReq {
  fullName: string
}

// Form state
const state = ref({
  specialRequests: props.booking.specialRequests ?? '',
  participants: props.booking.participants.map((p: ParticipantRes) => ({
    id: p.id,
    fullName: p.fullName,
    pickupAddress: p.pickupAddress ?? '',
    specialRequirements: p.specialRequirements ?? '',
    phoneNumber: p.phoneNumber ?? '',
    email: p.email ?? ''
  } as ParticipantFormState))
})

const saving = ref(false)

async function saveChanges() {
  saving.value = true
  try {
    await $fetch<unknown>(`/api/bookings/${props.booking.id}`, {
      method: 'PUT',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        specialRequests: state.value.specialRequests,
        participants: state.value.participants.map((p: ParticipantFormState): ParticipantUpdateReq => ({
          id: p.id,
          pickupAddress: p.pickupAddress,
          specialRequirements: p.specialRequirements,
          phoneNumber: p.phoneNumber,
          email: p.email
        }))
      })
    })

    toast.add({
      color: 'success',
      title: t('common.success'),
      description: t('profile.edit_booking.saved_success')
    })

    isOpen.value = false
    emit('saved')
  } catch (error: unknown) {
    console.error('Error updating booking:', error)
    const apiError = error as { data?: { message?: string } }
    toast.add({
      color: 'error',
      title: t('common.error'),
      description:
        apiError.data?.message || t('profile.edit_booking.save_error')
    })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <UButton
    :label="t('profile.edit_booking.title')"
    @click="isOpen = true"
  />

  <UModal v-model:open="isOpen">
    <template #content>
      <!-- Header -->
      <div class="flex justify-between items-center pb-4 px-6 border-b border-neutral-200 dark:border-neutral-700">
        <div>
          <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
            {{ t('profile.edit_booking.title') }}
          </h3>
          <p class="text-sm text-neutral-500 dark:text-neutral-300 mt-1">
            #{{ booking.id?.substring(0, 8) }} - {{ booking.tourName }}
          </p>
        </div>
        <UButton
          color="neutral"
          variant="ghost"
          icon="i-lucide-x"
          @click="isOpen = false"
        />
      </div>

      <!-- Scrollable content -->
      <div class="max-h-[60vh] overflow-y-auto py-4 px-6">
        <div class="space-y-6">
          <!-- Special Requests -->
          <div>
            <label
              class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-4"
            >
              {{ t('booking.special_requests') }}
            </label>
            <UTextarea
              v-model="state.specialRequests"
              :placeholder="t('booking.special_requirements_placeholder')"
              :rows="3"
              size="lg"
              class="w-full"
            />
          </div>

          <UDivider />

          <!-- Participants -->
          <div>
            <h4 class="text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-4">
              {{ t('profile.edit_booking.participants_info') }}
            </h4>
            <div class="space-y-4">
              <div
                v-for="participant in state.participants"
                :key="participant.id"
                class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg space-y-4"
              >
                <div class="flex items-center gap-2">
                  <UIcon
                    name="i-lucide-user"
                    class="w-4 h-4 text-neutral-500 dark:text-neutral-300"
                  />
                  <span class="font-medium text-neutral-900 dark:text-white">
                    {{ participant.fullName }}
                  </span>
                </div>

                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
                  >
                    {{ t('profile.edit_booking.pickup_address') }}
                  </label>
                  <UInput
                    v-model="participant.pickupAddress"
                    :placeholder="t('booking.pickup_address_placeholder')"
                    size="lg"
                    class="w-full"
                  />
                </div>

                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
                  >
                    {{ t('booking.special_requirements') }}
                  </label>
                  <UTextarea
                    v-model="participant.specialRequirements"
                    :placeholder="t('booking.special_requirements_placeholder')"
                    :rows="2"
                    size="lg"
                    class="w-full"
                  />
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
                    >
                      {{ t('profile.phone') }}
                    </label>
                    <UInput
                      v-model="participant.phoneNumber"
                      type="tel"
                      :placeholder="t('booking.phone_placeholder')"
                      size="lg"
                      class="w-full"
                    />
                  </div>

                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2"
                    >
                      {{ t('booking.email') }}
                    </label>
                    <UInput
                      v-model="participant.email"
                      type="email"
                      placeholder="email@example.com"
                      size="lg"
                      class="w-full"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex justify-end gap-3 pt-4 pb-6 px-6 border-t border-neutral-200 dark:border-neutral-700">
        <UButton
          color="neutral"
          variant="outline"
          :label="t('common.cancel')"
          :disabled="saving"
          @click="isOpen = false"
        />
        <UButton
          color="primary"
          :label="t('common.save')"
          icon="i-lucide-save"
          :loading="saving"
          @click="saveChanges"
        />
      </div>
    </template>
  </UModal>
</template>
