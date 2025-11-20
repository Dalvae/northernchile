import { fileURLToPath } from "node:url";
import viteTsconfigPaths from "vite-tsconfig-paths";

const apiBaseUrl =
  process.env.NUXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

export default defineNuxtConfig({
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "nuxt-vitalizer",
    "@nuxt/fonts",
    "@nuxtjs/i18n",
    "@pinia/nuxt",
    "@vueuse/nuxt",
    "@nuxt/image",
  ],

  devtools: {
    enabled: process.env.NODE_ENV === "development",
  },

  vitalizer: {
    disablePrefetchLinks: "dynamicImports",
    disablePreloadLinks: false,
    disableStylesheets: false,
  },
  image: {
    format: ["webp"],
    quality: 80,
    domains: ["northern-chile-assets.s3.sa-east-1.amazonaws.com", "localhost"],
    dir: "public",
  },

  fonts: {
    families: [
      {
        name: "Playfair Display",
        provider: "google",
        weights: [400, 700],
        display: "optional",
        fallbacks: ["Georgia", "serif"],
      },
      {
        name: "Inter",
        provider: "google",
        weights: [400, 600],
        display: "optional",
        fallbacks: [
          "-apple-system",
          "BlinkMacSystemFont",
          "Segoe UI",
          "Arial",
          "sans-serif",
        ],
      },
    ],
    defaults: {
      weights: [400],
      styles: ["normal"],
      subsets: ["latin"],
    },
    experimental: {
      processCSSVariables: true,
    },
  },

  css: ["~/assets/css/main.css"],

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
      apiBase: apiBaseUrl,
    },
  },

  routeRules: {
    "/": { isr: 3600 },
    "/tours": { isr: 3600 },
    "/about": { prerender: true },
    "/contact": { prerender: true },
    "/private-tours": { prerender: true },
    "/auth": { ssr: false },
    "/cart": { ssr: false },
    "/admin/**": { ssr: false },
    "/profile/**": { ssr: false },
    "/bookings/**": { ssr: false },

    "/**": {
      headers: {
        "X-Frame-Options": "SAMEORIGIN",
        "X-Content-Type-Options": "nosniff",
        "Referrer-Policy": "strict-origin-when-cross-origin",
        "Permissions-Policy": "camera=(), microphone=(), geolocation=()",
        "Strict-Transport-Security": "max-age=31536000; includeSubDomains",
        "Content-Security-Policy": [
          "default-src 'self'",
          "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
          "style-src 'self' 'unsafe-inline'",
          "font-src 'self' data:",
          "img-src 'self' data: https: blob:",
          `connect-src 'self' ${apiBaseUrl} https://api.openweathermap.org`,
          "frame-ancestors 'self'",
          "base-uri 'self'",
          "form-action 'self'",
        ].join("; "),
      },
    },
  },

  compatibilityDate: "2025-01-15",

  nitro: {
    devProxy: {
      "/api": {
        target: "http://localhost:8080/api",
        changeOrigin: true,
      },
    },
  },

  vite: {
    plugins: [viteTsconfigPaths()],
    build: {
      chunkSizeWarningLimit: 1000,
      sourcemap: process.env.NODE_ENV === "production" ? "hidden" : true,
      minify: "esbuild",
      cssCodeSplit: true,
    },
    optimizeDeps: {
      include: ["vue", "@vue/runtime-core", "@vue/runtime-dom"],
      exclude: ["@fullcalendar/vue3"],
    },
    ssr: {
      noExternal: ["@nuxt/ui"],
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
      { code: "es", language: "es-CL", name: "Español", file: "es.json" },
      { code: "en", language: "en-US", name: "English", file: "en.json" },
      { code: "pt", language: "pt-BR", name: "Português", file: "pt.json" },
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

