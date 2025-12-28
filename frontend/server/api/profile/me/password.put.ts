export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const body = await readBody(event)
  const cookie = getHeader(event, 'cookie') || ''

  try {
    return await $fetch(`${backendUrl}/api/profile/me/password`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Cookie': cookie },
      body
    })
  } catch (error) {
    const err = error as { response?: { status?: number }, data?: { message?: string } }
    throw createError({
      statusCode: err.response?.status || 500,
      statusMessage: err.data?.message || 'Failed to update password'
    })
  }
})
