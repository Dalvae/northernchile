<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

defineProps<{
  links: { label: string, to: string }[]
}>()

const emit = defineEmits<{
  close: []
}>()

const { t, locales, locale: currentLocale, setLocale } = useI18n()
const localePath = useLocalePath()
const authStore = useAuthStore()
const cartStore = useCartStore()

const cartItemsCount = computed(() => cartStore.totalItems)
const isAdmin = computed(() => authStore.isAdmin)

const accordionItems = [
  {
    label: t('nav.language'),
    slot: 'language',
    defaultOpen: false
  }
]

function handleClose() {
  emit('close')
}

async function handleLogout() {
  handleClose()
  await authStore.logout()
}
</script>

<template>
  <div
    class="flex flex-col h-full bg-[var(--color-paranal-950)] border-l border-white/10 font-sans text-white"
  >
    <!-- Header -->
    <div class="flex justify-between items-center p-6 border-b border-white/5">
      <span class="text-2xl font-display font-bold tracking-wide text-white">
        {{ t("common.menu") || "Menú" }}
      </span>
      <UButton
        icon="i-lucide-x"
        color="neutral"
        variant="ghost"
        size="lg"
        class="hover:bg-white/10 rounded-full text-white"
        :aria-label="t('common.close')"
        @click="handleClose"
      />
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto py-4 px-6">
      <nav class="space-y-0 divide-y divide-white/5">
        <!-- Links Principales -->
        <NuxtLink
          v-for="link in links"
          :key="link.label"
          :to="link.to"
          class="group flex items-center justify-between py-5 text-xl font-medium text-neutral-300 hover:text-white transition-colors"
          @click="handleClose"
        >
          {{ link.label }}
          <UIcon
            name="i-lucide-chevron-right"
            class="w-5 h-5 text-neutral-600 group-hover:text-white transition-colors"
          />
        </NuxtLink>

        <!-- Link Carrito -->
        <NuxtLink
          :to="localePath('/cart')"
          class="group flex items-center justify-between py-5 text-xl font-medium text-neutral-300 hover:text-white transition-colors"
          @click="handleClose"
        >
          {{ t("nav.cart") }}
          <div class="flex items-center gap-3">
            <ClientOnly>
              <UBadge
                v-if="cartItemsCount > 0"
                :label="cartItemsCount.toString()"
                color="primary"
                size="md"
                variant="solid"
              />
            </ClientOnly>
            <UIcon
              name="i-lucide-chevron-right"
              class="w-5 h-5 text-neutral-600 group-hover:text-white transition-colors"
            />
          </div>
        </NuxtLink>

        <!-- Acordeón Configuración -->
        <UAccordion
          :items="accordionItems"
          :ui="{
            root: 'w-full',
            item: 'py-0 border-none',
            trigger:
              'py-5 text-xl font-medium text-neutral-300 hover:text-white flex items-center justify-between w-full group shadow-none focus-visible:ring-0 bg-transparent',
            trailingIcon:
              'w-5 h-5 text-neutral-600 group-hover:text-white transition-transform duration-200 ms-auto'
          }"
        >
          <!-- Slot por defecto para el LABEL (el texto de la izquierda) -->
          <template #default="{ item }">
            <span>{{ item.label }}</span>
          </template>

          <!-- Contenido: Idioma -->
          <template #language>
            <div class="pb-5 pl-4 space-y-2">
              <button
                v-for="loc in locales"
                :key="loc.code"
                class="flex items-center justify-between py-2 px-4 rounded-lg transition-colors w-full"
                :class="
                  currentLocale === loc.code
                    ? 'bg-primary-500/10 text-primary-400'
                    : 'text-neutral-400 hover:text-white hover:bg-white/5'
                "
                @click="() => { setLocale(loc.code); handleClose(); }"
              >
                <span class="text-lg">{{ loc.name }}</span>
                <UIcon
                  v-if="currentLocale === loc.code"
                  name="i-lucide-check"
                  class="w-5 h-5"
                />
              </button>
            </div>
          </template>
        </UAccordion>
      </nav>
    </div>

    <!-- Footer Auth -->
    <div class="p-6 border-t border-white/10 bg-neutral-900/30">
      <ClientOnly>
        <div
          v-if="authStore.isAuthenticated"
          class="space-y-4"
        >
          <div class="flex items-center gap-4 mb-4">
            <div
              class="w-12 h-12 rounded-full bg-primary-900/30 border border-primary-500/30 flex items-center justify-center text-primary-400 font-bold text-xl"
            >
              {{ authStore.user?.fullName?.charAt(0) || "U" }}
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-lg font-medium text-white truncate">
                {{ authStore.user?.fullName }}
              </p>
              <p class="text-sm text-neutral-400 truncate">
                {{ authStore.user?.email }}
              </p>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <UButton
              :to="localePath('/profile')"
              color="neutral"
              variant="outline"
              block
              class="text-white border-white/20 hover:bg-white/10"
              @click="handleClose"
            >
              {{ t("nav.my_account") }}
            </UButton>
            <UButton
              color="neutral"
              variant="ghost"
              block
              class="text-error-400 hover:text-error-300 hover:bg-error-950/30"
              @click="handleLogout"
            >
              {{ t("nav.logout") }}
            </UButton>
          </div>

          <UButton
            v-if="isAdmin"
            :to="localePath('/admin')"
            color="primary"
            variant="soft"
            block
            class="mt-2"
            icon="i-lucide-shield-check"
            @click="handleClose"
          >
            Panel Admin
          </UButton>
        </div>

        <div v-else>
          <UButton
            :to="localePath('/auth')"
            color="primary"
            block
            size="xl"
            class="font-bold text-lg rounded-xl"
            @click="handleClose"
          >
            {{ t("nav.login") }}
            <UIcon
              name="i-lucide-arrow-right"
              class="ml-2 w-5 h-5"
            />
          </UButton>
        </div>

        <template #fallback>
          <div class="h-14 bg-white/10 rounded-xl animate-pulse" />
        </template>
      </ClientOnly>
    </div>
  </div>
</template>
