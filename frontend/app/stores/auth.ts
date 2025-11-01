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
    return JSON.parse(decodedJson);
  } catch (error) {
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
          this.isAuthenticated = true;
          this.token = response.token;
          
          // Decodificar el JWT para obtener user info
          const payload = decodeJwtPayload(response.token);
          if (payload) {
            this.user = {
              id: payload.sub,
              email: payload.email,
              fullName: payload.fullName,
              role: payload.roles || []
            };
          }

          if (process.client) {
            localStorage.setItem("auth_token", response.token);
            localStorage.setItem("user", JSON.stringify(this.user));
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
      this.isAuthenticated = false;
      this.user = null;
      this.token = null;

      if (process.client) {
        localStorage.removeItem("auth_token");
        localStorage.removeItem("user");
      }
      
      await navigateTo("/auth");
    },

    checkAuth() {
      this.loading = true;
      if (process.client) {
        const token = localStorage.getItem("auth_token");
        const user = localStorage.getItem("user");

        if (token && user) {
          // Decodificar y verificar si el token está expirado
          const payload = decodeJwtPayload(token);

          if (payload && payload.exp) {
            const currentTime = Math.floor(Date.now() / 1000);
            const isExpired = payload.exp < currentTime;

            if (isExpired) {
              // Token expirado, limpiar y desautenticar
              localStorage.removeItem("auth_token");
              localStorage.removeItem("user");
              this.isAuthenticated = false;
              this.user = null;
              this.token = null;
            } else {
              // Token válido
              this.isAuthenticated = true;
              this.token = token;
              this.user = JSON.parse(user);
            }
          } else {
            // Token inválido o sin exp, limpiar
            localStorage.removeItem("auth_token");
            localStorage.removeItem("user");
            this.isAuthenticated = false;
            this.user = null;
            this.token = null;
          }
        } else {
          this.isAuthenticated = false;
          this.user = null;
          this.token = null;
        }
      }
      this.loading = false;
    },
  },
});

