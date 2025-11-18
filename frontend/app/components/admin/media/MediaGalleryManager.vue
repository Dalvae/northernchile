<script setup lang="ts">
const props = defineProps<{
  tourId?: string
  scheduleId?: string
}>()

const emit = defineEmits(['update'])

const toast = useToast()
const {
  fetchTourGallery,
  fetchScheduleGallery,
  setTourHeroImage,
  assignMediaToTour,
  assignMediaToSchedule,
  reorderTourGallery,
  reorderScheduleGallery
} = useAdminData()

// State
const gallery = ref([])
const loading = ref(false)
const selectorModalOpen = ref(false)
const uploadModalOpen = ref(false)

// Fetch gallery
async function fetchGallery() {
  if (!props.tourId && !props.scheduleId) return

  loading.value = true
  try {
    gallery.value = props.tourId
      ? await fetchTourGallery(props.tourId)
      : await fetchScheduleGallery(props.scheduleId!)
  } catch (error) {
    console.error('Error fetching gallery:', error)
    toast.add({ color: 'error', title: 'Error al cargar galería' })
  } finally {
    loading.value = false
  }
}

// Remove media from gallery
async function removeFromGallery(mediaId: string) {
  if (!confirm('¿Quitar esta foto de la galería?')) return

  try {
    // Remove from join table (backend cascade will handle it)
    gallery.value = gallery.value.filter(m => m.id !== mediaId)
    toast.add({ color: 'success', title: 'Foto eliminada de la galería' })
    emit('update')
  } catch (error) {
    console.error('Error removing media:', error)
    toast.add({ color: 'error', title: 'Error al eliminar' })
    await fetchGallery() // Reload
  }
}

// Set hero image
async function setHero(mediaId: string) {
  if (!props.tourId) return

  try {
    await setTourHeroImage(props.tourId, mediaId)

    // Update local state
    gallery.value = gallery.value.map(m => ({
      ...m,
      isHero: m.id === mediaId
    }))

    toast.add({ color: 'success', title: 'Imagen destacada actualizada' })
    emit('update')
  } catch (error) {
    console.error('Error setting hero:', error)
    toast.add({ color: 'error', title: 'Error al establecer imagen destacada' })
  }
}

// Move media up in order
async function moveUp(index: number) {
  if (index === 0) return

  // Don't allow moving above inherited photos (for schedules)
  if (props.scheduleId && gallery.value[index - 1]?.isInherited) return

  const temp = gallery.value[index]
  gallery.value[index] = gallery.value[index - 1]
  gallery.value[index - 1] = temp

  await saveOrder()
}

// Move media down in order
async function moveDown(index: number) {
  if (index === gallery.value.length - 1) return

  const temp = gallery.value[index]
  gallery.value[index] = gallery.value[index + 1]
  gallery.value[index + 1] = temp

  await saveOrder()
}

// Save order to backend
async function saveOrder() {
  if (!props.tourId && !props.scheduleId) return

  try {
    // For schedules, only reorder non-inherited photos
    const photosToReorder = props.scheduleId
      ? gallery.value.filter(m => !m.isInherited)
      : gallery.value

    const orders = photosToReorder.map((m, index) => ({
      mediaId: m.id,
      displayOrder: index
    }))

    if (props.tourId) {
      await reorderTourGallery(props.tourId, orders)
    } else {
      await reorderScheduleGallery(props.scheduleId!, orders)
    }

    emit('update')
  } catch (error) {
    console.error('Error saving order:', error)
    toast.add({ color: 'error', title: 'Error al guardar orden' })
    await fetchGallery() // Reload
  }
}

// Handle media selected from library
async function handleMediaSelected(selectedMediaIds: string[]) {
  try {
    if (props.tourId) {
      await assignMediaToTour(props.tourId, selectedMediaIds)
    } else {
      await assignMediaToSchedule(props.scheduleId!, selectedMediaIds)
    }

    toast.add({
      color: 'success',
      title: `${selectedMediaIds.length} ${selectedMediaIds.length === 1 ? 'foto añadida' : 'fotos añadidas'}`
    })

    await fetchGallery()
    emit('update')
  } catch (error) {
    console.error('Error assigning media:', error)
    toast.add({ color: 'error', title: 'Error al asignar fotos' })
  }
}

// Watch for prop changes
watch(() => [props.tourId, props.scheduleId], () => {
  fetchGallery()
}, { immediate: true })
</script>

