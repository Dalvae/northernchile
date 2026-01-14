<script setup lang="ts">
import { h, resolveComponent, computed } from 'vue'
import type { BookingRes, LocalTime } from 'api-client'
import { getGroupedRowModel } from '@tanstack/vue-table'
import type { GroupingOptions } from '@tanstack/vue-table'
import { getStatusColor, getBookingStatusLabel } from '~/utils/adminOptions'

import AdminCountryCell from '~/components/admin/CountryCell.vue'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Reservas - Admin - Northern Chile'
})

const UBadge = resolveComponent('UBadge')

const { fetchAdminBookings, refundBooking } = useAdminData()
const { formatPrice: formatCurrency } = useCurrency()
const { formatLocalTime } = useDateTime()
const toast = useToast()

const {
  data: bookingsPage,
  pending,
  refresh
} = useAsyncData('admin-bookings', () => fetchAdminBookings(), {
  server: false,
  lazy: true,
  default: () => ({ content: [], totalElements: 0, totalPages: 0 })
})

const bookings = computed(() => bookingsPage.value?.content ?? [])

const q = ref('')
const activeTab = ref<'upcoming' | 'past'>('upcoming')
const viewMode = ref<'manifest' | 'bookings'>('manifest')

// Refund modal state
const refundModal = ref(false)
const selectedBooking = ref<BookingRes | null>(null)
const refundProcessing = ref(false)
const adminOverride = ref(false)

// Transform bookings into participant rows (for manifest view)
type ParticipantRow = {
  scheduleId: string
  tourName: string
  tourDate: string
  tourStartTime: string
  participantName: string
  documentId?: string
  nationality?: string
  age?: number
  pickupAddress?: string
  specialRequirements?: string
  participantPhone?: string
  participantEmail?: string
  bookingUserName?: string
  bookingUserPhone?: string
  bookingId: string
}

const participantRows = computed<ParticipantRow[]>(() => {
  if (!bookings.value || bookings.value.length === 0) return []

  const rows: ParticipantRow[] = []
  // Get today's date as YYYY-MM-DD string for proper comparison
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  for (const booking of bookings.value) {
    // Only show CONFIRMED bookings
    if (booking.status !== 'CONFIRMED') {
      continue
    }

    if (!booking.participants || booking.participants.length === 0 || !booking.tourDate) {
      continue
    }

    // Filter by date based on active tab (compare as strings to avoid timezone issues)
    const tourDateStr = booking.tourDate
    if (activeTab.value === 'upcoming' && tourDateStr < todayStr) {
      continue
    }
    if (activeTab.value === 'past' && tourDateStr >= todayStr) {
      continue
    }

    for (const participant of booking.participants) {
      // Filter by search query
      if (q.value) {
        const query = q.value.toLowerCase()
        const matchesSearch
          = participant.fullName?.toLowerCase().includes(query)
            || booking.tourName?.toLowerCase().includes(query)
            || booking.userFullName?.toLowerCase().includes(query)
            || participant.documentId?.toLowerCase().includes(query)

        if (!matchesSearch) continue
      }

      rows.push({
        scheduleId: booking.scheduleId,
        tourName: booking.tourName,
        tourDate: booking.tourDate,
        tourStartTime: formatLocalTime(booking.tourStartTime),
        participantName: participant.fullName,
        documentId: participant.documentId,
        nationality: participant.nationality,
        age: participant.age,
        pickupAddress: participant.pickupAddress,
        specialRequirements: participant.specialRequirements,
        participantPhone: participant.phoneNumber,
        participantEmail: participant.email,
        bookingUserName: booking.userFullName,
        bookingUserPhone: booking.userPhoneNumber,
        bookingId: booking.id
      })
    }
  }

  return rows
})

