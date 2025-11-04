<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import type { TourRes } from "~/lib/api-client";

const route = useRoute();
const router = useRouter();
const { locale } = useI18n();
const toast = useToast();
const cartStore = useCartStore();
const authStore = useAuthStore();

const tourSlug = route.params.slug as string;

const tour = ref<TourRes | null>(null);
const schedules = ref<any[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);

// Selected date and schedule
const selectedDate = ref<Date | null>(null);
const selectedSchedule = ref<any | null>(null);
const participantCount = ref(1);
const showParticipantModal = ref(false);

const translatedName = computed(
  () =>
    tour.value?.nameTranslations?.[locale.value] ||
    tour.value?.nameTranslations?.["es"] ||
    ""
);

// Fetch tour data
async function fetchTour() {
  try {
    const response = await $fetch(`/api/tours/slug/${tourSlug}`);
    tour.value = response as TourRes;
  } catch (e: any) {
    error.value = t("schedule.error_loading_tour");
    console.error("Failed to fetch tour", e);
  }
}

// Fetch schedules for the tour
async function fetchSchedules() {
  if (!tour.value?.id) return;

  try {
    // Fetch schedules for next 90 days
    const start = new Date();
    const end = new Date();
    end.setDate(end.getDate() + 90);

    const formatDate = (date: Date) => date.toISOString().split("T")[0];

    console.log("Fetching schedules for tour:", tour.value.id);
    console.log("Date range:", formatDate(start), "to", formatDate(end));

    const response = await $fetch(`/api/tours/${tour.value.id}/schedules`, {
      params: {
        start: formatDate(start),
        end: formatDate(end),
      },
    });

    schedules.value = response as any[];
    console.log("Schedules received:", schedules.value.length, schedules.value);
    console.log("Schedules by date:", schedulesByDate.value);
  } catch (e: any) {
    console.error("Failed to fetch schedules", e);
    schedules.value = [];
  }
}

// Group schedules by date
const schedulesByDate = computed(() => {
  const grouped: Record<string, any[]> = {};

  schedules.value.forEach((schedule) => {
    const date = new Date(schedule.startDatetime).toISOString().split("T")[0];
    if (!grouped[date]) {
      grouped[date] = [];
    }
    grouped[date].push(schedule);
  });

  return grouped;
});

// Available dates (dates with at least one schedule)
const availableDates = computed(() => {
  return Object.keys(schedulesByDate.value).map((dateStr) => new Date(dateStr));
});

// Schedules for selected date
const schedulesForSelectedDate = computed(() => {
  if (!selectedDate.value) return [];

  const dateStr = selectedDate.value.toISOString().split("T")[0];
  return schedulesByDate.value[dateStr] || [];
});

// Handle date selection
function selectDate(date: Date) {
  console.log("Date selected:", date);
  selectedDate.value = date;
  selectedSchedule.value = null;
  console.log("Schedules for this date:", schedulesForSelectedDate.value);
}

// Handle schedule selection
function selectSchedule(schedule: any) {
  console.log("Schedule clicked:", schedule);
  selectedSchedule.value = schedule;
  showParticipantModal.value = true;
  console.log(
    "Modal should open now, showParticipantModal:",
    showParticipantModal.value
  );
}

// Add to cart
async function addToCart() {
  if (!selectedSchedule.value || !tour.value) return;

  const cartItem = {
    tourScheduleId: selectedSchedule.value.id,
    tourId: tour.value.id,
    tourName: translatedName.value,
    tourSlug: tour.value.slug,
    startDatetime: selectedSchedule.value.startDatetime,
    price: tour.value.price,
    participantCount: participantCount.value,
    durationHours: tour.value.durationHours,
  };

  const success = await cartStore.addItem(cartItem);

  if (success) {
    toast.add({
      color: "success",
      title: t("schedule.cart.added_title"),
      description: t("schedule.cart.added_description", {
        count: participantCount.value,
        tourName: translatedName.value,
      }),
    });

    showParticipantModal.value = false;

    // Redirect to cart
    router.push("/cart");
  }
}

