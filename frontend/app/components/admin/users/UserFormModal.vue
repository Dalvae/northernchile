<script setup lang="ts">
import type { UserRes, UserCreateReq, UserUpdateReq } from 'api-client'
import { z } from 'zod'
import { USER_ROLE_OPTIONS } from '~/utils/adminOptions'

const props = defineProps<{
  user?: UserRes // If provided, it's edit mode; otherwise, create mode
}>()

const emit = defineEmits<{
  success: []
}>()

const { createAdminUser, updateAdminUser, resetAdminUserPassword } = useAdminData()
const { showSuccessToast, showErrorToast } = useApiError()

const isEditMode = computed(() => !!props.user)

// Form state
const state = reactive({
  email: '',
  fullName: '',
  password: '',
  role: 'ROLE_CLIENT',
  nationality: '',
  phoneNumber: ''
})

// Validation schema - password only required for create
const schema = computed(() => z.object({
  email: isEditMode.value ? z.string().optional() : z.string().email('Email inválido'),
  fullName: z.string().min(1, 'Nombre completo es requerido'),
  password: isEditMode.value ? z.string().optional() : z.string().min(8, 'Contraseña debe tener al menos 8 caracteres'),
  role: z.string().min(1, 'Rol es requerido'),
  nationality: z.string().optional(),
  phoneNumber: z.string().optional()
}))

// Password reset state (only for edit mode)
const isResettingPassword = ref(false)
const showPasswordReset = ref(false)
const newPassword = ref('')

// Initialize form with user data when editing
watch(() => props.user, (user) => {
  if (user) {
    state.email = user.email || ''
    state.fullName = user.fullName || ''
    state.password = ''
    state.role = user.role || 'ROLE_CLIENT'
    state.nationality = user.nationality || ''
    state.phoneNumber = user.phoneNumber || ''
  }
}, { immediate: true })

function resetForm() {
  state.email = ''
  state.fullName = ''
  state.password = ''
  state.role = 'ROLE_CLIENT'
  state.nationality = ''
  state.phoneNumber = ''
  showPasswordReset.value = false
  newPassword.value = ''
}

async function handlePasswordReset() {
  if (!newPassword.value || newPassword.value.length < 8) {
    showErrorToast({ message: 'La contraseña debe tener al menos 8 caracteres' })
    return
  }

  if (!confirm('¿Estás seguro de que quieres restablecer la contraseña de este usuario?')) {
    return
  }

  isResettingPassword.value = true
  try {
    await resetAdminUserPassword(props.user!.id!, newPassword.value)
    showSuccessToast('Contraseña restablecida', 'La contraseña del usuario ha sido actualizada correctamente')
    newPassword.value = ''
    showPasswordReset.value = false
  } catch (error) {
    showErrorToast(error, 'Error al restablecer contraseña')
  } finally {
    isResettingPassword.value = false
  }
}

const { isOpen, isSubmitting, handleSubmit } = useModalForm({
  onSubmit: async () => {
    if (isEditMode.value) {
      const payload: UserUpdateReq = {
        fullName: state.fullName,
        role: state.role,
        nationality: state.nationality || undefined,
        phoneNumber: state.phoneNumber || undefined
      }
      await updateAdminUser(props.user!.id!, payload)
    } else {
      const payload: UserCreateReq = {
        email: state.email,
        fullName: state.fullName,
        password: state.password,
        role: state.role,
        nationality: state.nationality || undefined,
        phoneNumber: state.phoneNumber || undefined
      }
      await createAdminUser(payload)
    }
  },
  onSuccess: () => {
    if (!isEditMode.value) {
      resetForm()
    }
    emit('success')
  },
  successMessage: computed(() => isEditMode.value ? 'Usuario actualizado' : 'Usuario creado').value,
  errorMessage: computed(() => isEditMode.value ? 'Error al actualizar' : 'Error al crear').value
})

