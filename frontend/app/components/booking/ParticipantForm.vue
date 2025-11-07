<template>
  <UCard>
    <template #header>
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-neutral-900 dark:text-white">
          {{ t('booking.participant', { number: index + 1 }) }}
        </h3>
      </div>
    </template>

    <div class="space-y-4">
      <!-- Full Name -->
      <UFormField
        :label="t('booking.full_name')"
        name="fullName"
        required
      >
        <UInput
          :model-value="participant.fullName"
          :placeholder="t('booking.full_name_placeholder')"
          size="lg"
          icon="i-lucide-user"
          @update:model-value="emit('update', { fullName: $event })"
        />
      </UFormField>

      <!-- Document ID and Nationality -->
      <div class="grid md:grid-cols-2 gap-4">
        <UFormField
          :label="t('booking.document_id')"
          name="documentId"
          required
        >
          <UInput
            :model-value="participant.documentId"
            :placeholder="t('booking.document_id_placeholder')"
            size="lg"
            icon="i-lucide-credit-card"
            @update:model-value="emit('update', { documentId: $event })"
          />
        </UFormField>

        <UFormField
          :label="t('booking.nationality')"
          name="nationality"
        >
          <UInput
            :model-value="participant.nationality"
            :placeholder="t('booking.nationality_placeholder')"
            size="lg"
            icon="i-lucide-globe"
            @update:model-value="emit('update', { nationality: $event })"
          />
        </UFormField>
      </div>

      <!-- Age -->
      <UFormField
        :label="t('booking.age')"
        name="age"
      >
        <UInput
          :model-value="participant.age?.toString() || ''"
          type="number"
          :placeholder="t('booking.age_placeholder')"
          size="lg"
          icon="i-lucide-calendar"
          @update:model-value="emit('update', { age: $event ? parseInt($event) : null })"
        />
      </UFormField>

      <!-- Pickup Address -->
      <UFormField
        :label="t('booking.pickup_address')"
        name="pickupAddress"
        required
        :help="t('booking.pickup_address_help')"
      >
        <UInput
          :model-value="participant.pickupAddress"
          :placeholder="t('booking.pickup_address_placeholder')"
          size="lg"
          icon="i-lucide-map-pin"
          @update:model-value="emit('update', { pickupAddress: $event })"
        />
      </UFormField>

      <!-- Special Requirements -->
      <UFormField
        :label="t('booking.special_requirements')"
        name="specialRequirements"
      >
        <UTextarea
          :model-value="participant.specialRequirements"
          :placeholder="t('booking.special_requirements_placeholder')"
          :rows="3"
          size="lg"
          class="w-full"
          @update:model-value="emit('update', { specialRequirements: $event })"
        />
      </UFormField>

      <!-- Phone and Email -->
      <div class="grid md:grid-cols-2 gap-4">
        <UFormField
          :label="t('booking.phone')"
          name="phoneNumber"
          help="Opcional. Para contactarte y enviarte fotos del tour"
        >
          <UInput
            :model-value="participant.phoneNumber"
            type="tel"
            placeholder="+56 9 1234 5678"
            size="lg"
            icon="i-lucide-phone"
            class="w-full"
            @update:model-value="emit('update', { phoneNumber: $event })"
          />
        </UFormField>

        <UFormField
          :label="t('booking.email')"
          name="email"
          help="Opcional. Para enviarte fotos del tour"
        >
          <UInput
            :model-value="participant.email"
            type="email"
            placeholder="tu@email.com"
            size="lg"
            icon="i-lucide-mail"
            class="w-full"
            @update:model-value="emit('update', { email: $event })"
          />
        </UFormField>
      </div>
    </div>
  </UCard>
</template>

<script setup lang="ts">
const { t } = useI18n()

defineProps<{
  participant: {
    fullName: string
    documentId: string
    nationality: string
    age: number | null
    pickupAddress: string
    specialRequirements: string
    phoneNumber: string
    email: string
  }
  index: number
  totalParticipants: number
}>()

const emit = defineEmits<{
  update: [data: Partial<{
    fullName: string
    documentId: string
    nationality: string
    age: number | null
    pickupAddress: string
    specialRequirements: string
    phoneNumber: string
    email: string
  }>]
}>()
</script>
