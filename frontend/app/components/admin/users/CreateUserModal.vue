<script setup lang="ts">
import { z } from 'zod'
import type { UserCreateReq } from 'api-client'
import { USER_ROLE_OPTIONS } from '~/utils/adminOptions'

const emit = defineEmits<{ success: [] }>()
const { createAdminUser } = useAdminData()
const { showSuccessToast, showErrorToast } = useApiError()

const isOpen = ref(false)

// Form state
const state = reactive<UserCreateReq & { password: string }>({
  email: '',
  fullName: '',
  password: '',
  role: 'ROLE_CLIENT',
  nationality: '',
  phoneNumber: ''
})

// Validation schema
const schema = z.object({
  email: z.string().email('Email inválido'),
  fullName: z.string().min(1, 'Nombre completo es requerido'),
  password: z.string().min(8, 'Contraseña debe tener al menos 8 caracteres'),
  role: z.string().min(1, 'Rol es requerido'),
  nationality: z.string().optional(),
  phoneNumber: z.string().optional()
})

const isSubmitting = ref(false)

function resetForm() {
  state.email = ''
  state.fullName = ''
  state.password = ''
  state.role = 'ROLE_CLIENT'
  state.nationality = ''
  state.phoneNumber = ''
}

async function handleSubmit() {
  isSubmitting.value = true
  try {
    const payload: UserCreateReq = {
      email: state.email,
      fullName: state.fullName,
      password: state.password,
      role: state.role,
      nationality: state.nationality || undefined,
      phoneNumber: state.phoneNumber || undefined
    }

    await createAdminUser(payload)
    showSuccessToast('Usuario creado')
    resetForm()
    isOpen.value = false
    emit('success')
  } catch (error) {
    showErrorToast(error, 'Error al crear')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <!-- Trigger Button -->
  <UButton
    icon="i-lucide-user-plus"
    color="primary"
    @click="isOpen = true"
  >
    Crear Usuario
  </UButton>

  <AdminBaseAdminModal
    v-model:open="isOpen"
    title="Crear Usuario"
    subtitle="Completa los datos del nuevo usuario"
    submit-label="Crear Usuario"
    :submit-loading="isSubmitting"
    @submit="handleSubmit"
  >
    <UForm
      :schema="schema"
      :state="state"
      class="space-y-4"
      @submit="handleSubmit"
    >
      <!-- Email -->
      <UFormField
        label="Email"
        name="email"
        required
      >
        <UInput
          v-model="state.email"
          type="email"
          placeholder="usuario@example.com"
          icon="i-lucide-mail"
          class="w-full"
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

      <!-- Password -->
      <UFormField
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
    </UForm>
  </AdminBaseAdminModal>
</template>
