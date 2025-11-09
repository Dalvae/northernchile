<template>
  <UBadge
    :color="badgeColor"
    :variant="variant"
    size="sm"
  >
    {{ label }}
  </UBadge>
</template>

<script setup lang="ts">
type BadgeColor = 'error' | 'info' | 'success' | 'primary' | 'secondary' | 'tertiary' | 'warning' | 'neutral'
type BadgeVariant = 'solid' | 'subtle' | 'soft' | 'outline'

const props = withDefaults(defineProps<{
  type: 'booking' | 'tour' | 'user'
  status: string
  variant?: BadgeVariant
}>(), {
  variant: 'subtle'
})

const badgeColor = computed<BadgeColor>(() => {
  const s = props.status

  if (props.type === 'booking') {
    const map: Record<string, BadgeColor> = {
      CONFIRMED: 'success',
      PENDING: 'warning',
      CANCELLED: 'error'
    }
    return map[s] || 'neutral'
  }

  if (props.type === 'tour') {
    const map: Record<string, BadgeColor> = {
      PUBLISHED: 'success',
      DRAFT: 'warning',
      ARCHIVED: 'neutral'
    }
    return map[s] || 'neutral'
  }

  if (props.type === 'user') {
    const map: Record<string, BadgeColor> = {
      ROLE_SUPER_ADMIN: 'error',
      ROLE_PARTNER_ADMIN: 'warning',
      ROLE_CLIENT: 'info'
    }
    return map[s] || 'neutral'
  }

  return 'neutral'
})

const label = computed(() => {
  if (props.type === 'booking') {
    switch (props.status) {
      case 'CONFIRMED':
        return 'Confirmada'
      case 'PENDING':
        return 'Pendiente'
      case 'CANCELLED':
        return 'Cancelada'
      default:
        return props.status
    }
  }

  if (props.type === 'tour') {
    switch (props.status) {
      case 'PUBLISHED':
        return 'Publicado'
      case 'DRAFT':
        return 'Borrador'
      case 'ARCHIVED':
        return 'Archivado'
      default:
        return props.status
    }
  }

  if (props.type === 'user') {
    switch (props.status) {
      case 'ROLE_SUPER_ADMIN':
        return 'Super Admin'
      case 'ROLE_PARTNER_ADMIN':
        return 'Partner Admin'
      case 'ROLE_CLIENT':
        return 'Cliente'
      default:
        return props.status
    }
  }

  return props.status
})
</script>
