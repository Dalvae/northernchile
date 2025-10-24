export default defineAppConfig({
  ui: {
    // --- TEMA ATACAMA (ACTUAL) ---
    // Descomenta este bloque para usar la paleta Atacama
    /*
    colors: {
      primary: 'sand',
      gray: 'earth',
      secondary: 'celeste',
      accent: 'copper'
    },
    */

    // --- TEMA COSMIC ---
    // Descomenta este bloque para usar la paleta Cosmic
    colors: {
      primary: "nebula",
      gray: "deep-space",
      secondary: "celeste",
      accent: "starburst",
    },

    button: {
      rounded: "rounded-lg",
    },
    card: {
      rounded: "rounded-xl",
      background: "bg-gray-900/50 dark:bg-gray-900/50", // Fondo más oscuro
      ring: "ring-1 ring-gray-800",
    },
    header: {
      wrapper:
        "!bg-gray-900 !dark:bg-gray-900 backdrop-blur-md border-b border-gray-800",
      base: "!bg-transparent",
      background: "!bg-transparent",
      container: "!bg-transparent",
    },
  },
  // Forzar tema oscuro
  colorMode: {
    preference: "dark",
  },
});
