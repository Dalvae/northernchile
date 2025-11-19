<template>
  <header
    class="sticky top-0 z-50 bg-[var(--color-paranal-950)]/80 backdrop-blur-md border-b border-white/10"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-20">
        <!-- Logo -->
        <div class="flex-shrink-0">
          <NuxtLink
            :to="localePath('/')"
            class="flex items-center gap-3 group"
            aria-label="Ir a la página de inicio de Northern Chile"
          >
            <UIcon
              name="i-lucide-telescope"
              class="w-8 h-8 text-primary group-hover:rotate-12 transition-transform"
            />
            <span class="font-display font-bold text-2xl text-white text-glow tracking-wide">
              Northern Chile
            </span>
          </NuxtLink>
        </div>

        <!-- Desktop Navigation -->
        <nav
          class="hidden md:flex items-center gap-2"
          role="navigation"
          aria-label="Navegación principal"
        >
          <UButton
            v-for="link in links"
            :key="link.label"
            :to="link.to"
            variant="ghost"
            color="white"
            class="font-medium text-neutral-200 hover:text-white hover:bg-white/10 transition-colors"
          >
            {{ link.label }}
          </UButton>
        </nav>

        <!-- Right Actions -->
        <div class="flex items-center gap-3">
          <!-- Language Switcher -->
          <LanguageSwitcher />

          <!-- Theme Switcher -->
          <ClientOnly>
            <ThemeSwitcher />
          </ClientOnly>

          <!-- Cart Button -->
          <UButton
            :to="localePath('/cart')"
            variant="ghost"
            color="white"
            icon="i-lucide-shopping-cart"
            :aria-label="t('nav.cart')"
            class="relative hover:bg-white/10"
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
          <ClientOnly>
            <template v-if="!authStore.isAuthenticated">
                    <UButton
                      :to="localePath('/auth')"
                      variant="ghost"
                      color="white"
                      icon="i-lucide-log-in"
                      class="hidden sm:flex hover:bg-white/10"
                    >
                {{ t("nav.login") }}
              </UButton>
            </template>

            <template v-else>
              <!-- User Menu -->
              <UDropdownMenu :items="userMenuItems">
                <UButton
                  variant="ghost"
                  color="white"
                  square
                  class="hidden sm:flex hover:bg-white/10"
                  aria-label="Menú de usuario"
                >
                  <div class="flex items-center gap-2">
                    <div
                       class="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center border border-primary/30"
                    >
                      <UIcon
                        name="i-lucide-user"
                         class="w-4 h-4 text-primary"
                      />
                    </div>
                    <UIcon
                      name="i-lucide-chevron-down"
                      class="w-4 h-4 text-neutral-400"
                    />
                  </div>
                </UButton>
              </UDropdownMenu>
            </template>
          </ClientOnly>

          <!-- Mobile Menu Button -->
          <UButton
            variant="ghost"
            color="white"
            icon="i-lucide-menu"
            class="md:hidden hover:bg-white/10"
            aria-label="Abrir menú de navegación"
            @click="mobileMenuOpen = true"
          />
        </div>
      </div>
    </div>

    <!-- Mobile Menu Drawer -->
    <USlideover
      v-model="mobileMenuOpen"
      side="right"
      :ui="{ background: 'bg-[var(--color-paranal-950)]', overlay: { background: 'bg-black/50 backdrop-blur-sm' } }"
    >
      <template #content>
        <div class="p-6 space-y-6 bg-[var(--color-paranal-950)] h-full border-l border-white/10">
          <!-- Close Button -->
          <div
             class="flex justify-between items-center pb-4 border-b border-white/10"
          >
            <h2
              class="text-xl font-display font-bold text-white"
            >
              {{ t("common.menu") || "Menú" }}
            </h2>
            <UButton
              icon="i-lucide-x"
              color="white"
              variant="ghost"
              :aria-label="t('common.close') || 'Cerrar menú'"
              @click="mobileMenuOpen = false"
            />
          </div>

          <!-- Navigation Links -->
          <nav
            class="space-y-2"
            role="navigation"
            aria-label="Navegación móvil"
          >
            <UButton
              v-for="link in links"
              :key="link.label"
              :to="link.to"
              variant="ghost"
              color="white"
              block
              class="justify-start text-lg"
              @click="mobileMenuOpen = false"
            >
              {{ link.label }}
            </UButton>
          </nav>

          <!-- Auth Actions -->
          <div
             class="space-y-4 pt-4 border-t border-white/10"
          >
            <ClientOnly>
              <template v-if="!authStore.isAuthenticated">
                <UButton
                  :to="localePath('/auth')"
                  color="primary"
                  block
                  icon="i-lucide-log-in"
                  class="cobre-glow"
                  @click="mobileMenuOpen = false"
                >
                  {{ t("nav.login") }}
                </UButton>
              </template>

              <template v-else>
                <!-- User Info -->
                <div
                  class="p-4 bg-white/5 rounded-xl mb-4 border border-white/10"
                >
                  <p
                     class="text-sm font-medium text-white"
                  >
                    {{ authStore.user?.fullName }}
                  </p>
                   <p class="text-xs text-neutral-400">
                    {{ authStore.user?.email }}
                  </p>
                </div>

                <UButton
                  v-if="isAdmin"
                  :to="localePath('/admin')"
                  variant="outline"
                  color="white"
                  block
                  icon="i-lucide-shield-check"
                  @click="mobileMenuOpen = false"
                >
                  {{ t("nav.admin") }}
                </UButton>

                <UButton
                  :to="localePath('/profile')"
                  variant="outline"
                  color="white"
                  block
                  icon="i-lucide-user"
                  @click="mobileMenuOpen = false"
                >
                  {{ t("nav.my_account") }}
                </UButton>

                <UButton
                  :to="localePath('/profile/bookings')"
                  variant="outline"
                  color="white"
                  block
                  icon="i-lucide-book-marked"
                  @click="mobileMenuOpen = false"
                >
                  {{ t("nav.bookings") }}
                </UButton>

                <UButton
                  variant="outline"
                  color="red"
                  block
                  icon="i-lucide-log-out"
                  @click="handleLogout"
                >
                  {{ t("nav.logout") }}
                </UButton>
              </template>
            </ClientOnly>
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
const cartStore = useCartStore()
const localePath = useLocalePath()

const mobileMenuOpen = ref(false)
const isAdmin = computed(() => authStore.isAdmin)

// Cart items count
const cartItemsCount = computed(() => cartStore.totalItems)

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

// Handle logout
async function handleLogout() {
  mobileMenuOpen.value = false
  await authStore.logout()
}

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
      to: localePath('/profile/bookings')
    }
  ],
  ...(isAdmin.value
    ? [
        [
          {
            label: t('nav.admin'),
            icon: 'i-lucide-shield-check',
            to: localePath('/admin')
          }
        ]
      ]
    : []),
  [
    {
      label: t('nav.logout'),
      icon: 'i-lucide-log-out',
      onClick: handleLogout
    }
  ]
])
</script>
