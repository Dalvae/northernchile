export default defineAppConfig({
  ui: {
    // Tema Astronómico - Inspirado en los cielos del desierto de Atacama
    colors: {
      // Primary: Acciones principales, navegación activa, elementos de marca
      // Morado profundo como el cielo nocturno
      primary: "purple",

      // Secondary: Botones secundarios, acciones alternativas
      // Índigo para complementar el morado
      secondary: "indigo",

      // Tertiary: Color astronómico profundo para acentos especiales
      // Color personalizado definido en cosmic.css
      tertiary: "deep-space",

      // Success: Mensajes de éxito, confirmaciones positivas
      success: "emerald",

      // Info: Alertas informativas, tooltips, notificaciones neutrales
      info: "sky",

      // Warning: Advertencias, estados pendientes, atención requerida
      warning: "amber",

      // Error: Mensajes de error, validaciones, acciones destructivas
      error: "rose",

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
});
