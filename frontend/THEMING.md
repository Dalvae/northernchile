# Guía de Theming - Northern Chile

## Colores Semánticos

Esta aplicación usa el sistema de colores semánticos de Nuxt UI. **NUNCA** uses colores hardcodeados como `red`, `green`, `blue` o clases de Tailwind como `bg-red-500`, `text-blue-400`, etc.

### Configuración Central

Todos los colores se configuran en `/app/app.config.ts`:

```typescript
export default defineAppConfig({
  ui: {
    colors: {
      primary: "green",        // Acciones principales, navegación activa, elementos de marca
      secondary: "blue",       // Botones secundarios, acciones alternativas
      tertiary: "deep-space",  // Color astronómico profundo para acentos especiales
      success: "green",        // Mensajes de éxito, confirmaciones positivas
      info: "blue",            // Alertas informativas, tooltips, notificaciones neutrales
      warning: "yellow",       // Advertencias, estados pendientes, atención requerida
      error: "red",            // Mensajes de error, validaciones, acciones destructivas
      neutral: "slate",        // Textos, bordes, fondos, estados deshabilitados
    },
  },
});
```

Para cambiar el tema completo, simplemente cambia estos valores.

### Color Personalizado: Deep Space

El color `deep-space` es un color personalizado definido en `app/assets/css/themes/cosmic.css` usando el formato Oklch y CSS `@theme`. Este color azul-gris profundo está inspirado en el cielo nocturno del desierto de Atacama.

Los colores personalizados se definen usando variables CSS:
```css
/* app/assets/css/themes/cosmic.css */
@theme {
  --color-deep-space-50: oklch(95% 0.01 250);
  --color-deep-space-100: oklch(90% 0.02 250);
  /* ... */
  --color-deep-space-950: oklch(5% 0.01 250);
}
```

## Uso en Componentes

### ✅ CORRECTO - Usar colores semánticos

#### En componentes de Nuxt UI:
```vue
<!-- Botones -->
<UButton color="primary" />
<UButton color="secondary" />
<UButton color="tertiary" />
<UButton color="error" />
<UButton color="success" />

<!-- Toasts -->
toast.add({
  title: "Éxito",
  color: "success",
  icon: "i-lucide-check-circle"
});

toast.add({
  title: "Error",
  color: "error",
  icon: "i-lucide-x-circle"
});

<!-- Badges -->
<UBadge color="success">Publicado</UBadge>
<UBadge color="warning">Borrador</UBadge>
<UBadge color="error">Cancelado</UBadge>
<UBadge color="tertiary">Premium</UBadge>
```

#### En clases de Tailwind:
```vue
<!-- Textos -->
<p class="text-neutral-900 dark:text-white">Título</p>
<p class="text-neutral-500 dark:text-neutral-400">Subtítulo</p>

<!-- Fondos -->
<div class="bg-neutral-800">Contenido</div>
<div class="bg-neutral-900/50">Contenido con opacidad</div>

<!-- Bordes -->
<div class="border border-neutral-200 dark:border-neutral-700">Contenido</div>

<!-- Estados de error/éxito en texto -->
<p class="text-error-500">Mensaje de error</p>
<p class="text-success-500">Mensaje de éxito</p>
<p class="text-tertiary-400">Acento especial</p>

<!-- Fondos con tertiary -->
<div class="bg-tertiary-900/20">Overlay astronómico</div>
```

### ❌ INCORRECTO - Hardcodear colores

```vue
<!-- MAL: color hardcodeado -->
<UButton color="red" />
<UButton color="green" />

<!-- MAL: clases de Tailwind específicas -->
<p class="text-gray-900">Título</p>
<p class="text-red-500">Error</p>
<div class="bg-blue-800">Contenido</div>
<div class="border-green-200">Contenido</div>

<!-- MAL: toasts con colores hardcodeados -->
toast.add({
  color: "red"  // ❌
});
```

## Mapeado de Colores

