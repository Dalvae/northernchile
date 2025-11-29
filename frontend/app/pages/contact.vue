<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui'

const { t } = useI18n()
const toast = useToast()

useSeoMeta({
  title: t('contact.seo.title'),
  description: t('contact.seo.description'),
  ogTitle: t('contact.seo.og_title'),
  ogDescription: t('contact.seo.og_description'),
  twitterCard: 'summary_large_image'
})

const state = reactive({
  name: '',
  email: '',
  phone: '',
  subject: '',
  message: ''
})

type Schema = typeof state

function validate(state: Partial<Schema>): FormError[] {
  const errors = []
  if (!state.name) errors.push({ name: 'name', message: t('contact.form.validation.name_required') })
  if (!state.email) errors.push({ name: 'email', message: t('contact.form.validation.email_required') })
  if (!state.subject) errors.push({ name: 'subject', message: t('contact.form.validation.subject_required') })
  if (!state.message) errors.push({ name: 'message', message: t('contact.form.validation.message_required') })
  return errors
}

const contactInfo = computed(() => [
  {
    icon: 'i-lucide-map-pin',
    title: t('contact.info.location.title'),
    details: [
      t('contact.info.location.city'),
      t('contact.info.location.region')
    ]
  },
  {
    icon: 'i-lucide-phone',
    title: t('contact.info.phone.title'),
    details: [
      t('contact.info.phone.number'),
      t('contact.info.phone.hours')
    ]
  },
  {
    icon: 'i-lucide-mail',
    title: t('contact.info.email.title'),
    details: [
      t('contact.info.email.info'),
      t('contact.info.email.reservations')
    ]
  }
])

const faqs = computed(() => [
  {
    label: t('contact.faq.q1.question'),
    content: t('contact.faq.q1.answer')
  },
  {
    label: t('contact.faq.q2.question'),
    content: t('contact.faq.q2.answer')
  },
  {
    label: t('contact.faq.q3.question'),
    content: t('contact.faq.q3.answer')
  },
  {
    label: t('contact.faq.q4.question'),
    content: t('contact.faq.q4.answer')
  }
])

