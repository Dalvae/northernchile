<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({
  title: t('about.seo.title'),
  description: t('about.seo.description'),
  ogTitle: t('about.seo.og_title'),
  ogDescription: t('about.seo.og_description'),
  twitterCard: 'summary_large_image'
})

const teamMembers = [
  {
    name: 'Alex',
    role: t('about.team.alex.role'),
    bio: t('about.team.alex.bio'),
    image: 'https://ui-avatars.com/api/?name=Alex&background=10b981&color=fff',
    credentials: [
      { icon: 'i-lucide-award', text: t('about.team.alex.credentials.award') },
      {
        icon: 'i-lucide-mic',
        text: t('about.team.alex.credentials.voice_over')
      },
      {
        icon: 'i-lucide-building',
        text: t('about.team.alex.credentials.collaborator')
      }
    ]
  },
  {
    name: 'David Gallardo',
    role: t('about.team.david.role'),
    bio: t('about.team.david.bio'),
    image:
      'https://ui-avatars.com/api/?name=David+Gallardo&background=10b981&color=fff',
    credentials: [
      {
        icon: 'i-lucide-camera',
        text: t('about.team.david.credentials.photographer')
      },
      {
        icon: 'i-lucide-palette',
        text: t('about.team.david.credentials.art_teacher')
      },
      {
        icon: 'i-lucide-star',
        text: t('about.team.david.credentials.tour_guide')
      }
    ]
  }
]

const values = [
  {
    icon: 'i-lucide-sparkles',
    title: t('about.values.experience_title'),
    description: t('about.values.experience_description')
  },
  {
    icon: 'i-lucide-telescope',
    title: t('about.values.equipment_title'),
    description: t('about.values.equipment_description')
  },
  {
    icon: 'i-lucide-award',
    title: t('about.values.quality_title'),
    description: t('about.values.quality_description')
  },
  {
    icon: 'i-lucide-mountain',
    title: t('about.values.auditorium_title'),
    description: t('about.values.auditorium_description')
  }
]
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
            {{ t("about.title") }}
          </h1>
          <p class="text-xl text-neutral-300">
            {{ t("about.subtitle") }}
          </p>
        </div>
      </UContainer>
    </section>

    <!-- Nuestra Historia -->
    <section class="py-16 bg-white dark:bg-neutral-800">
      <UContainer>
        <div class="max-w-4xl mx-auto">
          <h2
            class="text-3xl font-bold text-neutral-900 dark:text-white mb-8 text-center"
          >
            {{ t("about.historyTitle") }}
          </h2>
          <div class="prose prose-lg dark:prose-invert max-w-none">
            <p
              class="text-neutral-600 dark:text-neutral-400 leading-relaxed mb-6"
            >
              {{ t("about.historyP1") }}
            </p>
            <p
              class="text-neutral-600 dark:text-neutral-400 leading-relaxed mb-6"
            >
              <span v-html="t('about.historyP2')" />
            </p>
            <p
              class="text-neutral-600 dark:text-neutral-400 leading-relaxed mb-6"
            >
              {{ t("about.historyP3") }}
            </p>
            <p class="text-neutral-600 dark:text-neutral-400 leading-relaxed">
              {{ t("about.historyP4") }}
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestros Valores -->
    <section class="py-16 bg-neutral-50 dark:bg-neutral-800/50">
      <UContainer>
        <h2
          class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center"
        >
          {{ t("about.valuesTitle") }}
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div
            v-for="value in values"
            :key="value.title"
            class="text-center"
          >
            <div
              class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-100 dark:bg-primary-900/30 mb-4"
            >
              <UIcon
                :name="value.icon"
                class="w-8 h-8 text-primary-600 dark:text-primary-400"
              />
            </div>
            <h3
              class="text-xl font-semibold text-neutral-900 dark:text-white mb-2"
            >
              {{ value.title }}
            </h3>
            <p class="text-neutral-600 dark:text-neutral-400">
              {{ value.description }}
            </p>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- Nuestro Equipo -->
    <section class="py-16 bg-white dark:bg-neutral-800">
      <UContainer>
        <h2
          class="text-3xl font-bold text-neutral-900 dark:text-white mb-12 text-center"
        >
          {{ t("about.teamTitle") }}
        </h2>
        <div class="max-w-5xl mx-auto space-y-12">
          <div
            v-for="member in teamMembers"
            :key="member.name"
            class="flex flex-col md:flex-row items-center gap-8"
          >
            <img
              :src="member.image"
              :alt="member.name"
              class="w-40 h-40 rounded-full flex-shrink-0"
            >
            <div class="flex-1 text-center md:text-left">
              <h3
                class="text-2xl font-semibold text-neutral-900 dark:text-white mb-2"
              >
                {{ member.name }}
              </h3>
              <p
                class="text-primary-600 dark:text-primary-400 font-medium text-lg mb-4"
              >
                {{ member.role }}
              </p>
              <p class="text-neutral-600 dark:text-neutral-400 mb-4">
                {{ member.bio }}
              </p>
              <div
                class="space-y-2 text-sm text-neutral-600 dark:text-neutral-400"
              >
                <p
                  v-for="credential in member.credentials"
                  :key="credential.text"
                  class="flex items-center gap-2 justify-center md:justify-start"
                >
                  <UIcon
                    :name="credential.icon"
                    class="w-4 h-4 text-primary-600"
                  />
                  {{ credential.text }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </UContainer>
    </section>

    <!-- CTA Final -->
    <section
      class="py-16 bg-gradient-to-r from-primary-600 to-primary-700 text-white"
    >
      <UContainer>
        <div class="max-w-3xl mx-auto text-center">
          <h2 class="text-3xl font-bold mb-4">
            {{ t("about.cta.title") }}
          </h2>
          <p class="text-xl mb-8 text-primary-100">
            {{ t("about.cta.subtitle") }}
          </p>
          <UButton
            to="/tours"
            size="xl"
            color="neutral"
            variant="solid"
            icon="i-lucide-telescope"
          >
            {{ t("about.cta.button") }}
          </UButton>
        </div>
      </UContainer>
    </section>
  </div>
</template>
