# Actualización de Componentes Nuxt UI

## Resumen de Cambios

Se actualizaron todos los componentes de Nuxt UI para usar las APIs correctas de Nuxt UI v4.

## Componentes Corregidos

### 1. UModal (Modal)
**Archivos afectados:**
- `pages/admin/calendar.vue`

**Cambios:**
- ✅ Removido `UCard` dentro de `UModal` (API desactualizada)
- ✅ Todo el contenido ahora va dentro del slot `#content`
- ✅ Se usa prop `title` para el título del modal
- ✅ Header, body y footer están dentro de `#content`

**Antes:**
```vue
<UModal v-model="showModal">
  <UCard>
    <template #header>...</template>
    <div>...</div>
    <template #footer>...</template>
  </UCard>
</UModal>
```

**Después:**
```vue
<UModal v-model="showModal" title="Título">
  <template #content>
    <div class="p-6 space-y-4">
      <!-- contenido -->
    </div>
    <div class="flex justify-end gap-2 px-6 pb-6 pt-4 border-t">
      <!-- footer -->
    </div>
  </template>
</UModal>
```

### 2. USlideover
**Archivos afectados:**
- `components/admin/tours/TourSlideover.vue`

**Cambios:**
- ✅ Removido `UCard` dentro de `USlideover` (API desactualizada)
- ✅ Todo el contenido ahora va dentro del slot `#content`
- ✅ Header, body y footer estructurados correctamente dentro de `#content`

### 3. TourModal.vue
**Estado:** ✅ Ya estaba correctamente implementado
- Usa `v-model:open` correctamente
- Usa slot `#content` correctamente
- Estructura apropiada

## Colores Semánticos

Se reemplazaron todos los colores hardcoded por colores semánticos según `THEMING.md`:

### Componentes de Nuxt UI
**Antes → Después:**
- `color="gray"` → `color="neutral"`
- `color="red"` → `color="error"`
- `color="yellow"` → `color="warning"`
- `color="blue"` → `color="info"`
- `color="purple"` → `color="tertiary"` (para luna llena)
- `color="green"` → `color="success"`

### Clases de Tailwind
**Antes → Después:**
- `text-gray-*` → `text-neutral-*`
- `bg-gray-*` → `bg-neutral-*`
- `border-gray-*` → `border-neutral-*`

**Archivos actualizados:**
- `pages/admin/calendar.vue`
- `pages/admin/tours.vue`
- `pages/admin/index.vue`
- `layouts/admin.vue`
- `components/admin/tours/TourModal.vue`
- `components/admin/tours/TourSlideover.vue`
- `components/admin/AdminDashboardStat.vue`
- `components/admin/DashboardStat.vue`
- `components/calendar/DayWeatherInfo.vue`
- `components/layout/TheHeader.vue`
- `components/global/LanguageSwitcher.vue`

## Badges de Clima y Condiciones

Se actualizaron todos los badges de condiciones meteorológicas con colores semánticos:

- 💨 Viento: `color="error"` (rojo → error)
- ☁️ Nublado: `color="warning"` (amarillo → warning)
- 🌧️ Lluvia: `color="info"` (azul → info)
- 🌕 Luna llena: `color="tertiary"` (púrpura → tertiary)

## Componentes Verificados

✅ **Correctos (sin cambios necesarios):**
- `TourModal.vue` - Ya usaba la API correcta

✅ **Actualizados:**
- `calendar.vue` - UModal corregido
- `TourSlideover.vue` - USlideover corregido
- `DayWeatherInfo.vue` - Colores semánticos

## Notas Importantes

1. **FullCalendar**: El calendario usa FullCalendar (no UCalendar de Nuxt UI)
2. **Clases dinámicas**: Algunas clases de FullCalendar generadas dinámicamente en JavaScript aún usan colores directos (bg-red-100, etc.) - esto es necesario para el rendering dinámico
3. **Consistencia**: Todos los componentes ahora siguen la guía de theming en `THEMING.md`

## Referencias

- [Nuxt UI Modal Docs](https://ui.nuxt.com/docs/components/modal)
- [Nuxt UI Slideover Docs](https://ui.nuxt.com/docs/components/slideover)
- [Theming Guide](./THEMING.md)
