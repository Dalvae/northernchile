<script setup lang="ts">
const { t } = useI18n()
const toast = useToast()
const config = useRuntimeConfig()

useSeoMeta({
  title: t('privateTours.seo.title'),
  description: t('privateTours.seo.description'),
  ogTitle: t('privateTours.seo.og_title'),
  ogDescription: t('privateTours.seo.og_description'),
  twitterCard: 'summary_large_image'
})

const state = reactive({
  fullName: '',
  email: '',
  phone: '',
  numberOfPeople: 2,
  preferredDate: '',
  preferredTime: 'evening',
  tourType: 'ASTRONOMICAL',
  specialRequests: '',
  loading: false
})

const tourTypeOptions = [
  {
    label: t('privateTours.form.tour_type.astronomical'),
    value: 'ASTRONOMICAL'
  },
  { label: t('privateTours.form.tour_type.cultural'), value: 'CULTURAL' },
  { label: t('privateTours.form.tour_type.adventure'), value: 'ADVENTURE' },
  { label: t('privateTours.form.tour_type.photography'), value: 'PHOTOGRAPHY' },
  { label: t('privateTours.form.tour_type.combined'), value: 'COMBINED' }
]

const timeOptions = [
  { label: t('privateTours.form.time_options.morning'), value: 'morning' },
  { label: t('privateTours.form.time_options.afternoon'), value: 'afternoon' },
  { label: t('privateTours.form.time_options.evening'), value: 'evening' },
  { label: t('privateTours.form.time_options.flexible'), value: 'flexible' }
]

const benefits = [
  {
    icon: 'i-lucide-users',
    title: t('privateTours.benefits.exclusive_experience_title'),
    description: t('privateTours.benefits.exclusive_experience_description')
  },
  {
    icon: 'i-lucide-clock',
    title: t('privateTours.benefits.flexible_hours_title'),
    description: t('privateTours.benefits.flexible_hours_description')
  },
  {
    icon: 'i-lucide-map',
    title: t('privateTours.benefits.custom_itinerary_title'),
    description: t('privateTours.benefits.custom_itinerary_description')
  },
  {
    icon: 'i-lucide-graduation-cap',
    title: t('privateTours.benefits.exclusive_guide_title'),
    description: t('privateTours.benefits.exclusive_guide_description')
  },
  {
    icon: 'i-lucide-camera',
    title: t('privateTours.benefits.professional_photography_title'),
    description: t(
      'privateTours.benefits.professional_photography_description'
    )
  },
  {
    icon: 'i-lucide-sparkles',
    title: t('privateTours.benefits.unique_experiences_title'),
    description: t('privateTours.benefits.unique_experiences_description')
  }
]

