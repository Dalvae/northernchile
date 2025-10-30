<template>
  <header class="sticky top-0 z-50 bg-white/95 dark:bg-neutral-900/95 backdrop-blur-sm border-b border-neutral-200 dark:border-neutral-800">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <div class="flex-shrink-0">
          <NuxtLink :to="localePath('/')" class="flex items-center gap-2 group">
            <UIcon name="i-lucide-telescope" class="w-6 h-6 text-primary group-hover:rotate-12 transition-transform" />
            <span class="font-display font-bold text-xl text-neutral-900 dark:text-white">
              Northern Chile
            </span>
          </NuxtLink>
        </div>

        <!-- Desktop Navigation -->
        <nav class="hidden md:flex items-center gap-1">
          <UButton
            v-for="link in links"
            :key="link.label"
            :to="link.to"
            variant="ghost"
            color="neutral"
            class="font-medium"
          >
            {{ link.label }}
          </UButton>
        </nav>

        <!-- Right Actions -->
        <div class="flex items-center gap-2">
          <!-- Language Switcher -->
          <LanguageSwitcher />

          <!-- Cart Button -->
          <UButton
            :to="localePath('/cart')"
            variant="ghost"
            color="neutral"
            icon="i-lucide-shopping-cart"
            :aria-label="t('nav.cart')"
            class="relative"
          >
            <UBadge
              v-if="cartItemsCount > 0"
              :label="cartItemsCount.toString()"
              color="primary"
              size="xs"
              class="absolute -top-1 -right-1"
            />
          </UButton>

          <!-- Auth Actions -->
          <template v-if="!authStore.isAuthenticated">
            <UButton
              :to="localePath('/auth')"
              variant="ghost"
              color="neutral"
              icon="i-lucide-log-in"
              class="hidden sm:flex"
            >
              {{ t('nav.login') }}
            </UButton>
          </template>

          <template v-else>
            <!-- User Menu -->
            <UDropdownMenu :items="userMenuItems">
              <UButton
                variant="ghost"
                color="neutral"
                icon="i-lucide-user"
                trailing-icon="i-lucide-chevron-down"
                class="hidden sm:flex"
              >
                {{ authStore.user?.fullName?.split(' ')[0] || t('nav.my_account') }}
              </UButton>
            </UDropdownMenu>
          </template>

          <!-- Mobile Menu Button -->
          <UButton
            variant="ghost"
            color="neutral"
            icon="i-lucide-menu"
            class="md:hidden"
            @click="mobileMenuOpen = true"
          />
        </div>
      </div>
    </div>

    <!-- Mobile Menu Drawer -->
    <USlideover v-model="mobileMenuOpen" side="right">
      <template #content>
        <div class="p-6 space-y-6">
          <!-- Close Button -->
          <div class="flex justify-between items-center pb-4 border-b border-neutral-200 dark:border-neutral-800">
            <h2 class="text-lg font-semibold text-neutral-900 dark:text-white">
              {{ t('common.menu') || 'Menú' }}
            </h2>
            <UButton
              icon="i-lucide-x"
              color="neutral"
              variant="ghost"
              @click="mobileMenuOpen = false"
            />
          </div>

          <!-- Navigation Links -->
          <nav class="space-y-2">
            <UButton
              v-for="link in links"
              :key="link.label"
              :to="link.to"
              variant="ghost"
              color="neutral"
              block
              class="justify-start"
              @click="mobileMenuOpen = false"
            >
              {{ link.label }}
            </UButton>
          </nav>

          <!-- Auth Actions -->
          <div class="space-y-2 pt-4 border-t border-neutral-200 dark:border-neutral-800">
            <template v-if="!authStore.isAuthenticated">
              <UButton
                :to="localePath('/auth')"
                color="primary"
                block
                icon="i-lucide-log-in"
                @click="mobileMenuOpen = false"
              >
                {{ t('nav.login') }}
              </UButton>
            </template>

            <template v-else>
              <!-- User Info -->
              <div class="p-3 bg-neutral-100 dark:bg-neutral-800 rounded-lg mb-4">
                <p class="text-sm font-medium text-neutral-900 dark:text-white">
                  {{ authStore.user?.fullName }}
                </p>
                <p class="text-xs text-neutral-500">
                  {{ authStore.user?.email }}
                </p>
              </div>

              <UButton
                v-if="isAdmin"
                :to="localePath('/admin')"
                variant="outline"
                color="neutral"
                block
                icon="i-lucide-shield-check"
                @click="mobileMenuOpen = false"
              >
                {{ t('nav.admin') }}
              </UButton>

              <UButton
                :to="localePath('/profile')"
                variant="outline"
                color="neutral"
                block
                icon="i-lucide-user"
                @click="mobileMenuOpen = false"
              >
                {{ t('nav.my_account') }}
              </UButton>

              <UButton
                :to="localePath('/bookings')"
                variant="outline"
                color="neutral"
                block
                icon="i-lucide-book-marked"
                @click="mobileMenuOpen = false"
              >
                {{ t('nav.bookings') }}
              </UButton>

              <UButton
                variant="outline"
                color="error"
                block
                icon="i-lucide-log-out"
                @click="handleLogout"
              >
                {{ t('nav.logout') }}
              </UButton>
            </template>
          </div>
        </div>
      </template>
    </USlideover>
  </header>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const { t } = useI18n()
const authStore = useAuthStore()
const localePath = useLocalePath()

const mobileMenuOpen = ref(false)
const isAdmin = computed(() => authStore.isAdmin)

// Cart items count (TODO: connect to actual cart store)
const cartItemsCount = ref(0)

// Navigation links
const links = computed(() => [
  {
    label: t('nav.tours'),
    to: localePath('/tours')
  },
  {
    label: t('nav.private_tours'),
    to: localePath('/private-tours')
  },
  {
    label: t('nav.about_us'),
    to: localePath('/about')
  },
  {
    label: t('nav.contact'),
    to: localePath('/contact')
  }
])

// User menu items
const userMenuItems = computed(() => [
  [
    {
      label: t('nav.my_account'),
      icon: 'i-lucide-user',
      to: localePath('/profile')
    },
    {
      label: t('nav.bookings'),
      icon: 'i-lucide-book-marked',
      to: localePath('/bookings')
    }
  ],
  ...(isAdmin.value ? [[{
    label: t('nav.admin'),
    icon: 'i-lucide-shield-check',
    to: localePath('/admin')
  }]] : []),
  [
    {
      label: t('nav.logout'),
      icon: 'i-lucide-log-out',
      click: () => authStore.logout()
    }
  ]
])

function handleLogout() {
  mobileMenuOpen.value = false
  authStore.logout()
}
</script>