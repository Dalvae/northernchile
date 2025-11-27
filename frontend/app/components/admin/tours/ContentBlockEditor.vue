<script setup lang="ts">
import type { ContentBlock } from 'api-client'

const props = defineProps<{
  modelValue: ContentBlock[]
  language: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ContentBlock[]]
}>()

const blocks = computed({
  get: () => props.modelValue || [],
  set: value => emit('update:modelValue', value)
})

const blockTypeOptions = [
  { label: 'Título', value: 'heading', icon: 'i-heroicons-h1' },
  { label: 'Párrafo', value: 'paragraph', icon: 'i-heroicons-bars-3-bottom-left' },
  { label: 'Lista', value: 'list', icon: 'i-heroicons-list-bullet' },
  { label: 'Destacado', value: 'highlight', icon: 'i-heroicons-light-bulb' }
]

function addBlock(type: string = 'paragraph') {
  const newBlocks = [...blocks.value, { type, content: '' }]
  blocks.value = newBlocks
}

function removeBlock(index: number) {
  const newBlocks = blocks.value.filter((_, i) => i !== index)
  blocks.value = newBlocks
}

function moveBlockUp(index: number) {
  if (index === 0) return
  const newBlocks = [...blocks.value]
  const temp = newBlocks[index]!
  newBlocks[index] = newBlocks[index - 1]!
  newBlocks[index - 1] = temp
  blocks.value = newBlocks
}

function moveBlockDown(index: number) {
  if (index === blocks.value.length - 1) return
  const newBlocks = [...blocks.value]
  const temp = newBlocks[index]!
  newBlocks[index] = newBlocks[index + 1]!
  newBlocks[index + 1] = temp
  blocks.value = newBlocks
}

function updateBlock(index: number, field: 'type' | 'content', value: string) {
  const newBlocks = [...blocks.value]
  newBlocks[index] = { ...newBlocks[index], [field]: value }
  blocks.value = newBlocks
}

function getBlockTypeLabel(type: string): string {
  return blockTypeOptions.find(opt => opt.value === type)?.label || type
}

function getPlaceholder(type: string): string {
  switch (type) {
    case 'heading':
      return 'Título de la sección...'
    case 'paragraph':
      return 'Escribe el párrafo aquí...'
    case 'list':
      return 'Item 1\nItem 2\nItem 3'
    case 'highlight':
      return 'Información destacada...'
    default:
      return 'Contenido...'
  }
}
</script>

<template>
  <div class="space-y-4">
    <!-- Lista de bloques existentes -->
    <div
      v-if="blocks.length > 0"
      class="space-y-3"
    >
      <div
        v-for="(block, index) in blocks"
        :key="index"
        class="border border-neutral-200 dark:border-neutral-800 rounded-lg p-4 bg-neutral-50 dark:bg-neutral-900"
      >
        <div class="flex items-start gap-3">
          <!-- Controles de orden -->
          <div class="flex flex-col gap-1 pt-1">
            <UButton
              icon="i-heroicons-chevron-up"
              color="neutral"
              variant="ghost"
              size="xs"
              :disabled="index === 0"
              @click="moveBlockUp(index)"
            />
            <UButton
              icon="i-heroicons-chevron-down"
              color="neutral"
              variant="ghost"
              size="xs"
              :disabled="index === blocks.length - 1"
              @click="moveBlockDown(index)"
            />
          </div>

          <!-- Contenido del bloque -->
          <div class="flex-1 space-y-3">
            <!-- Selector de tipo de bloque -->
            <div class="flex items-center gap-3">
              <USelect
                :model-value="block.type"
                :items="blockTypeOptions"
                option-attribute="label"
                value-attribute="value"
                size="sm"
                class="w-full"
                @update:model-value="(value: string | undefined) => value && updateBlock(index, 'type', value)"
              />
              <span class="text-xs text-neutral-500 dark:text-neutral-300 whitespace-nowrap">
                Bloque {{ index + 1 }}
              </span>
            </div>

            <!-- Editor de contenido -->
            <UTextarea
              :model-value="block.content"
              :placeholder="getPlaceholder(block.type || 'paragraph')"
              :rows="block.type === 'heading' ? 2 : 4"
              autoresize
              size="md"
              class="w-full"
              @update:model-value="(value: string) => updateBlock(index, 'content', value)"
            />
          </div>

          <!-- Botón eliminar -->
          <UButton
            icon="i-heroicons-trash"
            color="error"
            variant="ghost"
            size="sm"
            @click="removeBlock(index)"
          />
        </div>
      </div>
    </div>

    <!-- Mensaje cuando no hay bloques -->
    <div
      v-else
      class="text-center py-8 border-2 border-dashed border-neutral-200 dark:border-neutral-800 rounded-lg"
    >
      <p class="text-sm text-neutral-500 dark:text-neutral-300 mb-4">
        No hay bloques de contenido. Agrega tu primer bloque.
      </p>
    </div>

    <!-- Botones para agregar bloques -->
    <div class="flex flex-wrap gap-2">
      <UButton
        v-for="option in blockTypeOptions"
        :key="option.value"
        :icon="option.icon"
        :label="`Añadir ${option.label}`"
        color="primary"
        variant="outline"
        size="sm"
        @click="addBlock(option.value)"
      />
    </div>
  </div>
</template>
