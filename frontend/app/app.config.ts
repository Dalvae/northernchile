export default defineAppConfig({
  ui: {
    // Tema Astronómico - Inspirado en los cielos del desierto de Atacama
    colors: {
      // Primary: Acciones principales, navegación activa, elementos de marca
      // Nebula - Azul profundo espacial
      primary: "nebula",

      // Secondary: Botones secundarios, acciones alternativas
      // Celeste - Azul celeste complementario
      secondary: "celeste",

      // Tertiary: Color astronómico profundo para acentos especiales
      // Deep Space - Gris espacial profundo
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
      // Deep Space - para mantener el tema oscuro espacial
      neutral: "deep-space",
    },

    button: {
      rounded: "rounded-lg",
    },
    card: {
      rounded: "rounded-xl",
    },
    header: {
      base: "!bg-transparent",
      background: "!bg-transparent",
      container: "!bg-transparent",
    },
  },
});
