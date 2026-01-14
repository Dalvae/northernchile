<script setup lang="ts">
import { h } from 'vue'
import AdminStatusBadge from '~/components/admin/StatusBadge.vue'
import AdminCountryCell from '~/components/admin/CountryCell.vue'
import type { UserRes } from 'api-client'
import { USER_ROLE_FILTER_OPTIONS } from '~/utils/adminOptions'

definePageMeta({
  layout: 'admin'
})

useHead({
  title: 'Usuarios - Admin - Northern Chile'
})

const { fetchAdminUsers, deleteAdminUser }
  = useAdminData()
const { formatDateTime } = useDateTime()

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
    header: 'Fecha Creación',
    cell: ({ row }) => {
      const value = row.getValue('createdAt') as string
      return h('span', { class: 'text-sm text-default' }, formatDateTime(value))
    }
  },
  {
    id: 'actions',
    header: 'Acciones'
  }
]

const roleOptions = USER_ROLE_FILTER_OPTIONS

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
  if (!user.id) return
  if (!confirm(`¿Estás seguro de que deseas eliminar al usuario ${user.email}?`)) return

  try {
    await deleteAdminUser(user.id)
    toast.add({
      color: 'success',
      title: 'Usuario eliminado',
      description: 'El usuario ha sido eliminado correctamente.'
    })
    refresh()
  } catch (error) {
    console.error('Error deleting user:', error)
    toast.add({
      color: 'error',
      title: 'Error',
      description: 'No se pudo eliminar el usuario.'
    })
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
          <AdminUsersUserFormModal @success="refresh" />
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

          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <!-- Edit User Modal -->
              <AdminUsersUserFormModal
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
