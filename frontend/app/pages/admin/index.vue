<template>
  <UContainer class="min-h-screen flex items-center justify-center py-12">
    <UPageCard class="w-full max-w-md">
      <UAuthForm
        :schema="schema"
        :fields="fields"
        :providers="providers"
        :title="isLogin ? 'Bienvenido de vuelta' : 'Crear cuenta'"
        :description="
          isLogin
            ? 'Ingresa a tu cuenta para gestionar tus reservas'
            : 'Regístrate para comenzar a reservar experiencias'
        "
        icon="i-lucide-user"
        :submit="submitButton"
        separator="o"
        @submit="handleSubmit"
      >
        <template #description>
          <span v-if="isLogin">
            ¿No tienes cuenta?
            <UButton
              variant="link"
              color="primary"
              @click="isLogin = false"
              class="p-0"
            >
              Regístrate
            </UButton>
          </span>
          <span v-else>
            ¿Ya tienes cuenta?
            <UButton
              variant="link"
              color="primary"
              @click="isLogin = true"
              class="p-0"
            >
              Inicia Sesión
            </UButton>
          </span>
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

// Schema de validación con Zod
const schema = z.object({
  email: z.string().email("Email inválido"),
  password: z.string().min(8, "La contraseña debe tener al menos 8 caracteres"),
  ...(isLogin.value
    ? {}
    : {
        fullName: z
          .string()
          .min(2, "El nombre debe tener al menos 2 caracteres"),
      }),
});

type Schema = z.output<typeof schema>;

// Campos del formulario
const fields = computed(() => {
  const baseFields = [
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

  if (!isLogin.value) {
    baseFields.unshift({
      name: "fullName",
      type: "text" as const,
      label: "Nombre Completo",
      placeholder: "Tu nombre completo",
      required: true,
    });
  }

  return baseFields;
});

// Proveedores de autenticación (OAuth)
const providers = [
  {
    label: "Continuar con Google",
    icon: "i-simple-icons-google",
    color: "neutral" as const,
    variant: "outline" as const,
    block: true,
    onClick: () => {
      // Lógica para login con Google
      console.log("Login with Google");
    },
  },
];

// Texto del botón de envío
const submitButton = computed(() => ({
  label: isLogin.value ? "Iniciar Sesión" : "Crear cuenta",
  block: true,
  color: "primary" as const,
}));

// Manejo del envío del formulario
const authStore = useAuthStore();
const router = useRouter();
const toast = useToast();

async function handleSubmit(event: FormSubmitEvent<Schema>) {
  try {
    if (isLogin.value) {
      await authStore.login({
        email: event.data.email,
        password: event.data.password,
      });
      toast.add({ title: "¡Bienvenido!", color: "green" });
    } else {
      await authStore.register({
        email: event.data.email,
        password: event.data.password,
        fullName: event.data.fullName,
      });
      toast.add({ title: "¡Cuenta creada!", color: "green" });
    }

    // Redirigir al home o al dashboard
    await router.push("/");
  } catch (error: any) {
    toast.add({
      title: "Error",
      description: error.message || "Ha ocurrido un error",
      color: "red",
    });
  }
}
</script>