// Filtered bookings for bookings view
const filteredBookings = computed(() => {
  if (!bookings.value || bookings.value.length === 0) return []

  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  return bookings.value.filter((booking) => {
    // Filter by search query
    if (q.value) {
      const query = q.value.toLowerCase()
      const matchesSearch
        = booking.tourName?.toLowerCase().includes(query)
          || booking.userFullName?.toLowerCase().includes(query)
          || booking.id?.toLowerCase().includes(query)

      if (!matchesSearch) return false
    }

    // Filter by date based on active tab
    if (!booking.tourDate) return false
    const tourDateStr = booking.tourDate
    if (activeTab.value === 'upcoming' && tourDateStr < todayStr) {
      return false
    }
    if (activeTab.value === 'past' && tourDateStr >= todayStr) {
      return false
    }

    return true
  })
})

const manifestColumns = [
  {
    id: 'title',
    header: 'Tour'
  },
  {
    id: 'scheduleId',
    accessorKey: 'scheduleId'
  },
  {
    accessorKey: 'participantName',
    header: 'Participante',
    cell: ({ row }: { row: import('@tanstack/vue-table').Row<ParticipantRow> }) =>
      row.getIsGrouped()
        ? `${row.getValue('participantName')} participantes`
        : row.getValue('participantName'),
    aggregationFn: 'count' as const
  },
  {
    accessorKey: 'documentId',
    header: 'Documento'
  },
  {
    accessorKey: 'nationality',
    header: 'Nacionalidad',
    cell: ({ row }: import('@tanstack/vue-table').CellContext<ParticipantRow, unknown>) => h(AdminCountryCell, { code: row.getValue('nationality') as string })
  },
  {
    accessorKey: 'age',
    header: 'Edad'
  },
  {
    accessorKey: 'pickupAddress',
    header: 'Direccion Recogida',
    meta: {
      class: {
        td: 'w-full'
      }
    }
  },
  {
    accessorKey: 'participantPhone',
    header: 'Contacto'
  }
]

const bookingColumns = [
  {
    id: 'id',
    accessorKey: 'id',
    header: 'ID'
  },
  {
    id: 'tourName',
    accessorKey: 'tourName',
    header: 'Tour'
  },
  {
    id: 'tourDate',
    accessorKey: 'tourDate',
    header: 'Fecha'
  },
  {
    id: 'userFullName',
    accessorKey: 'userFullName',
    header: 'Cliente'
  },
  {
    id: 'participantCount',
    header: 'Participantes'
  },
  {
    id: 'totalAmount',
    accessorKey: 'totalAmount',
    header: 'Total'
  },
  {
    id: 'status',
    accessorKey: 'status',
    header: 'Estado'
  },
  {
    id: 'actions',
    header: 'Acciones'
  }
]

const groupingOptions = ref<GroupingOptions>({
  groupedColumnMode: 'remove',
  getGroupedRowModel: getGroupedRowModel()
})

function formatTourDateTime(dateString: string, timeValue?: LocalTime | string | null): string {
  if (!dateString) return ''

  // Format date as DD-MM-YYYY
  const dateParts = dateString.split('-')
  if (dateParts.length !== 3) return dateString

  const formattedDate = `${dateParts[2]}-${dateParts[1]}-${dateParts[0]}`

  // Add time if provided
  const formattedTime = formatLocalTime(timeValue)
  if (formattedTime) {
    return `${formattedDate}, ${formattedTime}`
  }

  return formattedDate
}

// Status helpers imported from ~/utils/adminOptions

function openRefundModal(booking: BookingRes) {
  selectedBooking.value = booking
  adminOverride.value = false
  refundModal.value = true
}

async function handleRefund() {
  if (!selectedBooking.value?.id) return

  refundProcessing.value = true
  try {
    const result = await refundBooking(selectedBooking.value.id, adminOverride.value)

    toast.add({
      title: 'Reembolso procesado',
      description: `Se ha reembolsado ${formatCurrency(result.refundAmount)} correctamente.`,
      color: 'success',
      icon: 'i-lucide-check-circle'
    })

    refundModal.value = false
    selectedBooking.value = null
    await refresh()
  } catch (error: unknown) {
    const err = error as { data?: { message?: string }, message?: string }
    const message = err?.data?.message || err?.message || 'Error al procesar el reembolso'
    toast.add({
      title: 'Error en reembolso',
      description: message,
      color: 'error',
      icon: 'i-lucide-x-circle'
    })
  } finally {
    refundProcessing.value = false
  }
}

