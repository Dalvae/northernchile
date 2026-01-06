import { fileURLToPath } from 'node:url'
import viteTsconfigPaths from 'vite-tsconfig-paths'

const apiBaseUrl
  = process.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'

const isDev = process.env.NODE_ENV === 'development'

export default defineNuxtConfig({
  modules: [
    '@nuxt/eslint',
    '@nuxt/ui',
    '@nuxt/fonts',
    '@nuxtjs/i18n',
    '@pinia/nuxt',
    '@vueuse/nuxt',
    '@nuxt/image',
    'nuxt-gtag',
    '@nuxtjs/seo',
    'nuxt-vitalizer'
  ],

  devtools: {
    enabled: process.env.NODE_ENV === 'development'
  },
  app: {
    head: {
      link: [{ rel: 'icon', type: 'image/svg+xml', href: '/favicon.svg' }]
    }
  },

  site: {
    url: process.env.NUXT_PUBLIC_BASE_URL || 'https://www.northernchile.com',
    name: 'Northern Chile Tours',
    description: 'Tours astronómicos en San Pedro de Atacama, Chile. Stargazing, expediciones al desierto de Atacama, lagunas altiplánicas y más. Reserva online.',
    defaultLocale: 'es'
  },

  colorMode: {
    preference: 'dark',
    fallback: 'dark',
    dataValue: 'dark',
    storageKey: 'nuxt-color-mode',
    classSuffix: ''
  },

  ui: {
    theme: {
      colors: [
        'primary',
        'secondary',
        'tertiary',
        'success',
        'info',
        'warning',
        'error',
        'neutral'
      ]
    }
  },

  runtimeConfig: {
    public: {
      apiBase: apiBaseUrl,
      baseUrl: process.env.NUXT_PUBLIC_BASE_URL || 'https://www.northernchile.com',
      fbAppId: process.env.NUXT_PUBLIC_FB_APP_ID || ''
    }
  },

  alias: {
    'api-client': fileURLToPath(new URL('./lib/api-client', import.meta.url))
  },

  routeRules: {
    // Nuxt internal paths - no special handling needed
    '/_nuxt/**': {},
    '/_nuxt_icon/**': {},
    '/__og_image__/**': { headers: { 'cache-control': 'public, max-age=3600, stale-while-revalidate=60' } },
    '/': { swr: 300 },
    '/tours': { swr: 300 },
    '/tours/**': { swr: 300 },
    '/about': { prerender: true },
    '/contact': { prerender: true },
    '/private-tours': { prerender: true },
    '/auth': { ssr: false },
    '/cart': { ssr: false },
    '/admin/**': { ssr: false },
    '/profile/**': { ssr: false },
    '/bookings/**': { ssr: false },

    '/**': {
      headers: {
        'X-Frame-Options': 'SAMEORIGIN',
        'X-Content-Type-Options': 'nosniff',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
        'Permissions-Policy': 'camera=(), microphone=(), geolocation=()',
        'Strict-Transport-Security': 'max-age=31536000; includeSubDomains',
        'Content-Security-Policy': isDev
          ? [
              'default-src \'self\'',
              'script-src \'self\' \'unsafe-inline\' \'unsafe-eval\' https://www.googletagmanager.com https://www.google-analytics.com',
              'style-src \'self\' \'unsafe-inline\'',
              'font-src \'self\' data:',
              'img-src \'self\' data: https: blob: http://localhost:3000 https://www.google-analytics.com',
              `connect-src 'self' ${apiBaseUrl} ws://localhost:* http://localhost:* https://api.openweathermap.org https://api.iconify.design https://www.google-analytics.com https://analytics.google.com`,
              'frame-ancestors \'self\'',
              'base-uri \'self\'',
              'form-action \'self\' https://webpay3gint.transbank.cl https://webpay3g.transbank.cl https://www.mercadopago.cl https://www.mercadopago.com.br',
              'object-src \'none\''
            ].join('; ')
          : [
              'default-src \'self\'',
              'script-src \'self\' \'unsafe-inline\' \'unsafe-eval\' https://www.googletagmanager.com https://www.google-analytics.com',
              'style-src \'self\' \'unsafe-inline\'',
              'font-src \'self\' data:',
              'img-src \'self\' data: https: blob: https://www.google-analytics.com',
              `connect-src 'self' ${apiBaseUrl} https://api.openweathermap.org https://api.iconify.design https://www.google-analytics.com https://analytics.google.com`,
              'frame-ancestors \'self\'',
              'base-uri \'self\'',
              'form-action \'self\' https://webpay3gint.transbank.cl https://webpay3g.transbank.cl https://www.mercadopago.cl https://www.mercadopago.com.br',
              'object-src \'none\''
            ].join('; ')
      }
    }
  },
  sourcemap: {
    server: false,
    client: false
  },

  features: {
    inlineStyles: true
  },

  experimental: {
    renderJsonPayloads: true,
    asyncContext: true
  },

  compatibilityDate: '2025-01-15',

  nitro: {
    compressPublicAssets: true,
    minify: true,
    devProxy: {
      // Proxy /api to backend, but exclude Nuxt internal paths
      '/api': {
        target: 'http://localhost:8080/api',
        changeOrigin: true
      }
    }
  },

  vite: {
    plugins: [viteTsconfigPaths()],
    resolve: {
      alias: {
        'api-client': fileURLToPath(new URL('./lib/api-client', import.meta.url))
      }
    },
    build: {
      chunkSizeWarningLimit: 1000,
      sourcemap: process.env.NODE_ENV === 'production' ? 'hidden' : true,
      minify: 'esbuild',
      cssCodeSplit: true
    },
    optimizeDeps: {
      include: [
        'vue',
        '@vue/runtime-core',
        '@vue/runtime-dom',
        '@fullcalendar/core',
        '@fullcalendar/daygrid',
        '@fullcalendar/interaction',
        '@fullcalendar/vue3'
      ]
    },
    ssr: {
      noExternal: ['@nuxt/ui']
    }
  },

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  },

  fonts: {
    families: [
      {
        name: 'Playfair Display',
        provider: 'google',
        weights: [400, 700],
        display: 'optional',
        fallbacks: ['Georgia', 'serif']
      },
      {
        name: 'Inter',
        provider: 'google',
        weights: [400, 600],
        display: 'optional',
        fallbacks: [
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'Arial',
          'sans-serif'
        ]
      }
    ],
    defaults: {
      weights: [400],
      styles: ['normal'],
      subsets: ['latin']
    },
    experimental: {
      processCSSVariables: true
    }
  },

  gtag: {
    id: process.env.NUXT_PUBLIC_GTAG_ID || '',
    enabled: process.env.NODE_ENV === 'production',
    initMode: 'manual', // Don't auto-initialize, we'll do it selectively
    config: {
      anonymize_ip: true,
      send_page_view: true,
      cookie_flags: 'SameSite=None;Secure'
    }
  },

  i18n: {
    locales: [
      { code: 'es', language: 'es-CL', name: 'Español', file: 'es.json' },
      { code: 'en', language: 'en-US', name: 'English', file: 'en.json' },
      { code: 'pt', language: 'pt-BR', name: 'Português', file: 'pt.json' }
    ],
    defaultLocale: 'es',
    strategy: 'prefix_except_default',
    baseUrl: import.meta.env.NUXT_PUBLIC_BASE_URL || 'http://localhost:3000',
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n_redirected',
      redirectOn: 'root',
      fallbackLocale: 'en'
    }
  },
  image: {
    format: ['webp', 'avif'],
    quality: 80,
    densities: [1, 2],
    screens: {
      xs: 320,
      sm: 640,
      md: 768,
      lg: 1024,
      xl: 1280,
      xxl: 1536
    },
    domains: [
      'northern-chile-assets.s3.sa-east-1.amazonaws.com',
      'northern-chile-assets.s3.amazonaws.com',
      'localhost'
    ],
    provider: 'ipx',
    presets: {
      hero: {
        modifiers: {
          format: 'webp',
          quality: 90,
          width: 1920,
          height: 1080,
          fit: 'cover'
        }
      },
      thumbnail: {
        modifiers: {
          format: 'webp',
          quality: 75,
          width: 400,
          height: 300,
          fit: 'cover'
        }
      },
      card: {
        modifiers: {
          format: 'webp',
          quality: 80,
          width: 800,
          height: 600,
          fit: 'cover'
        }
      }
    }
  },

  ogImage: {
    defaults: {
      renderer: 'satori',
      cacheMaxAgeSeconds: 60 * 60 * 24
    },
    fonts: [
      'Inter:400',
      'Inter:700',
      'Playfair+Display:700'
    ],
    runtimeCacheStorage: {
      driver: 'memory'
    }
  },

  robots: {
    disallow: [
      '/admin',
      '/profile',
      '/bookings',
      '/cart',
      '/checkout',
      '/auth',
      '/payment'
    ]
  },

  schemaOrg: {
    identity: {
      type: 'Organization',
      name: 'Northern Chile Tours',
      url: process.env.NUXT_PUBLIC_BASE_URL || 'https://www.northernchile.com',
      logo: '/images/logo.png'
    }
  },

  sitemap: {
    sources: [
      '/api/__sitemap__/urls'
    ],
    exclude: ['/admin/**', '/profile/**']
  },

  vitalizer: {
    disablePrefetchLinks: true,
    disablePreloadLinks: true,
    disableStylesheets: 'entry'
  }
})
