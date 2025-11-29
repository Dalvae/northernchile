<template>
  <div
    class="min-h-screen flex items-center justify-center py-12 px-4 bg-neutral-50 dark:bg-neutral-800"
  >
    <UCard class="w-full max-w-md">
      <div class="p-6 space-y-6">
        <!-- Header -->
        <div class="text-center space-y-2">
          <div
            class="w-12 h-12 mx-auto rounded-full bg-primary/10 flex items-center justify-center"
          >
            <UIcon
              :name="currentView === 'login' ? 'i-lucide-log-in' : currentView === 'register' ? 'i-lucide-user-plus' : 'i-lucide-key'"
              class="w-6 h-6 text-primary"
            />
          </div>
          <h2 class="text-2xl font-bold text-neutral-900 dark:text-white">
            {{ viewTitle }}
          </h2>
          <p class="text-sm text-neutral-600 dark:text-neutral-300">
            {{ viewDescription }}
          </p>
        </div>

        <!-- Password Reset Request Form -->
        <template v-if="currentView === 'forgot-password'">
          <UForm
            :state="forgotPasswordState"
            :schema="forgotPasswordSchema"
            @submit="handleForgotPassword"
          >
            <div class="space-y-4">
              <UFormField
                :label="t('auth.email')"
                name="email"
                required
              >
                <UInput
                  v-model="forgotPasswordState.email"
                  type="email"
                  :placeholder="t('auth.email_placeholder')"
                  size="lg"
                  icon="i-lucide-mail"
                  class="w-full"
                />
              </UFormField>

              <UButton
                type="submit"
                color="primary"
                size="lg"
                block
                :loading="loading"
              >
                {{ t('auth.send_reset_link') }}
              </UButton>
            </div>
          </UForm>

          <div class="text-center text-sm">
            <UButton
              variant="link"
              color="primary"
              @click="currentView = 'login'"
            >
              {{ t('auth.back_to_login') }}
            </UButton>
          </div>
        </template>

        <!-- Password Reset Confirm Form -->
        <template v-else-if="currentView === 'reset-password'">
          <UForm
            :state="resetPasswordState"
            :schema="resetPasswordSchema"
            @submit="handleResetPassword"
          >
            <div class="space-y-4">
              <UFormField
                :label="t('auth.new_password')"
                name="newPassword"
                required
              >
                <UInput
                  v-model="resetPasswordState.newPassword"
                  type="password"
                  :placeholder="t('auth.password_placeholder')"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                :label="t('auth.confirm_password')"
                name="confirmPassword"
                required
              >
                <UInput
                  v-model="resetPasswordState.confirmPassword"
                  type="password"
                  :placeholder="t('auth.confirm_password_placeholder')"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>

              <UButton
                type="submit"
                color="primary"
                size="lg"
                block
                :loading="loading"
              >
                {{ t('auth.reset_password') }}
              </UButton>
            </div>
          </UForm>

          <div class="text-center text-sm">
            <UButton
              variant="link"
              color="primary"
              @click="currentView = 'login'"
            >
              {{ t('auth.back_to_login') }}
            </UButton>
          </div>
        </template>

        <!-- Login/Register Form -->
        <UForm
          v-else
          :state="state"
          :schema="schema"
          @submit="handleSubmit"
        >
          <div class="space-y-4">
            <!-- Login Fields -->
            <template v-if="currentView === 'login'">
              <UFormField
                :label="t('auth.email')"
                name="email"
                required
              >
                <UInput
                  v-model="state.email"
                  type="email"
                  :placeholder="t('auth.email_placeholder')"
                  size="lg"
                  icon="i-lucide-mail"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                :label="t('auth.password')"
                name="password"
                required
              >
                <UInput
                  v-model="state.password"
                  type="password"
                  :placeholder="t('auth.password_placeholder')"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>

              <div class="flex justify-end">
                <UButton
                  variant="link"
                  color="primary"
                  size="sm"
                  class="p-0"
                  @click="currentView = 'forgot-password'"
                >
                  {{ t('auth.forgot_password') }}
                </UButton>
              </div>
            </template>

            <!-- Register Fields -->
            <template v-else-if="currentView === 'register'">
              <UFormField
                :label="t('auth.full_name')"
                name="fullName"
                required
              >
                <UInput
                  v-model="state.fullName"
                  :placeholder="t('auth.full_name_placeholder')"
                  size="lg"
                  icon="i-lucide-user"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                :label="t('auth.email')"
                name="email"
                required
              >
                <UInput
                  v-model="state.email"
                  type="email"
                  :placeholder="t('auth.email_placeholder')"
                  size="lg"
                  icon="i-lucide-mail"
                  class="w-full"
                />
              </UFormField>

              <CountrySelect
                :model-value="state.nationality"
                :label="t('booking.nationality')"
                :placeholder="t('booking.nationality_placeholder')"
                size="lg"
                @update:model-value="handleNationalityChange($event)"
              />

              <UFormField
                :label="t('booking.phone')"
                name="phoneNumber"
              >
                <div class="flex gap-2">
                  <select
                    v-model="state.phoneCountryCode"
                    class="w-28 px-2 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent text-sm"
                    aria-label="Código de país"
                  >
                    <option
                      v-for="pc in phoneCodes"
                      :key="pc.code + pc.country"
                      :value="pc.code"
                    >
                      {{ getCountryFlag(pc.country) }} {{ pc.code }}
                    </option>
                  </select>
                  <UInput
                    v-model="state.phoneNumber"
                    type="tel"
                    placeholder="912345678"
                    size="lg"
                    icon="i-lucide-phone"
                    class="flex-1"
                    minlength="6"
                    maxlength="15"
                  />
                </div>
              </UFormField>

              <UFormField
                :label="t('booking.date_of_birth')"
                name="dateOfBirth"
              >
                <DateInput
                  v-model="state.dateOfBirth"
                  size="lg"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                :label="t('auth.password')"
                name="password"
                required
              >
                <UInput
                  v-model="state.password"
                  type="password"
                  :placeholder="t('auth.password_placeholder')"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>

              <UFormField
                :label="t('auth.confirm_password')"
                name="confirmPassword"
                required
              >
                <UInput
                  v-model="state.confirmPassword"
                  type="password"
                  :placeholder="t('auth.confirm_password_placeholder')"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>

              <!-- Terms Checkbox -->
              <UFormField
                name="acceptTerms"
                required
              >
                <div class="flex items-start gap-2">
                  <UCheckbox
                    v-model="state.acceptTerms"
                    name="acceptTerms"
                    class="mt-0.5"
                  />
                  <label
                    for="acceptTerms"
                    class="text-sm text-neutral-600 dark:text-neutral-300 cursor-pointer"
                  >
                    {{ t('auth.accept_terms') }}
                    <NuxtLink
                      :to="localePath('/terms')"
                      class="text-primary font-medium hover:underline"
                      target="_blank"
                    >
                      {{ t('auth.terms_of_service') }}
                    </NuxtLink>
                    {{ t('common.and') }}
                    <NuxtLink
                      :to="localePath('/privacy')"
                      class="text-primary font-medium hover:underline"
                      target="_blank"
                    >
                      {{ t('auth.privacy_policy') }}
                    </NuxtLink>
                  </label>
                </div>
              </UFormField>
            </template>

            <!-- Submit Button -->
            <UButton
              type="submit"
              color="primary"
              size="lg"
              block
              :loading="loading"
            >
              {{ currentView === 'login' ? t("auth.login") : t("auth.register") }}
            </UButton>
          </div>
        </UForm>

        <!-- Toggle Login/Register -->
        <div
          v-if="currentView === 'login' || currentView === 'register'"
          class="text-center text-sm"
        >
          <span class="text-neutral-600 dark:text-neutral-300">
            {{
              currentView === 'login'
                ? t("auth.dont_have_account")
                : t("auth.already_have_account")
            }}
          </span>
          <UButton
            variant="link"
            color="primary"
            class="ml-1"
            @click="toggleForm"
          >
            {{ currentView === 'login' ? t("auth.register") : t("auth.login") }}
          </UButton>
        </div>

        <!-- Terms -->
        <p
          v-if="currentView === 'login' || currentView === 'register'"
          class="text-xs text-center text-neutral-500 dark:text-neutral-300"
        >
          {{ t("auth.terms_text") }}
          <NuxtLink
            :to="localePath('/terms')"
            class="text-primary font-medium hover:underline"
          >
            {{ t("auth.terms_of_service") }}
          </NuxtLink>
          {{ t("common.and") }}
          <NuxtLink
            :to="localePath('/privacy')"
            class="text-primary font-medium hover:underline"
          >
            {{ t("auth.privacy_policy") }}
          </NuxtLink>
        </p>
      </div>
    </UCard>
  </div>