// Check if booking is within 24 hours
function isWithin24Hours(tourDate: string, tourTime?: LocalTime | string | null): boolean {
  const now = new Date()
  const timeStr = formatLocalTime(tourTime) || '00:00:00'
  const tourDateTime = new Date(`${tourDate}T${timeStr}`)
  const hoursUntilTour = (tourDateTime.getTime() - now.getTime()) / (1000 * 60 * 60)
  return hoursUntilTour < 24
}
</script>

<template>
  <div class="min-h-screen bg-default">
    <div class="p-6 space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-default">
            Gestion de Reservas
          </h1>
          <p class="text-default text-sm mt-1">
            Administra todas las reservas del sistema
          </p>
        </div>
        <div class="flex items-center gap-3">
          <UInput
            v-model="q"
            icon="i-lucide-search"
            :placeholder="viewMode === 'manifest' ? 'Buscar participante...' : 'Buscar reserva...'"
            class="w-80"
          />
        </div>
      </div>

      <!-- View Mode Toggle -->
      <div class="flex items-center gap-4">
        <div class="flex gap-2">
          <UButton
            :variant="viewMode === 'manifest' ? 'solid' : 'ghost'"
            :color="viewMode === 'manifest' ? 'primary' : 'neutral'"
            icon="i-lucide-users"
            @click="viewMode = 'manifest'"
          >
            Manifiesto
          </UButton>
          <UButton
            :variant="viewMode === 'bookings' ? 'solid' : 'ghost'"
            :color="viewMode === 'bookings' ? 'primary' : 'neutral'"
            icon="i-lucide-list"
            @click="viewMode = 'bookings'"
          >
            Reservas
          </UButton>
        </div>

        <div class="h-6 w-px bg-neutral-300 dark:bg-neutral-600" />

        <!-- Date Tabs -->
        <div class="flex gap-2">
          <UButton
            :variant="activeTab === 'upcoming' ? 'soft' : 'ghost'"
            :color="activeTab === 'upcoming' ? 'primary' : 'neutral'"
            size="sm"
            @click="activeTab = 'upcoming'"
          >
            Proximas
          </UButton>
          <UButton
            :variant="activeTab === 'past' ? 'soft' : 'ghost'"
            :color="activeTab === 'past' ? 'primary' : 'neutral'"
            size="sm"
            @click="activeTab = 'past'"
          >
            Anteriores
          </UButton>
        </div>
      </div>

      <!-- Manifest View -->
      <div
        v-if="viewMode === 'manifest'"
        class="bg-elevated rounded-lg shadow-sm border border-default overflow-hidden"
      >
        <UTable
          :data="participantRows"
          :columns="manifestColumns"
          :loading="pending"
          :grouping="['scheduleId']"
          :grouping-options="groupingOptions"
          :empty-state="{
            icon: 'i-lucide-calendar-x',
            label: 'No hay participantes registrados.'
          }"
          :ui="{
            root: 'min-w-full',
            td: 'p-4 text-sm text-default whitespace-nowrap empty:p-0 [&:has([role=checkbox])]:pe-0'
          }"
        >
          <template #title-cell="{ row }">
            <div
              v-if="row.getIsGrouped()"
              class="flex items-center"
            >
              <UButton
                variant="outline"
                color="neutral"
                class="mr-3"
                size="xs"
                :icon="row.getIsExpanded() ? 'i-lucide-minus' : 'i-lucide-plus'"
                @click="row.toggleExpanded()"
              />

              <!-- Schedule Group -->
              <div class="flex flex-col">
                <strong class="text-default">
                  {{ row.original.tourName }}
                </strong>
                <span class="text-sm text-default">
                  {{
                    formatTourDateTime(
                      row.original.tourDate,
                      row.original.tourStartTime
                    )
                  }}
                </span>
              </div>
            </div>
          </template>

          <template #participantName-cell="{ row }">
            <span class="font-medium text-default">
              {{ row.getValue("participantName") }}
            </span>
          </template>

          <template #documentId-cell="{ row }">
            <span class="text-sm text-default">
              {{ row.getValue("documentId") || "-" }}
            </span>
          </template>

          <template #age-cell="{ row }">
            <span class="text-sm text-default">
              {{ row.getValue("age") || "-" }}
            </span>
          </template>

          <template #pickupAddress-cell="{ row }">
            <div class="flex items-start gap-1.5">
              <UIcon
                v-if="row.getValue('pickupAddress')"
                name="i-lucide-map-pin"
                class="w-4 h-4 text-muted mt-0.5"
              />
              <span class="text-sm text-default">
                {{ row.getValue("pickupAddress") || "-" }}
              </span>
            </div>
          </template>

          <template #participantPhone-cell="{ row }">
            <div class="flex flex-col gap-1">
              <div
                v-if="row.original.participantPhone || row.original.bookingUserPhone"
                class="flex items-center gap-1.5"
              >
                <UIcon
                  name="i-lucide-phone"
                  class="w-3.5 h-3.5 text-muted"
                />
                <span class="text-sm text-neutral-900 dark:text-white">
                  {{ row.original.participantPhone || row.original.bookingUserPhone }}
                </span>
                <span
                  v-if="!row.original.participantPhone && row.original.bookingUserPhone"
                  class="text-xs text-neutral-500 dark:text-neutral-300"
                >
                  (telefono del contacto {{ row.original.bookingUserName }})
                </span>
              </div>

              <div
                v-if="row.original.participantEmail"
                class="flex items-center gap-1.5"
              >
                <UIcon
                  name="i-lucide-mail"
                  class="w-3.5 h-3.5 text-muted"
                />
                <span class="text-xs text-neutral-600 dark:text-neutral-300">
                  {{ row.original.participantEmail }}
                </span>
              </div>

              <span
                v-if="!row.original.participantPhone && !row.original.bookingUserPhone && !row.original.participantEmail"
                class="text-sm text-default"
              >
                Sin informacion de contacto
              </span>
            </div>
          </template>
        </UTable>
      </div>

      <!-- Bookings View -->
      <div
        v-else
        class="bg-elevated rounded-lg shadow-sm border border-default overflow-hidden"
      >
        <UTable
          :data="filteredBookings"
          :columns="bookingColumns"
          :loading="pending"
          :empty-state="{
            icon: 'i-lucide-calendar-x',
            label: 'No hay reservas registradas.'
          }"
          :ui="{
            root: 'min-w-full',
            td: 'p-4 text-sm text-default whitespace-nowrap [&:has([role=checkbox])]:pe-0'
          }"
        >
          <template #id-cell="{ row }">
            <span class="text-xs font-mono text-muted">
              {{ row.original.id?.substring(0, 8) }}...
            </span>
          </template>

          <template #tourName-cell="{ row }">
            <div class="flex flex-col">
              <span class="font-medium text-default">
                {{ row.original.tourName }}
              </span>
              <span class="text-xs text-muted">
                {{ formatLocalTime(row.original.tourStartTime) }}
              </span>
            </div>
          </template>

          <template #tourDate-cell="{ row }">
            <span class="text-sm text-default">
              {{ formatTourDateTime(row.original.tourDate) }}
            </span>
          </template>

          <template #userFullName-cell="{ row }">
            <div class="flex flex-col">
              <span class="font-medium text-default">
                {{ row.original.userFullName }}
              </span>
              <span class="text-xs text-muted">
                {{ row.original.userPhoneNumber }}
              </span>
            </div>
          </template>

          <template #participantCount-cell="{ row }">
            <UBadge
              :label="`${row.original.participants.length} pax`"
              color="neutral"
              variant="soft"
            />
          </template>

          <template #totalAmount-cell="{ row }">
            <span class="font-medium text-default">
              {{ formatCurrency(row.original.totalAmount) }}
            </span>
          </template>

          <template #status-cell="{ row }">
            <UBadge
              :label="getBookingStatusLabel(row.original.status)"
              :color="getStatusColor(row.original.status)"
              variant="soft"
            />
          </template>

          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <UButton
                v-if="row.original.status === 'CONFIRMED'"
                icon="i-lucide-undo-2"
                color="error"
                variant="soft"
                size="xs"
                @click="openRefundModal(row.original)"
              >
                Reembolsar
              </UButton>
              <span
                v-else
                class="text-xs text-muted"
              >
                -
              </span>
            </div>
          </template>
        </UTable>
      </div>
    </div>

    <!-- Refund Modal -->
    <UModal v-model:open="refundModal">
      <template #content>
        <div class="p-6">
          <div class="flex items-center gap-3 mb-4">
            <div class="w-10 h-10 bg-error/10 rounded-full flex items-center justify-center">
              <UIcon
                name="i-lucide-undo-2"
                class="w-5 h-5 text-error"
              />
            </div>
            <div>
              <h3 class="text-lg font-semibold text-default">
                Procesar Reembolso
              </h3>
              <p class="text-sm text-muted">
                Esta accion no se puede deshacer
              </p>
            </div>
          </div>

          <div
            v-if="selectedBooking"
            class="space-y-4"
          >
            <!-- Booking Details -->
            <div class="bg-neutral-50 dark:bg-neutral-800 rounded-lg p-4 space-y-2">
              <div class="flex justify-between">
                <span class="text-sm text-muted">Tour:</span>
                <span class="text-sm font-medium text-default">{{ selectedBooking.tourName }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted">Fecha:</span>
                <span class="text-sm text-default">{{ formatTourDateTime(selectedBooking.tourDate) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted">Cliente:</span>
                <span class="text-sm text-default">{{ selectedBooking.userFullName }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-sm text-muted">Participantes:</span>
                <span class="text-sm text-default">{{ selectedBooking.participants.length }}</span>
              </div>
              <div class="flex justify-between border-t border-neutral-200 dark:border-neutral-700 pt-2 mt-2">
                <span class="text-sm font-medium text-muted">Monto a reembolsar:</span>
                <span class="text-lg font-bold text-error">{{ formatCurrency(selectedBooking.totalAmount) }}</span>
              </div>
            </div>

            <!-- 24-hour warning -->
            <div
              v-if="isWithin24Hours(selectedBooking.tourDate, selectedBooking.tourStartTime)"
              class="bg-warning/10 border border-warning/30 rounded-lg p-4"
            >
              <div class="flex items-start gap-3">
                <UIcon
                  name="i-lucide-alert-triangle"
                  class="w-5 h-5 text-warning mt-0.5"
                />
                <div>
                  <p class="text-sm font-medium text-warning">
                    Reserva dentro de 24 horas
                  </p>
                  <p class="text-xs text-muted mt-1">
                    Segun la politica de cancelacion, las reservas dentro de las 24 horas previas no son elegibles para reembolso automatico.
                  </p>
                  <label class="flex items-center gap-2 mt-3">
                    <UCheckbox v-model="adminOverride" />
                    <span class="text-sm text-default">Aplicar reembolso de todas formas (anulacion administrativa)</span>
                  </label>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex justify-end gap-3 pt-4">
              <UButton
                color="neutral"
                variant="ghost"
                @click="refundModal = false"
              >
                Cancelar
              </UButton>
              <UButton
                color="error"
                :loading="refundProcessing"
                :disabled="isWithin24Hours(selectedBooking.tourDate, selectedBooking.tourStartTime) && !adminOverride"
                @click="handleRefund"
              >
                Confirmar Reembolso
              </UButton>
            </div>
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>
