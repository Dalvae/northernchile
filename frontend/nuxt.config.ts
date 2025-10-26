import viteTsconfigPaths from "vite-tsconfig-paths";

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  app: {
    head: {
      link: [
        {
          rel: "stylesheet",
          href: "https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Inter:wght@400;600&display=swap",
        },
      ],
    },
  },
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "@nuxtjs/i18n",
    "@pinia/nuxt",
    "@vueuse/nuxt",
  ],

  runtimeConfig: {
    // Estas claves SOLO están disponibles en el servidor de Nuxt.
    // Nunca se exponen al cliente.
    backendApiUrl: process.env.BACKEND_API_URL || "http://localhost:8080",

    // Las claves públicas SÍ se exponen al cliente.
    public: {
      // Hacemos la URL base de la API accesible en el lado del cliente.
      apiBase: process.env.NUXT_PUBLIC_API_BASE_URL || "http://localhost:8080",
    },
  },

  ui: {
    colorMode: { preference: "dark" },
  },

  i18n: {
    locales: [
      { code: "es", iso: "es-ES", name: "Español", file: "es.json" },
      { code: "en", iso: "en-US", name: "English", file: "en.json" },
      { code: "pt", iso: "pt-PT", name: "Português", file: "pt.json" },
    ],
    lazy: true,
    defaultLocale: "es",
    strategy: "prefix_except_default",
    baseUrl: process.env.NUXT_PUBLIC_BASE_URL || "http://localhost:3000",
    exclude: ["/admin/*"],
  },

  devtools: {
    enabled: true,
  },

  css: ["~/assets/css/main.css"],

  routeRules: {
    "/": { prerender: true },
  },

  compatibilityDate: "2025-01-15",

  eslint: {
    config: {
      stylistic: {
        commaDangle: "never",
        braceStyle: "1tbs",
      },
    },
  },

  vite: {
    plugins: [viteTsconfigPaths()],
  },
});
