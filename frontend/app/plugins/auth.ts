import { useAuthStore } from '~/stores/auth';

export default defineNuxtPlugin(nuxtApp => {
  if (process.client) {
    const authStore = useAuthStore();
    authStore.checkAuth();
  }
});