| Caso de Uso | Color Semántico | Ejemplo |
|------------|----------------|---------|
| Botón principal, CTA | `primary` | Crear, Guardar, Confirmar |
| Botón secundario | `secondary` | Ver más, Detalles |
| Acento astronómico/especial | `tertiary` | Elementos destacados, badges premium |
| Mensaje de éxito | `success` | "Tour creado exitosamente" |
| Información general | `info` | Tooltips, ayudas |
| Advertencia | `warning` | "Esta acción es irreversible" |
| Error, eliminación | `error` | "Error al guardar", Botón eliminar |
| Textos, bordes, UI neutral | `neutral` | Textos normales, bordes, divisores |

## Estados de Elementos

### Status Badges
```vue
<UBadge
  :color="status === 'PUBLISHED' ? 'success' : status === 'DRAFT' ? 'warning' : 'neutral'"
>
  {{ statusLabel }}
</UBadge>
```

### Botones de Acción
```vue
<!-- Crear/Guardar -->
<UButton color="primary" label="Guardar" />

<!-- Acción especial/astronómica -->
<UButton color="tertiary" label="Ver bajo las estrellas" />

<!-- Editar -->
<UButton color="neutral" variant="ghost" icon="i-lucide-pencil" />

<!-- Eliminar -->
<UButton color="error" variant="ghost" icon="i-lucide-trash-2" />

<!-- Cancelar -->
<UButton color="neutral" variant="ghost" label="Cancelar" />
```

## Cambiando el Tema

Para experimentar con diferentes temas, edita `app.config.ts`:

```typescript
// Tema Oscuro Moderno
colors: {
  primary: "violet",
  secondary: "indigo",
  tertiary: "deep-space",  // Siempre puedes usar deep-space
  success: "emerald",
  info: "sky",
  warning: "amber",
  error: "rose",
  neutral: "zinc",
}

// Tema Natural/Orgánico
colors: {
  primary: "lime",
  secondary: "teal",
  tertiary: "emerald",
  success: "green",
  info: "cyan",
  warning: "orange",
  error: "red",
  neutral: "stone",
}

// Tema Astronómico Completo (Recomendado)
colors: {
  primary: "purple",
  secondary: "indigo",
  tertiary: "deep-space",  // Color personalizado astronómico
  success: "green",
  info: "blue",
  warning: "yellow",
  error: "red",
  neutral: "slate",
}
```

Los colores de Tailwind disponibles son: `slate`, `gray`, `zinc`, `neutral`, `stone`, `red`, `orange`, `amber`, `yellow`, `lime`, `green`, `emerald`, `teal`, `cyan`, `sky`, `blue`, `indigo`, `violet`, `purple`, `fuchsia`, `pink`, `rose`.

**Nota**: El color `deep-space` es un color personalizado definido en `app/assets/css/themes/cosmic.css`.

## Beneficios

✅ **Consistencia**: Todos los componentes usan los mismos colores
✅ **Mantenibilidad**: Cambia el tema completo en un solo lugar
✅ **Flexibilidad**: Prueba diferentes paletas sin tocar componentes
✅ **Accesibilidad**: Nuxt UI maneja contraste y legibilidad automáticamente
✅ **Dark Mode**: Los colores se adaptan automáticamente al modo oscuro

## Creando Colores Personalizados

Para agregar nuevos colores personalizados:

1. Define el color en `app/assets/css/themes/cosmic.css` (o crea tu propio archivo de tema):
```css
@theme {
  --color-mi-color-50: oklch(95% 0.03 280);
  --color-mi-color-100: oklch(90% 0.06 280);
  /* ... define todos los tonos del 50 al 950 ... */
  --color-mi-color-950: oklch(10% 0.01 280);
}
```

2. Importa el archivo de tema en `app/assets/css/main.css`:
```css
@import "./themes/mi-tema.css";
```

3. Registra el color en `nuxt.config.ts` si quieres usarlo como color semántico:
```typescript
ui: {
  theme: {
    colors: [
      'primary',
      'secondary',
      'tertiary',
      'mi-color',  // Tu nuevo color
      // ...
    ]
  }
}
```

4. Úsalo en `app.config.ts`:
```typescript
colors: {
  tertiary: "mi-color"
}
```