async function onSubmit(event: FormSubmitEvent<Schema>) {
  try {
    await $fetch('/api/contact', {
      method: 'POST',
      body: {
        name: state.name,
        email: state.email,
        phone: state.phone,
        message: `${state.subject}\n\n${state.message}`
      },
      credentials: 'include'
    })

    toast.add({
      title: t('contact.form.toast.success_title'),
      description: t('contact.form.toast.success_description'),
      color: 'success',
      icon: 'i-heroicons-check-circle'
    })

    // Reset form
    state.name = ''
    state.email = ''
    state.phone = ''
    state.subject = ''
    state.message = ''
  } catch (error) {
    toast.add({
      title: t('contact.form.toast.error_title'),
      description: t('contact.form.toast.error_description'),
      color: 'error',
      icon: 'i-heroicons-exclamation-circle'
    })
  }
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-950">
    <!-- Simple Header with Stars Background -->
    <div class="relative h-64 md:h-80 flex items-center justify-center overflow-hidden">
      <UiBgStars />
      <div class="absolute inset-0 bg-gradient-to-b from-neutral-900/80 to-neutral-900/40 dark:from-neutral-950/80 dark:to-neutral-950/40" />

      <div class="relative z-10 text-center px-4">
        <h1 class="text-3xl md:text-5xl font-bold text-white mb-4">
          {{ t("contact.hero.title") }}
        </h1>
        <p class="text-lg text-neutral-200 max-w-xl mx-auto font-light">
          {{ t("contact.hero.subtitle") }}
        </p>
      </div>
    </div>

    <UContainer class="py-16 md:py-24">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-16 lg:gap-24">
        <!-- Left Column: Contact Info & Context -->
        <div class="space-y-12">
          <div>
            <h2 class="text-3xl font-bold text-neutral-900 dark:text-white mb-6">
              {{ t("contact.section_title") }}
            </h2>
            <p class="text-neutral-600 dark:text-neutral-300 text-lg leading-relaxed">
              {{ t("contact.section_description") }}
            </p>
          </div>

          <!-- Contact Details List -->
          <div class="space-y-8">
            <div
              v-for="info in contactInfo"
              :key="info.title"
              class="flex items-start gap-6"
            >
              <div class="flex-shrink-0 inline-flex items-center justify-center w-12 h-12 rounded-full bg-primary-50 dark:bg-primary-900/20">
                <UIcon
                  :name="info.icon"
                  class="w-6 h-6 text-primary-600 dark:text-primary-400"
                />
              </div>
              <div>
                <h3 class="text-lg font-semibold text-neutral-900 dark:text-white mb-1">
                  {{ info.title }}
                </h3>
                <div class="space-y-0.5">
                  <p
                    v-for="detail in info.details"
                    :key="detail"
                    class="text-neutral-600 dark:text-neutral-300"
                  >
                    {{ detail }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- Social Links -->
          <div>
            <h3 class="text-sm font-semibold text-neutral-900 dark:text-white uppercase tracking-wider mb-4">
              {{ t("contact.social.title") }}
            </h3>
            <div class="flex gap-4">
              <UButton
                v-for="social in ['facebook', 'instagram', 'whatsapp']"
                :key="social"
                :to="`https://${social}.com`"
                target="_blank"
                color="neutral"
                variant="ghost"
                size="lg"
                :icon="`i-simple-icons-${social}`"
                class="hover:bg-neutral-100 dark:hover:bg-neutral-800"
              />
            </div>
          </div>
        </div>

        <!-- Right Column: Form -->
        <div class="bg-neutral-50 dark:bg-neutral-900/50 rounded-2xl p-8 md:p-10 border border-neutral-200 dark:border-neutral-800">
          <UForm
            :state="state"
            :validate="validate"
            class="space-y-6"
            @submit="onSubmit"
          >
            <UFormField
              :label="t('contact.form.name_label')"
              name="name"
              required
            >
              <UInput
                v-model="state.name"
                :placeholder="t('contact.form.name_placeholder')"
                size="xl"
                class="w-full"
              />
            </UFormField>

            <UFormField
              :label="t('contact.form.email_label')"
              name="email"
              required
            >
              <UInput
                v-model="state.email"
                type="email"
                :placeholder="t('contact.form.email_placeholder')"
                size="xl"
                class="w-full"
              />
            </UFormField>

            <UFormField
              :label="t('contact.form.phone_label')"
              name="phone"
            >
              <UInput
                v-model="state.phone"
                :placeholder="t('contact.form.phone_placeholder')"
                size="xl"
                class="w-full"
              />
            </UFormField>

            <UFormField
              :label="t('contact.form.subject_label')"
              name="subject"
              required
            >
              <UInput
                v-model="state.subject"
                :placeholder="t('contact.form.subject_placeholder')"
                size="xl"
                class="w-full"
              />
            </UFormField>

            <UFormField
              :label="t('contact.form.message_label')"
              name="message"
              required
            >
              <UTextarea
                v-model="state.message"
                :rows="6"
                :placeholder="t('contact.form.message_placeholder')"
                size="xl"
                class="w-full"
              />
            </UFormField>

            <UButton
              type="submit"
              block
              size="xl"
              color="primary"
              class="font-semibold"
            >
              {{ t("contact.form.submit") }}
            </UButton>
          </UForm>
        </div>
      </div>

      <!-- FAQ & Map Section -->
      <div class="mt-24 pt-16 border-t border-neutral-200 dark:border-neutral-800">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-16">
          <!-- FAQ -->
          <div>
            <h2 class="text-2xl font-bold text-neutral-900 dark:text-white mb-8">
              {{ t("contact.faq.title") }}
            </h2>
            <UAccordion
              :items="faqs"
              color="neutral"
              variant="soft"
              size="md"
            />
          </div>

          <!-- Simple Map Link -->
          <div>
            <h2 class="text-2xl font-bold text-neutral-900 dark:text-white mb-8">
              {{ t("contact.map.title") }}
            </h2>
            <div class="bg-neutral-100 dark:bg-neutral-800 rounded-xl overflow-hidden h-[300px] relative group">
              <!-- Static Image for performance/design -->
              <NuxtImg
                src="https://images.unsplash.com/photo-1518558997970-4ddc236affcd?q=80&w=1000&auto=format&fit=crop"
                alt="San Pedro de Atacama"
                class="w-full h-full object-cover filter grayscale hover:grayscale-0 transition-all duration-500"
                format="webp"
                loading="lazy"
                placeholder
              />
              <div class="absolute inset-0 flex items-center justify-center bg-black/20 group-hover:bg-black/10 transition-colors">
                <UButton
                  to="https://maps.google.com/?q=San+Pedro+de+Atacama"
                  target="_blank"
                  color="neutral"
                  variant="solid"
                  icon="i-lucide-map-pin"
                  size="lg"
                >
                  {{ t("contact.map.button") }}
                </UButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