// Modal config based on mode
const modalTitle = computed(() => isEditMode.value ? 'Editar Usuario' : 'Crear Usuario')
const modalSubtitle = computed(() => isEditMode.value ? 'Modifica la información del usuario' : 'Completa los datos del nuevo usuario')
const submitLabel = computed(() => isEditMode.value ? 'Guardar Cambios' : 'Crear Usuario')
</script>

<template>
  <!-- Trigger Button - different for create vs edit -->
  <UButton
    v-if="!isEditMode"
    icon="i-lucide-user-plus"
    color="primary"
    @click="isOpen = true"
  >
    Crear Usuario
  </UButton>
  <UButton
    v-else
    icon="i-lucide-pencil"
    color="neutral"
    variant="ghost"
    size="sm"
    aria-label="Editar usuario"
    @click="isOpen = true"
  />

  <AdminBaseAdminModal
    v-model:open="isOpen"
    :title="modalTitle"
    :subtitle="modalSubtitle"
    :submit-label="submitLabel"
    :submit-loading="isSubmitting"
    @submit="handleSubmit"
  >
    <UForm
      :schema="schema"
      :state="state"
      class="space-y-4"
      @submit="() => { handleSubmit() }"
    >
      <!-- Email -->
      <UFormField
        label="Email"
        name="email"
        :required="!isEditMode"
      >
        <UInput
          v-model="state.email"
          type="email"
          :placeholder="isEditMode ? '' : 'usuario@example.com'"
          icon="i-lucide-mail"
          class="w-full"
          :disabled="isEditMode"
          :readonly="isEditMode"
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
          class="w-full"
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
          class="w-full"
        />
      </UFormField>

      <!-- Role -->
      <UFormField
        label="Rol"
        name="role"
        required
      >
        <USelect
          v-model="state.role"
          :items="[...USER_ROLE_OPTIONS] as { label: string, value: string }[]"
          option-attribute="label"
          value-attribute="value"
          placeholder="Selecciona un rol"
          class="w-full"
        />
      </UFormField>

      <!-- Nationality -->
      <CountrySelect
        v-model="state.nationality"
        label="Nacionalidad"
        placeholder="Selecciona nacionalidad"
      />

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
          class="w-full"
        />
      </UFormField>

      <!-- Password Reset Section (only for edit) -->
      <div
        v-if="isEditMode"
        class="pt-4 border-t border-default"
      >
        <div class="flex items-center justify-between mb-3">
          <div>
            <h4 class="font-medium text-default">
              Restablecer Contraseña
            </h4>
            <p class="text-sm text-muted">
              Establece una nueva contraseña para este usuario
            </p>
          </div>
          <UButton
            v-if="!showPasswordReset"
            icon="i-lucide-key"
            color="warning"
            variant="outline"
            size="sm"
            @click="showPasswordReset = true"
          >
            Cambiar Contraseña
          </UButton>
        </div>

        <div
          v-if="showPasswordReset"
          class="space-y-3 p-4 bg-warning-50 dark:bg-warning-950 rounded-lg border border-warning-200 dark:border-warning-800"
        >
          <UFormField
            label="Nueva Contraseña"
            help="Mínimo 8 caracteres"
          >
            <UInput
              v-model="newPassword"
              type="password"
              placeholder="••••••••"
              icon="i-lucide-lock"
              class="w-full"
            />
          </UFormField>
          <div class="flex gap-2">
            <UButton
              color="warning"
              :loading="isResettingPassword"
              icon="i-lucide-key"
              @click="handlePasswordReset"
            >
              Restablecer Contraseña
            </UButton>
            <UButton
              color="neutral"
              variant="outline"
              :disabled="isResettingPassword"
              @click="showPasswordReset = false; newPassword = ''"
            >
              Cancelar
            </UButton>
          </div>
        </div>
      </div>
    </UForm>
  </AdminBaseAdminModal>
</template>