// Format date
function formatDate(date: Date) {
  return new Intl.DateTimeFormat(locale.value, {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  }).format(date);
}

// Format time
function formatTime(datetime: string) {
  return new Intl.DateTimeFormat(locale.value, {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(datetime));
}

// Initialize
onMounted(async () => {
  loading.value = true;
  await fetchTour();
  await fetchSchedules();
  loading.value = false;
});
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800">
    <UContainer class="py-8">
      <!-- Header -->
      <div class="mb-8">
        <UButton
          :to="`/tours/${tourSlug}`"
          color="neutral"
          variant="ghost"
          icon="i-lucide-arrow-left"
          class="mb-4"
        >
          {{ t("schedule.back_to_tour") }}
        </UButton>

        <h1 class="text-4xl font-bold text-neutral-900 dark:text-white mb-2">
          {{ t("schedule.select_date_title") }}
        </h1>
        <p class="text-lg text-neutral-600 dark:text-neutral-400">
          {{ translatedName }}
        </p>
      </div>

      <div v-if="loading" class="text-center py-12">
        <UIcon
          name="i-lucide-loader-2"
          class="w-8 h-8 animate-spin text-primary mx-auto"
        />
        <p class="mt-4 text-neutral-600 dark:text-neutral-400">
          {{ t("schedule.loading_schedules") }}
        </p>
      </div>

      <div v-else-if="error" class="text-center py-12">
        <UAlert color="error" :title="error" />
      </div>

      <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Calendar Column -->
        <div class="lg:col-span-2">
          <UCard>
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ t("schedule.calendar_availability_title") }}
              </h2>
            </template>

            <!-- Simple calendar (we'll enhance this) -->
            <div class="space-y-4">
              <div class="grid grid-cols-7 gap-2">
                <div
                  v-for="day in [
                    t('common.days.sunday_short'),
                    t('common.days.monday_short'),
                    t('common.days.tuesday_short'),
                    t('common.days.wednesday_short'),
                    t('common.days.thursday_short'),
                    t('common.days.friday_short'),
                    t('common.days.saturday_short'),
                  ]"
                  :key="day"
                  class="text-center text-sm font-medium text-neutral-500 dark:text-neutral-400"
                >
                  {{ day }}
                </div>
              </div>

              <!-- TODO: Implement proper calendar grid -->
              <div class="space-y-2">
                <div
                  v-for="(dateSchedules, dateStr) in schedulesByDate"
                  :key="dateStr"
                  class="p-4 rounded-lg border cursor-pointer transition-colors"
                  :class="[
                    selectedDate &&
                    selectedDate.toISOString().split('T')[0] === dateStr
                      ? 'border-primary bg-primary/10'
                      : 'border-neutral-200 dark:border-neutral-800 hover:border-primary/50',
                  ]"
                  @click="selectDate(new Date(dateStr))"
                >
                  <div class="flex justify-between items-center">
                    <span class="font-medium text-neutral-900 dark:text-white">
                      {{ formatDate(new Date(dateStr)) }}
                    </span>
                    <UBadge color="primary" variant="subtle">
                      {{ dateSchedules.length }}
                      {{
                        t("schedule.schedules_count", {
                          count: dateSchedules.length,
                        })
                      }}
                    </UBadge>
                  </div>
                </div>
              </div>

              <div v-if="availableDates.length === 0" class="text-center py-8">
                <UIcon
                  name="i-lucide-calendar-x"
                  class="w-12 h-12 text-neutral-400 mx-auto mb-4"
                />
                <p class="text-neutral-600 dark:text-neutral-400">
                  {{ t("schedule.no_schedules_available") }}
                </p>
              </div>
            </div>
          </UCard>
        </div>

        <!-- Schedule Selection Column -->
        <div class="lg:col-span-1">
          <div class="sticky top-4">
            <UCard v-if="selectedDate">
              <template #header>
                <h3
                  class="text-lg font-semibold text-neutral-900 dark:text-white"
                >
                  {{ t("schedule.available_schedules_title") }}
                </h3>
                <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
                  {{ formatDate(selectedDate) }}
                </p>
              </template>

              <div class="space-y-3">
                <div
                  v-for="schedule in schedulesForSelectedDate"
                  :key="schedule.id"
                  class="p-4 rounded-lg border border-neutral-200 dark:border-neutral-800 hover:border-primary transition-colors cursor-pointer"
                  @click="selectSchedule(schedule)"
                >
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <UIcon
                        name="i-lucide-clock"
                        class="w-4 h-4 text-primary"
                      />
                      <span
                        class="font-medium text-neutral-900 dark:text-white"
                      >
                        {{ formatTime(schedule.startDatetime) }}
                      </span>
                    </div>
                    <UBadge
                      :color="
                        schedule.status === 'OPEN' ? 'success' : 'warning'
                      "
                      size="sm"
                    >
                      {{ schedule.status }}
                    </UBadge>
                  </div>

                  <div
                    class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400"
                  >
                    <UIcon name="i-lucide-users" class="w-4 h-4" />
                    <span>{{
                      t("schedule.available_spots", {
                        count: schedule.maxParticipants,
                      })
                    }}</span>
                  </div>
                </div>
              </div>
            </UCard>

            <UCard v-else>
              <div class="text-center py-8">
                <UIcon
                  name="i-lucide-calendar"
                  class="w-12 h-12 text-neutral-400 mx-auto mb-4"
                />
                <p class="text-neutral-600 dark:text-neutral-400">
                  {{ t("schedule.select_date_prompt") }}
                </p>
              </div>
            </UCard>
          </div>
        </div>
      </div>

      <!-- Participant Count Modal -->
      <UModal v-model:open="showParticipantModal">
        <template #content>
          <div class="p-6">
            <div class="flex justify-between items-center mb-6">
              <h3
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                {{ t("schedule.participant_modal.title") }}
              </h3>
              <UButton
                icon="i-lucide-x"
                color="neutral"
                variant="ghost"
                @click="showParticipantModal = false"
              />
            </div>

            <div class="space-y-6">
              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-2"
                >
                  {{ t("schedule.participant_modal.participant_count_label") }}
                </label>
                <div class="flex items-center gap-4">
                  <UButton
                    icon="i-lucide-minus"
                    color="neutral"
                    :disabled="participantCount <= 1"
                    @click="participantCount--"
                  />
                  <span
                    class="text-2xl font-bold text-neutral-900 dark:text-white w-12 text-center"
                  >
                    {{ participantCount }}
                  </span>
                  <UButton
                    icon="i-lucide-plus"
                    color="neutral"
                    :disabled="
                      participantCount >=
                      (selectedSchedule?.maxParticipants || 15)
                    "
                    @click="participantCount++"
                  />
                </div>
              </div>

              <div class="p-4 bg-neutral-100 dark:bg-neutral-800 rounded-lg">
                <div class="flex justify-between items-center mb-2">
                  <span class="text-neutral-600 dark:text-neutral-400">{{
                    t("schedule.participant_modal.price_per_person")
                  }}</span>
                  <span class="font-medium text-neutral-900 dark:text-white"
                    >${{ tour?.price }}</span
                  >
                </div>
                <div
                  class="flex justify-between items-center text-lg font-bold border-t border-neutral-200 dark:border-neutral-700 pt-2 mt-2"
                >
                  <span class="text-neutral-900 dark:text-white">{{
                    t("common.total")
                  }}</span>
                  <span class="text-primary"
                    >${{ (tour?.price || 0) * participantCount }}</span
                  >
                </div>
              </div>

              <UButton
                color="primary"
                size="lg"
                block
                icon="i-lucide-shopping-cart"
                @click="addToCart"
              >
                {{ t("schedule.participant_modal.add_to_cart_button") }}
              </UButton>
            </div>
          </div>
        </template>
      </UModal>
    </UContainer>
  </div>
</template>
