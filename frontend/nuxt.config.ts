import { fileURLToPath } from "node:url";
import viteTsconfigPaths from "vite-tsconfig-paths";

export default defineNuxtConfig({
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "@nuxtjs/i18n",
    "@pinia/nuxt",
    "@vueuse/nuxt",
  ],

  devtools: {
    enabled: false,
  },

  // Completely disable devtools in production
  hooks: {
    "vite:extendConfig"(config) {
      if (import.meta.env.PROD) {
        config.define = config.define || {};
        config.define.__VUE_PROD_DEVTOOLS__ = false;
        config.define.__VUE_PROD_HYDRATION_MISMATCH_DETAILS__ = false;
      }
    },
  },
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

  css: ["~/assets/css/main.css", "~/assets/css/theme.css"],

  /* colorMode: {
    preference: "dark",
    fallback: "dark",
    storageKey: "nuxt-color-mode",
    classSuffix: "",
    storage: "localStorage",
  }, */

  ui: {
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

  runtimeConfig: {
    public: {
      apiBase:
        import.meta.env.NUXT_PUBLIC_API_BASE_URL || "http://localhost:8080",
    },
  },

  routeRules: {
    "/": { prerender: true },
    "/auth": { ssr: false },
    "/cart": { ssr: false },
    "/admin/**": { ssr: false },
    "/profile/**": { ssr: false },
    "/bookings/**": { ssr: false },
  },

  compatibilityDate: "2025-01-15",

  nitro: {
    devProxy: {
      "/api": {
        target: "http://localhost:8080/api",
        changeOrigin: true,
      },
    },
    // Exclude Vue DevTools from production builds
    moduleSideEffects(id) {
      return !id.includes("@vue/devtools") && !id.includes("perfect-debounce");
    },
    replace: {
      __VUE_PROD_DEVTOOLS__: "false",
    },
  },

  vite: {
    plugins: [viteTsconfigPaths()],
    // Fix SSR issues with Vue DevTools in production
    ssr: {
      noExternal: ["perfect-debounce"],
    },
    optimizeDeps: {
      exclude: ["@vue/devtools-kit", "@vue/devtools-api", "perfect-debounce"],
    },
  },

  eslint: {
    config: {
      stylistic: {
        commaDangle: "never",
        braceStyle: "1tbs",
      },
    },
  },

  alias: {
    "api-client": fileURLToPath(new URL("./lib/api-client", import.meta.url)),
  },

  i18n: {
    locales: [
      { code: "es", iso: "es-CL", name: "Español", file: "es.json" },
      { code: "en", iso: "en-US", name: "English", file: "en.json" },
      { code: "pt", iso: "pt-BR", name: "Português", file: "pt.json" },
    ],
    defaultLocale: "es",
    strategy: "prefix_except_default",
    baseUrl: import.meta.env.NUXT_PUBLIC_BASE_URL || "http://localhost:3000",
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: "i18n_redirected",
      redirectOn: "root",
    },
  },
});
