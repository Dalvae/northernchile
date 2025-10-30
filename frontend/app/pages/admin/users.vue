<script setup lang="ts">
import { h } from "vue";
import type { UserRes } from "~/lib/api-client";

definePageMeta({
  layout: "admin",
});

const { fetchAdminUsers, createAdminUser, updateAdminUser, deleteAdminUser } = useAdminData();

const {
  data: users,
  pending,
  refresh,
} = useAsyncData(
  "admin-users",
  () => fetchAdminUsers(),
  {
    server: false,
    lazy: true,
    default: () => []
  }
);

const isModalOpen = ref(false);
const selectedUser = ref<UserRes | null>(null);
const isEditMode = ref(false);
const q = ref("");
const roleFilter = ref<string>("ALL");

const columns = [
  {
    id: "id",
    accessorKey: "id",
    header: "ID",
  },
  {
    id: "email",
    accessorKey: "email",
    header: "Email",
  },
  {
    id: "fullName",
    accessorKey: "fullName",
    header: "Nombre Completo",
  },
  {
    id: "role",
    accessorKey: "role",
    header: "Rol",
  },
  {
    id: "authProvider",
    accessorKey: "authProvider",
    header: "Proveedor Auth",
  },
  {
    id: "createdAt",
    accessorKey: "createdAt",
    header: "Fecha Creación",
  },
  {
    id: "actions",
    header: "Acciones",
  },
];

const roleOptions = [
  { label: "Todos", value: "ALL" },
  { label: "Clientes", value: "ROLE_CLIENT" },
  { label: "Partner Admin", value: "ROLE_PARTNER_ADMIN" },
  { label: "Super Admin", value: "ROLE_SUPER_ADMIN" },
];

const filteredRows = computed(() => {
  if (!users.value || users.value.length === 0) return [];

  let rows = users.value;

  // Filter by search query
  if (q.value) {
    const query = q.value.toLowerCase();
    rows = rows.filter((user) =>
      user.email?.toLowerCase().includes(query) ||
      user.fullName?.toLowerCase().includes(query) ||
      user.id?.toLowerCase().includes(query)
    );
  }

  // Filter by role
  if (roleFilter.value && roleFilter.value !== "ALL") {
    rows = rows.filter((user) => user.role === roleFilter.value);
  }

  return rows;
});

function openCreateModal() {
  selectedUser.value = null;
  isEditMode.value = false;
  isModalOpen.value = true;
}

function openEditModal(user: UserRes) {
  selectedUser.value = user;
  isEditMode.value = true;
  isModalOpen.value = true;
}

function closeModal() {
  isModalOpen.value = false;
  selectedUser.value = null;
  isEditMode.value = false;
}

function onModalSuccess() {
  closeModal();
  refresh();
}

const toast = useToast();

async function handleDelete(user: UserRes) {
  const userName = user.fullName || user.email || "este usuario";
  if (confirm(`¿Estás seguro de que quieres eliminar a "${userName}"?`)) {
    try {
      await deleteAdminUser(user.id);
      toast.add({
        title: "Usuario eliminado",
        color: "success",
        icon: "i-lucide-check-circle",
      });
      await refresh();
    } catch (e: any) {
      toast.add({
        title: "Error al eliminar",
        description: e.message || "Error desconocido",
        color: "error",
        icon: "i-lucide-x-circle",
      });
    }
  }
}

function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString("es-CL", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function getRoleLabel(role: string): string {
  switch (role) {
    case "ROLE_CLIENT":
      return "Cliente";
    case "ROLE_PARTNER_ADMIN":
      return "Partner Admin";
    case "ROLE_SUPER_ADMIN":
      return "Super Admin";
    default:
      return role;
  }
}

function getRoleBadgeColor(role: string): string {
  switch (role) {
    case "ROLE_SUPER_ADMIN":
      return "error";
    case "ROLE_PARTNER_ADMIN":
      return "warning";
    case "ROLE_CLIENT":
      return "info";
    default:
      return "neutral";
  }
}
</script>

<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-900">
    <div
      class="border-b border-neutral-200 dark:border-neutral-800 bg-white dark:bg-neutral-900"
    >
      <div class="px-6 py-4">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-neutral-900 dark:text-white">
              Gestión de Usuarios
            </h1>
            <p class="text-neutral-600 dark:text-neutral-400 mt-1">
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
            <UButton
              icon="i-lucide-user-plus"
              color="primary"
              @click="openCreateModal"
            >
              Crear Usuario
            </UButton>
          </div>
        </div>
      </div>
    </div>

    <div class="p-6">
      <div
        class="bg-white dark:bg-neutral-800 rounded-lg shadow-sm border border-neutral-200 dark:border-neutral-700 overflow-hidden"
      >
        <UTable
          :data="filteredRows"
          :columns="columns"
          :loading="pending"
          :empty-state="{
            icon: 'i-lucide-users',
            label: 'No hay usuarios registrados.',
          }"
        >
          <template #id-data="{ row }">
            <span class="font-mono text-xs text-neutral-600 dark:text-neutral-400">
              {{ row.getValue("id")?.slice(0, 8) }}...
            </span>
          </template>

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
            <UBadge
              :color="getRoleBadgeColor(row.getValue('role'))"
              variant="subtle"
            >
              {{ getRoleLabel(row.getValue("role")) }}
            </UBadge>
          </template>

          <template #authProvider-data="{ row }">
            <span class="text-sm">
              {{ row.getValue("authProvider") || "LOCAL" }}
            </span>
          </template>

          <template #createdAt-data="{ row }">
            <span class="text-sm">
              {{ formatDate(row.getValue("createdAt")) }}
            </span>
          </template>

          <template #actions-cell="{ row }">
            <div class="flex items-center gap-2">
              <UButton
                icon="i-lucide-pencil"
                color="neutral"
                variant="ghost"
                size="sm"
                aria-label="Editar usuario"
                @click="openEditModal(row.original)"
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

    <!-- User Modal -->
    <AdminUsersUserModal
      v-model:open="isModalOpen"
      :user="selectedUser"
      :is-edit-mode="isEditMode"
      @close="closeModal"
      @success="onModalSuccess"
    />
  </div>
</template>
