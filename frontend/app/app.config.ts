export default defineAppConfig({
  ui: {
    // Colores Semánticos - Cambia estos colores para actualizar todo el tema
    colors: {
      // Primary: Acciones principales, navegación activa, elementos de marca
      primary: "green",

      // Secondary: Botones secundarios, acciones alternativas
      secondary: "blue",

      // Tertiary: Color astronómico profundo para acentos especiales
      tertiary: "deep-space",

      // Success: Mensajes de éxito, confirmaciones positivas
      success: "green",

      // Info: Alertas informativas, tooltips, notificaciones neutrales
      info: "blue",

      // Warning: Advertencias, estados pendientes, atención requerida
      warning: "yellow",

      // Error: Mensajes de error, validaciones, acciones destructivas
      error: "red",

      // Neutral: Textos, bordes, fondos, estados deshabilitados
      neutral: "slate",
    },

    button: {
      rounded: "rounded-lg",
    },
    card: {
      rounded: "rounded-xl",
      background: "bg-neutral-900/50 dark:bg-neutral-900/50",
      ring: "ring-1 ring-neutral-800",
    },
    header: {
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
