export default defineAppConfig({
  ui: {
    colors: {
      primary: "nebula", // El azul profundo para acciones principales.
      secondary: "celeste", // Un azul más claro para acentos.
      tertiary: "starburst", // Un magenta para detalles especiales (si lo definieras).
      neutral: "deep-space", // El gris espacial para fondos, textos y bordes.

      success: "emerald",
      info: "sky",
      warning: "amber",
      error: "rose",
    },

    button: {
      rounded: "rounded-lg",
    },
    card: {
      rounded: "rounded-xl",
    },
  },
});

