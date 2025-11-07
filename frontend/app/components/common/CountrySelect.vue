<script setup lang="ts">
const props = defineProps<{
  modelValue: string | null | undefined
  label?: string
  placeholder?: string
  required?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | null]
}>()

const { countries } = useCountries()

const selected = computed({
  get: () => props.modelValue || null,
  set: value => emit('update:modelValue', value)
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
      searchable
      by="value"
      return-object="false"
      class="w-full"
    >
      <template #label>
        <div class="flex items-center gap-2">
          <span
            v-if="selected"
            class="text-xl"
          >
            {{ selected?.toUpperCase() === 'CL' ? '🇨🇱' : '' }}
          </span>
          <span>
            {{ countries.find(c => c.value === selected)?.label || placeholder || 'Selecciona un país' }}
          </span>
        </div>
      </template>

      <template #item="{ option }">
        <div class="flex items-center gap-2">
          <span class="text-xl">
            {{ option.value === 'CL' ? '🇨🇱' : '' }}
          </span>
          <span>{{ option.label }}</span>
        </div>
      </template>
    </USelectMenu>
  </UFormField>
</template>
