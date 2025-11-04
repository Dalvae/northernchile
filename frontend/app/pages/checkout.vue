<script setup lang="ts">
const cartStore = useCartStore();
const authStore = useAuthStore();
const router = useRouter();
const toast = useToast();
const { locale } = useI18n();

// Redirect if cart is empty
if (cartStore.cart.items.length === 0) {
  router.push("/cart");
}

// Wizard steps
const currentStep = ref(1);
const totalSteps = 3;

// Step 1: Contact Information
const contactForm = ref({
  email: authStore.user?.email || "",
  fullName: authStore.user?.fullName || "",
  phone: "",
  countryCode: "+56",
});

// Step 2: Participants
const participants = ref<
  Array<{
    fullName: string;
    email: string;
    phone: string;
    nationality: string;
    documentType: string;
    documentNumber: string;
  }>
>([]);

// Initialize participants based on total count
const totalParticipants = computed(() => cartStore.totalItems);

function initializeParticipants() {
  participants.value = Array.from(
    { length: totalParticipants.value },
    (_, i) => ({
      fullName: i === 0 ? contactForm.value.fullName : "",
      email: i === 0 ? contactForm.value.email : "",
      phone: i === 0 ? contactForm.value.phone : "",
      nationality: "CL",
      documentType: "RUT",
      documentNumber: "",
    })
  );
}

// Step 3: Payment
const paymentMethod = ref("credit_card");

// Validation
const step1Valid = computed(() => {
  return (
    contactForm.value.email &&
    contactForm.value.fullName &&
    contactForm.value.phone.length >= 8
  );
});

const step2Valid = computed(() => {
  return participants.value.every(
    (p) => p.fullName && p.email && p.documentNumber
  );
});

// Navigation
function nextStep() {
  if (currentStep.value === 1 && !step1Valid.value) {
    toast.add({
      color: "warning",
      title: "Información incompleta",
      description: "Por favor completa todos los campos requeridos",
    });
    return;
  }

  if (currentStep.value === 1) {
    initializeParticipants();
  }

  if (currentStep.value === 2 && !step2Valid.value) {
    toast.add({
      color: "warning",
      title: "Información incompleta",
      description:
        "Por favor completa la información de todos los participantes",
    });
    return;
  }

  if (currentStep.value < totalSteps) {
    currentStep.value++;
  }
}

function prevStep() {
  if (currentStep.value > 1) {
    currentStep.value--;
  }
}

// Clone contact info to first participant
function cloneContactToParticipant() {
  if (participants.value.length > 0) {
    participants.value[0].fullName = contactForm.value.fullName;
    participants.value[0].email = contactForm.value.email;
    participants.value[0].phone = contactForm.value.phone;
  }
}

// Submit booking
async function submitBooking() {
  // For now, simulate payment and redirect to success
  toast.add({
    color: "success",
    title: "Procesando pago...",
    description: "Redirigiendo a la pasarela de pago",
  });

  // Simulate payment delay
  setTimeout(() => {
    // Create booking record
    const booking = {
      id: `NCH-${Date.now().toString().slice(-8)}`,
      bookingDate: new Date().toISOString(),
      status: "CONFIRMED",
      contact: contactForm.value,
      participants: participants.value,
      items: cartStore.cart.items,
      subtotal: subtotal.value,
      tax: tax.value,
      total: total.value,
      paymentMethod: paymentMethod.value,
    };

    // Save to localStorage
    const existingBookings = JSON.parse(
      localStorage.getItem("local_bookings") || "[]"
    );
    existingBookings.push(booking);
    localStorage.setItem("local_bookings", JSON.stringify(existingBookings));

    // Clear cart
    cartStore.clearCart();

    // Redirect to confirmation with booking ID
    router.push(`/payment/callback?status=success&bookingId=${booking.id}`);
  }, 1500);
}

// Format currency
function formatCurrency(amount: number) {
  return new Intl.NumberFormat("es-CL", {
    style: "currency",
    currency: "CLP",
  }).format(amount);
}

