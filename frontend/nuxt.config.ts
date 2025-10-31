import viteTsconfigPaths from "vite-tsconfig-paths";

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
    backendApiUrl: process.env.BACKEND_API_URL || "http://localhost:8080",
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE_URL || "http://localhost:8080",
    },
  },

  colorMode: {
    preference: "light",
    fallback: "light",
    dataValue: "light",
    classSuffix: "",
    storageKey: "nuxt-color-mode",
  },

  ui: {
    colors: {
      primary: "green",
      neutral: "zinc",
    },
    theme: {
      colors: [
        "primary",
        "secondary",
        "tertiary",
        "success",
        "info",
        "warning",
        "error",
        "neutral",
      ],
    },
  },

  i18n: {
    locales: [
      { code: "es", iso: "es-CL", name: "Español", file: "es.json" },
      { code: "en", iso: "en-US", name: "English", file: "en.json" },
      { code: "pt", iso: "pt-BR", name: "Português", file: "pt.json" },
    ],
    lazy: true,
    defaultLocale: "es",
    strategy: "prefix_except_default",
    baseUrl: process.env.NUXT_PUBLIC_BASE_URL || "http://localhost:3000",
    exclude: ["/admin/*"],
  },

  devtools: true,

  css: ["~/assets/css/main.css"],

  routeRules: {
    "/": { prerender: true },
    "/admin/**": { ssr: false },
    "/profile/**": { ssr: false },
    "/bookings/**": { ssr: false },
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
