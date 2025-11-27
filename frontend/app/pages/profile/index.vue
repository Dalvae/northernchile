<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-800 py-12">
    <UContainer>
      <div class="grid lg:grid-cols-3 gap-6">
        <!-- Header spanning full width -->
        <div class="lg:col-span-3 mb-2">
          <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
            {{ t("user.profile") }}
          </h1>
          <p class="text-neutral-600 dark:text-neutral-300">
            {{ t("profile.profile_description") }}
          </p>
        </div>
        <!-- Sidebar Navigation -->
        <div class="lg:col-span-1">
          <UCard>
            <nav class="space-y-2">
              <UButton
                :color="activeTab === 'personal' ? 'primary' : 'neutral'"
                :variant="activeTab === 'personal' ? 'solid' : 'ghost'"
                block
                icon="i-lucide-user"
                @click="activeTab = 'personal'"
              >
                {{ t("user.personal_info") }}
              </UButton>

              <UButton
                :color="activeTab === 'bookings' ? 'primary' : 'neutral'"
                :variant="activeTab === 'bookings' ? 'solid' : 'ghost'"
                block
                icon="i-lucide-calendar-check"
                @click="activeTab = 'bookings'"
              >
                {{ t("user.my_bookings") }}
              </UButton>

              <UButton
                :color="activeTab === 'security' ? 'primary' : 'neutral'"
                :variant="activeTab === 'security' ? 'solid' : 'ghost'"
                block
                icon="i-lucide-lock"
                @click="activeTab = 'security'"
              >
                {{ t("user.change_password") }}
              </UButton>
            </nav>
          </UCard>
        </div>

        <!-- Main Content -->
        <div class="lg:col-span-2">
          <!-- Personal Information Tab -->
          <UCard v-if="activeTab === 'personal'">
            <template #header>
              <div class="flex items-center justify-between">
                <h2 class="text-xl font-semibold text-neutral-900 dark:text-white">
                  Información Personal
                </h2>
                <UButton
                  v-if="!isEditing"
                  color="primary"
                  @click="startEditing"
                >
                  Editar
                </UButton>
              </div>
            </template>

            <!-- Loading -->
            <div
              v-if="isLoading"
              class="py-12 text-center"
            >
              <p class="text-neutral-600 dark:text-neutral-300">
                Cargando...
              </p>
            </div>

            <!-- Form -->
            <form
              v-else
              class="space-y-5"
              @submit.prevent="saveProfile"
            >
              <!-- Email -->
              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2">
                  Email
                </label>
                <div class="px-4 py-3 rounded-lg bg-neutral-100 dark:bg-neutral-800 border border-neutral-300 dark:border-neutral-700">
                  <span class="text-neutral-900 dark:text-white">{{ profileForm.email }}</span>
                </div>
                <p class="text-xs text-neutral-500 mt-1">
                  El email no se puede cambiar
                </p>
              </div>

              <!-- Full Name -->
              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2">
                  Nombre Completo *
                </label>
                <UInput
                  v-if="isEditing"
                  v-model="profileForm.fullName"
                  type="text"
                  size="lg"
                  required
                  class="w-full"
                />
                <div
                  v-else
                  class="px-4 py-3 rounded-lg bg-neutral-50 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700"
                >
                  <span class="text-neutral-900 dark:text-white">{{ profileForm.fullName || '-' }}</span>
                </div>
              </div>

              <!-- Nationality -->
              <CountrySelect
                v-if="isEditing"
                v-model="profileForm.nationality"
                label="Nacionalidad"
                placeholder="Selecciona tu país"
                size="lg"
              />
              <div v-else>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2">
                  Nacionalidad
                </label>
                <div class="px-4 py-3 rounded-lg bg-neutral-50 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700">
                  <div
                    v-if="profileForm.nationality"
                    class="flex items-center gap-2"
                  >
                    <span class="text-xl">{{ getCountryFlag(profileForm.nationality) }}</span>
                    <span class="text-neutral-900 dark:text-white">{{ getCountryLabel(profileForm.nationality) }}</span>
                  </div>
                  <span
                    v-else
                    class="text-neutral-900 dark:text-white"
                  >-</span>
                </div>
              </div>

              <!-- Phone Number -->
              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2">
                  Teléfono
                </label>
                <UInput
                  v-if="isEditing"
                  v-model="profileForm.phoneNumber"
                  type="tel"
                  size="lg"
                  placeholder="+56 9 1234 5678"
                  class="w-full"
                />
                <div
                  v-else
                  class="px-4 py-3 rounded-lg bg-neutral-50 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700"
                >
                  <span class="text-neutral-900 dark:text-white">{{ profileForm.phoneNumber || '-' }}</span>
                </div>
              </div>

              <!-- Date of Birth -->
              <div>
                <label class="block text-sm font-medium text-neutral-700 dark:text-neutral-200 mb-2">
                  Fecha de Nacimiento
                </label>
                <UInput
                  v-if="isEditing"
                  v-model="profileForm.dateOfBirth"
                  type="date"
                  size="lg"
                  class="w-full"
                />
                <div
                  v-else
                  class="px-4 py-3 rounded-lg bg-neutral-50 dark:bg-neutral-800 border border-neutral-200 dark:border-neutral-700"
                >
                  <span class="text-neutral-900 dark:text-white">{{ profileForm.dateOfBirth || '-' }}</span>
                </div>
              </div>

              <!-- Action buttons (only show when editing) -->
              <div
                v-if="isEditing"
                class="flex gap-3 justify-end pt-4 border-t border-neutral-200 dark:border-neutral-700"
              >
                <UButton
                  type="button"
                  variant="outline"
                  color="neutral"
                  @click="cancelEditing"
                >
                  {{ t("common.cancel") }}
                </UButton>

                <UButton
                  type="submit"
                  color="primary"
                  :disabled="isSaving"
                >
                  {{ isSaving ? 'Guardando...' : 'Guardar' }}
                </UButton>
              </div>
            </form>
          </UCard>

          <!-- Change Password Tab -->
          <UCard v-else-if="activeTab === 'security'">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ t("user.change_password") }}
              </h2>
            </template>

            <form
              class="space-y-6"
              @submit.prevent="changePassword"
            >
              <UFormField
                :label="t('profile.current_password')"
                name="currentPassword"
                required
              >
                <UInput
                  v-model="passwordForm.currentPassword"
                  type="password"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                :label="t('profile.new_password')"
                name="newPassword"
                required
              >
                <UInput
                  v-model="passwordForm.newPassword"
                  type="password"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
                <template #help>
                  <span class="text-sm text-neutral-500">
                    {{ t("auth.password_min") }}
                  </span>
                </template>
              </UFormField>

              <UFormField
                :label="t('auth.confirm_password')"
                name="confirmPassword"
                required
              >
                <UInput
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>

              <div class="flex justify-end">
                <UButton
                  type="submit"
                  color="primary"
                  size="lg"
                  :loading="isChangingPassword"
                >
                  {{ t("profile.update_password") }}
                </UButton>
              </div>
            </form>
          </UCard>

          <!-- Bookings Tab -->
          <ProfileBookingsList v-else-if="activeTab === 'bookings'" />
        </div>
      </div>
    </UContainer>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()
