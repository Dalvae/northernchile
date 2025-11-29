<script setup lang="ts">
const { t } = useI18n();
const toast = useToast();
const config = useRuntimeConfig();

useSeoMeta({
  title: t("privateTours.seo.title"),
  description: t("privateTours.seo.description"),
  ogTitle: t("privateTours.seo.og_title"),
  ogDescription: t("privateTours.seo.og_description"),
  twitterCard: "summary_large_image",
});

const state = reactive({
  fullName: "",
  email: "",
  phone: "",
  numberOfPeople: 2,
  preferredDate: "",
  preferredTime: "evening",
  tourType: "ASTRONOMICAL",
  specialRequests: "",
  loading: false,
});

// Date picker state
const preferredDateValue = ref<any>(undefined)

// Sync between CalendarDate and string
watch(preferredDateValue, (newDate) => {
  if (newDate) {
    state.preferredDate = `${newDate.year}-${String(newDate.month).padStart(2, '0')}-${String(newDate.day).padStart(2, '0')}`
  } else {
    state.preferredDate = ''
  }
})

const tourTypeOptions = [
  {
    label: t("privateTours.form.tour_type.astronomical"),
    value: "ASTRONOMICAL",
  },
  { label: t("privateTours.form.tour_type.cultural"), value: "CULTURAL" },
  { label: t("privateTours.form.tour_type.adventure"), value: "ADVENTURE" },
  { label: t("privateTours.form.tour_type.photography"), value: "PHOTOGRAPHY" },
  { label: t("privateTours.form.tour_type.combined"), value: "COMBINED" },
];

const timeOptions = [
  { label: t("privateTours.form.time_options.morning"), value: "morning" },
  { label: t("privateTours.form.time_options.afternoon"), value: "afternoon" },
  { label: t("privateTours.form.time_options.evening"), value: "evening" },
  { label: t("privateTours.form.time_options.flexible"), value: "flexible" },
];

const benefits = [
  {
    icon: "i-lucide-users",
    title: t("privateTours.benefits.exclusive_experience_title"),
    description: t("privateTours.benefits.exclusive_experience_description"),
  },
  {
    icon: "i-lucide-clock",
    title: t("privateTours.benefits.flexible_hours_title"),
    description: t("privateTours.benefits.flexible_hours_description"),
  },
  {
    icon: "i-lucide-map",
    title: t("privateTours.benefits.custom_itinerary_title"),
    description: t("privateTours.benefits.custom_itinerary_description"),
  },
  {
    icon: "i-lucide-graduation-cap",
    title: t("privateTours.benefits.exclusive_guide_title"),
    description: t("privateTours.benefits.exclusive_guide_description"),
  },
  {
    icon: "i-lucide-camera",
    title: t("privateTours.benefits.professional_photography_title"),
    description: t(
      "privateTours.benefits.professional_photography_description",
    ),
  },
  {
    icon: "i-lucide-sparkles",
    title: t("privateTours.benefits.unique_experiences_title"),
    description: t("privateTours.benefits.unique_experiences_description"),
  },
];

async function submitRequest() {
  state.loading = true;
  try {
    const response = await $fetch(
      `${config.public.apiBase}/api/private-tours/requests`,
      {
        method: "POST",
        credentials: "include",
        body: {
          fullName: state.fullName,
          email: state.email,
          phone: state.phone,
          numberOfPeople: state.numberOfPeople,
          preferredDate: state.preferredDate,
          tourType: state.tourType,
          specialRequests: state.specialRequests,
          status: "PENDING",
        },
      },
    );

    toast.add({
      title: t("privateTours.form.toast.success_title"),
      description: t("privateTours.form.toast.success_description"),
      color: "success",
    });

    // Reset form
    state.fullName = "";
    state.email = "";
    state.phone = "";
    state.numberOfPeople = 2;
    state.preferredDate = "";
    preferredDateValue.value = undefined;
    state.preferredTime = "evening";
    state.tourType = "ASTRONOMICAL";
    state.specialRequests = "";
  } catch (error) {
    toast.add({
      title: t("privateTours.form.toast.error_title"),
      description: t("privateTours.form.toast.error_description"),
      color: "error",
    });
  } finally {
    state.loading = false;
  }
}
</script>

