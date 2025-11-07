<script setup lang="ts">
const props = defineProps<{
  booking: any
}>()

const emit = defineEmits<{
  close: []
  saved: []
}>()

const config = useRuntimeConfig()
const authStore = useAuthStore()
const toast = useToast()

// Form state
const state = ref({
  specialRequests: props.booking.specialRequests || '',
  participants: props.booking.participants.map((p: any) => ({
    id: p.id,
    fullName: p.fullName,
    pickupAddress: p.pickupAddress || '',
    specialRequirements: p.specialRequirements || '',
    phoneNumber: p.phoneNumber || '',
    email: p.email || ''
  }))
})

const saving = ref(false)

async function saveChanges() {
  saving.value = true
  try {
    const token = authStore.token
    await $fetch(`${config.public.apiBase}/api/bookings/${props.booking.id}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        specialRequests: state.value.specialRequests,
        participants: state.value.participants.map(p => ({
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
      title: 'Reserva Actualizada',
      description: 'Los cambios se han guardado exitosamente.'
    })

    emit('saved')
    emit('close')
  } catch (error: any) {
    console.error('Error updating booking:', error)
    toast.add({
      color: 'error',
      title: 'Error',
      description:
        error.data?.message || 'No se pudieron guardar los cambios. Por favor, inténtalo de nuevo.'
    })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <UModal>
    <UButton label="Editar Reserva" />

    <template #content>
      <!-- Header -->
      <div class="flex justify-between items-center pb-4 px-6 border-b border-neutral-200 dark:border-neutral-700">
        <div>
          <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
            Editar Reserva
          </h3>
          <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
            #{{ booking.id.substring(0, 8) }} - {{ booking.tourName }}
          </p>
        </div>
        <UButton
          color="neutral"
          variant="ghost"
          icon="i-lucide-x"
          @click="emit('close')"
        />
      </div>

      <!-- Scrollable content -->
      <div class="max-h-[60vh] overflow-y-auto py-4 px-6">
        <div class="space-y-6">
          <!-- Special Requests -->
          <div>
            <label
              class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-4"
            >
              Solicitudes Especiales
            </label>
            <UTextarea
              v-model="state.specialRequests"
              placeholder="Agregar cualquier solicitud especial para el tour..."
              rows="3"
              size="lg"
              class="w-full"
            />
          </div>

          <UDivider />

          <!-- Participants -->
          <div>
            <h4 class="text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-4">
              Información de Participantes
            </h4>
            <div class="space-y-4">
              <div
                v-for="(participant, index) in state.participants"
                :key="participant.id"
                class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg space-y-4"
              >
                <div class="flex items-center gap-2">
                  <UIcon
                    name="i-lucide-user"
                    class="w-4 h-4 text-neutral-500 dark:text-neutral-400"
                  />
                  <span class="font-medium text-neutral-900 dark:text-white">
                    {{ participant.fullName }}
                  </span>
                </div>

                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    Dirección de Recogida
                  </label>
                  <UInput
                    v-model="participant.pickupAddress"
                    placeholder="Ej: Hotel San Pedro, Calle Caracoles 123"
                    size="lg"
                    class="w-full"
                  />
                </div>

                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    Requisitos Especiales
                  </label>
                  <UTextarea
                    v-model="participant.specialRequirements"
                    placeholder="Ej: Restricciones alimentarias, necesidades de movilidad, etc."
                    rows="2"
                    size="lg"
                    class="w-full"
                  />
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                    >
                      Teléfono
                    </label>
                    <UInput
                      v-model="participant.phoneNumber"
                      type="tel"
                      placeholder="+56 9 1234 5678"
                      size="lg"
                      class="w-full"
                    />
                    <p class="text-xs text-neutral-500 dark:text-neutral-400 mt-1">
                      Para contactarte y enviarte fotos
                    </p>
                  </div>

                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                    >
                      Email
                    </label>
                    <UInput
                      v-model="participant.email"
                      type="email"
                      placeholder="tu@email.com"
                      size="lg"
                      class="w-full"
                    />
                    <p class="text-xs text-neutral-500 dark:text-neutral-400 mt-1">
                      Para enviarte fotos del tour
                    </p>
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
          label="Cancelar"
          :disabled="saving"
          @click="emit('close')"
        />
        <UButton
          color="primary"
          label="Guardar Cambios"
          icon="i-lucide-save"
          :loading="saving"
          @click="saveChanges"
        />
      </div>
    </template>
  </UModal>
</template>
