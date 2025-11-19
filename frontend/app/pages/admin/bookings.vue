<script setup lang="ts">
import { h, resolveComponent } from 'vue'
import type { BookingRes } from 'api-client'
import { getGroupedRowModel } from '@tanstack/vue-table'
import type { GroupingOptions } from '@tanstack/vue-table'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Reservas - Admin - Northern Chile'
})

import AdminStatusBadge from '~/components/admin/StatusBadge.vue'
import AdminCountryCell from '~/components/admin/CountryCell.vue'

const UBadge = resolveComponent('UBadge')

const { fetchAdminBookings } = useAdminData()
const { formatPrice: formatCurrency } = useCurrency()
const { getCountryLabel, getCountryFlag } = useCountries()

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
  scheduleId: string | undefined
  tourName?: string
  tourDate?: string
  tourStartTime?: string | any
  participantName?: string
  documentId?: string
  nationality?: string
  age?: number
  pickupAddress?: string
  specialRequirements?: string
  participantPhone?: string
  participantEmail?: string
  bookingUserName?: string
  bookingUserPhone?: string
  bookingId?: string
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

    if (!booking.participants || booking.participants.length === 0 || !booking.tourDate) {
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
         bookingId: booking.id
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
    header: 'Dirección Recogida',
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

const groupingOptions = ref<GroupingOptions>({
  groupedColumnMode: 'remove',
  getGroupedRowModel: getGroupedRowModel()
})

const { formatDateTime } = useDateTime()

function formatTourDateTime(dateString: string, timeString?: string): string {
  const base = formatDateTime(dateString)
  return timeString ? `${base} - ${timeString}` : base
}
</script>

<template>
  <div class="min-h-screen bg-default">
    <div class="p-6 space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-default">
            Gestión de Reservas
          </h1>
          <p class="text-default text-sm mt-1">
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

      <!-- Table Container -->
      <div
        class="bg-elevated rounded-lg shadow-sm border border-default overflow-hidden"
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
                      row.original.tourDate || '',
                      row.original.tourStartTime || ''
                    )
                  }}
                </span>
              </div>
            </div>
          </template>

          <template #participantName-data="{ row }">
             <span class="font-medium text-default">
              {{ row.getValue("participantName") }}
            </span>
          </template>

           <template #documentId-data="{ row }">
            <span class="text-sm text-default">
              {{ row.getValue("documentId") || "-" }}
            </span>
          </template>

           <template #age-data="{ row }">
            <span class="text-sm text-default">
              {{ row.getValue("age") || "-" }}
            </span>
          </template>

          <template #pickupAddress-data="{ row }">
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

          <template #contact-data="{ row }">
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
                  (teléfono del contacto {{ row.original.bookingUserName }})
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
                Sin información de contacto
              </span>
            </div>
          </template>
        </UTable>
      </div>
    </div>
  </div>
</template>
