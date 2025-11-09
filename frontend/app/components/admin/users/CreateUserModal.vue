<script setup lang="ts">
import { z } from 'zod'
import type { UserCreateReq } from 'api-client'

const emit = defineEmits<{ success: []; close: [] }>()
const { t } = useI18n()
const { createAdminUser } = useAdminData()
const toast = useToast()

// Role options for select
const roleOptions = [
  { label: 'Cliente', value: 'ROLE_CLIENT' },
  { label: 'Partner Admin', value: 'ROLE_PARTNER_ADMIN' },
  { label: 'Super Admin', value: 'ROLE_SUPER_ADMIN' }
]

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
    toast.add({
      title: 'Usuario creado',
      color: 'success',
      icon: 'i-lucide-check-circle'
    })

    // Reset form
    state.email = ''
    state.fullName = ''
    state.password = ''
    state.role = 'ROLE_CLIENT'
    state.nationality = ''
    state.phoneNumber = ''

    emit('success')
  } catch (error: unknown) {
    let description = 'Error desconocido'

    if (typeof error === 'string') {
      description = error
    } else if (error && typeof error === 'object') {
      const anyError = error as { data?: any; message?: string }
      description
        = anyError.data?.message
        || anyError.message
        || description
    }

    toast.add({
      title: 'Error al crear',
      description,
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
      icon="i-lucide-user-plus"
      color="primary"
    >
      Crear Usuario
    </UButton>

    <template #content>
      <div class="p-6">
        <!-- Header -->
        <div class="flex justify-between items-start pb-4 border-b border-default">
          <div>
            <h3 class="text-xl font-semibold text-default">
              Crear Usuario
            </h3>
            <p class="text-sm text-muted mt-1">
              Completa los datos del nuevo usuario
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
        </UForm>

        <!-- Footer -->
        <div class="flex justify-end gap-3 pt-4 border-t border-default">
          <UButton
            label="Cancelar"
            color="neutral"
            variant="outline"
            :disabled="isSubmitting"
            @click="$emit('close')"
          />
          <UButton
            label="Crear Usuario"
            color="primary"
            :loading="isSubmitting"
            @click="handleSubmit"
          />
        </div>
      </div>
    </template>
  </UModal>
</template>
