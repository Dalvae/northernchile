<script setup lang="ts">
import TourCard from "~/components/tour/TourCard.vue";
import type { TourRes } from "api-client";

const { t } = useI18n();
const localePath = useLocalePath();

// 1. SEO Primero
useSeoMeta({
  title: "Northern Chile - Tours Astronómicos en San Pedro de Atacama",
  description:
    "Descubre las estrellas del desierto de Atacama con nuestros tours astronómicos guiados. Experiencias únicas bajo el cielo más claro del mundo.",
  ogTitle: "Northern Chile - Tours Astronómicos en San Pedro de Atacama",
  ogDescription:
    "Descubre las estrellas del desierto de Atacama con nuestros tours astronómicos guiados",
  ogImage: "https://www.northernchile.cl/og-image-homepage.jpg",
  twitterCard: "summary_large_image",
});

// 2. Fetch de datos (ÚNICA LLAMADA)
// Usamos useFetch directo con transform para eficiencia y lazy: true
const { data: featuredTours } = useFetch<TourRes[]>("/api/tours", {
  lazy: true,
  transform: (tours) => {
    return (tours || [])
      .filter((t) => t.status === "PUBLISHED")
      .slice(0, 6)
      .map((tour) => ({
        id: tour.id,
        slug: tour.slug,
        nameTranslations: tour.nameTranslations,
        images: tour.images?.slice(0, 1),
        category: tour.category,
        price: tour.price,
        durationHours: tour.durationHours,
        defaultMaxParticipants: (tour as any).defaultMaxParticipants,
        moonSensitive: tour.moonSensitive,
        windSensitive: tour.windSensitive,
        descriptionTranslations: tour.descriptionTranslations,
      }));
  },
});
</script>

<template>
  <div>
    <HomeHeroSection />

    <LazyHomeExperience />

    <section class="relative py-24 z-10 overflow-hidden">
      <div
        class="absolute inset-0 bg-gradient-to-b from-neutral-950 to-neutral-900 z-0"
      />

      <UContainer class="relative z-10">
        <div class="text-center mb-16">
          <h2
            class="text-4xl md:text-5xl font-display font-bold text-white mb-4 text-glow"
          >
            {{ t("home.featured_tours_title") || "Experiencias Destacadas" }}
          </h2>
          <p class="text-xl text-neutral-200 max-w-2xl mx-auto">
            {{
              t("home.featured_tours_subtitle") ||
              "Descubre lo mejor del desierto de Atacama"
            }}
          </p>
        </div>

        <!-- SKELETON LOADING (Evita saltos y errores si featuredTours es null al inicio) -->
        <div
          v-if="!featuredTours"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
        >
          <div
            v-for="i in 3"
            :key="i"
            class="h-[500px] bg-neutral-900/50 rounded-xl animate-pulse border border-white/5"
          ></div>
        </div>

        <!-- GRID REAL -->
        <div
          v-else
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
        >
          <TourCard
            v-for="(tour, index) in featuredTours"
            :key="tour.id"
            :tour="tour"
            :index="index"
            class="h-full"
          />
        </div>

        <div class="mt-16 text-center">
          <UButton
            :to="localePath('/tours')"
            size="xl"
            variant="ghost"
            color="primary"
            class="group text-lg text-neutral-200 hover:text-white"
            trailing-icon="i-lucide-arrow-right"
          >
            {{ t("home.view_all_tours") || "Ver todos los tours" }}
          </UButton>
        </div>
      </UContainer>
    </section>

    <LazyHomeWhyChooseUs />

    <section class="relative py-20 sm:py-32 z-10">
      <!-- Fondo (Gradient) -->
      <div
        class="absolute inset-0 bg-gradient-to-t from-neutral-950 via-neutral-900 to-neutral-950 opacity-80 z-0"
      />

      <UContainer class="relative z-10 text-center px-6">
        <!-- Títulos -->
        <h2
          class="text-3xl md:text-6xl font-display font-bold text-white mb-4 sm:mb-8 text-glow leading-tight"
        >
          {{ t("home.cta_title") || "Ready to explore the universe?" }}
        </h2>
        <p
          class="text-base md:text-2xl text-neutral-300 mb-8 sm:mb-12 max-w-2xl mx-auto font-light"
        >
          {{
            t("home.cta_subtitle") ||
            "Join us on an unforgettable astronomical adventure"
          }}
        </p>
        <div
          class="flex flex-col sm:flex-row gap-3 sm:gap-6 justify-center items-center"
        >
          <UButton
            :to="localePath('/tours')"
            size="xl"
            color="primary"
            variant="solid"
            class="w-full sm:w-auto max-w-xs justify-center px-6 py-3 sm:px-12 sm:py-4 text-base sm:text-lg font-bold shadow-[0_0_20px_-5px_var(--ui-primary)] hover:scale-105 transition-transform rounded-xl"
          >
            {{ t("home.book_now") }}
          </UButton>

          <UButton
            :to="localePath('/contact')"
            size="xl"
            variant="outline"
            color="white"
            class="w-full sm:w-auto max-w-xs justify-center px-6 py-3 sm:px-12 sm:py-4 text-base sm:text-lg font-medium backdrop-blur-sm hover:bg-white/10 rounded-xl border-white/20"
          >
            {{ t("home.contact_us") }}
          </UButton>
        </div>
      </UContainer>
    </section>
  </div>
</template>

