<script setup lang="ts">
import type { UserRes } from '~/lib/api-client'
import { z } from 'zod'

const props = defineProps<{
  user: UserRes
}>()

const emit = defineEmits<{
  success: []
}>()

const { updateAdminUser, resetAdminUserPassword } = useAdminData()
const toast = useToast()
const { countries } = useCountries()

// Role options for select
const roleOptions = [
  { label: 'Cliente', value: 'ROLE_CLIENT' },
  { label: 'Partner Admin', value: 'ROLE_PARTNER_ADMIN' },
  { label: 'Super Admin', value: 'ROLE_SUPER_ADMIN' }
]

// Form state - initialize with user data
const state = reactive({
  fullName: props.user.fullName || '',
  role: props.user.role || 'ROLE_CLIENT',
  nationality: props.user.nationality || '',
  phoneNumber: props.user.phoneNumber || ''
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
    toast.add({
      title: 'Error',
      description: 'La contraseña debe tener al menos 8 caracteres',
      color: 'error',
      icon: 'i-lucide-x-circle'
    })
    return
  }

  if (!confirm('¿Estás seguro de que quieres restablecer la contraseña de este usuario?')) {
    return
  }

  isResettingPassword.value = true
  try {
    await resetAdminUserPassword(props.user.id, newPassword.value)
    toast.add({
      title: 'Contraseña restablecida',
      description: 'La contraseña del usuario ha sido actualizada correctamente',
      color: 'success',
      icon: 'i-lucide-check-circle'
    })
    newPassword.value = ''
    showPasswordReset.value = false
  } catch (e: any) {
    toast.add({
      title: 'Error al restablecer contraseña',
      description: e.message || 'Error desconocido',
      color: 'error',
      icon: 'i-lucide-x-circle'
    })
  } finally {
    isResettingPassword.value = false
  }
}

async function handleSubmit() {
  isSubmitting.value = true
  try {
    await updateAdminUser(props.user.id, {
      fullName: state.fullName,
      role: state.role,
      nationality: state.nationality || null,
      phoneNumber: state.phoneNumber || null
    })
    toast.add({
      title: 'Usuario actualizado',
      color: 'success',
      icon: 'i-lucide-check-circle'
    })
    emit('success')
  } catch (e: any) {
    toast.add({
      title: 'Error al actualizar',
      description: e.message || 'Error desconocido',
      color: 'error',
      icon: 'i-lucide-x-circle'
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <UModal>
    <!-- Trigger Button -->
    <UButton
      icon="i-lucide-pencil"
      color="neutral"
      variant="ghost"
      size="sm"
      aria-label="Editar usuario"
    />

    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex justify-between items-start pb-4 border-b border-neutral-200 dark:border-neutral-700">
          <div>
            <h3 class="text-xl font-semibold text-neutral-900 dark:text-white">
              Editar Usuario
            </h3>
            <p class="text-sm text-neutral-600 dark:text-neutral-400 mt-1">
              Modifica la información del usuario
            </p>
          </div>
          <UButton
            icon="i-lucide-x"
            color="neutral"
            variant="ghost"
            size="sm"
            @click="$emit('close')"
          />
        </div>

        <!-- Form -->
        <UForm
          :schema="schema"
          :state="state"
          class="py-4 space-y-4 max-h-[60vh] overflow-y-auto"
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
            <USelectMenu
              v-model="state.role"
              :options="roleOptions"
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
          <div class="pt-4 border-t border-neutral-200 dark:border-neutral-700">
            <div class="flex items-center justify-between mb-3">
              <div>
                <h4 class="font-medium text-neutral-900 dark:text-white">
                  Restablecer Contraseña
                </h4>
                <p class="text-sm text-neutral-600 dark:text-neutral-400">
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

        <!-- Footer -->
        <div class="flex justify-end gap-3 pt-4 border-t border-neutral-200 dark:border-neutral-700">
          <UButton
            label="Cancelar"
            color="neutral"
            variant="outline"
            :disabled="isSubmitting"
            @click="$emit('close')"
          />
          <UButton
            label="Guardar Cambios"
            color="primary"
            :loading="isSubmitting"
            @click="handleSubmit"
          />
        </div>
      </div>
    </template>
  </UModal>
</template>
