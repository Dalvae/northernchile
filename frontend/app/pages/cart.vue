<script setup lang="ts">
const cartStore = useCartStore();
const authStore = useAuthStore();
const router = useRouter();
const { locale } = useI18n();

useSeoMeta({
  title: "Carrito - Northern Chile",
  description: "Revisa tu carrito de compras y procede al pago de tus tours",
  robots: "noindex, nofollow",
});

const isEmpty = computed(() => cartStore.cart.items.length === 0);

// Calculations
const subtotal = computed(() => cartStore.cart.cartTotal);
const tax = computed(() => subtotal.value * 0.19); // IVA 19%
const total = computed(() => subtotal.value + tax.value);

// Actions
async function removeItem(itemId: string) {
  await cartStore.removeItem(itemId);
}

function proceedToCheckout() {
  if (isEmpty.value) return;
  router.push("/checkout");
}

// Format helpers
function formatCurrency(amount: number) {
  return new Intl.NumberFormat("es-CL", {
    style: "currency",
    currency: "CLP",
  }).format(amount);
}

function formatDate(datetime: string) {
  const date = new Date(datetime);
  return new Intl.DateTimeFormat(locale, {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800 py-12">
    <UContainer>
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          Carrito de Compras
        </h1>
        <p class="text-neutral-600 dark:text-neutral-400">
          {{
            isEmpty
              ? "Tu carrito está vacío"
              : `${cartStore.totalItems} ${
                  cartStore.totalItems === 1 ? "persona" : "personas"
                }`
          }}
        </p>
      </div>

      <!-- Empty cart -->
      <div v-if="isEmpty" class="text-center py-16">
        <UIcon
          name="i-lucide-shopping-cart"
          class="w-20 h-20 text-neutral-300 dark:text-neutral-600 mx-auto mb-6"
        />
        <h2
          class="text-2xl font-semibold text-neutral-900 dark:text-white mb-4"
        >
          Tu carrito está vacío
        </h2>
        <p class="text-neutral-600 dark:text-neutral-400 mb-6">
          No has agregado ningún tour a tu carrito aún
        </p>
        <UButton
          to="/tours"
          size="lg"
          color="primary"
          icon="i-lucide-telescope"
        >
          Explorar Tours
        </UButton>
      </div>

      <!-- Cart with items -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Cart Items -->
        <div class="lg:col-span-2 space-y-4">
          <UCard v-for="item in cartStore.cart.items" :key="item.itemId">
            <div class="flex gap-6">
              <!-- Tour Info -->
              <div class="flex-1">
                <h3
                  class="text-lg font-semibold text-neutral-900 dark:text-white mb-2"
                >
                  {{ item.tourName }}
                </h3>
                <div
                  class="space-y-1 text-sm text-neutral-600 dark:text-neutral-400"
                >
                  <p v-if="item.startDatetime" class="flex items-center gap-2">
                    <UIcon name="i-lucide-calendar" class="w-4 h-4" />
                    {{ formatDate(item.startDatetime) }}
                  </p>
                  <p v-if="item.durationHours" class="flex items-center gap-2">
                    <UIcon name="i-lucide-clock" class="w-4 h-4" />
                    {{ item.durationHours }} horas
                  </p>
                  <p class="flex items-center gap-2">
                    <UIcon name="i-lucide-user" class="w-4 h-4" />
                    {{ formatCurrency(item.pricePerParticipant) }} por persona
                  </p>
                  <p class="flex items-center gap-2">
                    <UIcon name="i-lucide-users" class="w-4 h-4" />
                    {{ item.numParticipants }}
                    {{ item.numParticipants === 1 ? "persona" : "personas" }}
                  </p>
                </div>
              </div>

              <!-- Price & Remove -->
              <div class="flex flex-col items-end justify-between">
                <p class="text-xl font-bold text-neutral-900 dark:text-white">
                  {{ formatCurrency(item.itemTotal) }}
                </p>
                <UButton
                  icon="i-lucide-trash-2"
                  color="error"
                  variant="soft"
                  size="sm"
                  @click="removeItem(item.itemId)"
                >
                  Eliminar
                </UButton>
              </div>
            </div>
          </UCard>
        </div>

        <!-- Order Summary -->
        <div class="lg:col-span-1">
          <UCard>
            <h3
              class="text-xl font-semibold text-neutral-900 dark:text-white mb-6"
            >
              Resumen del Pedido
            </h3>

            <div class="space-y-4 mb-6">
              <div
                class="flex justify-between text-neutral-600 dark:text-neutral-400"
              >
                <span>Subtotal</span>
                <span class="font-semibold">{{
                  formatCurrency(subtotal)
                }}</span>
              </div>
              <div
                class="flex justify-between text-neutral-600 dark:text-neutral-400"
              >
                <span>IVA (19%)</span>
                <span class="font-semibold">{{ formatCurrency(tax) }}</span>
              </div>
              <UDivider />
              <div
                class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white"
              >
                <span>Total</span>
                <span>{{ formatCurrency(total) }}</span>
              </div>
            </div>

            <UButton
              block
              size="lg"
              color="primary"
              icon="i-lucide-arrow-right"
              trailing
              @click="proceedToCheckout"
            >
              Proceder al Pago
            </UButton>

            <UButton
              block
              variant="soft"
              color="neutral"
              class="mt-3"
              icon="i-lucide-arrow-left"
              to="/tours"
            >
              Seguir Comprando
            </UButton>

            <!-- Security badges -->
            <div
              class="mt-6 pt-6 border-t border-neutral-200 dark:border-neutral-700"
            >
              <div
                class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400 mb-2"
              >
                <UIcon
                  name="i-lucide-shield-check"
                  class="w-5 h-5 text-success"
                />
                <span>Pago seguro y encriptado</span>
              </div>
              <div
                class="flex items-center gap-2 text-sm text-neutral-600 dark:text-neutral-400"
              >
                <UIcon name="i-lucide-rotate-ccw" class="w-5 h-5 text-info" />
                <span>Cancelación gratis hasta 24h antes</span>
              </div>
            </div>
          </UCard>
        </div>
      </div>
    </UContainer>
  </div>
</template>
