<script setup lang="ts">
const route = useRoute()
const router = useRouter()

const status = route.query.status as string
const isSuccess = status === 'success'

// Get booking ID from query or generate one
const bookingId = ref((route.query.bookingId as string) || `NCH-${Date.now().toString().slice(-8)}`)

function goToBookings() {
  router.push('/profile/bookings')
}

function goToHome() {
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-white dark:bg-neutral-900 flex items-center justify-center py-12">
    <UContainer>
      <div class="max-w-2xl mx-auto text-center">
        <!-- Success State -->
        <div v-if="isSuccess">
          <div class="mb-6">
            <div class="w-20 h-20 bg-success/10 rounded-full flex items-center justify-center mx-auto mb-4">
              <UIcon name="i-lucide-check-circle" class="w-12 h-12 text-success" />
            </div>
            <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
              ¡Pago Exitoso!
            </h1>
            <p class="text-lg text-neutral-600 dark:text-neutral-400">
              Tu reserva ha sido confirmada
            </p>
          </div>

          <UCard class="text-left">
            <div class="space-y-4">
              <div class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
                <p class="text-sm text-neutral-500 dark:text-neutral-400 mb-1">
                  Número de Reserva
                </p>
                <p class="text-2xl font-bold text-neutral-900 dark:text-white font-mono">
                  {{ bookingId }}
                </p>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <p class="text-neutral-500 dark:text-neutral-400 mb-1">
                    Estado
                  </p>
                  <UBadge color="success" size="lg">
                    Confirmada
                  </UBadge>
                </div>
                <div>
                  <p class="text-neutral-500 dark:text-neutral-400 mb-1">
                    Fecha de Reserva
                  </p>
                  <p class="font-medium text-neutral-900 dark:text-white">
                    {{ new Date().toLocaleDateString('es-CL', {
                      day: 'numeric',
                      month: 'long',
                      year: 'numeric'
                    }) }}
                  </p>
                </div>
              </div>

              <UDivider />

              <div class="space-y-2">
                <div class="flex items-start gap-2 text-sm">
                  <UIcon name="i-lucide-mail" class="w-5 h-5 text-primary mt-0.5" />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      Confirmación enviada
                    </p>
                    <p class="text-neutral-600 dark:text-neutral-400">
                      Hemos enviado los detalles de tu reserva a tu correo electrónico
                    </p>
                  </div>
                </div>

                <div class="flex items-start gap-2 text-sm">
                  <UIcon name="i-lucide-calendar" class="w-5 h-5 text-primary mt-0.5" />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      Recordatorios automáticos
                    </p>
                    <p class="text-neutral-600 dark:text-neutral-400">
                      Te enviaremos recordatorios 24 horas antes de cada tour
                    </p>
                  </div>
                </div>

                <div class="flex items-start gap-2 text-sm">
                  <UIcon name="i-lucide-info" class="w-5 h-5 text-primary mt-0.5" />
                  <div>
                    <p class="font-medium text-neutral-900 dark:text-white">
                      Política de cancelación
                    </p>
                    <p class="text-neutral-600 dark:text-neutral-400">
                      Cancelación gratuita hasta 24 horas antes del tour
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <template #footer>
              <div class="flex flex-col sm:flex-row gap-3">
                <UButton
                  color="primary"
                  size="lg"
                  block
                  icon="i-lucide-list"
                  @click="goToBookings"
                >
                  Ver Mis Reservas
                </UButton>
                <UButton
                  color="neutral"
                  variant="outline"
                  size="lg"
                  block
                  icon="i-lucide-home"
                  @click="goToHome"
                >
                  Volver al Inicio
                </UButton>
              </div>
            </template>
          </UCard>
        </div>

        <!-- Error State -->
        <div v-else>
          <div class="mb-6">
            <div class="w-20 h-20 bg-error/10 rounded-full flex items-center justify-center mx-auto mb-4">
              <UIcon name="i-lucide-x-circle" class="w-12 h-12 text-error" />
            </div>
            <h1 class="text-3xl font-bold text-neutral-900 dark:text-white mb-2">
              Pago Rechazado
            </h1>
            <p class="text-lg text-neutral-600 dark:text-neutral-400">
              No se pudo procesar tu pago
            </p>
          </div>

          <UCard class="text-left">
            <div class="space-y-4">
              <p class="text-neutral-600 dark:text-neutral-400">
                El procesamiento de tu pago no fue exitoso. Por favor intenta nuevamente o contacta a tu banco para más información.
              </p>

              <div class="p-4 bg-neutral-50 dark:bg-neutral-800 rounded-lg">
                <h3 class="font-medium text-neutral-900 dark:text-white mb-2">
                  Posibles razones:
                </h3>
                <ul class="space-y-1 text-sm text-neutral-600 dark:text-neutral-400">
                  <li class="flex items-center gap-2">
                    <UIcon name="i-lucide-dot" class="w-4 h-4" />
                    Fondos insuficientes
                  </li>
                  <li class="flex items-center gap-2">
                    <UIcon name="i-lucide-dot" class="w-4 h-4" />
                    Tarjeta vencida o bloqueada
                  </li>
                  <li class="flex items-center gap-2">
                    <UIcon name="i-lucide-dot" class="w-4 h-4" />
                    Datos incorrectos
                  </li>
                  <li class="flex items-center gap-2">
                    <UIcon name="i-lucide-dot" class="w-4 h-4" />
                    Límite de transacciones excedido
                  </li>
                </ul>
              </div>
            </div>

            <template #footer>
              <div class="flex flex-col sm:flex-row gap-3">
                <UButton
                  color="primary"
                  size="lg"
                  block
                  icon="i-lucide-rotate-ccw"
                  to="/checkout"
                >
                  Intentar Nuevamente
                </UButton>
                <UButton
                  color="neutral"
                  variant="outline"
                  size="lg"
                  block
                  icon="i-lucide-shopping-cart"
                  to="/cart"
                >
                  Volver al Carrito
                </UButton>
              </div>
            </template>
          </UCard>
        </div>
      </div>
    </UContainer>
  </div>
</template>
