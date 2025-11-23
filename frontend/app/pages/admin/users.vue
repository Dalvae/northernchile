<script setup lang="ts">
import { h } from 'vue'
import AdminStatusBadge from '~/components/admin/StatusBadge.vue'
import AdminCountryCell from '~/components/admin/CountryCell.vue'
import type { UserRes } from 'api-client'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Usuarios - Admin - Northern Chile'
})

const { fetchAdminUsers, createAdminUser, updateAdminUser, deleteAdminUser }
  = useAdminData()
const { getCountryLabel, getCountryFlag } = useCountries()
const { formatDate } = useDateTime()

const {
  data: users,
  pending,
  refresh
} = useAsyncData<UserRes[]>('admin-users', () => fetchAdminUsers() as Promise<UserRes[]>, {
  server: false,
  lazy: true,
  default: () => []
})

const q = ref('')
const roleFilter = ref<string>('ALL')

const columns: Array<{ id: string, accessorKey?: keyof UserRes, header: string, cell?: (ctx: { row: { original: UserRes, getValue: (key: keyof UserRes | string) => unknown } }) => ReturnType<typeof h> }> = [
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
    cell: ({ row }) => h(AdminStatusBadge, { type: 'user', status: row.getValue('role') as string })
  },
  {
    id: 'nationality',
    accessorKey: 'nationality',
    header: 'Nacionalidad',
    cell: ({ row }) => h(AdminCountryCell, { code: row.getValue('nationality') as string | null | undefined })
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
      if (!user.id) throw new Error('Missing user id')
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
    <div class="p-6 space-y-6">
      <!-- Header -->
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

      <!-- Table Container -->
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