<template>
  <div class="space-y-4">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h4 class="text-lg font-semibold text-neutral-900 dark:text-neutral-100">
          Galería de Fotos
        </h4>
        <p class="text-sm text-neutral-600 dark:text-neutral-400">
          Gestiona las fotos de este {{ tourId ? 'tour' : 'programa' }}
        </p>
      </div>

      <div class="flex gap-2">
        <UButton
          icon="i-heroicons-cloud-arrow-up"
          color="secondary"
          variant="outline"
          @click="uploadModalOpen = true"
        >
          Subir Fotos
        </UButton>

        <UButton
          icon="i-heroicons-plus"
          color="primary"
          @click="selectorModalOpen = true"
        >
          Asignar Fotos
        </UButton>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="flex justify-center py-8">
      <UIcon name="i-heroicons-arrow-path" class="w-6 h-6 animate-spin text-neutral-400" />
    </div>

    <!-- Empty state -->
    <div
      v-else-if="gallery.length === 0"
      class="text-center py-12 border-2 border-dashed border-neutral-300 dark:border-neutral-700 rounded-lg"
    >
      <UIcon name="i-heroicons-photo" class="w-12 h-12 mx-auto mb-4 text-neutral-400" />
      <p class="text-neutral-600 dark:text-neutral-400 mb-4">
        No hay fotos en la galería
      </p>
      <div class="flex gap-2 justify-center">
        <UButton
          icon="i-heroicons-cloud-arrow-up"
          color="secondary"
          variant="soft"
          @click="uploadModalOpen = true"
        >
          Subir Fotos
        </UButton>

        <UButton
          icon="i-heroicons-plus"
          color="primary"
          variant="soft"
          @click="selectorModalOpen = true"
        >
          Asignar Primera Foto
        </UButton>
      </div>
    </div>

    <!-- Gallery grid -->
    <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      <div
        v-for="(item, index) in gallery"
        :key="item.id"
        class="relative group"
      >
        <!-- Image -->
        <img
          :src="item.url"
          :alt="item.altTranslations?.es || item.originalFilename"
          class="w-full h-40 object-cover rounded-lg shadow-sm"
        />

        <!-- Hero badge -->
        <UBadge
          v-if="item.isHero"
          color="primary"
          variant="solid"
          class="absolute top-2 left-2"
        >
          <UIcon name="i-heroicons-star-solid" class="w-3 h-3" />
          Destacada
        </UBadge>

        <!-- Inherited badge (for schedule galleries) -->
        <UBadge
          v-if="item.isInherited"
          color="info"
          variant="soft"
          class="absolute top-2 right-2"
        >
          <UIcon name="i-heroicons-arrow-down-tray" class="w-3 h-3" />
          Del Tour
        </UBadge>

        <!-- Actions overlay -->
        <div class="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity rounded-lg flex items-center justify-center gap-2">
          <!-- Inherited photos are read-only -->
          <template v-if="!item.isInherited">
            <!-- Move up (not available if previous item is inherited) -->
            <UButton
              v-if="index > 0 && !gallery[index - 1]?.isInherited"
              icon="i-heroicons-arrow-up"
              size="sm"
              color="neutral"
              @click="moveUp(index)"
            />

            <!-- Move down -->
            <UButton
              v-if="index < gallery.length - 1"
              icon="i-heroicons-arrow-down"
              size="sm"
              color="neutral"
              @click="moveDown(index)"
            />

            <!-- Set hero (only for tours) -->
            <UButton
              v-if="tourId && !item.isHero"
              icon="i-heroicons-star"
              size="sm"
              color="primary"
              @click="setHero(item.id)"
            />

            <!-- Remove -->
            <UButton
              icon="i-heroicons-trash"
              size="sm"
              color="error"
              @click="removeFromGallery(item.id)"
            />
          </template>

          <!-- Info badge for inherited photos -->
          <template v-else>
            <div class="bg-neutral-900/90 text-white text-xs px-3 py-1.5 rounded">
              Heredada del tour (solo lectura)
            </div>
          </template>
        </div>

        <!-- Display order badge -->
        <div class="absolute bottom-2 right-2 bg-black/70 text-white text-xs px-2 py-1 rounded">
          #{{ index + 1 }}
        </div>
      </div>
    </div>

    <!-- Media selector modal -->
    <AdminMediaSelectorModal
      v-model="selectorModalOpen"
      @selected="handleMediaSelected"
    />

    <!-- Upload modal -->
    <AdminMediaUploadModal
      v-model="uploadModalOpen"
      :tour-id="tourId"
      :schedule-id="scheduleId"
      @success="fetchGallery"
    />
  </div>
</template>
