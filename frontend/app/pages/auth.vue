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
              :name="isLogin ? 'i-lucide-log-in' : 'i-lucide-user-plus'"
              class="w-6 h-6 text-primary"
            />
          </div>
          <h2 class="text-2xl font-bold text-neutral-900 dark:text-white">
            {{ isLogin ? t("auth.login") : t("auth.register") }}
          </h2>
          <p class="text-sm text-neutral-600 dark:text-neutral-400">
            {{
              isLogin
                ? t("auth.login_description")
                : t("auth.register_description")
            }}
          </p>
        </div>

        <!-- Form -->
        <UForm :state="state" :schema="schema" @submit="handleSubmit">
          <div class="space-y-4">
            <!-- Login Fields -->
            <template v-if="isLogin">
              <UFormField :label="t('auth.email')" name="email" required>
                <UInput
                  v-model="state.email"
                  type="email"
                  :placeholder="t('auth.email_placeholder')"
                  size="lg"
                  icon="i-lucide-mail"
                  class="w-full"
                />
              </UFormField>

              <UFormField :label="t('auth.password')" name="password" required>
                <UInput
                  v-model="state.password"
                  type="password"
                  :placeholder="t('auth.password_placeholder')"
                  size="lg"
                  icon="i-lucide-lock"
                  class="w-full"
                />
              </UFormField>
            </template>

            <!-- Register Fields -->
            <template v-else>
              <UFormField :label="t('auth.full_name')" name="fullName" required>
                <UInput
                  v-model="state.fullName"
                  :placeholder="t('auth.full_name_placeholder')"
                  size="lg"
                  icon="i-lucide-user"
                  class="w-full"
                />
              </UFormField>

              <UFormField :label="t('auth.email')" name="email" required>
                <UInput
                  v-model="state.email"
                  type="email"
                  :placeholder="t('auth.email_placeholder')"
                  size="lg"
                  icon="i-lucide-mail"
                  class="w-full"
                />
              </UFormField>

              <div class="grid grid-cols-2 gap-4">
                <UFormField
                  :label="t('booking.nationality')"
                  name="nationality"
                >
                  <UInput
                    v-model="state.nationality"
                    :placeholder="t('booking.nationality_placeholder')"
                    size="lg"
                    icon="i-lucide-globe"
                    class="w-full"
                  />
                </UFormField>

                <UFormField :label="t('booking.phone')" name="phoneNumber">
                  <UInput
                    v-model="state.phoneNumber"
                    type="tel"
                    :placeholder="t('booking.phone_placeholder')"
                    size="lg"
                    icon="i-lucide-phone"
                    class="w-full"
                  />
                </UFormField>
              </div>

              <UFormField
                :label="t('booking.date_of_birth')"
                name="dateOfBirth"
              >
                <UInput
                  v-model="state.dateOfBirth"
                  type="date"
                  size="lg"
                  icon="i-lucide-calendar"
                  class="w-full"
                />
              </UFormField>

              <UFormField :label="t('auth.password')" name="password" required>
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
            </template>

            <!-- Submit Button -->
            <UButton
              type="submit"
              color="primary"
              size="lg"
              block
              :loading="loading"
            >
              {{ isLogin ? t("auth.login") : t("auth.register") }}
            </UButton>
          </div>
        </UForm>

        <!-- Toggle Login/Register -->
        <div class="text-center text-sm">
          <span class="text-neutral-600 dark:text-neutral-400">
            {{
              isLogin
                ? t("auth.dont_have_account")
                : t("auth.already_have_account")
            }}
          </span>
          <UButton
            variant="link"
            color="primary"
            @click="toggleForm"
            class="ml-1"
          >
            {{ isLogin ? t("auth.register") : t("auth.login") }}
          </UButton>
        </div>

        <!-- Terms -->
        <p class="text-xs text-center text-neutral-500 dark:text-neutral-400">
          {{ t("auth.terms_text") }}
          <NuxtLink
            to="/terms"
            class="text-primary font-medium hover:underline"
          >
            {{ t("auth.terms_of_service") }}
          </NuxtLink>
          {{ t("common.and") }}
          <NuxtLink
            to="/privacy"
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
import type { FormSubmitEvent } from "@nuxt/ui";
import { z } from "zod";

const { t } = useI18n();
const authStore = useAuthStore();
const router = useRouter();
const toast = useToast();

// Estado
const isLogin = ref(true);
const loading = ref(false);

// State del formulario
const state = reactive({
  email: "",
  password: "",
  confirmPassword: "",
  fullName: "",
  nationality: "",
  phoneNumber: "",
  dateOfBirth: "",
});

// Schema de validación
const schema = computed(() => {
  if (isLogin.value) {
    return z.object({
      email: z.string().email(t("auth.email_invalid")),
      password: z.string().min(1, t("auth.password_required")),
    });
  } else {
    return z
      .object({
        fullName: z.string().min(2, t("auth.full_name_min")),
        email: z.string().email(t("auth.email_invalid")),
        nationality: z.string().optional(),
        phoneNumber: z.string().optional(),
        dateOfBirth: z.string().optional(),
        password: z.string().min(8, t("auth.password_min")),
        confirmPassword: z.string(),
      })
      .refine((data) => data.password === data.confirmPassword, {
        message: t("auth.passwords_dont_match"),
        path: ["confirmPassword"],
      });
  }
});

// Alternar formulario
function toggleForm() {
  isLogin.value = !isLogin.value;
  // Reset form
  Object.keys(state).forEach((key) => {
    state[key as keyof typeof state] = "";
  });
}

// Submit
async function handleSubmit(event: FormSubmitEvent<any>) {
  loading.value = true;

  try {
    if (isLogin.value) {
      await authStore.login({
        email: state.email,
        password: state.password,
      });

      toast.add({
        title: t("common.success"),
        description: t("auth.login_success"),
        color: "success",
      });

      await router.push("/");
    } else {
      await authStore.register({
        email: state.email,
        password: state.password,
        fullName: state.fullName,
        nationality: state.nationality || undefined,
        phoneNumber: state.phoneNumber || undefined,
        dateOfBirth: state.dateOfBirth || undefined,
      });

      toast.add({
        title: t("common.success"),
        description: t("auth.register_success"),
        color: "success",
      });

      // Cambiar a login
      isLogin.value = true;
      Object.keys(state).forEach((key) => {
        state[key as keyof typeof state] = "";
      });
    }
  } catch (error: any) {
    console.error("Error en auth:", error);

    let errorMessage = t("common.error");

    if (error.response?.status === 403) {
      errorMessage = t("auth.invalid_credentials");
    } else if (error.response?.status === 400) {
      errorMessage = t("auth.invalid_data");
    } else if (error.message) {
      errorMessage = error.message;
    }

    toast.add({
      title: t("common.error"),
      description: errorMessage,
      color: "error",
    });
  } finally {
    loading.value = false;
  }
}

definePageMeta({
  layout: "default",
});
</script>