// Calculate totals
const subtotal = computed(() => cartStore.cart.cartTotal);
const tax = computed(() => subtotal.value * 0.19);
const total = computed(() => subtotal.value + tax.value);
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-800 py-8">
    <UContainer>
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
          Finalizar Compra
        </h1>
        <p class="text-neutral-600 dark:text-neutral-400">
          Completa tu reserva en {{ totalSteps }} simples pasos
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main Content -->
        <div class="lg:col-span-2">
          <!-- Progress Steps -->
          <div class="mb-8">
            <div class="flex items-center justify-between mb-4">
              <div
                v-for="step in totalSteps"
                :key="step"
                class="flex items-center"
                :class="{ 'flex-1': step < totalSteps }"
              >
                <div
                  class="flex items-center justify-center w-10 h-10 rounded-full font-semibold transition-colors"
                  :class="[
                    currentStep >= step
                      ? 'bg-primary text-white'
                      : 'bg-neutral-200 dark:bg-neutral-700 text-neutral-600 dark:text-neutral-400',
                  ]"
                >
                  {{ step }}
                </div>
                <div
                  v-if="step < totalSteps"
                  class="flex-1 h-1 mx-2"
                  :class="[
                    currentStep > step
                      ? 'bg-primary'
                      : 'bg-neutral-200 dark:bg-neutral-700',
                  ]"
                />
              </div>
            </div>
            <div class="flex justify-between text-sm">
              <span
                :class="
                  currentStep >= 1
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-400'
                "
              >
                Contacto
              </span>
              <span
                :class="
                  currentStep >= 2
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-400'
                "
              >
                Participantes
              </span>
              <span
                :class="
                  currentStep >= 3
                    ? 'text-neutral-900 dark:text-white font-medium'
                    : 'text-neutral-500 dark:text-neutral-400'
                "
              >
                Pago
              </span>
            </div>
          </div>

          <!-- Step 1: Contact Information -->
          <UCard v-if="currentStep === 1">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                Información de Contacto
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
                Usaremos esta información para enviarte la confirmación
              </p>
            </template>

            <div class="space-y-4">
              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
                >
                  Nombre Completo *
                </label>
                <input
                  v-model="contactForm.fullName"
                  type="text"
                  placeholder="Juan Pérez"
                  class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
                >
                  Email *
                </label>
                <input
                  v-model="contactForm.email"
                  type="email"
                  placeholder="juan@example.com"
                  class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label
                  class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
                >
                  Teléfono *
                </label>
                <div class="flex gap-2">
                  <select
                    v-model="contactForm.countryCode"
                    class="w-24 px-3 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                  >
                    <option value="+56">+56</option>
                    <option value="+55">+55</option>
                    <option value="+1">+1</option>
                    <option value="+44">+44</option>
                  </select>
                  <input
                    v-model="contactForm.phone"
                    type="tel"
                    placeholder="912345678"
                    class="flex-1 px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                    required
                  />
                </div>
              </div>
            </div>

            <template #footer>
              <div class="flex justify-between">
                <UButton
                  to="/cart"
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-left"
                >
                  Volver al Carrito
                </UButton>
                <UButton
                  color="primary"
                  icon="i-lucide-arrow-right"
                  trailing
                  @click="nextStep"
                  :disabled="!step1Valid"
                >
                  Continuar
                </UButton>
              </div>
            </template>
          </UCard>

          <!-- Step 2: Participants -->
          <UCard v-if="currentStep === 2">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                Datos de Participantes
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
                {{ totalParticipants }}
                {{ totalParticipants === 1 ? "persona" : "personas" }} en total
              </p>
            </template>

            <div class="space-y-6">
              <div
                v-for="(participant, index) in participants"
                :key="index"
                class="p-4 rounded-lg border border-neutral-200 dark:border-neutral-700"
              >
                <h3 class="font-medium text-neutral-900 dark:text-white mb-4">
                  Participante {{ index + 1 }}
                  <span
                    v-if="index === 0"
                    class="text-sm text-neutral-500 dark:text-neutral-400 font-normal ml-2"
                  >
                    (Contacto principal)
                  </span>
                </h3>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
                    >
                      Nombre Completo *
                    </label>
                    <input
                      v-model="participant.fullName"
                      type="text"
                      class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
                    >
                      Email *
                    </label>
                    <input
                      v-model="participant.email"
                      type="email"
                      class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                      required
                    />
                  </div>

                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
                    >
                      Tipo de Documento *
                    </label>
                    <select
                      v-model="participant.documentType"
                      class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                    >
                      <option value="RUT">RUT</option>
                      <option value="DNI">DNI</option>
                      <option value="PASSPORT">Pasaporte</option>
                    </select>
                  </div>

                  <div>
                    <label
                      class="block text-sm font-medium text-neutral-700 dark:text-neutral-300 mb-1"
                    >
                      Número de Documento *
                    </label>
                    <input
                      v-model="participant.documentNumber"
                      type="text"
                      class="w-full px-4 py-2 rounded-lg border border-neutral-300 dark:border-neutral-600 bg-white dark:bg-neutral-800 text-neutral-900 dark:text-white focus:ring-2 focus:ring-primary focus:border-transparent"
                      required
                    />
                  </div>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="flex justify-between">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-left"
                  @click="prevStep"
                >
                  Atrás
                </UButton>
                <UButton
                  color="primary"
                  icon="i-lucide-arrow-right"
                  trailing
                  @click="nextStep"
                  :disabled="!step2Valid"
                >
                  Continuar
                </UButton>
              </div>
            </template>
          </UCard>

          <!-- Step 3: Payment -->
          <UCard v-if="currentStep === 3">
            <template #header>
              <h2
                class="text-xl font-semibold text-neutral-900 dark:text-white"
              >
                Método de Pago
              </h2>
              <p class="text-sm text-neutral-500 dark:text-neutral-400 mt-1">
                Selecciona cómo deseas pagar
              </p>
            </template>

            <div class="space-y-4">
              <div
                class="p-4 rounded-lg border cursor-pointer transition-colors"
                :class="[
                  paymentMethod === 'credit_card'
                    ? 'border-primary bg-primary/10'
                    : 'border-neutral-200 dark:border-neutral-700 hover:border-primary/50',
                ]"
                @click="paymentMethod = 'credit_card'"
              >
                <div class="flex items-center gap-3">
                  <UIcon
                    name="i-lucide-credit-card"
                    class="w-6 h-6 text-primary"
                  />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      Tarjeta de Crédito/Débito
                    </p>
                    <p class="text-sm text-neutral-500 dark:text-neutral-400">
                      Visa, Mastercard, AmEx
                    </p>
                  </div>
                </div>
              </div>

              <div
                class="p-4 rounded-lg border cursor-pointer transition-colors"
                :class="[
                  paymentMethod === 'transfer'
                    ? 'border-primary bg-primary/10'
                    : 'border-neutral-200 dark:border-neutral-700 hover:border-primary/50',
                ]"
                @click="paymentMethod = 'transfer'"
              >
                <div class="flex items-center gap-3">
                  <UIcon
                    name="i-lucide-building-2"
                    class="w-6 h-6 text-primary"
                  />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      Transferencia Bancaria
                    </p>
                    <p class="text-sm text-neutral-500 dark:text-neutral-400">
                      Pago directo desde tu banco
                    </p>
                  </div>
                </div>
              </div>

              <div
                class="mt-6 p-4 bg-neutral-100 dark:bg-neutral-800 rounded-lg"
              >
                <p class="text-sm text-neutral-600 dark:text-neutral-400">
                  <UIcon name="i-lucide-info" class="w-4 h-4 inline mr-1" />
                  Este es un pago de demostración. No se procesará ningún cargo
                  real.
                </p>
              </div>
            </div>

            <template #footer>
              <div class="flex justify-between">
                <UButton
                  color="neutral"
                  variant="ghost"
                  icon="i-lucide-arrow-left"
                  @click="prevStep"
                >
                  Atrás
                </UButton>
                <UButton
                  color="primary"
                  size="lg"
                  icon="i-lucide-lock"
                  @click="submitBooking"
                >
                  Pagar {{ formatCurrency(total) }}
                </UButton>
              </div>
            </template>
          </UCard>
        </div>

        <!-- Order Summary Sidebar -->
        <div class="lg:col-span-1">
          <div class="sticky top-4">
            <UCard>
              <template #header>
                <h3
                  class="text-lg font-semibold text-neutral-900 dark:text-white"
                >
                  Resumen de Compra
                </h3>
              </template>

              <div class="space-y-4">
                <!-- Cart Items -->
                <div
                  v-for="item in cartStore.cart.items"
                  :key="item.itemId"
                  class="pb-4 border-b border-neutral-200 dark:border-neutral-700"
                >
                  <p
                    class="font-medium text-neutral-900 dark:text-white text-sm"
                  >
                    {{ item.tourName }}
                  </p>
                  <p
                    class="text-xs text-neutral-500 dark:text-neutral-400 mt-1"
                  >
                    {{ item.numParticipants }}
                    {{ item.numParticipants === 1 ? "persona" : "personas" }}
                  </p>
                  <p
                    class="text-sm font-semibold text-neutral-900 dark:text-white mt-2"
                  >
                    {{ formatCurrency(item.itemTotal) }}
                  </p>
                </div>

                <!-- Totals -->
                <div class="space-y-2 pt-2">
                  <div
                    class="flex justify-between text-sm text-neutral-600 dark:text-neutral-400"
                  >
                    <span>Subtotal</span>
                    <span>{{ formatCurrency(subtotal) }}</span>
                  </div>
                  <div
                    class="flex justify-between text-sm text-neutral-600 dark:text-neutral-400"
                  >
                    <span>IVA (19%)</span>
                    <span>{{ formatCurrency(tax) }}</span>
                  </div>
                  <UDivider />
                  <div
                    class="flex justify-between text-lg font-bold text-neutral-900 dark:text-white"
                  >
                    <span>Total</span>
                    <span>{{ formatCurrency(total) }}</span>
                  </div>
                </div>
              </div>
            </UCard>
          </div>
        </div>
      </div>
    </UContainer>
  </div>
</template>
