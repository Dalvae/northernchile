# Imágenes Pendientes para Subir al Bucket S3

Las siguientes imágenes necesitan ser subidas a `/public/images/` o al bucket S3:

## Imágenes Faltantes

1. **hero-bg.jpg**
   - Ubicación: `/public/images/hero-bg.jpg`
   - Usado en: `components/home/HeroSection.vue`
   - Descripción: Imagen de fondo del hero section (cielo estrellado de Atacama)
   - Dimensiones recomendadas: 1920x1080px o superior
   - Formato: JPG

2. **astro-experience.jpg**
   - Ubicación: `/public/images/astro-experience.jpg`
   - Usado en: `components/home/Experience.vue`
   - Descripción: Imagen para la sección "Astronomía con Alma"
   - Dimensiones recomendadas: 1200x900px (aspect ratio 4:3)
   - Formato: JPG

3. **geo-experience.jpg**
   - Ubicación: `/public/images/geo-experience.jpg`
   - Usado en: `components/home/Experience.vue`
   - Descripción: Imagen para la sección "Paisajes Vivos" (formaciones geológicas del desierto)
   - Dimensiones recomendadas: 1200x900px (aspect ratio 4:3)
   - Formato: JPG

## Imágenes Existentes

✅ `tour-placeholder.svg` - Ya existe
✅ `private-tours-hero.png` - Ya existe

## Instrucciones de Subida

### Opción 1: Subir a /public/images/
```bash
# Copiar las imágenes a la carpeta public/images/
cp /ruta/de/las/imagenes/*.jpg frontend/public/images/
```

### Opción 2: Subir al Bucket S3
```bash
# Subir a S3 (requiere AWS CLI configurado)
aws s3 cp hero-bg.jpg s3://northern-chile-assets/images/
aws s3 cp astro-experience.jpg s3://northern-chile-assets/images/
aws s3 cp geo-experience.jpg s3://northern-chile-assets/images/
```

Si subes al S3, actualizar las rutas en los componentes:
- Cambiar `/images/hero-bg.jpg` por `https://northern-chile-assets.s3.sa-east-1.amazonaws.com/images/hero-bg.jpg`
- O usar la configuración de Nuxt Image que ya maneja dominios de S3 automáticamente

## Notas

- Las imágenes deben ser optimizadas para web (calidad 80-90%)
- El sitio usa Nuxt Image con formato WebP automático
- Se generan versiones con blur placeholder automáticamente