const authStore = useAuthStore()
const toast = useToast()
const { getCountryLabel, getCountryFlag } = useCountries()

definePageMeta({
  layout: 'default'
})

// State
const activeTab = ref('personal')
const isEditing = ref(false)
const isSaving = ref(false)
const isChangingPassword = ref(false)
const isLoading = ref(true)

// Profile form
const profileForm = reactive({
  fullName: '',
  email: '',
  nationality: '',
  dateOfBirth: '',
  phoneNumber: ''
})

// Load user data on mount
onMounted(async () => {
  isLoading.value = true

  // Fetch fresh user data from backend
  await authStore.fetchUser()

  // Update form with user data
  if (authStore.user) {
    profileForm.fullName = authStore.user.fullName || ''
    profileForm.email = authStore.user.email || ''
    profileForm.nationality = authStore.user.nationality || ''
    profileForm.dateOfBirth = authStore.user.dateOfBirth || ''
    profileForm.phoneNumber = authStore.user.phoneNumber || ''
  }

  isLoading.value = false
})

// Password form
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Original values for cancel
let originalValues: any = null

// Start editing
function startEditing() {
  originalValues = { ...profileForm }
  isEditing.value = true
}

// Cancel editing
function cancelEditing() {
  Object.assign(profileForm, originalValues)
  isEditing.value = false
}

// Save profile
async function saveProfile() {
  isSaving.value = true

  try {
    const config = useRuntimeConfig()
    const apiBase = config.public.apiBase

    await $fetch(`${apiBase}/api/profile/me`, {
      method: 'PUT',
      credentials: 'include',
      body: {
        fullName: profileForm.fullName,
        nationality: profileForm.nationality || null,
        phoneNumber: profileForm.phoneNumber || null,
        dateOfBirth: profileForm.dateOfBirth || null
      }
    })

    // Update auth store
    await authStore.fetchUser()

    toast.add({
      title: t('common.success'),
      description: t('profile.profile_updated_success'),
      color: 'success'
    })

    isEditing.value = false
  } catch (error: any) {
    toast.add({
      title: t('common.error'),
      description: error.data?.message || t('profile.profile_update_error'),
      color: 'error'
    })
  } finally {
    isSaving.value = false
  }
}

// Change password
async function changePassword() {
  // Validate passwords match
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    toast.add({
      title: t('common.error'),
      description: t('auth.passwords_dont_match'),
      color: 'error'
    })
    return
  }

  // Validate password length
  if (passwordForm.newPassword.length < 8) {
    toast.add({
      title: t('common.error'),
      description: t('auth.password_min'),
      color: 'error'
    })
    return
  }

  isChangingPassword.value = true

  try {
    await $fetch('/api/profile/me/password', {
      method: 'PUT',
      credentials: 'include',
      body: {
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword
      }
    })

    toast.add({
      title: t('common.success'),
      description: t('profile.password_updated_success'),
      color: 'success'
    })

    // Reset form
    passwordForm.currentPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error: any) {
    toast.add({
      title: t('common.error'),
      description: error.data?.message || t('profile.password_update_error'),
      color: 'error'
    })
  } finally {
    isChangingPassword.value = false
  }
}
</script>
