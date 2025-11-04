### **Guía de Implementación: Widgets de TripAdvisor**

#### **Paso 0: Tu Acción Requerida (¡Necesito esto de ti!)**

Antes de empezar, necesito que me proporciones el **enlace URL completo de tu perfil de negocio en TripAdvisor**.

Se verá algo así: `https://www.tripadvisor.cl/Attraction_Review-g312677-dXXXXXXX-Reviews-Northern_Chile-San_Pedro_de_Atacama...`

Con ese enlace, podremos generar los códigos exactos de los widgets. Mientras tanto, usaré ejemplos genéricos.

---

#### **Paso 1: Generar el Código del Widget en TripAdvisor**

Esto lo debes hacer tú una vez que tengas el enlace a tu perfil:

1.  **Ve al Centro de Widgets de TripAdvisor:** [https://www.tripadvisor.com/Widgets](https://www.tripadvisor.com/Widgets)
2.  **Pega la URL de tu perfil** de "Northern Chile" donde te lo pida.
3.  **Selecciona los Widgets:** Busca y configura los siguientes dos widgets. Te recomiendo personalizarlos para que se ajusten al diseño de la web (versión oscura, tamaño, etc.).
    - **Widget de Puntuación y Opiniones:** Ideal para la página de detalle de cada tour.
    - **Widget de Fragmentos de Opiniones:** Perfecto para la página de inicio.
4.  **Copia el Código:** Por cada widget, TripAdvisor te dará un fragmento de código que se verá más o menos así:

    ```html
    <!-- Este es un DIV que contendrá el widget -->
    <div id="TA_selfserveprop123" class="TA_selfserveprop">
      <!-- ... contenido ... -->
    </div>

    <!-- Este es el SCRIPT que carga el widget -->
    <script
      async
      src="https://www.jscache.com/wejs?wtype=selfserveprop&uniq=123&locationId=...&lang=es&rating=true..."
      data-loadtrk="true"
    ></script>
    ```

    De este código, solo necesitaremos la URL que está en `src="..."`.

---

#### **Paso 2: Crear el Componente Reutilizable en Nuxt**

Ahora, creamos un componente inteligente en el frontend para manejar estos widgets de forma segura.

**Crea el siguiente archivo:** `frontend/components/common/TripAdvisorWidget.vue`

**Pega este código dentro del archivo:**

```vue
<script setup lang="ts">
import { onMounted, ref } from "vue";

const props = defineProps<{
  // La URL completa del script que te da TripAdvisor
  scriptSrc: string;
}>();

// Usamos una referencia para apuntar al div que contendrá el widget
const widgetContainer = ref<HTMLElement | null>(null);

onMounted(() => {
  // El truco aquí es inyectar el script de TripAdvisor dinámicamente
  // después de que el componente se haya montado en el navegador.
  // Esto evita problemas con el renderizado del lado del servidor (SSR) de Nuxt.

  if (widgetContainer.value) {
    const script = document.createElement("script");
    script.src = props.scriptSrc;
    script.async = true;
    script.setAttribute("data-loadtrk", "true"); // Atributo que usa TripAdvisor

    // Limpiamos el contenedor por si acaso y añadimos el nuevo script
    widgetContainer.value.innerHTML = "";
    widgetContainer.value.appendChild(script);
  }
});
</script>

<template>
  <div ref="widgetContainer" class="tripadvisor-widget-container">
    <!-- El script de TripAdvisor llenará este div automáticamente -->
    <!-- Puedes añadir un esqueleto de carga si quieres -->
    <USkeleton class="h-24 w-full" />
  </div>
</template>
```

---

#### **Paso 3: Integrar el Widget en tus Páginas**

Ahora que tenemos nuestro componente, usarlo es muy fácil.

**A. En la Página de Inicio (para mostrar las últimas reseñas):**

Edita el archivo `frontend/app/pages/index.vue`. Busca un buen lugar, por ejemplo, después de la sección de "Tours Destacados", y añade esto:

```vue
<!-- ... dentro de la sección donde quieras mostrar las reseñas ... -->
<UContainer>
  <div class="max-w-3xl mx-auto">
    <h2 class="text-3xl font-bold text-center mb-8">Lo que dicen nuestros viajeros</h2>
    <ClientOnly>
      <CommonTripAdvisorWidget
        script-src="URL_DEL_SCRIPT_DEL_WIDGET_DE_FRAGMENTOS_DE_OPINIONES"
      />
    </ClientOnly>
  </div>
</UContainer>
```

**Importante:** La etiqueta `<ClientOnly>` es crucial. Le dice a Nuxt que este componente solo debe renderizarse en el navegador del cliente, lo cual es necesario para los scripts de terceros como el de TripAdvisor.

**B. En la Página de Detalle del Tour (para mostrar la puntuación):**

Edita el archivo `frontend/app/pages/tours/[slug]/index.vue`. Búsca la `UCard` que funciona como barra lateral de reserva y añade el widget allí.

```vue
<!-- ... dentro de la UCard de la barra lateral ... -->
<template #default>
  <div class="space-y-4">
    <div>
      <p class="text-sm text-neutral-600">Desde</p>
      <p class="text-3xl font-bold">{{ formatPrice(tour.price) }}</p>
    </div>

    <!-- AQUÍ INTEGRAMOS EL WIDGET -->
    <div class="py-4">
      <ClientOnly>
        <CommonTripAdvisorWidget
          script-src="URL_DEL_SCRIPT_DEL_WIDGET_DE_PUNTUACION"
        />
      </ClientOnly>
    </div>

    <UButton
      label="Ver Horarios Disponibles"
      color="primary"
      block
      size="lg"
      @click="goToSchedule"
    />
  </div>
</template>
```

### **Resumen del Flujo de Trabajo**

1.  **Tú:** Me das la URL de tu perfil de TripAdvisor.
2.  **Tú:** Vas al centro de widgets y generas los dos códigos que te mencioné (puntuación y fragmentos).
3.  **Tú:** Me pasas las dos URLs de los scripts que aparecen en el atributo `src`.
4.  **Yo (o tú):** Reemplazamos las placeholders `URL_DEL_SCRIPT_...` en los archivos `.vue` con las URLs reales.

¡Y listo! Con estos pasos, tendrás tu prueba social de TripAdvisor integrada de una manera limpia, segura y optimizada para Nuxt 3.
