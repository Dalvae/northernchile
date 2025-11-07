<script setup lang="ts">
import { h, resolveComponent } from 'vue'
import type { BookingRes } from '~/lib/api-client'
import { getGroupedRowModel } from '@tanstack/vue-table'
import type { GroupingOptions } from '@tanstack/vue-table'

definePageMeta({
  layout: 'admin'
})

const UBadge = resolveComponent('UBadge')

const { fetchAdminBookings } = useAdminData()
const { formatPrice: formatCurrency } = useCurrency()

const {
  data: bookings,
  pending,
  refresh
} = useAsyncData('admin-bookings', () => fetchAdminBookings(), {
  server: false,
  lazy: true,
  default: () => []
})

const q = ref('')
const activeTab = ref<'upcoming' | 'past'>('upcoming')

// Transform bookings into participant rows
type ParticipantRow = {
  scheduleId: string
  tourName: string
  tourDate: string
  tourStartTime: string
  participantName: string
  documentId: string
  nationality: string
  age: number
  pickupAddress: string
  specialRequirements: string
  participantPhone: string
  participantEmail: string
  bookingUserName: string
  bookingUserPhone: string
  bookingId: string
}

const participantRows = computed<ParticipantRow[]>(() => {
  if (!bookings.value || bookings.value.length === 0) return []

  const rows: ParticipantRow[] = []
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  for (const booking of bookings.value) {
    // Only show CONFIRMED bookings
    if (booking.status !== 'CONFIRMED') {
      continue
    }

    if (!booking.participants || booking.participants.length === 0) {
      continue
    }

    // Filter by date based on active tab
    const tourDate = new Date(booking.tourDate)
    if (activeTab.value === 'upcoming' && tourDate < today) {
      continue
    }
    if (activeTab.value === 'past' && tourDate >= today) {
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
        tourStartTime: booking.tourStartTime,
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
        bookingId: booking.id,
        contact:
           participant.phoneNumber
           || booking.userPhoneNumber
           || participant.email
           || booking.userFullName,
        isBookingContactPhone:
           !participant.phoneNumber && !!booking.userPhoneNumber
      })
    }
  }

  return rows
})

const columns = [
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
    cell: ({ row }) =>
      row.getIsGrouped()
        ? `${row.getValue('participantName')} participantes`
        : row.getValue('participantName'),
    aggregationFn: 'count'
  },
  {
    accessorKey: 'documentId',
    header: 'Documento'
  },
  {
    accessorKey: 'nationality',
    header: 'Nacionalidad'
  },
  {
    accessorKey: 'age',
    header: 'Edad'
  },
  {
    accessorKey: 'pickupAddress',
    header: 'Dirección Recogida',
    meta: {
      class: {
        td: 'w-full'
      }
    }
  },
  {
    accessorKey: 'contact',
    header: 'Contacto'
  }
]

const groupingOptions = ref<GroupingOptions>({
  groupedColumnMode: 'remove',
  getGroupedRowModel: getGroupedRowModel()
})

function formatDateTime(dateString: string, timeString: string): string {
  const date = new Date(dateString)
  const dateFormatted = date.toLocaleDateString('es-CL', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })

  if (timeString) {
    return `${dateFormatted} - ${timeString}`
  }

  return dateFormatted
}
</script>

