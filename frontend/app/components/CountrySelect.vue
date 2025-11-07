<script setup lang="ts">
const props = defineProps<{
  modelValue: string | null | undefined
  label?: string
  placeholder?: string
  required?: boolean
  size?: 'sm' | 'md' | 'lg' | 'xl'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const { countries, getCountryLabel, getCountryFlag } = useCountries()

const selected = computed({
  get: () => props.modelValue || null,
  set: (value) => {
    // Si value es un objeto, extraer solo el código
    const code = typeof value === 'object' && value !== null ? (value as any).value : value
    emit('update:modelValue', code)
  }
})
</script>

<template>
  <UFormField
    :label="label"
    name="country"
    :required="required"
  >
    <USelectMenu
      v-model="selected"
      :items="countries"
      value-attribute="value"
      option-attribute="label"
      :placeholder="placeholder || 'Selecciona un país'"
      :size="size || 'md'"
      searchable
      by="value"
      class="w-full"
    >
      <template #label>
        <div
          v-if="selected"
          class="flex items-center gap-2"
        >
          <span class="text-xl leading-none">
            {{ getCountryFlag(selected) }}
          </span>
          <span>
            {{ getCountryLabel(selected) }}
          </span>
        </div>
        <span v-else>
          {{ placeholder || 'Selecciona un país' }}
        </span>
      </template>

      <template #item="{ item }">
        <div class="flex items-center gap-2">
          <span class="text-xl leading-none">
            {{ getCountryFlag(item.value) }}
          </span>
          <span>{{ item.label }}</span>
        </div>
      </template>
    </USelectMenu>
  </UFormField>
</template>
