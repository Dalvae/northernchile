# Checkout Frictionless Mejorado

## Resumen

Mejorar el flujo de checkout para:
1. Mostrar toggle visible Login/Registro (UTabs)
2. Verificar si email existe con debounce 1s
3. Crear cuenta al pasar de Step 1 a Step 2 (no al pagar)
4. Manejar conflicto de carritos con modal de preview

## Cambios por Archivo

### Backend

#### 1. `AuthController.java`
Agregar endpoint para verificar email:
```java
@GetMapping("/check-email")
public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
    boolean exists = userRepository.existsByEmail(email);
    return ResponseEntity.ok(Map.of("exists", exists));
}
```
- Rate limit: 5 req/min por IP (usar @RateLimiter si existe o implementar simple)

### Frontend

#### 2. `server/api/auth/check-email.get.ts` (nuevo)
Proxy al backend:
```typescript
export default defineEventHandler(async (event) => {
  const query = getQuery(event)
  const config = useRuntimeConfig()

  return await $fetch(`${config.apiBaseUrl}/auth/check-email`, {
    params: { email: query.email }
  })
})
```

#### 3. `components/checkout/CartConflictModal.vue` (nuevo)
Modal que muestra:
- Carrito actual (izquierda)
- Carrito guardado (derecha)
- 3 botones: "Usar actual" | "Usar guardado" | "Combinar"

Props:
- `currentCart: CartItem[]`
- `savedCart: CartItem[]`
- `open: boolean`

Emits:
- `@select="'current' | 'saved' | 'merge'"`

#### 4. `pages/checkout.vue`
Refactor Step 1:

**Nuevas refs:**
```typescript
const authMode = ref<'register' | 'login'>('register')
const emailCheckResult = ref<{ exists: boolean } | null>(null)
const isCheckingEmail = ref(false)
const showCartConflictModal = ref(false)
const savedUserCart = ref<CartItem[] | null>(null)
```

**Debounce email check:**
```typescript
const debouncedCheckEmail = useDebounceFn(async (email: string) => {
  if (!email || !isValidEmail(email)) return
  isCheckingEmail.value = true
  try {
    emailCheckResult.value = await $fetch('/api/auth/check-email', {
      params: { email }
    })
  } finally {
    isCheckingEmail.value = false
  }
}, 1000)

watch(() => contactForm.value.email, debouncedCheckEmail)
```

**Modificar `nextStep()`:**
```typescript
async function nextStep() {
  if (currentStep.value === 1) {
    if (!step1Valid.value) {
      // ... validación existente
      return
    }

    // Crear cuenta o login ANTES de pasar a Step 2
    if (!authStore.isAuthenticated) {
      try {
        if (authMode.value === 'register') {
          toast.add({ color: 'info', title: t('auth.creating_account') })
          await authStore.register({ ... })
          await authStore.login({ ... })
        } else {
          toast.add({ color: 'info', title: t('auth.logging_in') })
          await authStore.login({ ... })

          // Verificar conflicto de carrito
          const userCart = await cartStore.fetchUserCart()
          if (userCart && hasCartConflict(userCart, cartStore.cart)) {
            savedUserCart.value = userCart
            showCartConflictModal.value = true
            return // No avanzar hasta resolver conflicto
          }
        }

        // Fetch saved participants ahora que está logueado
        await fetchSavedParticipants()
        toast.add({ color: 'success', title: t('auth.welcome') })

      } catch (error) {
        // Manejar errores
        return
      }
    }

    initializeParticipants()
  }

  // ... resto de la lógica
  currentStep.value++
}
```

**Template Step 1:**
```vue
<UTabs v-model="authMode" :items="[
  { label: t('auth.create_account'), value: 'register' },
  { label: t('auth.already_have_account'), value: 'login' }
]" />

<!-- Campos comunes: nombre, email, teléfono -->

<!-- Mensaje de verificación de email -->
<p v-if="emailCheckResult?.exists && authMode === 'register'"
   class="text-warning-500 text-sm">
  {{ t('checkout.email_exists') }}
  <button @click="authMode = 'login'" class="underline">
    {{ t('auth.login_instead') }}
  </button>
</p>

<p v-if="!emailCheckResult?.exists && authMode === 'login'"
   class="text-warning-500 text-sm">
  {{ t('checkout.email_not_found') }}
  <button @click="authMode = 'register'" class="underline">
    {{ t('auth.register_instead') }}
  </button>
</p>

<!-- Campos de password según modo -->
<template v-if="authMode === 'register'">
  <input v-model="contactForm.password" type="password" />
  <input v-model="contactForm.confirmPassword" type="password" />
</template>
<template v-else>
  <input v-model="contactForm.password" type="password" />
</template>
```

#### 5. `stores/cart.ts`
Agregar método para obtener carrito del usuario:
```typescript
async fetchUserCart(): Promise<CartItem[] | null> {
  // Llamar al backend para obtener carrito asociado al usuario
}
```

Agregar método para resolver conflicto:
```typescript
async resolveCartConflict(choice: 'current' | 'saved' | 'merge', savedCart: CartItem[]) {
  if (choice === 'current') {
    // Mantener actual, guardar en backend
    await this.syncCartToBackend()
  } else if (choice === 'saved') {
    // Reemplazar con guardado
    this.cart.items = savedCart
  } else {
    // Merge: combinar items
    this.cart.items = mergeCartItems(this.cart.items, savedCart)
  }
  await this.syncCartToBackend()
}
```

#### 6. Traducciones
Agregar en `es.json`, `en.json`, `pt.json`:
```json
{
  "auth": {
    "create_account": "Crear cuenta",
    "already_have_account": "Ya tengo cuenta",
    "creating_account": "Creando tu cuenta...",
    "logging_in": "Iniciando sesión...",
    "login_instead": "Iniciar sesión",
    "register_instead": "Crear cuenta",
    "welcome": "¡Bienvenido!"
  },
  "checkout": {
    "email_exists": "Este email ya tiene cuenta.",
    "email_not_found": "No encontramos esta cuenta.",
    "cart_conflict_title": "Tienes un carrito guardado",
    "cart_current": "Carrito actual",
    "cart_saved": "Carrito guardado",
    "use_current": "Usar actual",
    "use_saved": "Usar guardado",
    "merge_carts": "Combinar"
  }
}
```

## Orden de Implementación

1. **Backend**: Endpoint check-email (5 min)
2. **Frontend proxy**: server/api/auth/check-email.get.ts (2 min)
3. **Traducciones**: Agregar textos (5 min)
4. **checkout.vue**: Refactor Step 1 con tabs y verificación email (20 min)
5. **CartConflictModal.vue**: Crear componente (15 min)
6. **cart store**: Métodos para conflicto (10 min)
7. **Testing manual**: Probar flujos (10 min)

## Flujos a Probar

1. Usuario nuevo → crea cuenta → pasa a Step 2 logueado
2. Usuario existente en tab "Crear cuenta" → mensaje + switch a login
3. Email no existe en tab "Login" → mensaje + switch a registro
4. Login con carrito guardado diferente → modal de conflicto
5. Login con carrito guardado igual → sin modal, continúa
6. Login sin carrito guardado → sin modal, continúa