<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-800">
    <div
      class="border-b border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-800"
    >
      <div class="px-6 py-4">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h1 class="text-2xl font-bold text-neutral-900 dark:text-white">
              Gestión de Reservas
            </h1>
            <p class="text-neutral-600 dark:text-neutral-400 mt-1">
              Administra todas las reservas confirmadas del sistema
            </p>
          </div>
          <div class="flex items-center gap-3">
            <UInput
              v-model="q"
              icon="i-lucide-search"
              placeholder="Buscar participante..."
              class="w-80"
            />
          </div>
        </div>

        <!-- Tabs -->
        <div class="flex gap-2">
          <UButton
            :variant="activeTab === 'upcoming' ? 'solid' : 'ghost'"
            :color="activeTab === 'upcoming' ? 'primary' : 'neutral'"
            @click="activeTab = 'upcoming'"
          >
            Próximas
          </UButton>
          <UButton
            :variant="activeTab === 'past' ? 'solid' : 'ghost'"
            :color="activeTab === 'past' ? 'primary' : 'neutral'"
            @click="activeTab = 'past'"
          >
            Anteriores
          </UButton>
        </div>
      </div>
    </div>

    <div class="p-6">
      <div
        class="bg-white dark:bg-neutral-800 rounded-lg shadow-sm border border-neutral-200 dark:border-neutral-700 overflow-hidden"
      >
        <UTable
          :data="participantRows"
          :columns="columns"
          :loading="pending"
          :grouping="['scheduleId']"
          :grouping-options="groupingOptions"
          :empty-state="{
            icon: 'i-lucide-calendar-x',
            label: 'No hay participantes registrados.'
          }"
          :ui="{
            root: 'min-w-full',
            td: 'empty:p-0'
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
                <strong class="text-neutral-900 dark:text-white">
                  {{ row.original.tourName }}
                </strong>
                <span class="text-sm text-neutral-600 dark:text-neutral-400">
                  {{
                    formatDateTime(
                      row.original.tourDate,
                      row.original.tourStartTime
                    )
                  }}
                </span>
              </div>
            </div>
          </template>

          <template #participantName-data="{ row }">
            <span class="font-medium text-neutral-900 dark:text-white">
              {{ row.getValue("participantName") }}
            </span>
          </template>

          <template #documentId-data="{ row }">
            <span class="text-sm text-neutral-600 dark:text-neutral-400">
              {{ row.getValue("documentId") || "-" }}
            </span>
          </template>

          <template #nationality-data="{ row }">
            <span class="text-sm text-neutral-600 dark:text-neutral-400">
              {{ row.getValue("nationality") || "-" }}
            </span>
          </template>

          <template #age-data="{ row }">
            <span class="text-sm text-neutral-600 dark:text-neutral-400">
              {{ row.getValue("age") || "-" }}
            </span>
          </template>

          <template #pickupAddress-data="{ row }">
            <div class="flex items-start gap-1.5">
              <UIcon
                v-if="row.getValue('pickupAddress')"
                name="i-lucide-map-pin"
                class="w-4 h-4 text-neutral-500 dark:text-neutral-400 mt-0.5"
              />
              <span class="text-sm text-neutral-600 dark:text-neutral-400">
                {{ row.getValue("pickupAddress") || "-" }}
              </span>
            </div>
          </template>

          <template #contact-data="{ row }">
            <div class="flex flex-col gap-1">
              <div
                v-if="row.original.participantPhone || row.original.bookingUserPhone"
                class="flex items-center gap-1.5"
              >
                <UIcon
                  name="i-lucide-phone"
                  class="w-3.5 h-3.5 text-neutral-500 dark:text-neutral-400"
                />
                <span class="text-sm text-neutral-900 dark:text-white">
                  {{ row.original.participantPhone || row.original.bookingUserPhone }}
                </span>
                <span
                  v-if="!row.original.participantPhone && row.original.bookingUserPhone"
                  class="text-xs text-neutral-500 dark:text-neutral-400"
                >
                  (teléfono del contacto {{ row.original.bookingUserName }})
                </span>
              </div>

              <div
                v-if="row.original.participantEmail"
                class="flex items-center gap-1.5"
              >
                <UIcon
                  name="i-lucide-mail"
                  class="w-3.5 h-3.5 text-neutral-500 dark:text-neutral-400"
                />
                <span class="text-xs text-neutral-600 dark:text-neutral-400">
                  {{ row.original.participantEmail }}
                </span>
              </div>

              <span
                v-if="!row.original.participantPhone && !row.original.bookingUserPhone && !row.original.participantEmail"
                class="text-sm text-neutral-600 dark:text-neutral-400"
              >
                Sin información de contacto
              </span>
            </div>
          </template>
        </UTable>
      </div>
    </div>
  </div>
</template>
