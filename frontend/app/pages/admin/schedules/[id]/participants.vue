<script setup lang="ts">
const route = useRoute()
const config = useRuntimeConfig()
const toast = useToast()
const { getCountryLabel, getCountryFlag } = useCountries()

const scheduleId = route.params.id as string

// Fetch participants data
const {
  data: participantsData,
  pending,
  error,
  refresh
} = await useAsyncData(`schedule-participants-${scheduleId}`, async () => {
  try {
    const response = await $fetch<{
      scheduleId: string
      startDatetime: string
      tourName: string
      status: string
      totalBookings: number
      totalParticipants: number
      participants: Array<{
        participantId: string
        fullName: string
        documentId: string
        nationality: string
        age: number
        type: string
        bookingId: string
        bookingStatus: string
        bookingReference: string
        pickupAddress: string
      }>
    }>(
      `${config.public.apiBaseUrl}/admin/schedules/${scheduleId}/participants`,
      {
        credentials: 'include'
      }
    )
    return response
  } catch (err) {
    console.error('Error fetching participants:', err)
    toast.add({
      color: 'error',
      title: 'Error',
      description: 'No se pudieron cargar los participantes'
    })
    throw err
  }
})

// Search and filter
const searchQuery = ref('')
const filterByStatus = ref('')
const filterByType = ref('')

const filteredParticipants = computed(() => {
  if (!participantsData.value?.participants) return []

  let filtered = participantsData.value.participants

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      p =>
        p.fullName.toLowerCase().includes(query)
        || p.documentId.toLowerCase().includes(query)
        || p.nationality.toLowerCase().includes(query)
        || p.bookingReference.toLowerCase().includes(query)
    )
  }

  // Filter by booking status
  if (filterByStatus.value) {
    filtered = filtered.filter(p => p.bookingStatus === filterByStatus.value)
  }

  // Filter by participant type
  if (filterByType.value) {
    filtered = filtered.filter(p => p.type === filterByType.value)
  }

  return filtered
})

// Status badge color mapping
const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    CONFIRMED: 'success',
    PENDING: 'warning',
    CANCELLED: 'error',
    COMPLETED: 'info'
  }
  return colors[status] || 'neutral'
}

// Type badge color mapping
const getTypeColor = (type: string) => {
  return type === 'ADULT' ? 'primary' : 'secondary'
}

// Format datetime
const { formatDateTime } = useDateTime()

