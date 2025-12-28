export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const body = await readBody(event)

  try {
    return await $fetch(`${backendUrl}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body
    })
  } catch (error) {
    const status = (error as { response?: { status?: number } })?.response?.status || 500
    throw createError({ statusCode: status, statusMessage: 'Failed to register' })
  }
})
