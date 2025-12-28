<script setup lang="ts">
const props = withDefaults(defineProps<{
  modelValue: string[]
  placeholder?: string
  helpText?: string
  lowercase?: boolean
}>(), {
  placeholder: 'Escribe y presiona Enter',
  helpText: 'Presiona Enter para agregar. Click en la etiqueta para eliminar.',
  lowercase: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const tagInput = ref('')

function addTag() {
  let tag = tagInput.value.trim()
  if (props.lowercase) {
    tag = tag.toLowerCase()
  }
  if (tag && !props.modelValue.includes(tag)) {
    emit('update:modelValue', [...props.modelValue, tag])
    tagInput.value = ''
  }
}

function removeTag(index: number) {
  const newTags = [...props.modelValue]
  newTags.splice(index, 1)
  emit('update:modelValue', newTags)
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    event.preventDefault()
    addTag()
  }
}
</script>

<template>
  <div>
    <!-- Tags display -->
    <div
      v-if="modelValue.length > 0"
      class="flex flex-wrap gap-2 mb-2"
    >
      <UBadge
        v-for="(tag, index) in modelValue"
        :key="index"
        color="primary"
        variant="soft"
        class="cursor-pointer"
        @click="removeTag(index)"
      >
        {{ tag }}
        <UIcon
          name="i-heroicons-x-mark"
          class="w-3 h-3 ml-1"
        />
      </UBadge>
    </div>

    <!-- Tag input -->
    <UInput
      v-model="tagInput"
      :placeholder="placeholder"
      size="lg"
      class="w-full"
      @keydown="handleKeydown"
    />

    <p
      v-if="helpText"
      class="text-xs text-neutral-600 dark:text-neutral-300 mt-1"
    >
      {{ helpText }}
    </p>
  </div>
</template>
