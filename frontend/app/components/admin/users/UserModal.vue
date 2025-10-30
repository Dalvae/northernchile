<script setup lang="ts">
import type { UserRes } from "~/lib/api-client";
import { z } from "zod";

const props = defineProps<{
  open: boolean;
  user: UserRes | null;
  isEditMode: boolean;
}>();

const emit = defineEmits<{
  close: [];
  success: [];
  "update:open": [value: boolean];
}>();

const { createAdminUser, updateAdminUser } = useAdminData();
const toast = useToast();

// Role options for select
const roleOptions = [
  { label: "Cliente", value: "ROLE_CLIENT" },
  { label: "Partner Admin", value: "ROLE_PARTNER_ADMIN" },
  { label: "Super Admin", value: "ROLE_SUPER_ADMIN" },
];

// Form state
const state = reactive({
  email: "",
  fullName: "",
  password: "",
  role: "ROLE_CLIENT",
  nationality: "",
  phoneNumber: "",
});

// Validation schema
const createSchema = z.object({
  email: z.string().email("Email inválido"),
  fullName: z.string().min(1, "Nombre completo es requerido"),
  password: z.string().min(8, "Contraseña debe tener al menos 8 caracteres"),
  role: z.string().min(1, "Rol es requerido"),
  nationality: z.string().optional(),
  phoneNumber: z.string().optional(),
});

const editSchema = z.object({
  fullName: z.string().min(1, "Nombre completo es requerido"),
  role: z.string().min(1, "Rol es requerido"),
  nationality: z.string().optional(),
  phoneNumber: z.string().optional(),
});

const schema = computed(() => props.isEditMode ? editSchema : createSchema);

// Watch for prop changes to populate form
watch(
  () => [props.open, props.user, props.isEditMode],
  ([isOpen, user, isEdit]) => {
    if (isOpen && isEdit && user) {
      // Edit mode - populate with user data
      state.email = user.email || "";
      state.fullName = user.fullName || "";
      state.password = "";
      state.role = user.role || "ROLE_CLIENT";
      state.nationality = user.nationality || "";
      state.phoneNumber = user.phoneNumber || "";
    } else if (isOpen && !isEdit) {
      // Create mode - reset form
      state.email = "";
      state.fullName = "";
      state.password = "";
      state.role = "ROLE_CLIENT";
      state.nationality = "";
      state.phoneNumber = "";
    }
  },
  { immediate: true }
);

const isSubmitting = ref(false);

async function handleSubmit() {
  isSubmitting.value = true;
  try {
    if (props.isEditMode && props.user) {
      // Update user
      await updateAdminUser(props.user.id, {
        fullName: state.fullName,
        role: state.role,
        nationality: state.nationality || null,
        phoneNumber: state.phoneNumber || null,
      });
      toast.add({
        title: "Usuario actualizado",
        color: "success",
        icon: "i-lucide-check-circle",
      });
    } else {
      // Create user
      await createAdminUser({
        email: state.email,
        fullName: state.fullName,
        password: state.password,
        role: state.role,
        nationality: state.nationality || null,
        phoneNumber: state.phoneNumber || null,
      });
      toast.add({
        title: "Usuario creado",
        color: "success",
        icon: "i-lucide-check-circle",
      });
    }
    emit("update:open", false);
    emit("success");
  } catch (e: any) {
    toast.add({
      title: props.isEditMode ? "Error al actualizar" : "Error al crear",
      description: e.message || "Error desconocido",
      color: "error",
      icon: "i-lucide-x-circle",
    });
  } finally {
    isSubmitting.value = false;
  }
}

function handleClose() {
  emit("update:open", false);
  emit("close");
}
</script>

<template>
  <UModal :model-value="open" @update:model-value="emit('update:open', $event)">
    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex justify-between items-start pb-4 border-b border-neutral-200 dark:border-neutral-700">
          <div>
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white">
              {{ isEditMode ? "Editar Usuario" : "Crear Usuario" }}
            </h3>
            <p class="text-sm text-neutral-600 dark:text-neutral-400 mt-1">
              {{ isEditMode ? "Modifica la información del usuario" : "Completa los datos del nuevo usuario" }}
            </p>
          </div>
          <UButton
            icon="i-lucide-x"
            color="neutral"
            variant="ghost"
            size="sm"
            @click="handleClose"
          />
        </div>

        <!-- Form -->
        <UForm
          :schema="schema"
          :state="state"
          class="py-4 space-y-4 max-h-[60vh] overflow-y-auto"
          @submit="handleSubmit"
        >
          <!-- Email (only for create) -->
          <UFormField
            v-if="!isEditMode"
            label="Email"
            name="email"
            required
          >
            <UInput
              v-model="state.email"
              type="email"
              placeholder="usuario@example.com"
              icon="i-lucide-mail"
            />
          </UFormField>

          <!-- Full Name -->
          <UFormField
            label="Nombre Completo"
            name="fullName"
            required
          >
            <UInput
              v-model="state.fullName"
              placeholder="Nombre completo"
              icon="i-lucide-user"
            />
          </UFormField>

          <!-- Password (only for create) -->
          <UFormField
            v-if="!isEditMode"
            label="Contraseña"
            name="password"
            required
            help="Mínimo 8 caracteres"
          >
            <UInput
              v-model="state.password"
              type="password"
              placeholder="••••••••"
              icon="i-lucide-lock"
            />
          </UFormField>

          <!-- Role -->
          <UFormField
            label="Rol"
            name="role"
            required
          >
            <USelectMenu
              v-model="state.role"
              :options="roleOptions"
              option-attribute="label"
              value-attribute="value"
              placeholder="Selecciona un rol"
            />
          </UFormField>

          <!-- Nationality -->
          <UFormField
            label="Nacionalidad"
            name="nationality"
          >
            <UInput
              v-model="state.nationality"
              placeholder="Ej: Chilena, Argentina"
              icon="i-lucide-globe"
            />
          </UFormField>

          <!-- Phone Number -->
          <UFormField
            label="Teléfono"
            name="phoneNumber"
          >
            <UInput
              v-model="state.phoneNumber"
              type="tel"
              placeholder="+56 9 1234 5678"
              icon="i-lucide-phone"
            />
          </UFormField>
        </UForm>

        <!-- Footer -->
        <div class="flex justify-end gap-3 pt-4 border-t border-neutral-200 dark:border-neutral-700">
          <UButton
            label="Cancelar"
            color="neutral"
            variant="outline"
            @click="handleClose"
            :disabled="isSubmitting"
          />
          <UButton
            :label="isEditMode ? 'Guardar Cambios' : 'Crear Usuario'"
            color="primary"
            :loading="isSubmitting"
            @click="handleSubmit"
          />
        </div>
      </div>
    </template>
  </UModal>
</template>
