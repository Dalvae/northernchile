export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig()
  const backendUrl = config.public.apiBase || 'http://localhost:8080'

  // Get the multipart form data
  const formData = await readMultipartFormData(event)

  if (!formData) {
    throw createError({
      statusCode: 400,
      message: 'No file uploaded'
    })
  }

  // Forward the multipart data to backend
  const backendFormData = new FormData()

  for (const part of formData) {
    if (part.name === 'file' && part.data) {
      // Create a Blob from the buffer for the file
      const blob = new Blob([part.data], { type: part.type || 'application/octet-stream' })
      backendFormData.append('file', blob, part.filename || 'file')
    } else if (part.data) {
      // For other fields, convert buffer to string
      backendFormData.append(part.name || '', part.data.toString())
    }
  }

  // Forward to backend with auth header
  const authHeader = getHeader(event, 'authorization')
  const headers: Record<string, string> = {}
  if (authHeader) {
    headers['Authorization'] = authHeader
  }

  try {
    const response = await $fetch(`${backendUrl}/api/admin/media`, {
      method: 'POST',
      body: backendFormData,
      headers
    })

    return response
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Failed to upload media'
    })
  }
})
