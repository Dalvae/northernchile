<template>
  <UContainer class="min-h-screen flex items-center justify-center py-12">
    <UPageCard class="w-full max-w-md">
      <UAuthForm
        :key="formKey"
        :schema="schema"
        :fields="fields"
        :providers="providers"
        :title="isLogin ? 'Bienvenido de vuelta' : 'Crear cuenta'"
        :icon="isLogin ? 'i-lucide-log-in' : 'i-lucide-user-plus'"
        :submit="submitButton"
        separator="o"
        @submit="handleSubmit"
      >
        <template #description>
          <div class="text-center">
            <span class="text-gray-500 dark:text-gray-400">
              {{ isLogin ? "¿No tienes cuenta?" : "¿Ya tienes cuenta?" }}
            </span>
            <UButton
              variant="link"
              color="primary"
              @click="toggleForm"
              class="ml-1 p-0 h-auto"
            >
              {{ isLogin ? "Regístrate aquí" : "Inicia sesión aquí" }}
            </UButton>
          </div>
        </template>

        <template #footer>
          <p class="text-xs text-center text-gray-500 dark:text-gray-400">
            Al continuar, aceptas nuestros
            <ULink to="/terms" class="text-primary font-medium"
              >Términos de Servicio</ULink
            >
            y
            <ULink to="/privacy" class="text-primary font-medium"
              >Política de Privacidad</ULink
            >.
          </p>
        </template>
      </UAuthForm>
    </UPageCard>
  </UContainer>
</template>

<script setup lang="ts">
import type { FormSubmitEvent } from "@nuxt/ui";
import { z } from "zod";

// Estado para alternar entre login y registro
const isLogin = ref(true);
const formKey = ref(0);
const loading = ref(false);

// Función para alternar entre formularios
function toggleForm() {
  isLogin.value = !isLogin.value;
  formKey.value++; // Force re-render
}

// Schema de validación
const schema = computed(() => {
  if (isLogin.value) {
    return z.object({
      email: z.string().min(1, "Email es requerido").email("Email inválido"),
      password: z.string().min(1, "Contraseña es requerida"),
    });
  } else {
    return z.object({
      fullName: z.string().min(2, "El nombre debe tener al menos 2 caracteres"),
      email: z.string().min(1, "Email es requerido").email("Email inválido"),
      password: z
        .string()
        .min(8, "La contraseña debe tener al menos 8 caracteres"),
    });
  }
});

// Campos del formulario
const fields = computed(() => {
  if (isLogin.value) {
    return [
      {
        name: "email",
        type: "email" as const,
        label: "Email",
        placeholder: "tu@email.com",
        required: true,
      },
      {
        name: "password",
        type: "password" as const,
        label: "Contraseña",
        placeholder: "••••••••",
        required: true,
      },
    ];
  } else {
    return [
      {
        name: "fullName",
        type: "text" as const,
        label: "Nombre Completo",
        placeholder: "Tu nombre completo",
        required: true,
      },
      {
        name: "email",
        type: "email" as const,
        label: "Email",
        placeholder: "tu@email.com",
        required: true,
      },
      {
        name: "password",
        type: "password" as const,
        label: "Contraseña",
        placeholder: "••••••••",
        required: true,
      },
    ];
  }
});

// Proveedores de autenticación
const providers = [
  {
    label: "Continuar con Google",
    icon: "i-simple-icons-google",
    color: "neutral" as const,
    variant: "outline" as const,
    block: true,
    onClick: () => {
      console.log("Login with Google");
    },
  },
];

// Botón de envío - más simple
const submitButton = {
  label: "Continuar",
  block: true,
  color: "secondary" as const,
  class: "text-white",
};

// Manejo del envío
const authStore = useAuthStore();
const router = useRouter();
const toast = useToast();

async function handleSubmit(event: FormSubmitEvent<any>) {
  loading.value = true;

  try {
    if (isLogin.value) {
      console.log('Intentando login con:', { email: event.data.email });
      await authStore.login({
        email: event.data.email,
        password: event.data.password,
      });
      await router.push("/");
    } else {
      console.log('Intentando registro con:', {
        email: event.data.email,
        fullName: event.data.fullName
      });
      await authStore.register({
        email: event.data.email,
        password: event.data.password,
        fullName: event.data.fullName,
      });
      // Opcional: cambiar a login después del registro
      isLogin.value = true;
      formKey.value++;
    }
  } catch (error: any) {
    console.error('Error en auth:', error);

    // Manejo específico de errores
    let errorMessage = "Ha ocurrido un error";

    if (error.response?.status === 403) {
      errorMessage = "Credenciales incorrectas. Verifica tu email y contraseña.";
    } else if (error.response?.status === 400) {
      errorMessage = "Datos inválidos. Verifica la información ingresada.";
    } else if (error.message) {
      errorMessage = error.message;
    }

  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
/* Si hay problemas con Tailwind, usar CSS nativo */
:deep(.auth-form-container) {
  width: 100%;
}

:deep(.submit-button) {
  width: 100%;
  margin-top: 1rem;
}
</style>
