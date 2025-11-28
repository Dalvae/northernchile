<template>
  <header
    class="sticky top-0 z-50 bg-neutral-950/80 backdrop-blur-md border-b border-white/10"
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
            <span
              class="font-display font-bold text-2xl text-white text-glow tracking-wide"
            >
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
            color="neutral"
            class="font-medium text-neutral-200 hover:text-white hover:bg-white/10 transition-colors"
          >
            {{ link.label }}
          </UButton>
        </nav>

        <!-- Right Actions -->
        <div class="flex items-center gap-3">
          <!-- DESKTOP ONLY -->
          <div class="hidden md:flex items-center gap-3">
            <LanguageSwitcher />

            <UButton
              :to="localePath('/cart')"
              variant="ghost"
              color="neutral"
              icon="i-lucide-shopping-cart"
              :aria-label="t('nav.cart')"
              class="relative hover:bg-white/10"
            >
              <ClientOnly>
                <UBadge
                  v-if="cartItemsCount > 0"
                  :label="cartItemsCount.toString()"
                  color="primary"
                  size="xs"
                  class="absolute -top-1 -right-1"
                />
              </ClientOnly>
            </UButton>

            <ClientOnly>
              <div v-if="!authStore.isAuthenticated">
                <UButton
                  :to="localePath('/auth')"
                  variant="ghost"
                  color="neutral"
                  icon="i-lucide-log-in"
                  class="hover:bg-white/10"
                >
                  {{ t("nav.login") }}
                </UButton>
              </div>
              <div v-else>
                <UDropdownMenu :items="userMenuItems">
                  <UButton
                    variant="ghost"
                    color="neutral"
                    square
                    class="hover:bg-white/10"
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
                    </div>
                  </UButton>
                </UDropdownMenu>
              </div>

              <template #fallback>
                <UButton
                  variant="ghost"
                  color="neutral"
                  icon="i-lucide-log-in"
                  class="hover:bg-white/10 opacity-70 text-white"
                  disabled
                >
                  {{ t("nav.login") }}
                </UButton>
              </template>
            </ClientOnly>
          </div>

          <!-- Mobile Menu -->
          <USlideover
            v-model:open="mobileMenuOpen"
            side="right"
          >
            <UButton
              variant="ghost"
              color="neutral"
              icon="i-lucide-menu"
              class="md:hidden hover:bg-white/10"
              aria-label="Abrir menú"
            />

            <template #content>
              <LayoutMobileMenu
                :links="links"
                @close="mobileMenuOpen = false"
              />
            </template>
          </USlideover>
        </div>
      </div>
    </div>
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
const cartItemsCount = computed(() => cartStore.totalItems)

const links = computed(() => [
  { label: t('nav.tours'), to: localePath('/tours') },
  { label: t('nav.private_tours'), to: localePath('/private-tours') },
  { label: t('nav.about_us'), to: localePath('/about') },
  { label: t('nav.contact'), to: localePath('/contact') }
])

async function handleLogout() {
  mobileMenuOpen.value = false
  await authStore.logout()
}

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
  [{ label: t('nav.logout'), icon: 'i-lucide-log-out', onClick: handleLogout }]
])
</script>
