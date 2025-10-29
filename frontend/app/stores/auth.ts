import { defineStore } from "pinia";

interface User {
  id: string;
  email: string;
  fullName: string;
  role: string[];
}

// Función auxiliar para decodificar el payload del JWT de forma segura
function decodeJwtPayload(token: string): any | null {
  try {
    const payloadBase64 = token.split(".")[1];
    if (!payloadBase64) return null;
    const decodedJson = atob(
      payloadBase64.replace(/-/g, "+").replace(/_/g, "/"),
    );
    const payload = JSON.parse(decodedJson);
    console.log('JWT Payload:', payload);
    return payload;
  } catch (error) {
    console.error("Failed to decode JWT:", error);
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isAuthenticated: false,
    user: null as User | null,
    token: null as string | null,
    loading: true, // Start in a loading state
  }),

  getters: {
    isAdmin(state): boolean {
      if (!state.user?.role) return false;
      return (
        state.user.role.includes("ROLE_SUPER_ADMIN") ||
        state.user.role.includes("ROLE_PARTNER_ADMIN")
      );
    },
  },

  actions: {
    async login(credentials: { email: string; password: string }) {
      this.loading = true;
      const toast = useToast();
      try {
        // La llamada a la API ahora devuelve solo el usuario, la cookie se maneja en segundo plano.
        const response: { user: User } = await $fetch('/api/auth/login', {
          method: "POST",
          body: credentials,
        });
        
        if (response && response.user) {
          this.isAuthenticated = true;
          this.user = response.user;
          // El token se maneja a través de la cookie, ya no necesitamos guardarlo en el state.
          this.token = "cookie_set"; // Placeholder value

          if (process.client) {
            localStorage.setItem("user", JSON.stringify(response.user));
          }

          toast.add({
            title: "¡Bienvenido!",
            description: "Has iniciado sesión correctamente.",
            color: "green",
          });
        } else {
          throw new Error("La respuesta del login es inválida.");
        }
      } catch (error: any) {
        let errorMessage = error.data?.message || "Error en el login";
        if (error.statusCode === 403 || error.statusCode === 401) {
          errorMessage = "Credenciales inválidas. Verifica tu email y contraseña.";
        }
        toast.add({
          title: "Error de Autenticación",
          description: errorMessage,
          color: "red",
        });
        throw error;
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
        await $fetch(`${apiBase}/api/auth/register`, {
          method: "POST",
          body: userData,
          headers: {
            "Content-Type": "application/json",
          },
        });

        toast.add({
          title: "Registro Exitoso",
          description: "Tu cuenta ha sido creada. Ahora puedes iniciar sesión.",
          color: "green",
        });
      } catch (error: any) {
        let errorMessage = error.data?.message || "Error en el registro";
        if (error.statusCode === 409) {
          errorMessage = "El correo electrónico ya está en uso.";
        }
        toast.add({
          title: "Error de Registro",
          description: errorMessage,
          color: "red",
        });
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async logout() {
      // Llamamos a un endpoint de logout en el servidor para que borre la cookie httpOnly.
      await $fetch('/api/auth/logout', { method: 'POST' });

      this.isAuthenticated = false;
      this.user = null;
      this.token = null;

      if (process.client) {
        localStorage.removeItem("user");
      }
      
      // Aseguramos la redirección después de que todo se haya limpiado.
      await navigateTo("/auth");
    },

    checkAuth() {
      this.loading = true;
      if (process.client) {
        const token = localStorage.getItem("auth_token");
        const user = localStorage.getItem("user");
        if (token && user) {
          this.isAuthenticated = true;
          this.token = token;
          this.user = JSON.parse(user);
        } else {
          // Si no hay token en localStorage, nos aseguramos de que todo esté limpio.
          this.logout();
        }
      }
      this.loading = false;
    },
  },
});

