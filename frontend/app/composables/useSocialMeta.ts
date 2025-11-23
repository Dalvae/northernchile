export const useSocialMeta = () => {
  const config = useRuntimeConfig()

  // Esta función devuelve SIEMPRE una URL absoluta válida
  const getSocialImage = (imagePath?: string | null) => {
    // 1. Definir la imagen por defecto
    const defaultImage = '/images/og-default.jpg'

    // 2. Si no pasamos nada, usamos la default. Si pasamos algo, lo usamos.
    const targetImage = imagePath || defaultImage

    // 3. Si viene de S3 (empieza con http), devolver tal cual
    if (targetImage.startsWith('http')) {
      return targetImage
    }

    // 4. Construir URL absoluta para imágenes locales
    // Asegúrate de que NUXT_PUBLIC_BASE_URL esté en tu .env (ej: https://www.northernchile.cl)
    const baseUrl = config.public.baseUrl || 'https://www.northernchile.cl'
    const cleanBase = baseUrl.replace(/\/$/, '')
    const cleanPath = targetImage.startsWith('/') ? targetImage : `/${targetImage}`

    return `${cleanBase}${cleanPath}`
  }

  return {
    getSocialImage
  }
}
