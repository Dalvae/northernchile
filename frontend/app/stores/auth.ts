import { defineStore } from "pinia";

interface User {
  id: string;
  email: string;
  fullName: string;
  role: string[];
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isAuthenticated: false,
    user: null as User | null,
    token: null as string | null,
    loading: false,
  }),

  actions: {
    async login(credentials: { email: string; password: string }) {
      this.loading = true;
      const toast = useToast();
      try {
        const config = useRuntimeConfig();
        const apiBase = config.public.apiBase;
        const response: any = await $fetch(`${apiBase}/api/auth/login`, {
          method: 'POST',
          body: credentials,
          headers: {
            'Content-Type': 'application/json',
          },
        });

        return response;
      } catch (error: any) {
        let errorMessage = error.data?.message || 'Error en el login';
        if (error.statusCode === 403) {
          errorMessage = 'Acceso denegado. Credenciales inválidas.';
        } else if (error.statusCode === 401) {
          errorMessage = 'No autorizado. Por favor, verifica tus credenciales.';
        }
        toast.add({
          title: 'Error',
          description: errorMessage,
          color: 'error',
        });
        throw new Error(errorMessage);
      } finally {
        this.loading = false;
      }
    },

    async register(userData: {
      email: string;
      password: string;
      fullName: string;
    }) {
      this.loading = true;
      const toast = useToast();
      try {
        const config = useRuntimeConfig();
        const apiBase = config.public.apiBase;
        const response: any = await $fetch(`${apiBase}/api/auth/register`, {
          method: 'POST',
          body: userData,
          headers: {
            'Content-Type': 'application/json',
          },
        });

        toast.add({
          title: 'Éxito',
          description: 'Registro exitoso. Por favor, inicia sesión.',
          color: 'green',
        });

        return response;
      } catch (error: any) {
        let errorMessage = error.data?.message || 'Error en el registro';
        if (error.statusCode === 409) { // Assuming 409 Conflict for existing user
          errorMessage = 'El usuario ya existe. Por favor, inicia sesión o usa otro correo.';
        } else if (error.statusCode === 400) { // Assuming 400 Bad Request for validation errors
          errorMessage = 'Datos de registro inválidos. Por favor, verifica la información.';
        }
        toast.add({
          title: 'Error',
          description: errorMessage,
          color: 'error',
        });
        throw new Error(errorMessage);
      } finally {
        this.loading = false;
      }
    },

    logout() {
      this.isAuthenticated = false;
      this.user = null;
      this.token = null;
      localStorage.removeItem("auth_token");
      localStorage.removeItem("user");
      const router = useRouter();
      router.push("/auth");
    },

    checkAuth() {
      if (process.client) {
        const token = localStorage.getItem("auth_token");
        const user = localStorage.getItem("user");
        if (token && user) {
          this.isAuthenticated = true;
          this.token = token;
          this.user = JSON.parse(user);
        }
      }
    },
  },
});