</template>

<script setup lang="ts">
import type { FormSubmitEvent } from '@nuxt/ui'
import { z } from 'zod'

const { t } = useI18n()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const localePath = useLocalePath()
const toast = useToast()
const config = useRuntimeConfig()
const { phoneCodes, getPhoneCodeByCountry, getCountryFlag } = useCountries()

type AuthView = 'login' | 'register' | 'forgot-password' | 'reset-password'

// Estado
const currentView = ref<AuthView>('login')
const loading = ref(false)

// Check for reset token in URL
onMounted(() => {
  const token = route.query.token as string | undefined
  if (token) {
    currentView.value = 'reset-password'
    resetPasswordState.token = token
  }
})

// View title and description
const viewTitle = computed(() => {
  switch (currentView.value) {
    case 'login':
      return t('auth.login')
    case 'register':
      return t('auth.register')
    case 'forgot-password':
      return t('auth.forgot_password')
    case 'reset-password':
      return t('auth.reset_password')
    default:
      return t('auth.login')
  }
})

const viewDescription = computed(() => {
  switch (currentView.value) {
    case 'login':
      return t('auth.login_description')
    case 'register':
      return t('auth.register_description')
    case 'forgot-password':
      return t('auth.forgot_password_description')
    case 'reset-password':
      return t('auth.reset_password_description')
    default:
      return t('auth.login_description')
  }
})

