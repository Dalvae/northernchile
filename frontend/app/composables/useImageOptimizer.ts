/**
 * Composable para optimizar imágenes en el cliente antes de subirlas
 * Convierte a WebP con compresión inteligente según tamaño
 * Solo se usa en admin - no afecta rendimiento de páginas públicas
 */
export const useImageOptimizer = () => {
  /**
   * Crea un elemento Image desde un File
   */
  const createImageFromFile = (file: File): Promise<HTMLImageElement> => {
    return new Promise((resolve, reject) => {
      const img = new Image()
      const url = URL.createObjectURL(file)

      img.onload = () => {
        URL.revokeObjectURL(url)
        resolve(img)
      }

      img.onerror = () => {
        URL.revokeObjectURL(url)
        reject(new Error('Error loading image'))
      }

      img.src = url
    })
  }

  /**
   * Convierte canvas a Blob con compresión WebP
   */
  const canvasToBlob = (canvas: HTMLCanvasElement, quality: number): Promise<Blob> => {
    return new Promise((resolve, reject) => {
      canvas.toBlob(
        (blob) => {
          if (blob) {
            resolve(blob)
          } else {
            reject(new Error('Error converting canvas to blob'))
          }
        },
        'image/webp',
        quality
      )
    })
  }

  /**
   * Determina la calidad de compresión según el tamaño del archivo original
   */
  const getCompressionQuality = (fileSize: number): number => {
    const MB = 1024 * 1024

    // Imágenes muy grandes: compresión alta
    if (fileSize > 5 * MB) return 0.70
    // Imágenes grandes: compresión media-alta
    if (fileSize > 2 * MB) return 0.75
    // Imágenes medianas: compresión media
    if (fileSize > 500 * 1024) return 0.80
    // Imágenes pequeñas: compresión baja
    return 0.85
  }

  /**
   * Calcula nuevas dimensiones respetando aspect ratio
   */
  const calculateDimensions = (
    originalWidth: number,
    originalHeight: number,
    maxDimension: number = 4000
  ): { width: number; height: number } => {
    // Si la imagen es pequeña, mantener dimensiones originales
    if (originalWidth <= maxDimension && originalHeight <= maxDimension) {
      return { width: originalWidth, height: originalHeight }
    }

    // Calcular ratio para redimensionar
    const ratio = Math.min(
      maxDimension / originalWidth,
      maxDimension / originalHeight
    )

    return {
      width: Math.round(originalWidth * ratio),
      height: Math.round(originalHeight * ratio)
    }
  }

  /**
   * Optimiza una imagen: convierte a WebP, comprime y redimensiona si es necesario
   */
  const optimizeImage = async (
    file: File,
    options?: {
      maxDimension?: number
      forceOptimize?: boolean
    }
  ): Promise<{ file: File; originalSize: number; newSize: number; savings: number }> => {
    const maxDimension = options?.maxDimension || 4000
    const forceOptimize = options?.forceOptimize || false

    // Si no es una imagen, retornar sin procesar
    if (!file.type.startsWith('image/')) {
      return {
        file,
        originalSize: file.size,
        newSize: file.size,
        savings: 0
      }
    }

    // Si ya es WebP pequeño y no forzamos optimización, retornar sin procesar
    if (!forceOptimize && file.type === 'image/webp' && file.size < 500 * 1024) {
      return {
        file,
        originalSize: file.size,
        newSize: file.size,
        savings: 0
      }
    }

    try {
      const originalSize = file.size

      // Cargar imagen
      const img = await createImageFromFile(file)

      // Determinar calidad de compresión
      const quality = getCompressionQuality(originalSize)

      // Calcular nuevas dimensiones
      const { width, height } = calculateDimensions(img.width, img.height, maxDimension)

      // Crear canvas y dibujar imagen redimensionada
      const canvas = document.createElement('canvas')
      canvas.width = width
      canvas.height = height

      const ctx = canvas.getContext('2d')
      if (!ctx) {
        throw new Error('Could not get canvas context')
      }

      // Usar mejor calidad de interpolación
      ctx.imageSmoothingEnabled = true
      ctx.imageSmoothingQuality = 'high'

      // Dibujar imagen en canvas
      ctx.drawImage(img, 0, 0, width, height)

      // Convertir a WebP con compresión
      const blob = await canvasToBlob(canvas, quality)

      // Crear nuevo nombre de archivo con extensión .webp
      const newFileName = file.name.replace(/\.[^.]+$/, '.webp')

      // Crear nuevo File desde el Blob
      const optimizedFile = new File([blob], newFileName, {
        type: 'image/webp',
        lastModified: Date.now()
      })

      const newSize = optimizedFile.size
      const savings = ((originalSize - newSize) / originalSize) * 100

      return {
        file: optimizedFile,
        originalSize,
        newSize,
        savings
      }
    } catch (error) {
      console.error('Error optimizing image:', error)
      // Si falla la optimización, retornar archivo original
      return {
        file,
        originalSize: file.size,
        newSize: file.size,
        savings: 0
      }
    }
  }

  /**
   * Optimiza múltiples imágenes en batch
   */
  const optimizeImages = async (
    files: File[],
    options?: {
      maxDimension?: number
      forceOptimize?: boolean
      onProgress?: (current: number, total: number) => void
    }
  ): Promise<Array<{ file: File; originalSize: number; newSize: number; savings: number }>> => {
    const results = []
    const total = files.length

    for (let i = 0; i < files.length; i++) {
      const result = await optimizeImage(files[i], options)
      results.push(result)

      if (options?.onProgress) {
        options.onProgress(i + 1, total)
      }
    }

    return results
  }

  /**
   * Formatea el tamaño de archivo en formato legible
   */
  const formatFileSize = (bytes: number): string => {
    const KB = 1024
    const MB = KB * 1024

    if (bytes >= MB) {
      return `${(bytes / MB).toFixed(2)} MB`
    } else if (bytes >= KB) {
      return `${(bytes / KB).toFixed(2)} KB`
    } else {
      return `${bytes} bytes`
    }
  }

  return {
    optimizeImage,
    optimizeImages,
    formatFileSize
  }
}
