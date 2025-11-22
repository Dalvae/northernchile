const STORAGE_KEY = 'nc-theme'
const COOKIE_KEY = 'nc-theme'

const themes = [
  'atacama-nocturna',
  'atacama-cobre-lunar',
  'atacama-cosmic-desert',
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

  const current = useState<Theme>('nc-theme', () => themeCookie.value || 'atacama-cosmic-desert')

  // Apply theme class to HTML element using useHead
  useHead({
    htmlAttrs: {
      class: computed(() => current.value)
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
      // Add new theme class
      document.documentElement.classList.add(theme)
      // Save to localStorage as backup
      localStorage.setItem(STORAGE_KEY, theme)
    }
  }

  // Initialize theme on client
  onMounted(() => {
    const saved = localStorage.getItem(STORAGE_KEY) as Theme | null
    if (saved && themes.includes(saved) && saved !== current.value) {
      apply(saved)
    } else {
      // Ensure current theme class is applied
      document.documentElement.classList.add(current.value)
    }
  })

  return {
    themes,
    currentTheme: computed(() => current.value),
    setTheme: apply
  }
}


    