// State del formulario principal
const state = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  fullName: '',
  nationality: '',
  phoneCountryCode: '+56',
  phoneNumber: '',
  dateOfBirth: '',
  acceptTerms: false
})

// State para recuperar contraseña
const forgotPasswordState = reactive({
  email: ''
})

// State para reset de contraseña
const resetPasswordState = reactive({
  token: '',
  newPassword: '',
  confirmPassword: ''
})

// Date of birth is now handled directly by DateInput component as ISO string

// Schema de validación principal
const schema = computed(() => {
  if (currentView.value === 'login') {
    return z.object({
      email: z.string().email(t('auth.email_invalid')),
      password: z.string().min(1, t('auth.password_required'))
    })
  } else {
    return z
      .object({
        fullName: z.string().min(2, t('auth.full_name_min')),
        email: z.string().email(t('auth.email_invalid')),
        nationality: z.string().optional(),
        phoneNumber: z.string().optional(),
        dateOfBirth: z.string().optional(),
        password: z.string().min(8, t('auth.password_min')),
        confirmPassword: z.string(),
        acceptTerms: z.literal(true, {
          errorMap: () => ({ message: t('auth.accept_terms_required') })
        })
      })
      .refine(data => data.password === data.confirmPassword, {
        message: t('auth.passwords_dont_match'),
        path: ['confirmPassword']
      })
  }
})

// Schema para recuperar contraseña
const forgotPasswordSchema = z.object({
  email: z.string().email(t('auth.email_invalid'))
})

// Schema para reset de contraseña
const resetPasswordSchema = computed(() => {
  return z
    .object({
      newPassword: z.string().min(8, t('auth.password_min')),
      confirmPassword: z.string()
    })
    .refine(data => data.newPassword === data.confirmPassword, {
      message: t('auth.passwords_dont_match'),
      path: ['confirmPassword']
    })
})

