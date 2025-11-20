import { fileURLToPath } from "node:url";
import viteTsconfigPaths from "vite-tsconfig-paths";

export default defineNuxtConfig({
  modules: [
    "@nuxt/eslint",
    "@nuxt/ui",
    "nuxt-vitalizer",
    "@nuxt/fonts",
    "@nuxtjs/i18n",
    "@pinia/nuxt",
    "@vueuse/nuxt",
  ],

  devtools: {
    enabled: process.env.NODE_ENV === "development",
  },

  // Nuxt Vitalizer - LCP optimization without breaking CSR pages
  vitalizer: {
    // Disable prefetch for dynamic imports (improves LCP, enabled by default)
    disablePrefetchLinks: 'dynamicImports',
    // Keep preload links for now (can disable if needed)
    disablePreloadLinks: false,
    // Keep stylesheets for now to ensure CSR pages work (can optimize later)
    disableStylesheets: false,
  },

  // Font optimization with @nuxt/fonts
  fonts: {
    families: [
      {
        name: "Playfair Display",
        provider: "google",
        weights: [400, 700],
        // Fallback to Georgia for serif display font (reduces CLS)
        fallbacks: ["Georgia", "serif"],
      },
      {
        name: "Inter",
        provider: "google",
        weights: [400, 600],
        // Fallback to system fonts (reduces CLS)
        fallbacks: ["-apple-system", "BlinkMacSystemFont", "Segoe UI", "Arial", "sans-serif"],
      },
    ],
    defaults: {
      weights: [400],
      styles: ["normal"],
      subsets: ["latin"],
      // Use 'optional' instead of 'swap' to prevent FOIT/FOUT
      // Browser decides if font is ready or uses fallback immediately
      display: "optional",
    },
    // Local download for better performance (self-hosted WOFF2)
    local: true,
    // Automatic font metric optimization with fontaine
    experimental: {
      processCSSVariables: true,
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
    "/tours": {
      isr: 3600, // Regenerate every hour (ISR allows CSS inlining like prerender)
    },
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
    headers: {
      // Security headers for production
      "X-Frame-Options": "SAMEORIGIN",
      "X-Content-Type-Options": "nosniff",
      "Referrer-Policy": "strict-origin-when-cross-origin",
      "Cross-Origin-Opener-Policy": "same-origin",
      "Permissions-Policy": "camera=(), microphone=(), geolocation=()",
      // HSTS: Force HTTPS (enable in production)
      "Strict-Transport-Security": "max-age=31536000; includeSubDomains",
      // CSP: Content Security Policy
      "Content-Security-Policy": [
        "default-src 'self'",
        "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
        "style-src 'self' 'unsafe-inline'",
        "font-src 'self' data:",
        "img-src 'self' data: https: blob:",
        "connect-src 'self' http://localhost:8080 https://api.openweathermap.org",
        "frame-ancestors 'self'",
        "base-uri 'self'",
        "form-action 'self'",
      ].join("; "),
    },
  },

  vite: {
    plugins: [viteTsconfigPaths()],
    build: {
      rollupOptions: {
        output: {
          manualChunks(id) {
            // Aggressive vendor splitting for better caching and smaller bundles
            if (id.includes('node_modules')) {
              // Vue core - critical
              if (id.includes('vue') || id.includes('@vue')) {
                return 'vendor-vue'
              }

              // Nuxt UI and related - split into smaller chunks
              if (id.includes('@nuxt/ui')) {
                return 'vendor-ui'
              }
              if (id.includes('@headlessui') || id.includes('@floating-ui')) {
                return 'vendor-ui-deps'
              }

              // i18n - can be large with all locales
              if (id.includes('i18n') || id.includes('intl')) {
                return 'vendor-i18n'
              }

              // Icons - separate iconify from lucide
              if (id.includes('@iconify-json/lucide')) {
                return 'vendor-icons-lucide'
              }
              if (id.includes('@iconify')) {
                return 'vendor-icons'
              }

              // Date libraries
              if (id.includes('date-fns')) {
                return 'vendor-date'
              }

              // FullCalendar - large library
              if (id.includes('fullcalendar')) {
                return 'vendor-calendar'
              }

              // API client / Axios
              if (id.includes('axios') || id.includes('api-client')) {
                return 'vendor-api'
              }

              // Pinia store
              if (id.includes('pinia')) {
                return 'vendor-store'
              }

              // VueUse utilities
              if (id.includes('@vueuse')) {
                return 'vendor-vueuse'
              }

              // Other vendor code
              return 'vendor'
            }
          },
        },
      },
      // Increase chunk size warning limit
      chunkSizeWarningLimit: 600,
      // Enable source maps for production debugging
      sourcemap: process.env.NODE_ENV === 'production' ? 'hidden' : true,
      // Use esbuild for faster, safer minification (avoids terser bugs)
      minify: 'esbuild',
      // Enable CSS code splitting
      cssCodeSplit: true,
      // Aggressive tree-shaking
      modulePreload: {
        polyfill: false, // Don't include polyfill if not needed
      },
    },
    // Optimize dependencies
    optimizeDeps: {
      include: ['vue', '@vue/runtime-core', '@vue/runtime-dom'],
      exclude: ['@fullcalendar/vue3'], // Lazy-loaded, don't pre-bundle
    },
    ssr: {
      // Don't externalize these packages for better tree-shaking
      noExternal: ['@nuxt/ui', '@headlessui/vue'],
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
