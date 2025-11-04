<template>
  <div class="min-h-screen bg-neutral-50 dark:bg-neutral-800 py-12">
    <UContainer>
      <!-- Header -->
      <div class="text-center mb-12">
        <div class="inline-block p-4 bg-tertiary/10 rounded-full mb-4">
          <UIcon name="i-lucide-moon" class="w-12 h-12 text-tertiary" />
        </div>
        <h1
          class="text-4xl md:text-5xl font-bold text-neutral-900 dark:text-white mb-4"
        >
          {{ t("moon.calendar_title") }}
        </h1>
        <p
          class="text-lg text-neutral-600 dark:text-neutral-400 max-w-2xl mx-auto"
        >
          {{ t("moon.calendar_description") }}
        </p>
      </div>

      <!-- Month Selector -->
      <div class="flex justify-center gap-4 mb-8">
        <UButton
          icon="i-lucide-chevron-left"
          color="neutral"
          variant="outline"
          @click="previousMonth"
        />

        <div class="flex items-center gap-2">
          <span class="text-xl font-semibold text-neutral-900 dark:text-white">
            {{ currentMonthLabel }}
          </span>
        </div>

        <UButton
          icon="i-lucide-chevron-right"
          color="neutral"
          variant="outline"
          @click="nextMonth"
        />
      </div>

      <!-- Next Full Moons Alert -->
      <UCard class="mb-8 bg-tertiary/5 border-tertiary/20">
        <div class="flex items-start gap-4">
          <div class="p-3 bg-tertiary/10 rounded-lg">
            <UIcon name="i-lucide-moon-star" class="w-6 h-6 text-tertiary" />
          </div>
          <div class="flex-1">
            <h3
              class="text-lg font-semibold text-neutral-900 dark:text-white mb-2"
            >
              {{ t("moon.next_full_moons") }}
            </h3>
            <div
              v-if="nextFullMoons && nextFullMoons.length > 0"
              class="space-y-2"
            >
              <div
                v-for="moon in nextFullMoons"
                :key="moon.date"
                class="flex items-center gap-3 text-sm"
              >
                <span class="text-2xl">{{ moon.icon }}</span>
                <span class="font-medium text-neutral-900 dark:text-white">
                  {{ formatFullDate(moon.date) }}
                </span>
                <UBadge color="tertiary" variant="subtle">
                  {{ moon.illumination }}% {{ t("moon.illumination") }}
                </UBadge>
              </div>
            </div>
          </div>
        </div>
      </UCard>

      <!-- Loading State -->
      <div v-if="pending" class="flex justify-center py-12">
        <UIcon
          name="i-lucide-loader-2"
          class="w-8 h-8 animate-spin text-primary"
        />
      </div>

      <!-- Calendar Grid -->
      <div v-else class="grid grid-cols-7 gap-2">
        <!-- Day Headers -->
        <div
          v-for="day in dayHeaders"
          :key="day"
          class="text-center font-semibold text-neutral-700 dark:text-neutral-300 py-2"
        >
          {{ day }}
        </div>

        <!-- Calendar Days -->
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          :class="[
            'aspect-square rounded-lg border-2 transition-all',
            day.isToday
              ? 'border-primary bg-primary/5'
              : day.isCurrentMonth
              ? 'border-neutral-200 dark:border-neutral-700 bg-white dark:bg-neutral-800 hover:border-tertiary/50 hover:shadow-lg'
              : 'border-transparent bg-neutral-100 dark:bg-neutral-800',
            day.isFullMoon ? 'ring-2 ring-tertiary/50' : '',
          ]"
        >
          <div class="h-full flex flex-col items-center justify-center p-2">
            <span
              :class="[
                'text-sm font-medium mb-1',
                day.isToday
                  ? 'text-primary font-bold'
                  : day.isCurrentMonth
                  ? 'text-neutral-900 dark:text-white'
                  : 'text-neutral-400 dark:text-neutral-600',
              ]"
            >
              {{ day.dayNumber }}
            </span>

            <span class="text-3xl mb-1">{{ day.icon }}</span>

            <span
              :class="[
                'text-xs font-medium',
                day.isFullMoon
                  ? 'text-tertiary'
                  : day.isCurrentMonth
                  ? 'text-neutral-600 dark:text-neutral-400'
                  : 'text-neutral-400 dark:text-neutral-600',
              ]"
            >
              {{ day.illumination }}%
            </span>

            <span
              :class="[
                'text-xs mt-1 text-center leading-tight',
                day.isCurrentMonth
                  ? 'text-neutral-500 dark:text-neutral-500'
                  : 'text-neutral-400 dark:text-neutral-600',
              ]"
            >
              {{ translatePhaseName(day.phaseName) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Legend -->
      <div class="mt-8 bg-white dark:bg-neutral-800 rounded-lg p-6">
        <h3 class="text-lg font-semibold text-neutral-900 dark:text-white mb-4">
          {{ t("moon.legend") }}
        </h3>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="flex items-center gap-2">
            <span class="text-2xl">🌑</span>
            <span class="text-sm text-neutral-600 dark:text-neutral-400">
              {{ t("moon.phases.new_moon") }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-2xl">🌓</span>
            <span class="text-sm text-neutral-600 dark:text-neutral-400">
              {{ t("moon.phases.first_quarter") }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-2xl">🌕</span>
            <span class="text-sm text-neutral-600 dark:text-neutral-400">
              {{ t("moon.phases.full_moon") }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-2xl">🌗</span>
            <span class="text-sm text-neutral-600 dark:text-neutral-400">
              {{ t("moon.phases.last_quarter") }}
            </span>
          </div>
        </div>
      </div>

      <!-- Info Section -->
      <div
        class="mt-8 bg-gradient-to-br from-tertiary/5 to-primary/5 rounded-lg p-6 border border-tertiary/20"
      >
        <div class="flex items-start gap-4">
          <UIcon
            name="i-lucide-info"
            class="w-6 h-6 text-tertiary flex-shrink-0 mt-1"
          />
          <div>
            <h3
              class="text-lg font-semibold text-neutral-900 dark:text-white mb-2"
            >
              {{ t("moon.why_matters_title") }}
            </h3>
            <p class="text-neutral-600 dark:text-neutral-400">
              {{ t("moon.why_matters_description") }}
            </p>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>

<script setup lang="ts">
const { t, locale } = useI18n();
const config = useRuntimeConfig();

definePageMeta({
  layout: "default",
});

// State
const currentDate = ref(new Date());
const pending = ref(false);
const moonData = ref<any[]>([]);
const nextFullMoons = ref<any[]>([]);

// Computed
const currentMonthLabel = computed(() => {
  return currentDate.value.toLocaleDateString(locale.value, {
    month: "long",
    year: "numeric",
  });
});

const dayHeaders = computed(() => {
  const days = [];
  const baseDate = new Date(2025, 0, 5); // A Sunday
  for (let i = 0; i < 7; i++) {
    const date = new Date(baseDate);
    date.setDate(baseDate.getDate() + i);
    days.push(date.toLocaleDateString(locale.value, { weekday: "short" }));
  }
  return days;
});

const calendarDays = computed(() => {
  if (!moonData.value || moonData.value.length === 0) return [];

  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth();

  // First day of month
  const firstDay = new Date(year, month, 1);
  const startDayOfWeek = firstDay.getDay(); // 0 = Sunday

  // Last day of month
  const lastDay = new Date(year, month + 1, 0);
  const daysInMonth = lastDay.getDate();

  // Days from previous month
  const prevMonthDays = startDayOfWeek;
  const prevMonthLastDay = new Date(year, month, 0).getDate();

  const days = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // Previous month days
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i;
    const date = new Date(year, month - 1, day);
    const dateStr = date.toISOString().split("T")[0];
    const moonDay = moonData.value.find((d) => d.date === dateStr);

    days.push({
      dayNumber: day,
      isCurrentMonth: false,
      isToday: false,
      ...moonDay,
    });
  }

  // Current month days
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(year, month, i);
    const dateStr = date.toISOString().split("T")[0];
    const moonDay = moonData.value.find((d) => d.date === dateStr);
    const isToday = date.getTime() === today.getTime();

    days.push({
      dayNumber: i,
      isCurrentMonth: true,
      isToday,
      ...moonDay,
    });
  }

  // Next month days to fill grid
  const remainingDays = 42 - days.length; // 6 rows * 7 days
  for (let i = 1; i <= remainingDays; i++) {
    const date = new Date(year, month + 1, i);
    const dateStr = date.toISOString().split("T")[0];
    const moonDay = moonData.value.find((d) => d.date === dateStr);

    days.push({
      dayNumber: i,
      isCurrentMonth: false,
      isToday: false,
      ...moonDay,
    });
  }

  return days;
});

// Methods
const fetchMoonData = async () => {
  pending.value = true;
  try {
    const year = currentDate.value.getFullYear();
    const month = currentDate.value.getMonth();

    // Get data for previous month, current month, and next month
    const prevMonth = new Date(year, month - 1, 1);
    const nextMonth = new Date(year, month + 2, 0);

    const startDate = prevMonth.toISOString().split("T")[0];
    const endDate = nextMonth.toISOString().split("T")[0];

    const response = await $fetch(
      `${config.public.apiBaseUrl}/lunar/calendar`,
      {
        params: { startDate, endDate },
      }
    );

    moonData.value = response;
  } catch (error) {
    console.error("Error fetching moon data:", error);
  } finally {
    pending.value = false;
  }
};

const fetchNextFullMoons = async () => {
  try {
    const response = await $fetch(
      `${config.public.apiBaseUrl}/lunar/next-full-moons`,
      {
        params: { count: 3 },
      }
    );
    nextFullMoons.value = response;
  } catch (error) {
    console.error("Error fetching next full moons:", error);
  }
};

const previousMonth = () => {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() - 1,
    1
  );
  fetchMoonData();
};

const nextMonth = () => {
  currentDate.value = new Date(
    currentDate.value.getFullYear(),
    currentDate.value.getMonth() + 1,
    1
  );
  fetchMoonData();
};

const formatFullDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString(locale.value, {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

const translatePhaseName = (phaseName: string) => {
  const phaseMap: Record<string, string> = {
    "New Moon": t("moon.phases.new_moon"),
    "Waxing Crescent": t("moon.phases.waxing_crescent"),
    "First Quarter": t("moon.phases.first_quarter"),
    "Waxing Gibbous": t("moon.phases.waxing_gibbous"),
    "Full Moon": t("moon.phases.full_moon"),
    "Waning Gibbous": t("moon.phases.waning_gibbous"),
    "Last Quarter": t("moon.phases.last_quarter"),
    "Waning Crescent": t("moon.phases.waning_crescent"),
  };
  return phaseMap[phaseName] || phaseName;
};

// Initialize
onMounted(() => {
  fetchMoonData();
  fetchNextFullMoons();
});
</script>
