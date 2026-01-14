const STORAGE_KEY = 'nc-theme-v2'
const COOKIE_KEY = 'nc-theme-v2'

const themes = [
  'atacama-cosmic-desert',
  'atacama-nocturna',
  'atacama-cobre-lunar',
  'aurora',
  'atacama-classic',
  'cosmic'
] as const

export type Theme = (typeof themes)[number]

export function useTheme() {
  const themeCookie = useCookie<Theme>(COOKIE_KEY, {
    default: () => 'atacama-cosmic-desert',
    maxAge: 60 * 60 * 24 * 365, // 1 year
    sameSite: 'lax'
  })

  const current = useState<Theme>('nc-theme-v2', () => themeCookie.value || 'atacama-cosmic-desert')

  // Apply theme class to HTML element using useHead
  // Include 'dark' class to ensure Nuxt UI components respect dark mode
  useHead({
    htmlAttrs: {
      class: computed(() => `dark ${current.value}`)
    }
  })

  const apply = (theme: Theme) => {
    if (!themes.includes(theme)) return

    // Update state
    current.value = theme
    themeCookie.value = theme

    // Update DOM immediately on client
    if (import.meta.client) {
      // Remove all theme classes
      document.documentElement.classList.remove(...themes)
      // Add new theme class and ensure dark mode is set
      document.documentElement.classList.add('dark', theme)
      // Save to localStorage as backup
      localStorage.setItem(STORAGE_KEY, theme)
    }
  }

  // Initialize theme on client
  onMounted(() => {
    // LIMPIEZA (Opcional): Borrar la basura vieja del usuario
    if (localStorage.getItem('nc-theme')) {
      localStorage.removeItem('nc-theme')
    }

    // Lógica normal
    const saved = localStorage.getItem(STORAGE_KEY) as Theme | null
    if (saved && themes.includes(saved) && saved !== current.value) {
      apply(saved)
    } else {
      // Ensure current theme class and dark mode are applied
      document.documentElement.classList.add('dark', current.value)
    }
  })

  return {
    themes,
    currentTheme: computed(() => current.value),
    setTheme: apply
  }
}
