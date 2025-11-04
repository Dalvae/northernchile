import { defineStore } from "pinia";
import { useLocalStorage } from "@vueuse/core"; // Importa el composable

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
    return JSON.parse(decodedJson);
  } catch (error) {
    return null;
  }
}

export const useAuthStore = defineStore("auth", {
  // --- PASO 1: Refactorizar el `state` ---
  // En lugar de usar refs normales, usamos useLocalStorage.
  // Esto sincroniza automáticamente el estado con localStorage.
  state: () => ({
    token: useLocalStorage<string | null>('auth_token', null),
    user: useLocalStorage<User | null>('user', null),
    loading: true,
  }),

  getters: {
    // `isAuthenticated` ahora es un getter que reacciona a los cambios en `token`.
    isAuthenticated(state): boolean {
      return !!state.token && !!state.user;
    },
    isAdmin(state): boolean {
      if (!state.user?.role) return false;
      return (
        state.user.role.includes("ROLE_SUPER_ADMIN") ||
        state.user.role.includes("ROLE_PARTNER_ADMIN")
      );
    },
  },

  actions: {
    async login(credentials: { email: string; password:string }) {
      this.loading = true;
      const toast = useToast();
      try {
        const config = useRuntimeConfig();
        const apiBase = config.public.apiBase;
        
        // Llamada directa al backend Java
        const response = await $fetch<any>(`${apiBase}/api/auth/login`, {
          method: "POST",
          body: credentials,
        });
        
        if (response && response.token) {
          const payload = decodeJwtPayload(response.token);
          
          // ¡MAGIA! Simplemente asigna los valores.
          // useLocalStorage se encargará de guardarlos en localStorage.
          this.token = response.token;
          if (payload) {
            this.user = {
              id: payload.sub,
              email: payload.email,
              fullName: payload.fullName,
              role: payload.roles || []
            };
          }

          toast.add({
            title: "¡Bienvenido!",
            description: "Has iniciado sesión correctamente.",
            color: "green",
          });
        }
      } catch (error: any) {
        let errorMessage = error.data?.message || "Error en el login";
        if (error.statusCode === 403 || error.statusCode === 401) {
          errorMessage = "Credenciales inválidas.";
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
      // Simplemente establece los valores a null.
      // useLocalStorage se encargará de eliminarlos de localStorage.
      this.token = null;
      this.user = null;
      
      await navigateTo("/auth");
    },

    // --- PASO 5: Reemplazar `checkAuth` con una inicialización más simple ---
    // Esta función solo necesita verificar la expiración del token al inicio.
    initializeAuth() {
      this.loading = true;
      if (this.token) {
        const payload = decodeJwtPayload(this.token);
        if (payload && payload.exp) {
          const currentTime = Math.floor(Date.now() / 1000);
          if (payload.exp < currentTime) {
            // Si el token está expirado, simplemente cerramos sesión.
            this.logout();
          }
        } else {
          // Si el token es inválido, cerramos sesión.
          this.logout();
        }
      }
      this.loading = false;
    },
  },
});

