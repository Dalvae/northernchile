<script setup lang="ts">
import { h } from 'vue'
import AdminStatusBadge from '~/components/admin/StatusBadge.vue'
import AdminCountryCell from '~/components/admin/CountryCell.vue'
import type { UserRes } from '~/lib/api-client'

definePageMeta({
  layout: 'admin'
})

const { fetchAdminUsers, createAdminUser, updateAdminUser, deleteAdminUser }
  = useAdminData()
const { getCountryLabel, getCountryFlag } = useCountries()

const {
  data: users,
  pending,
  refresh
} = useAsyncData('admin-users', () => fetchAdminUsers(), {
  server: false,
  lazy: true,
  default: () => []
})

const q = ref('')
const roleFilter = ref<string>('ALL')

const columns = [
  {
    id: 'email',
    accessorKey: 'email',
    header: 'Email'
  },
  {
    id: 'fullName',
    accessorKey: 'fullName',
    header: 'Nombre Completo'
  },
  {
    id: 'role',
    accessorKey: 'role',
    header: 'Rol',
    cell: ({ row }: any) => h(AdminStatusBadge, { type: 'user', status: row.getValue('role') })
  },
{
      id: 'nationality',
      accessorKey: 'nationality',
      header: 'Nacionalidad',
      cell: ({ row }: any) => h(AdminCountryCell, { code: row.getValue('nationality') })
    },
  {
    id: 'phoneNumber',
    accessorKey: 'phoneNumber',
    header: 'Teléfono'
  },
  {
    id: 'createdAt',
    accessorKey: 'createdAt',
    header: 'Fecha Creación'
  },
  {
    id: 'actions',
    header: 'Acciones'
  }
]

const roleOptions = [
  { label: 'Todos', value: 'ALL' },
  { label: 'Clientes', value: 'ROLE_CLIENT' },
  { label: 'Partner Admin', value: 'ROLE_PARTNER_ADMIN' },
  { label: 'Super Admin', value: 'ROLE_SUPER_ADMIN' }
]

const filteredRows = computed(() => {
  if (!users.value || users.value.length === 0) return []

  let rows = users.value

  // Filter by search query
  if (q.value) {
    const query = q.value.toLowerCase()
    rows = rows.filter(
      user =>
        user.email?.toLowerCase().includes(query)
        || user.fullName?.toLowerCase().includes(query)
        || user.id?.toLowerCase().includes(query)
    )
  }

  // Filter by role
  if (roleFilter.value && roleFilter.value !== 'ALL') {
    rows = rows.filter(user => user.role === roleFilter.value)
  }

  return rows
})

const toast = useToast()

async function handleDelete(user: UserRes) {
  const userName = user.fullName || user.email || 'este usuario'
  if (confirm(`¿Estás seguro de que quieres eliminar a "${userName}"?`)) {
    try {
      await deleteAdminUser(user.id)
      toast.add({
        title: 'Usuario eliminado',
        color: 'success',
        icon: 'i-lucide-check-circle'
      })
      await refresh()
    } catch (e: any) {
      toast.add({
        title: 'Error al eliminar',
        description: e.message || 'Error desconocido',
        color: 'error',
        icon: 'i-lucide-x-circle'
      })
    }
  }
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString('es-CL', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

function getRoleLabel(role: string): string {
  switch (role) {
    case 'ROLE_CLIENT':
      return 'Cliente'
    case 'ROLE_PARTNER_ADMIN':
      return 'Partner Admin'
    case 'ROLE_SUPER_ADMIN':
      return 'Super Admin'
    default:
      return role
  }
}

function getRoleBadgeColor(role: string): string {
  switch (role) {
    case 'ROLE_SUPER_ADMIN':
      return 'error'
    case 'ROLE_PARTNER_ADMIN':
      return 'warning'
    case 'ROLE_CLIENT':
      return 'info'
    default:
      return 'neutral'
  }
}
</script>

<template>
  <div class="min-h-screen bg-default">
    <div
      class="border-b border-default bg-elevated"
    >
      <div class="px-6 py-4">
        <div class="flex items-center justify-between">
          <div>
             <h1 class="text-2xl font-bold text-default">
              Gestión de Usuarios
            </h1>
             <p class="text-default text-sm mt-1">
              Administra todos los usuarios del sistema
            </p>
          </div>
          <div class="flex items-center gap-3">
            <UInput
              v-model="q"
              icon="i-lucide-search"
              placeholder="Buscar usuario..."
              class="w-80"
            />
            <USelectMenu
              v-model="roleFilter"
              :options="roleOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Filtrar por rol"
            />
            <!-- Create User Modal -->
            <AdminUsersCreateUserModal @success="refresh" />
          </div>
        </div>
      </div>
    </div>

    <div class="p-6">
      <div
        class="bg-elevated rounded-lg shadow-sm border border-default overflow-hidden"
      >
        <UTable
          :data="filteredRows"
          :columns="columns"
          :loading="pending"
          :empty-state="{
            icon: 'i-lucide-users',
            label: 'No hay usuarios registrados.'
          }"
          :ui="{
            td: 'p-4 text-sm text-default whitespace-nowrap [&:has([role=checkbox])]:pe-0'
          }"
        >
          <template #email-data="{ row }">
            <span class="text-sm font-medium">
              {{ row.getValue("email") }}
            </span>
          </template>

          <template #fullName-data="{ row }">
            <span class="font-medium">
              {{ row.getValue("fullName") || "Sin nombre" }}
            </span>
          </template>

          <template #role-data="{ row }">
            <AdminStatusBadge
              type="user"
              :status="row.getValue('role')"
            />
          </template>

          <template #phoneNumber-data="{ row }">
            <span class="text-sm">
              {{ row.getValue("phoneNumber") || "-" }}
            </span>
          </template>

          <template #createdAt-data="{ row }">
             <span class="text-sm text-default">
              {{ formatDate(row.getValue("createdAt")) }}
            </span>
          </template>

          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <!-- Edit User Modal -->
              <AdminUsersEditUserModal
                :user="row.original"
                @success="refresh"
              />

              <UButton
                icon="i-lucide-trash-2"
                color="error"
                variant="ghost"
                size="sm"
                aria-label="Eliminar usuario"
                @click="handleDelete(row.original)"
              />
            </div>
          </template>
        </UTable>
      </div>
    </div>
  </div>
</template>
