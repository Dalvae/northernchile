<script setup lang="ts">
const props = defineProps<{
  modelValue?: string | null
  label?: string
  placeholder?: string
  required?: boolean
  size?: 'sm' | 'md' | 'lg' | 'xl'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const { countries, getCountryLabel, getCountryFlag } = useCountries()

const selected = computed<{ value: string; label: string } | undefined>({
  get: () => {
    if (!props.modelValue) return undefined
    const country = countries.find(c => c.value === props.modelValue)
    return country
  },
  set: (value) => {
    const code = value ? value.value : null
    emit('update:modelValue', code)
  }
})

const displayValue = computed(() => {
  if (!selected.value) return props.placeholder || 'Selecciona un país'
  return `${getCountryFlag(selected.value.value)} ${selected.value.label}`
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
