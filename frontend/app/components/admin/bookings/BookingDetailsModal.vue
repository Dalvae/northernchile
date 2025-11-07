<script setup lang="ts">
import type { BookingRes } from "~/lib/api-client";

const props = defineProps<{
  booking: BookingRes;
}>();

const { formatPrice: formatCurrency } = useCurrency();

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString("es-CL", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function getStatusBadgeColor(status: string): string {
  switch (status) {
    case "CONFIRMED":
      return "success";
    case "PENDING":
      return "warning";
    case "CANCELLED":
      return "error";
    default:
      return "neutral";
  }
}

function getStatusLabel(status: string): string {
  switch (status) {
    case "CONFIRMED":
      return "Confirmada";
    case "PENDING":
      return "Pendiente";
    case "CANCELLED":
      return "Cancelada";
    default:
      return status;
  }
}
</script>

<template>
  <UModal>
    <!-- Trigger Button -->
    <UButton
      icon="i-lucide-eye"
      color="neutral"
      variant="ghost"
      size="sm"
      aria-label="Ver detalles"
    />

    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex justify-between items-start pb-4 border-b border-neutral-200 dark:border-neutral-700">
          <div>
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white">
              Detalle de Reserva
            </h3>
            <p class="text-sm text-neutral-600 dark:text-neutral-400 mt-1 font-mono">
              ID: {{ booking.id?.slice(0, 8) }}...
            </p>
          </div>
          <UButton
            icon="i-lucide-x"
            color="neutral"
            variant="ghost"
            size="sm"
            @click="$emit('close')"
          />
        </div>

        <!-- Content -->
        <div class="py-4 space-y-6 max-h-[60vh] overflow-y-auto">
          <!-- Status -->
          <div>
            <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Estado
            </label>
            <div class="mt-1">
              <UBadge
                :color="getStatusBadgeColor(booking.status)"
                variant="subtle"
                size="lg"
              >
                {{ getStatusLabel(booking.status) }}
              </UBadge>
            </div>
          </div>

          <!-- Tour Info -->
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300">
                Tour
              </label>
              <p class="mt-1 text-neutral-900 dark:text-white">
                {{ booking.tourName || "Sin nombre" }}
              </p>
            </div>
            <div>
              <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300">
                Fecha del Tour
              </label>
              <p class="mt-1 text-neutral-900 dark:text-white">
                {{ formatDate(booking.tourDate) }}
              </p>
            </div>
          </div>

          <!-- Client Info -->
          <div>
            <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Cliente
            </label>
            <p class="mt-1 text-neutral-900 dark:text-white">
              {{ booking.userFullName || "Sin nombre" }}
            </p>
          </div>

          <!-- Participants -->
          <div>
            <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2 block">
              Participantes ({{ booking.participants?.length || 0 }})
            </label>
            <div class="space-y-3">
              <div
                v-for="(participant, index) in booking.participants"
                :key="participant.id"
                class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg border border-neutral-200 dark:border-neutral-700"
              >
                <div class="flex items-start justify-between mb-3">
                  <h4 class="font-medium text-neutral-900 dark:text-white">
                    {{ index + 1 }}. {{ participant.fullName }}
                  </h4>
                </div>

                <div class="grid grid-cols-2 gap-3 text-sm">
                  <div v-if="participant.documentId">
                    <span class="text-neutral-600 dark:text-neutral-400">Documento:</span>
                    <span class="ml-2 text-neutral-900 dark:text-white">
                      {{ participant.documentId }}
                    </span>
                  </div>
                  <div v-if="participant.nationality">
                    <span class="text-neutral-600 dark:text-neutral-400">Nacionalidad:</span>
                    <span class="ml-2 text-neutral-900 dark:text-white">
                      {{ participant.nationality }}
                    </span>
                  </div>
                  <div v-if="participant.age">
                    <span class="text-neutral-600 dark:text-neutral-400">Edad:</span>
                    <span class="ml-2 text-neutral-900 dark:text-white">
                      {{ participant.age }} años
                    </span>
                  </div>
                  <div v-if="participant.pickupAddress">
                    <span class="text-neutral-600 dark:text-neutral-400">Dirección:</span>
                    <span class="ml-2 text-neutral-900 dark:text-white">
                      {{ participant.pickupAddress }}
                    </span>
                  </div>
                </div>

                <div v-if="participant.specialRequirements" class="mt-3 pt-3 border-t border-neutral-200 dark:border-neutral-700">
                  <span class="text-neutral-600 dark:text-neutral-400 text-sm">Requerimientos especiales:</span>
                  <p class="mt-1 text-sm text-neutral-900 dark:text-white">
                    {{ participant.specialRequirements }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Special Requests -->
          <div v-if="booking.specialRequests">
            <label class="text-sm font-medium text-neutral-700 dark:text-neutral-300">
              Solicitudes Especiales
            </label>
            <p class="mt-1 text-neutral-900 dark:text-white">
              {{ booking.specialRequests }}
            </p>
          </div>

          <!-- Pricing -->
          <div class="pt-4 border-t border-neutral-200 dark:border-neutral-700">
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-neutral-600 dark:text-neutral-400">Subtotal:</span>
                <span class="text-neutral-900 dark:text-white font-medium">
                  {{ formatCurrency(booking.subtotal) }}
                </span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-neutral-600 dark:text-neutral-400">IVA (19%):</span>
                <span class="text-neutral-900 dark:text-white font-medium">
                  {{ formatCurrency(booking.taxAmount) }}
                </span>
              </div>
              <div class="flex justify-between text-lg font-semibold pt-2 border-t border-neutral-200 dark:border-neutral-700">
                <span class="text-neutral-900 dark:text-white">Total:</span>
                <span class="text-primary">
                  {{ formatCurrency(booking.totalAmount) }}
                </span>
              </div>
            </div>
          </div>

          <!-- Created At -->
          <div class="text-xs text-neutral-500 dark:text-neutral-400">
            <span>Creada: {{ new Date(booking.createdAt).toLocaleString("es-CL") }}</span>
          </div>
        </div>

        <!-- Footer -->
        <div class="flex justify-end pt-4 border-t border-neutral-200 dark:border-neutral-700">
          <UButton
            label="Cerrar"
            color="neutral"
            variant="outline"
            @click="$emit('close')"
          />
        </div>
      </div>
    </template>
  </UModal>
</template>