// Export to CSV
const exportToCSV = () => {
  if (!participantsData.value?.participants) return

  const headers = [
    'Nombre',
    'Documento',
    'Nacionalidad',
    'Edad',
    'Tipo',
    'Estado Reserva',
    'Referencia',
    'Dirección Pickup'
  ]
  const rows = participantsData.value.participants.map(p => [
    p.fullName,
    p.documentId,
    p.nationality,
    p.age.toString(),
    p.type,
    p.bookingStatus,
    p.bookingReference,
    p.pickupAddress
  ])

  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `participantes-${scheduleId}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// Status filter options
const statusOptions = [
  { label: 'Todos los estados', value: '' },
  { label: 'Confirmada', value: 'CONFIRMED' },
  { label: 'Pendiente', value: 'PENDING' },
  { label: 'Cancelada', value: 'CANCELLED' },
  { label: 'Completada', value: 'COMPLETED' }
]

// Type filter options
const typeOptions = [
  { label: 'Todos los tipos', value: '' },
  { label: 'Adulto', value: 'ADULT' },
  { label: 'Niño', value: 'CHILD' }
]
</script>

<template>
  <div class="min-h-screen p-6 bg-default">
    <!-- Header -->
    <div class="mb-6">
      <div class="flex items-center gap-2 mb-2">
        <UButton
          icon="i-lucide-arrow-left"
          color="neutral"
          variant="ghost"
          to="/admin/calendar"
        />
        <h1 class="text-2xl font-bold text-default">
          Participantes del Tour
        </h1>
      </div>

      <div
        v-if="participantsData"
        class="space-y-1"
      >
        <p class="text-muted">
          <span class="font-medium">Tour:</span> {{ participantsData.tourName }}
        </p>
        <p class="text-muted">
          <span class="font-medium">Fecha:</span>
          {{ formatDateTime(participantsData.startDatetime) }}
        </p>
        <p class="text-muted">
          <span class="font-medium">Estado:</span>
          <UBadge
            :color="getStatusColor(participantsData.status)"
            variant="soft"
            size="sm"
            class="ml-2"
          >
            {{ participantsData.status }}
          </UBadge>
        </p>
      </div>
    </div>

    <!-- Loading state -->
    <div
      v-if="pending"
      class="flex justify-center items-center py-12"
    >
      <UIcon
        name="i-lucide-loader-2"
        class="w-8 h-8 animate-spin text-primary"
      />
    </div>

    <!-- Error state -->
    <div
      v-else-if="error"
      class="text-center py-12"
    >
      <UIcon
        name="i-lucide-alert-circle"
        class="w-12 h-12 text-error mx-auto mb-4"
      />
       <p class="text-muted">
        Error al cargar los participantes
      </p>
      <UButton
        color="primary"
        variant="soft"
        class="mt-4"
        @click="refresh"
      >
        Reintentar
      </UButton>
    </div>

    <!-- Content -->
    <div v-else-if="participantsData">
      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div
           class="bg-elevated rounded-lg p-4 border border-default"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-neutral-600 dark:text-neutral-400">
                Total Reservas
              </p>
              <p class="text-2xl font-bold text-neutral-900 dark:text-white">
                {{ participantsData.totalBookings }}
              </p>
            </div>
            <UIcon
              name="i-lucide-book-marked"
              class="w-8 h-8 text-primary"
            />
          </div>
        </div>

        <div
           class="bg-elevated rounded-lg p-4 border border-default"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-neutral-600 dark:text-neutral-400">
                Total Participantes
              </p>
              <p class="text-2xl font-bold text-neutral-900 dark:text-white">
                {{ participantsData.totalParticipants }}
              </p>
            </div>
            <UIcon
              name="i-lucide-users"
              class="w-8 h-8 text-success"
            />
          </div>
        </div>

        <div
           class="bg-elevated rounded-lg p-4 border border-default"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-neutral-600 dark:text-neutral-400">
                Resultados Filtrados
              </p>
              <p class="text-2xl font-bold text-neutral-900 dark:text-white">
                {{ filteredParticipants.length }}
              </p>
            </div>
            <UIcon
              name="i-lucide-filter"
              class="w-8 h-8 text-info"
            />
          </div>
        </div>
      </div>

      <!-- Filters and Search -->
      <div
        class="bg-elevated rounded-lg p-4 mb-6 border border-default"
      >
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- Search -->
          <div class="md:col-span-2">
            <UInput
              v-model="searchQuery"
              icon="i-lucide-search"
              placeholder="Buscar por nombre, documento, nacionalidad o referencia..."
              size="lg"
            />
          </div>

          <!-- Status filter -->
          <div>
            <USelect
              v-model="filterByStatus"
              :items="statusOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Filtrar por estado"
              size="lg"
            />
          </div>

          <!-- Type filter -->
          <div>
            <USelect
              v-model="filterByType"
              :items="typeOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Filtrar por tipo"
              size="lg"
            />
          </div>
        </div>

        <div class="flex justify-end mt-4">
          <UButton
            color="primary"
            variant="soft"
            icon="i-lucide-download"
            @click="exportToCSV"
          >
            Exportar a CSV
          </UButton>
        </div>
      </div>

      <!-- Participants Table -->
      <div
        class="bg-elevated rounded-lg border border-default overflow-hidden"
      >
        <div class="overflow-x-auto">
          <table
            class="min-w-full divide-y divide-default"
          >
            <thead class="bg-elevated">
              <tr>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Nombre Completo
                </th>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Documento
                </th>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Nacionalidad
                </th>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Edad
                </th>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Tipo
                </th>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Estado Reserva
                </th>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Referencia
                </th>
                <th
                   class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Pickup
                </th>
              </tr>
            </thead>
            <tbody
              class="bg-elevated divide-y divide-default"
            >
              <tr
                v-for="participant in filteredParticipants"
                :key="participant.participantId"
                class="hover:bg-muted transition-colors"
              >
                <td class="px-4 py-3 whitespace-nowrap">
                  <div
                    class="text-sm font-medium text-default"
                  >
                    {{ participant.fullName }}
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <div class="text-sm text-default">
                    {{ participant.documentId }}
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <div
                    v-if="participant.nationality"
                    class="text-sm text-muted inline-flex items-center gap-1.5"
                  >
                    <span class="text-lg">{{ getCountryFlag(participant.nationality) }}</span>
                    {{ getCountryLabel(participant.nationality) }}
                  </div>
                  <div
                    v-else
                    class="text-sm text-muted"
                  >
                    -
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <div class="text-sm text-default">
                    {{ participant.age }}
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <UBadge
                    :color="getTypeColor(participant.type)"
                    variant="soft"
                    size="sm"
                  >
                    {{ participant.type }}
                  </UBadge>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <UBadge
                    :color="getStatusColor(participant.bookingStatus)"
                    variant="soft"
                    size="sm"
                  >
                    {{ participant.bookingStatus }}
                  </UBadge>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <div class="text-sm text-default">
                    {{ participant.bookingReference }}
                  </div>
                </td>
                <td class="px-4 py-3">
                  <div
                    class="text-sm text-default max-w-xs truncate"
                  >
                    {{ participant.pickupAddress }}
                  </div>
                </td>
              </tr>

              <!-- Empty state -->
              <tr v-if="filteredParticipants.length === 0">
                <td
                  colspan="8"
                  class="px-4 py-12 text-center"
                >
                  <UIcon
                     name="i-lucide-users"
                    class="w-12 h-12 text-muted mx-auto mb-4"
                  />
                  <p class="text-muted">
                    No se encontraron participantes
                  </p>
                  <p
                    class="text-sm text-muted mt-2"
                  >
                    {{
                      searchQuery || filterByStatus || filterByType
                        ? "Intenta cambiar los filtros"
                        : "No hay participantes registrados para este tour"
                    }}
                  </p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
