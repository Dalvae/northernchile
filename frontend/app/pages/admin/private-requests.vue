<script setup lang="ts">
definePageMeta({
  layout: 'admin'
})

const config = useRuntimeConfig()
const toast = useToast()

// Fetch private tour requests
const {
  data: requests,
  pending,
  error,
  refresh
} = await useAsyncData(
  'private-tour-requests',
  async () => {
    try {
      const response = await $fetch<
        Array<{
          id: string
          customerName: string
          customerEmail: string
          customerPhone: string
          requestedTourType: string
          requestedDatetime: string
          numAdults: number
          numChildren: number
          specialRequests: string
          status: string
          quotedPrice: number
          createdAt: string
        }>
      >(`${config.public.apiBase}/api/admin/private-tours/requests`, {
        headers:
          import.meta.client && localStorage.getItem('auth_token')
            ? {
                Authorization: `Bearer ${localStorage.getItem('auth_token')}`
              }
            : {}
      })
      return response
    } catch (err) {
      console.error('Error fetching private tour requests:', err)
      toast.add({
        color: 'error',
        title: 'Error',
        description: 'No se pudieron cargar las solicitudes'
      })
      throw err
    }
  },
  {
    server: false,
    lazy: true
  }
)

// Filter and search
const searchQuery = ref('')
const filterByStatus = ref('')

const filteredRequests = computed(() => {
  if (!requests.value || !Array.isArray(requests.value)) return []

  let filtered = requests.value

  // Search
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(
      r =>
        r.customerName.toLowerCase().includes(query)
        || r.customerEmail.toLowerCase().includes(query)
        || r.requestedTourType.toLowerCase().includes(query)
    )
  }

  // Filter by status
  if (filterByStatus.value) {
    filtered = filtered.filter(r => r.status === filterByStatus.value)
  }

  return filtered
})

// Stats
const stats = computed(() => {
  if (!requests.value || !Array.isArray(requests.value)) {
    return { total: 0, pending: 0, quoted: 0, confirmed: 0, cancelled: 0 }
  }

  return {
    total: requests.value.length,
    pending: requests.value.filter(r => r.status === 'PENDING').length,
    quoted: requests.value.filter(r => r.status === 'QUOTED').length,
    confirmed: requests.value.filter(r => r.status === 'CONFIRMED').length,
    cancelled: requests.value.filter(r => r.status === 'CANCELLED').length
  }
})

// Modal state
const showDetailsModal = ref(false)
const selectedRequest = ref<any>(null)
const updatingStatus = ref(false)
const statusForm = ref({
  status: '',
  quotedPrice: ''
})

// Status options - sin valor vacío porque USelect no lo permite
const statusOptions = [
  { label: 'Pendiente', value: 'PENDING' },
  { label: 'Cotizado', value: 'QUOTED' },
  { label: 'Confirmado', value: 'CONFIRMED' },
  { label: 'Cancelado', value: 'CANCELLED' }
]

const statusSelectOptions = [
  { label: 'Pendiente', value: 'PENDING' },
  { label: 'Cotizado', value: 'QUOTED' },
  { label: 'Confirmado', value: 'CONFIRMED' },
  { label: 'Cancelado', value: 'CANCELLED' }
]

// Functions
const openDetailsModal = (request: any) => {
  selectedRequest.value = request
  statusForm.value = {
    status: request.status,
    quotedPrice: request.quotedPrice?.toString() || ''
  }
  showDetailsModal.value = true
}

const closeDetailsModal = () => {
  showDetailsModal.value = false
  selectedRequest.value = null
  statusForm.value = { status: '', quotedPrice: '' }
}

const updateRequestStatus = async () => {
  if (!selectedRequest.value) return

  try {
    updatingStatus.value = true

    await $fetch(
      `${config.public.apiBaseUrl}/admin/private-tours/requests/${selectedRequest.value.id}`,
      {
        method: 'PUT',
        body: {
          status: statusForm.value.status,
          quotedPrice: statusForm.value.quotedPrice
            ? parseFloat(statusForm.value.quotedPrice)
            : null
        },
        credentials: 'include'
      }
    )

    toast.add({
      color: 'success',
      title: 'Estado actualizado',
      description: 'La solicitud ha sido actualizada correctamente'
    })

    closeDetailsModal()
    await refresh()
  } catch (err) {
    console.error('Error updating request:', err)
    toast.add({
      color: 'error',
      title: 'Error',
      description: 'No se pudo actualizar la solicitud'
    })
  } finally {
    updatingStatus.value = false
  }
}

type BadgeColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'

const getStatusColor = (status: string): BadgeColor => {
  const colors: Record<string, BadgeColor> = {
    PENDING: 'warning',
    QUOTED: 'info',
    CONFIRMED: 'success',
    CANCELLED: 'error'
  }
  return colors[status] || 'neutral'
}

const formatDateTime = (datetime: string) => {
  return new Date(datetime).toLocaleString('es-CL', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const { formatPrice } = useCurrency()
const formatCurrency = (value: number | null | undefined) => value != null ? formatPrice(value) : '-' as string
</script>

<template>
  <div class="p-6 bg-default min-h-screen">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-default">
        Solicitudes de Tours Privados
      </h1>
      <p class="text-sm text-muted mt-1">
        Gestiona las solicitudes de tours personalizados
      </p>
    </div>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
      <AdminDashboardStat
        label="Total"
        :value="stats.total"
        icon="i-lucide-inbox"
        icon-color="bg-neutral-100 dark:bg-neutral-800"
        icon-text-color="text-neutral-600 dark:text-neutral-400"
      />
      <AdminDashboardStat
        label="Pendientes"
        :value="stats.pending"
        icon="i-lucide-clock"
        icon-color="bg-warning/10"
        icon-text-color="text-warning"
      />
      <AdminDashboardStat
        label="Cotizadas"
        :value="stats.quoted"
        icon="i-lucide-file-text"
        icon-color="bg-info/10"
        icon-text-color="text-info"
      />
      <AdminDashboardStat
        label="Confirmadas"
        :value="stats.confirmed"
        icon="i-lucide-check-circle"
        icon-color="bg-success/10"
        icon-text-color="text-success"
      />
      <AdminDashboardStat
        label="Canceladas"
        :value="stats.cancelled"
        icon="i-lucide-x-circle"
        icon-color="bg-error/10"
        icon-text-color="text-error"
      />
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
        Error al cargar las solicitudes
      </p>
      <UButton
        color="primary"
        variant="soft"
        class="mt-4"
        @click="() => refresh()"
      >
        Reintentar
      </UButton>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Filters -->
      <div
        class="bg-elevated rounded-lg p-4 mb-6 border border-default"
      >
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <UInput
            v-model="searchQuery"
            icon="i-lucide-search"
            placeholder="Buscar por nombre, email o tipo de tour..."
            size="lg"
          />
          <USelect
            v-model="filterByStatus"
            :items="statusOptions"
            option-attribute="label"
            value-attribute="value"
            placeholder="Filtrar por estado"
            size="lg"
            clearable
          />
        </div>
      </div>

      <!-- Requests Table -->
      <div
        class="bg-elevated rounded-lg border border-default overflow-hidden"
      >
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-default">
            <thead class="bg-elevated">
              <tr>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Cliente
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Tipo de Tour
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Fecha Solicitada
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Participantes
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Estado
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Cotización
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-muted uppercase tracking-wider"
                >
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody class="bg-elevated divide-y divide-default">
              <tr
                v-for="request in filteredRequests"
                :key="request.id"
                 class="transition-colors hover:bg-muted"
              >
                <td class="px-4 py-3 whitespace-nowrap">
                  <div>
                     <div
                       class="text-sm font-medium text-default"
                    >
                      {{ request.customerName }}
                    </div>
                     <div class="text-sm text-muted">
                      {{ request.customerEmail }}
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <div class="text-sm text-default">
                    {{ request.requestedTourType }}
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <div class="text-sm text-default">
                    {{ formatDateTime(request.requestedDatetime) }}
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                   <div class="text-sm text-default">
                     {{ request.numAdults }} adultos,
                     {{ request.numChildren }} niños
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <UBadge
                    :color="getStatusColor(request.status)"
                    variant="soft"
                    size="sm"
                  >
                    {{ request.status }}
                  </UBadge>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                   <div class="text-sm text-default">
                     {{
                       request.quotedPrice
                          ? formatCurrency(request.quotedPrice || 0)
                         : "-"
                     }}
                  </div>
                </td>
                <td class="px-4 py-3 whitespace-nowrap">
                  <UButton
                    color="primary"
                    variant="soft"
                    size="sm"
                    icon="i-lucide-eye"
                    @click="openDetailsModal(request)"
                  >
                    Ver Detalles
                  </UButton>
                </td>
              </tr>

              <!-- Empty state -->
              <tr v-if="filteredRequests.length === 0">
                <td
                  colspan="7"
                  class="px-4 py-12 text-center"
                >
                  <UIcon
                    name="i-lucide-inbox"
                    class="w-12 h-12 text-muted mx-auto mb-4"
                  />
                  <p class="text-muted">
                    No se encontraron solicitudes
                  </p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Details Modal -->
    <UModal v-model:open="showDetailsModal">
      <template #content>
        <div
          v-if="selectedRequest"
          class="p-6"
        >
          <!-- Header -->
          <div
            class="flex justify-between items-center pb-4 border-b border-default"
          >
             <h3 class="text-xl font-semibold text-default">
              Detalles de Solicitud
            </h3>
            <UButton
              icon="i-lucide-x"
              color="neutral"
              variant="ghost"
              size="sm"
              @click="closeDetailsModal"
            />
          </div>

          <!-- Content -->
          <div class="py-4 space-y-4">
            <!-- Customer Info -->
            <div>
              <h4
                class="text-sm font-medium text-default mb-2"
              >
                Información del Cliente
              </h4>
              <div
                class="bg-elevated rounded-lg p-3 space-y-2"
              >
                <div class="flex justify-between">
                  <span class="text-sm text-muted">Nombre:</span>
                  <span
                    class="text-sm font-medium text-default"
                  >{{ selectedRequest.customerName }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-sm text-muted">Email:</span>
                  <span
                    class="text-sm font-medium text-default"
                  >{{ selectedRequest.customerEmail }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-sm text-muted">Teléfono:</span>
                  <span
                    class="text-sm font-medium text-default"
                  >{{ selectedRequest.customerPhone }}</span>
                </div>
              </div>
            </div>

            <!-- Tour Info -->
            <div>
              <h4
                class="text-sm font-medium text-default mb-2"
              >
                Información del Tour
              </h4>
              <div
                class="bg-elevated rounded-lg p-3 space-y-2"
              >
                <div class="flex justify-between">
                  <span class="text-sm text-muted">Tipo:</span>
                  <span
                    class="text-sm font-medium text-default"
                  >{{ selectedRequest.requestedTourType }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-sm text-muted">Fecha:</span>
                  <span
                    class="text-sm font-medium text-default"
                  >{{
                    formatDateTime(selectedRequest.requestedDatetime)
                  }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-sm text-muted">Adultos:</span>
                  <span
                    class="text-sm font-medium text-default"
                  >{{ selectedRequest.numAdults }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-sm text-muted">Niños:</span>
                  <span
                    class="text-sm font-medium text-default"
                  >{{ selectedRequest.numChildren }}</span>
                </div>
              </div>
            </div>

            <!-- Special Requests -->
            <div v-if="selectedRequest.specialRequests">
              <h4
                class="text-sm font-medium text-default mb-2"
              >
                Requisitos Especiales
              </h4>
              <div class="bg-elevated rounded-lg p-3">
                <p class="text-sm text-default">
                  {{ selectedRequest.specialRequests }}
                </p>
              </div>
            </div>

            <!-- Status Update Form -->
            <div>
              <h4
                class="text-sm font-medium text-default mb-2"
              >
                Actualizar Estado
              </h4>
              <div class="space-y-3">
                <USelect
                  v-model="statusForm.status"
                  :items="statusSelectOptions"
                  option-attribute="label"
                  value-attribute="value"
                  placeholder="Seleccionar estado"
                  size="lg"
                />
                <UInput
                  v-model="statusForm.quotedPrice"
                  type="number"
                  placeholder="Precio cotizado (opcional)"
                  size="lg"
                  icon="i-lucide-dollar-sign"
                />
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div
            class="flex justify-end gap-3 pt-4 border-t border-default"
          >
            <UButton
              color="neutral"
              variant="ghost"
              @click="closeDetailsModal"
            >
              Cancelar
            </UButton>
            <UButton
              color="primary"
              :loading="updatingStatus"
              @click="updateRequestStatus"
            >
              Actualizar Estado
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
  </div>
</template>