// Alternar formulario
function toggleForm() {
  currentView.value = currentView.value === 'login' ? 'register' : 'login'
  // Reset form
  state.email = ''
  state.password = ''
  state.confirmPassword = ''
  state.fullName = ''
  state.nationality = ''
  state.phoneCountryCode = '+56'
  state.phoneNumber = ''
  state.dateOfBirth = ''
  state.acceptTerms = false
}

// Auto-update phone country code when nationality changes
function handleNationalityChange(nationality: string) {
  state.nationality = nationality
  if (nationality && !state.phoneNumber) {
    state.phoneCountryCode = getPhoneCodeByCountry(nationality)
  }
}

// Submit principal (login/register)
async function handleSubmit(_event: FormSubmitEvent<z.infer<typeof schema.value>>) {
  loading.value = true

  try {
    if (currentView.value === 'login') {
      await authStore.login({
        email: state.email,
        password: state.password
      })

      toast.add({
        title: t('common.success'),
        description: t('auth.login_success'),
        color: 'success'
      })

      await router.push(localePath('/'))
    } else {
      await authStore.register({
        email: state.email,
        password: state.password,
        fullName: state.fullName,
        nationality: state.nationality || undefined,
        phoneNumber: state.phoneNumber ? `${state.phoneCountryCode}${state.phoneNumber}` : undefined
      })

      toast.add({
        title: t('common.success'),
        description: t('auth.register_success'),
        color: 'success'
      })

      // Cambiar a login
      currentView.value = 'login'
      Object.keys(state).forEach((key) => {
        state[key as keyof typeof state] = ''
      })
    }
  } catch (error: unknown) {
    console.error('Error en auth:', error)
    const { showErrorToast } = useApiError()
    showErrorToast(error)
  } finally {
    loading.value = false
  }
}

// Solicitar recuperación de contraseña
async function handleForgotPassword(_event: FormSubmitEvent<z.infer<typeof forgotPasswordSchema>>) {
  loading.value = true

  try {
    const apiBase = config.public.apiBase

    await $fetch(`${apiBase}/api/auth/password-reset/request`, {
      method: 'POST',
      body: { email: forgotPasswordState.email }
    })

    toast.add({
      title: t('common.success'),
      description: t('auth.reset_link_sent'),
      color: 'success'
    })

    // Volver a login
    currentView.value = 'login'
    forgotPasswordState.email = ''
  } catch (error: unknown) {
    const err = error as { data?: { message?: string } }
    console.error('Error en forgot password:', error)

    // Siempre mostrar mensaje genérico por seguridad
    toast.add({
      title: t('common.success'),
      description: t('auth.reset_link_sent'),
      color: 'success'
    })

    currentView.value = 'login'
    forgotPasswordState.email = ''
  } finally {
    loading.value = false
  }
}

// Confirmar reset de contraseña
async function handleResetPassword(_event: FormSubmitEvent<z.infer<typeof resetPasswordSchema.value>>) {
  loading.value = true

  try {
    const apiBase = config.public.apiBase

    await $fetch(`${apiBase}/api/auth/password-reset/confirm`, {
      method: 'POST',
      body: {
        token: resetPasswordState.token,
        newPassword: resetPasswordState.newPassword
      }
    })

    toast.add({
      title: t('common.success'),
      description: t('auth.password_reset_success'),
      color: 'success'
    })

    // Limpiar estado y volver a login
    resetPasswordState.token = ''
    resetPasswordState.newPassword = ''
    resetPasswordState.confirmPassword = ''
    currentView.value = 'login'

    // Limpiar token de la URL
    await router.replace(localePath('/auth'))
  } catch (error: unknown) {
    console.error('Error en reset password:', error)
    const { showErrorToast } = useApiError()
    showErrorToast(error, t('auth.reset_password_error'))
  } finally {
    loading.value = false
  }
}

definePageMeta({
  layout: 'default'
})
</script>
