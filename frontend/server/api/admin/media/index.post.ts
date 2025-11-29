import type { MediaRes } from 'api-client'

export default defineEventHandler(async (event): Promise<MediaRes> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''

  // Get the multipart form data
  const formData = await readMultipartFormData(event)

  if (!formData) {
    throw createError({
      statusCode: 400,
      statusMessage: 'No file uploaded'
    })
  }

  // Forward the multipart data to backend
  const backendFormData = new FormData()

  for (const part of formData) {
    if (part.name === 'file' && part.data) {
      const blob = new Blob([new Uint8Array(part.data)], { type: part.type || 'application/octet-stream' })
      backendFormData.append('file', blob, part.filename || 'file')
    } else if (part.data) {
      backendFormData.append(part.name || '', part.data.toString())
    }
  }

  try {
    const response = await $fetch<MediaRes>(`${backendUrl}/api/admin/media`, {
      method: 'POST',
      body: backendFormData,
      headers: { 'Cookie': cookie }
    })
    return response
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to upload media'
    })
  }
})
