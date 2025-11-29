export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const cookie = getHeader(event, 'cookie') || ''
  const body = await readBody(event)

  try {
    const response = await $fetch(`${backendUrl}/api/payments/init`, {
      method: 'POST',
      body: body,
      headers: {
        'Cookie': cookie,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    })
    return response
  } catch (error: unknown) {
    const err = error as { 
      statusCode?: number, 
      statusMessage?: string, 
      data?: { message?: string, error?: string, status?: number, path?: string }, 
      message?: string,
      response?: { status?: number, _data?: unknown, headers?: Headers }
    }
    
    const statusCode = err.statusCode || err.response?.status || err.data?.status || 500
    const errorMessage = err.data?.message || err.data?.error || err.statusMessage || err.message || 'Failed to initialize payment'
    
    throw createError({
      statusCode,
      statusMessage: errorMessage,
      data: {
        message: errorMessage,
        statusCode,
        error: err.data?.error,
        path: err.data?.path
      }
    })
  }
})
