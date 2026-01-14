<script setup lang="ts">
import type { UserRes, UserUpdateReq } from 'api-client'
import { z } from 'zod'
import { USER_ROLE_OPTIONS } from '~/utils/adminOptions'

const props = defineProps<{
  user: UserRes
}>()

const emit = defineEmits<{
  success: []
}>()

const { updateAdminUser, resetAdminUserPassword } = useAdminData()
const { showSuccessToast, showErrorToast } = useApiError()

const isOpen = ref(false)

// Form state - initialize with user data
const state = reactive<UserUpdateReq>({
  fullName: props.user.fullName,
  role: props.user.role,
  nationality: props.user.nationality ?? '',
  phoneNumber: props.user.phoneNumber ?? ''
})

// Validation schema
const schema = z.object({
  fullName: z.string().min(1, 'Nombre completo es requerido'),
  role: z.string().min(1, 'Rol es requerido'),
  nationality: z.string().optional(),
  phoneNumber: z.string().optional()
})

const isSubmitting = ref(false)
const isResettingPassword = ref(false)
const showPasswordReset = ref(false)
const newPassword = ref('')

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
    await resetAdminUserPassword(props.user.id!, newPassword.value)
    showSuccessToast('Contraseña restablecida', 'La contraseña del usuario ha sido actualizada correctamente')
    newPassword.value = ''
    showPasswordReset.value = false
  } catch (error) {
    showErrorToast(error, 'Error al restablecer contraseña')
  } finally {
    isResettingPassword.value = false
  }
}

async function handleSubmit() {
  isSubmitting.value = true
  try {
    const payload: UserUpdateReq = {
      fullName: state.fullName,
      role: state.role,
      nationality: state.nationality || undefined,
      phoneNumber: state.phoneNumber || undefined
    }

    await updateAdminUser(props.user.id!, payload)
    showSuccessToast('Usuario actualizado')
    isOpen.value = false
    emit('success')
  } catch (error) {
    showErrorToast(error, 'Error al actualizar')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <!-- Trigger Button -->
  <UButton
    icon="i-lucide-pencil"
    color="neutral"
    variant="ghost"
    size="sm"
    aria-label="Editar usuario"
    @click="isOpen = true"
  />

  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Editar Usuario"
    subtitle="Modifica la información del usuario"
    submit-label="Guardar Cambios"
    :submit-loading="isSubmitting"
    @submit="handleSubmit"
  >
    <UForm
      :schema="schema"
      :state="state"
      class="space-y-4"
      @submit="handleSubmit"
    >
      <!-- Email (readonly) -->
      <UFormField
        label="Email"
        name="email"
      >
        <UInput
          :model-value="user.email"
          type="email"
          icon="i-lucide-mail"
          class="w-full"
          disabled
          readonly
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

      <!-- Password Reset Section -->
      <div class="pt-4 border-t border-default">
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
