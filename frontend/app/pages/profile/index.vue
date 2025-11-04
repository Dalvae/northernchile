<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-800 py-12">
    <UContainer>
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t("user.profile") }}
        </h1>
        <p class="text-neutral-600 dark:text-neutral-400">
          {{ t("profile.profile_description") }}
        </p>
      </div>

      <div class="grid lg:grid-cols-3 gap-6">
        <!-- Sidebar Navigation -->
        <div class="lg:col-span-1">
          <UCard>
            <template #content>
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
                  to="/profile/bookings"
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
            </template>
          </UCard>
        </div>

        <!-- Main Content -->
        <div class="lg:col-span-2">
          <!-- Personal Information Tab -->
          <UCard v-if="activeTab === 'personal'">
            <template #header>
              <div class="flex items-center justify-between">
                <h2
                  class="text-xl font-semibold text-neutral-900 dark:text-white"
                >
                  {{ t("user.personal_info") }}
                </h2>
                <UButton
                  v-if="!isEditing"
                  color="primary"
                  variant="outline"
                  size="sm"
                  icon="i-lucide-pencil"
                  @click="startEditing"
                >
                  {{ t("common.edit") }}
                </UButton>
              </div>
            </template>

            <template #content>
              <form @submit.prevent="saveProfile" class="space-y-6">
                <UFormField
                  :label="t('auth.full_name')"
                  name="fullName"
                  required
                >
                  <UInput
                    v-model="profileForm.fullName"
                    :disabled="!isEditing"
                    size="lg"
                    icon="i-lucide-user"
                    class="w-full"
                  />
                </UFormField>

                <UFormField :label="t('auth.email')" name="email" required>
                  <UInput
                    v-model="profileForm.email"
                    type="email"
                    disabled
                    size="lg"
                    icon="i-lucide-mail"
                    class="w-full"
                  />
                  <template #help>
                    <span class="text-sm text-neutral-500">
                      {{ t("profile.email_cannot_change") }}
                    </span>
                  </template>
                </UFormField>

                <UFormField
                  :label="t('booking.nationality')"
                  name="nationality"
                >
                  <UInput
                    v-model="profileForm.nationality"
                    :disabled="!isEditing"
                    size="lg"
                    icon="i-lucide-globe"
                    :placeholder="t('booking.nationality_placeholder')"
                    class="w-full"
                  />
                </UFormField>

                <UFormField
                  :label="t('booking.date_of_birth')"
                  name="dateOfBirth"
                >
                  <UInput
                    v-model="profileForm.dateOfBirth"
                    type="date"
                    :disabled="!isEditing"
                    size="lg"
                    icon="i-lucide-calendar"
                    class="w-full"
                  />
                </UFormField>

                <UFormField :label="t('booking.phone')" name="phoneNumber">
                  <UInput
                    v-model="profileForm.phoneNumber"
                    :disabled="!isEditing"
                    size="lg"
                    icon="i-lucide-phone"
                    :placeholder="t('booking.phone_placeholder')"
                    class="w-full"
                  />
                </UFormField>

                <UFormField
                  :label="t('user.language_preference')"
                  name="preferredLanguage"
                >
                  <USelect
                    v-model="profileForm.preferredLanguage"
                    :disabled="!isEditing"
                    :items="languageOptions"
                    option-attribute="label"
                    value-attribute="value"
                    size="lg"
                    class="w-full"
                  />
                </UFormField>

                <div v-if="isEditing" class="flex gap-3 justify-end">
                  <UButton
                    type="button"
                    variant="outline"
                    color="neutral"
                    size="lg"
                    @click="cancelEditing"
                  >
                    {{ t("common.cancel") }}
                  </UButton>

                  <UButton
                    type="submit"
                    color="primary"
                    size="lg"
                    :loading="isSaving"
                  >
                    {{ t("common.save") }}
                  </UButton>
                </div>
              </form>
            </template>
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

            <template #content>
              <form @submit.prevent="changePassword" class="space-y-6">
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
            </template>
          </UCard>
        </div>
      </div>
    </UContainer>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
const authStore = useAuthStore();
const toast = useToast();

definePageMeta({
  layout: "default",
});

// State
const activeTab = ref("personal");
const isEditing = ref(false);
const isSaving = ref(false);
const isChangingPassword = ref(false);

// Profile form
const profileForm = reactive({
  fullName: authStore.user?.fullName || "",
  email: authStore.user?.email || "",
  nationality: authStore.user?.nationality || "",
  dateOfBirth: authStore.user?.dateOfBirth || "",
  phoneNumber: authStore.user?.phoneNumber || "",
  preferredLanguage: authStore.user?.preferredLanguage || "es",
});

// Password form
const passwordForm = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// Language options
const languageOptions = [
  { label: "Español", value: "es" },
  { label: "English", value: "en" },
  { label: "Português", value: "pt" },
];

// Original values for cancel
let originalValues: any = null;

// Start editing
function startEditing() {
  originalValues = { ...profileForm };
  isEditing.value = true;
}

// Cancel editing
function cancelEditing() {
  Object.assign(profileForm, originalValues);
  isEditing.value = false;
}

// Save profile
async function saveProfile() {
  isSaving.value = true;

  try {
    await $fetch("/api/users/me", {
      method: "PUT",
      credentials: "include",
      body: profileForm,
    });

    // Update auth store
    await authStore.fetchUser();

    toast.add({
      title: t("common.success"),
      description: t("profile.profile_updated_success"),
      color: "success",
    });

    isEditing.value = false;
  } catch (error: any) {
    toast.add({
      title: t("common.error"),
      description: error.data?.message || t("profile.profile_update_error"),
      color: "error",
    });
  } finally {
    isSaving.value = false;
  }
}

// Change password
async function changePassword() {
  // Validate passwords match
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    toast.add({
      title: t("common.error"),
      description: t("auth.passwords_dont_match"),
      color: "error",
    });
    return;
  }

  // Validate password length
  if (passwordForm.newPassword.length < 8) {
    toast.add({
      title: t("common.error"),
      description: t("auth.password_min"),
      color: "error",
    });
    return;
  }

  isChangingPassword.value = true;

  try {
    await $fetch("/api/profile/me/password", {
      method: "PUT",
      credentials: "include",
      body: {
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      },
    });

    toast.add({
      title: t("common.success"),
      description: t("profile.password_updated_success"),
      color: "success",
    });

    // Reset form
    passwordForm.currentPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";
  } catch (error: any) {
    toast.add({
      title: t("common.error"),
      description: error.data?.message || t("profile.password_update_error"),
      color: "error",
    });
  } finally {
    isChangingPassword.value = false;
  }
}
</script>