<template>
  <div class="bg-neutral-950 min-h-screen">
    <!-- Hero Section -->
    <section
      class="relative h-[70vh] min-h-[600px] flex items-center justify-center overflow-hidden"
    >
      <!-- Background Image -->
      <div class="absolute inset-0">
        <NuxtImg
          src="/images/private-tours-hero.png"
          alt="Private Tours"
          class="w-full h-full object-cover"
          format="webp"
          loading="eager"
          fetchpriority="high"
        />
        <div
          class="absolute inset-0 bg-gradient-to-b from-black/70 via-black/50 to-neutral-950"
        />
      </div>

      <UContainer class="relative z-10 text-center">
        <div class="max-w-4xl mx-auto space-y-8 animate-fade-in-up">
          <div
            class="inline-flex items-center justify-center p-1 rounded-full bg-white/10 backdrop-blur-sm border border-white/20 mb-6"
          >
            <span class="px-4 py-1 text-sm font-medium text-white">
              <UIcon
                name="i-lucide-sparkles"
                class="w-4 h-4 inline-block mr-2 text-primary-400"
              />
              Experiencia Premium
            </span>
          </div>

          <h1
            class="text-5xl md:text-7xl font-bold text-white tracking-tight drop-shadow-lg"
          >
            {{ t("privateTours.title") }}
          </h1>

          <p
            class="text-xl md:text-2xl text-neutral-200 max-w-2xl mx-auto leading-relaxed font-light"
          >
            {{ t("privateTours.subtitle") }}
          </p>

          <div class="pt-8">
            <UButton
              size="xl"
              color="primary"
              variant="solid"
              to="#request-form"
              class="rounded-full px-8 py-4 font-semibold shadow-lg shadow-primary-500/20 hover:shadow-primary-500/40 transition-all duration-300 transform hover:-translate-y-1"
            >
              {{ t("privateTours.form.send_request") }}
              <UIcon name="i-lucide-arrow-right" class="ml-2 w-5 h-5" />
            </UButton>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Benefits -->
    <section class="py-24 bg-neutral-950 relative overflow-hidden">
      <!-- Decorative gradient -->
      <div
        class="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-px bg-gradient-to-r from-transparent via-primary-500/30 to-transparent"
      />

      <UContainer>
        <div class="text-center mb-16">
          <h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
            {{ t("privateTours.benefits.title") }}
          </h2>
          <div class="w-24 h-1.5 bg-primary-500 mx-auto rounded-full" />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <UCard
            v-for="benefit in benefits"
            :key="benefit.title"
            class="group hover:ring-2 hover:ring-primary-500/50 transition-all duration-500 bg-neutral-900/50 border-neutral-800 relative overflow-hidden ring-1 ring-neutral-800 shadow-none hover:shadow-2xl hover:shadow-primary-900/20"
          >
            <!-- Meteor Effect -->
            <ClientOnly>
              <UiMeteors :count="10" class="opacity-30" />
            </ClientOnly>

            <!-- Content Wrapper (z-10 to stay above meteors) -->
            <div class="relative z-10 flex flex-col items-center">
              <div class="mb-6 relative">
                <!-- Eliminado el div del blur-2xl que no te gustaba -->
                <div
                  class="relative w-16 h-16 rounded-2xl bg-neutral-800 border border-neutral-700 flex items-center justify-center group-hover:border-primary-500/50 group-hover:scale-110 transition-all duration-500"
                >
                  <UIcon
                    :name="benefit.icon"
                    class="w-8 h-8 text-primary-400 group-hover:text-primary-300"
                  />
                </div>
              </div>

              <h3
                class="text-xl font-semibold text-white mb-3 group-hover:text-primary-400 transition-colors"
              >
                {{ benefit.title }}
              </h3>

              <p class="text-neutral-400 leading-relaxed">
                {{ benefit.description }}
              </p>
            </div>
          </UCard>
        </div>
      </UContainer>
    </section>

    <!-- Request Form -->
    <section id="request-form" class="py-24 bg-neutral-950 relative">
      <div
        class="absolute inset-0 bg-[url('/images/grid-pattern.svg')] opacity-5"
      />

      <UContainer class="relative z-10">
        <div class="max-w-4xl mx-auto">
          <div class="text-center mb-12">
            <h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
              {{ t("privateTours.form.title") }}
            </h2>
            <p class="text-neutral-400 text-lg">
              {{ t("privateTours.form.subtitle") }}
            </p>
          </div>

          <UCard
            class="bg-neutral-900/80 backdrop-blur border-neutral-800 shadow-2xl"
          >
            <form class="space-y-8 p-8 sm:p-12" @submit.prevent="submitRequest">
              <!-- Personal Info -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div class="space-y-2">
                  <label class="block text-sm font-medium text-neutral-300">
                    {{ t("privateTours.form.full_name_label") }}
                    <span class="text-primary-500">*</span>
                  </label>
                  <UInput
                    v-model="state.fullName"
                    size="xl"
                    :placeholder="t('privateTours.form.full_name_placeholder')"
                    icon="i-lucide-user"
                    class="w-full bg-neutral-950 text-white"
                    required
                  />
                </div>

                <div class="space-y-2">
                  <label class="block text-sm font-medium text-neutral-300">
                    {{ t("privateTours.form.email_label") }}
                    <span class="text-primary-500">*</span>
                  </label>
                  <UInput
                    v-model="state.email"
                    type="email"
                    size="xl"
                    placeholder="juan@gmail.com"
                    icon="i-lucide-mail"
                    class="w-full bg-neutral-950 text-white"
                    required
                  />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div class="space-y-2">
                  <label class="block text-sm font-medium text-neutral-300">
                    {{ t("privateTours.form.phone_label") }}
                    <span class="text-primary-500">*</span>
                  </label>
                  <UInput
                    v-model="state.phone"
                    size="xl"
                    :placeholder="t('privateTours.form.phone_placeholder')"
                    icon="i-lucide-phone"
                    class="w-full bg-neutral-950 text-white"
                    required
                  />
                </div>

                <div class="space-y-2">
                  <label class="block text-sm font-medium text-neutral-300">
                    {{ t("privateTours.form.number_of_people_label") }}
                    <span class="text-primary-500">*</span>
                  </label>
                  <UInput
                    v-model.number="state.numberOfPeople"
                    type="number"
                    min="1"
                    max="20"
                    size="xl"
                    :placeholder="
                      t('privateTours.form.number_of_people_placeholder')
                    "
                    icon="i-lucide-users"
                    class="w-full bg-neutral-950 text-white"
                    required
                  />
                </div>
              </div>

              <!-- Tour Details -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                <div class="space-y-2">
                  <label class="block text-sm font-medium text-neutral-300">
                    {{ t("privateTours.form.preferred_date_label") }}
                    <span class="text-primary-500">*</span>
                  </label>
                  <UInputDate
                    v-model="preferredDateValue"
                    size="xl"
                    class="w-full bg-neutral-950 text-white"
                    required
                  />
                </div>

                <div class="space-y-2">
                  <label class="block text-sm font-medium text-neutral-300">
                    {{ t("privateTours.form.preferred_time_label") }}
                  </label>
                  <USelect
                    v-model="state.preferredTime"
                    :items="timeOptions"
                    option-attribute="label"
                    value-attribute="value"
                    size="xl"
                    icon="i-lucide-clock"
                    class="w-full bg-neutral-950 text-white"
                  />
                </div>
              </div>

              <div class="space-y-2">
                <label class="block text-sm font-medium text-neutral-300">
                  {{ t("privateTours.form.tour_type_label") }}
                  <span class="text-primary-500">*</span>
                </label>
                <USelect
                  v-model="state.tourType"
                  :items="tourTypeOptions"
                  option-attribute="label"
                  value-attribute="value"
                  size="xl"
                  icon="i-lucide-map"
                  class="w-full bg-neutral-950 text-white"
                />
              </div>

              <div class="space-y-2">
                <label class="block text-sm font-medium text-neutral-300">
                  {{ t("privateTours.form.special_requests_label") }}
                </label>
                <UTextarea
                  v-model="state.specialRequests"
                  :rows="4"
                  size="xl"
                  :placeholder="
                    t('privateTours.form.special_requests_placeholder')
                  "
                  class="w-full bg-neutral-950 text-white"
                />
              </div>

              <div class="pt-8">
                <UButton
                  type="submit"
                  size="xl"
                  color="primary"
                  :loading="state.loading"
                  block
                  class="rounded-xl py-4 font-bold text-lg shadow-lg shadow-primary-500/20 hover:shadow-primary-500/40 transition-all"
                >
                  {{
                    state.loading
                      ? t("privateTours.form.sending_request")
                      : t("privateTours.form.send_request")
                  }}
                  <UIcon
                    v-if="!state.loading"
                    name="i-lucide-send"
                    class="ml-2 w-5 h-5"
                  />
                </UButton>
              </div>
            </form>
          </UCard>

          <!-- Contact Info -->
          <div class="mt-16 text-center">
            <p class="text-neutral-400">
              {{ t("privateTours.form.contact_info") }}
            </p>
          </div>
        </div>
      </UContainer>
    </section>
  </div>
</template>
