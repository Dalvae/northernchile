export default defineEventHandler(async (event): Promise<any> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const body = await readBody(event)
  const cookie = getHeader(event, 'cookie') || ''

  try {
    const response = await $fetch(`${backendUrl}/api/profile/me/password`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Cookie': cookie
      },
      body
    })
    return response
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      statusMessage: error.data?.message || 'Failed to update password'
    })
  }
})
