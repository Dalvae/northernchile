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

        this.isAuthenticated = true;
        this.user = response.user;
        this.token = response.token;

        if (process.client) {
          localStorage.setItem('auth_token', response.token);
          localStorage.setItem('user', JSON.stringify(response.user));
        }

        return response;
      } catch (error: any) {
        throw new Error(error.data?.message || 'Error en el login');
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
        return response;
      } catch (error: any) {
        throw new Error(error.data?.message || 'Error en el registro');
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
