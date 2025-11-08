const STORAGE_KEY = 'nc-theme'

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
  const current = useState<Theme>('nc-theme', () => 'atacama-nocturna')

  const apply = (theme: Theme) => {
    if (!themes.includes(theme)) return

    current.value = theme

    if (import.meta.client) {
      // Remover todas las clases de temas anteriores
      document.documentElement.classList.remove(...themes)
      // Agregar la clase del tema actual
      document.documentElement.classList.add(theme)
      // Guardar en localStorage
      localStorage.setItem(STORAGE_KEY, theme)
    }
  }

  // Inicializar tema en el cliente
  if (import.meta.client) {
    const saved = localStorage.getItem(STORAGE_KEY) as Theme | null
    const initial = saved && themes.includes(saved) ? saved : current.value
    apply(initial)
  }

  return {
    themes,
    currentTheme: computed(() => current.value),
    setTheme: apply
  }
}


    
