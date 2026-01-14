<script setup lang="ts">
import type { BookingRes } from 'api-client'
import { getStatusColor, getBookingStatusLabel } from '~/utils/adminOptions'

defineProps<{
  booking: BookingRes
}>()

const { formatPrice: formatCurrency } = useCurrency()
const { getCountryLabel, getCountryFlag } = useCountries()
const { formatDateTime } = useDateTime()

const isOpen = ref(false)

function formatDate(dateString: string): string {
  return formatDateTime(dateString)
}
</script>

<template>
  <!-- Trigger Button -->
  <UButton
    icon="i-lucide-eye"
    color="neutral"
    variant="ghost"
    size="sm"
    aria-label="Ver detalles"
    @click="isOpen = true"
  />

  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Detalle de Reserva"
    :subtitle="`ID: ${booking.id?.slice(0, 8)}...`"
    submit-label="Cerrar"
    hide-cancel
    @submit="isOpen = false"
  >
    <div class="space-y-6">
      <!-- Status -->
      <div>
        <label class="text-sm font-medium text-muted">
          Estado
        </label>
        <div class="mt-1">
          <UBadge
            :color="getStatusColor(booking.status || 'PENDING')"
            variant="subtle"
            size="lg"
          >
            {{ getBookingStatusLabel(booking.status || 'PENDING') }}
          </UBadge>
        </div>
      </div>

      <!-- Tour Info -->
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="text-sm font-medium text-default">
            Tour
          </label>
          <p class="mt-1 text-default">
            {{ booking.tourName || "Sin nombre" }}
          </p>
        </div>
        <div>
          <label class="text-sm font-medium text-default">
            Fecha del Tour
          </label>
          <p class="mt-1 text-default">
            {{ formatDate(booking.tourDate) }}
          </p>
        </div>
      </div>

      <!-- Client Info -->
      <div>
        <label class="text-sm font-medium text-muted">
          Cliente
        </label>
        <p class="mt-1 text-default">
          {{ booking.userFullName || "Sin nombre" }}
        </p>
      </div>

      <!-- Participants -->
      <div>
        <label class="text-sm font-medium text-muted mb-2 block">
          Participantes ({{ booking.participants.length }})
        </label>
        <div class="space-y-3">
          <div
            v-for="(participant, index) in booking.participants"
            :key="participant.id"
            class="p-4 bg-elevated rounded-lg border border-default"
          >
            <div class="flex items-start justify-between mb-3">
              <h4 class="font-medium text-default">
                {{ index + 1 }}. {{ participant.fullName }}
              </h4>
            </div>

            <div class="grid grid-cols-2 gap-3 text-sm">
              <div v-if="participant.documentId">
                <span class="text-muted">Documento:</span>
                <span class="ml-2 text-default">
                  {{ participant.documentId }}
                </span>
              </div>
              <div v-if="participant.nationality">
                <span class="text-muted">Nacionalidad:</span>
                <span class="ml-2 text-default inline-flex items-center gap-1.5">
                  <span class="text-lg">{{ getCountryFlag(participant.nationality) }}</span>
                  {{ getCountryLabel(participant.nationality) || participant.nationality }}
                </span>
              </div>
              <div v-if="participant.age">
                <span class="text-muted">Edad:</span>
                <span class="ml-2 text-default">
                  {{ participant.age }} años
                </span>
              </div>
              <div v-if="participant.pickupAddress">
                <span class="text-muted">Dirección:</span>
                <span class="ml-2 text-default">
                  {{ participant.pickupAddress }}
                </span>
              </div>
            </div>

            <div
              v-if="participant.specialRequirements"
              class="mt-3 pt-3 border-t border-default"
            >
              <span class="text-muted text-sm">Requerimientos especiales:</span>
              <p class="mt-1 text-sm text-default">
                {{ participant.specialRequirements }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Special Requests -->
      <div v-if="booking.specialRequests">
        <label class="text-sm font-medium text-muted">
          Solicitudes Especiales
        </label>
        <p class="mt-1 text-default">
          {{ booking.specialRequests }}
        </p>
      </div>

      <!-- Pricing -->
      <div class="pt-4 border-t border-default">
        <div class="space-y-2">
          <div class="flex justify-between text-sm">
            <span class="text-muted">Subtotal:</span>
            <span class="text-default font-medium">
              {{ formatCurrency(booking.subtotal) }}
            </span>
          </div>
          <div class="flex justify-between text-sm">
            <span class="text-muted">IVA (19%):</span>
            <span class="text-default font-medium">
              {{ formatCurrency(booking.taxAmount) }}
            </span>
          </div>
          <div class="flex justify-between text-lg font-semibold pt-2 border-t border-default">
            <span class="text-default">Total:</span>
            <span class="text-primary">
              {{ formatCurrency(booking.totalAmount) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Created At -->
      <div class="text-xs text-muted">
        <span>Creada: {{ booking.createdAt ? new Date(booking.createdAt).toLocaleString("es-CL") : 'N/A' }}</span>
      </div>
    </div>
  </AdminBaseAdminModal>
</template>