async function submitRequest() {
  state.loading = true
  try {
    const response = await $fetch(
      `${config.public.apiBase}/private-tour-requests`,
      {
        method: 'POST',
        credentials: 'include',
        body: {
          fullName: state.fullName,
          email: state.email,
          phone: state.phone,
          numberOfPeople: state.numberOfPeople,
          preferredDate: state.preferredDate,
          tourType: state.tourType,
          specialRequests: state.specialRequests,
          status: 'PENDING'
        }
      }
    )

    toast.add({
      title: t('privateTours.form.toast.success_title'),
      description: t('privateTours.form.toast.success_description'),
      color: 'success'
    })

    // Reset form
    state.fullName = ''
    state.email = ''
    state.phone = ''
    state.numberOfPeople = 2
    state.preferredDate = ''
    state.preferredTime = 'evening'
    state.tourType = 'ASTRONOMICAL'
    state.specialRequests = ''
  } catch (error) {
    toast.add({
      title: t('privateTours.form.toast.error_title'),
      description: t('privateTours.form.toast.error_description'),
      color: 'error'
    })
  } finally {
    state.loading = false
  }
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section
      class="relative py-20 bg-gradient-to-b from-neutral-900 to-neutral-800 text-white"
    >
      <UContainer>
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-4xl md:text-5xl font-bold mb-6">
            {{ t("privateTours.title") }}
          </h1>
          <p class="text-xl text-neutral-300">
            {{ t("privateTours.subtitle") }}
          </p>
        </div>
      </UContainer>
    </section>

    <!-- Benefits -->
    <section class="py-16 bg-white dark:bg-neutral-800">
      <UContainer>
        <h2
          class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center"
        >
          {{ t("privateTours.benefits.title") }}
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div
            v-for="benefit in benefits"
            :key="benefit.title"
            class="flex gap-4"
          >
            <div class="flex-shrink-0">
              <div
                class="w-12 h-12 rounded-lg bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center"
              >
                <UIcon
                  :name="benefit.icon"
                  class="w-6 h-6 text-primary-600 dark:text-primary-400"
                />
              </div>
            </div>
            <div>
              <h3
                class="text-lg font-semibold text-neutral-900 dark:text-white mb-2"
              >
                {{ benefit.title }}
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm">
                {{ benefit.description }}
              </p>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Request Form -->
    <section class="py-16 bg-neutral-50 dark:bg-neutral-800/50">
      <UContainer>
        <div class="max-w-3xl mx-auto">
          <div class="text-center mb-12">
            <h2
              class="text-3xl font-bold text-neutral-900 dark:text-white mb-4"
            >
              {{ t("privateTours.form.title") }}
            </h2>
            <p class="text-neutral-600 dark:text-neutral-400">
              {{ t("privateTours.form.subtitle") }}
            </p>
          </div>

          <UCard>
            <form
              class="space-y-6"
              @submit.prevent="submitRequest"
            >
              <!-- Personal Info -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    {{ t("privateTours.form.full_name_label") }} *
                  </label>
                  <UInput
                    v-model="state.fullName"
                    size="lg"
                    :placeholder="t('privateTours.form.full_name_placeholder')"
                    class="w-full"
                    required
                  />
                </div>

                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    {{ t("privateTours.form.email_label") }} *
                  </label>
                  <UInput
                    v-model="state.email"
                    type="email"
                    size="lg"
                    placeholder="juan@gmail.com"
                    class="w-full"
                    required
                  />
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    {{ t("privateTours.form.phone_label") }} *
                  </label>
                  <UInput
                    v-model="state.phone"
                    size="lg"
                    :placeholder="t('privateTours.form.phone_placeholder')"
                    class="w-full"
                    required
                  />
                </div>

                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    {{ t("privateTours.form.number_of_people_label") }} *
                  </label>
                  <UInput
                    v-model.number="state.numberOfPeople"
                    type="number"
                    min="1"
                    max="20"
                    size="lg"
                    :placeholder="
                      t('privateTours.form.number_of_people_placeholder')
                    "
                    class="w-full"
                    required
                  />
                </div>
              </div>

              <!-- Tour Details -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    {{ t("privateTours.form.preferred_date_label") }} *
                  </label>
                  <UInput
                    v-model="state.preferredDate"
                    type="date"
                    size="lg"
                    class="w-full"
                    required
                  />
                </div>

                <div>
                  <label
                    class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                  >
                    {{ t("privateTours.form.preferred_time_label") }}
                  </label>
                  <USelect
                    v-model="state.preferredTime"
                    :items="timeOptions"
                    option-attribute="label"
                    value-attribute="value"
                    size="lg"
                    class="w-full"
                  />
                </div>
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  {{ t("privateTours.form.tour_type_label") }} *
                </label>
                <USelect
                  v-model="state.tourType"
                  :items="tourTypeOptions"
                  option-attribute="label"
                  value-attribute="value"
                  size="lg"
                  class="w-full"
                />
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  {{ t("privateTours.form.special_requests_label") }}
                </label>
                <UTextarea
                  v-model="state.specialRequests"
                  :rows="4"
                  size="lg"
                  :placeholder="
                    t('privateTours.form.special_requests_placeholder')
                  "
                  class="w-full"
                />
              </div>

              <div class="pt-4">
                <UButton
                  type="submit"
                  size="lg"
                  color="primary"
                  :loading="state.loading"
                  block
                >
                  {{
                    state.loading
                      ? t("privateTours.form.sending_request")
                      : t("privateTours.form.send_request")
                  }}
                </UButton>
              </div>
            </form>
          </UCard>

          <!-- Contact Info -->
          <div class="mt-12 text-center">
            <p class="text-neutral-600 dark:text-neutral-400">
              {{ t("privateTours.form.contact_info") }}
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Popular Private Tours -->
    <section class="py-16 bg-white dark:bg-neutral-800">
      <UContainer>
        <h2
          class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center"
        >
          {{ t("privateTours.popular_tours.title") }}
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          <UCard>
            <div class="text-center p-4">
              <UIcon
                name="i-lucide-telescope"
                class="w-12 h-12 text-primary-600 dark:text-primary-400 mx-auto mb-4"
              />
              <h3
                class="text-xl font-semibold text-neutral-900 dark:text-white mb-2"
              >
                {{ t("privateTours.popular_tours.astrophotography.title") }}
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm mb-4">
                {{
                  t("privateTours.popular_tours.astrophotography.description")
                }}
              </p>
              <p
                class="text-2xl font-bold text-primary-600 dark:text-primary-400"
              >
                {{ t("privateTours.popular_tours.astrophotography.price") }}
              </p>
            </div>
          </UCard>

          <UCard>
            <div class="text-center p-4">
              <UIcon
                name="i-lucide-heart"
                class="w-12 h-12 text-primary-600 dark:text-primary-400 mx-auto mb-4"
              />
              <h3
                class="text-xl font-semibold text-neutral-900 dark:text-white mb-2"
              >
                {{ t("privateTours.popular_tours.romantic_experience.title") }}
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm mb-4">
                {{
                  t(
                    "privateTours.popular_tours.romantic_experience.description"
                  )
                }}
              </p>
              <p
                class="text-2xl font-bold text-primary-600 dark:text-primary-400"
              >
                {{ t("privateTours.popular_tours.romantic_experience.price") }}
              </p>
            </div>
          </UCard>

          <UCard>
            <div class="text-center p-4">
              <UIcon
                name="i-lucide-users-round"
                class="w-12 h-12 text-primary-600 dark:text-primary-400 mx-auto mb-4"
              />
              <h3
                class="text-xl font-semibold text-neutral-900 dark:text-white mb-2"
              >
                {{ t("privateTours.popular_tours.family_tour.title") }}
              </h3>
              <p class="text-neutral-600 dark:text-neutral-400 text-sm mb-4">
                {{ t("privateTours.popular_tours.family_tour.description") }}
              </p>
              <p
                class="text-2xl font-bold text-primary-600 dark:text-primary-400"
              >
                {{ t("privateTours.popular_tours.family_tour.price") }}
              </p>
            </div>
          </UCard>
        </div>
      </UContainer>
    </section>
  </div>
</template>
