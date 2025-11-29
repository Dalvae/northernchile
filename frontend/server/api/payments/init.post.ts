export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase

  const cookie = getHeader(event, 'cookie') || ''
  const body = await readBody(event)

  console.log('[payments/init] Forwarding request to:', `${backendUrl}/api/payments/init`)
  console.log('[payments/init] Cookie:', cookie ? 'present' : 'missing')
  console.log('[payments/init] Request body:', JSON.stringify(body))

  try {
    const response = await $fetch(`${backendUrl}/api/payments/init`, {
      method: 'POST',
      body: body,
      headers: {
        'Cookie': cookie,
        'Content-Type': 'application/json'
      }
    })
    console.log('[payments/init] Success response:', JSON.stringify(response))
    return response
  } catch (error: unknown) {
    console.error('[payments/init] Error caught:', error)
    console.error('[payments/init] Error type:', typeof error)
    console.error('[payments/init] Error constructor:', error?.constructor?.name)
    
    const err = error as { 
      statusCode?: number, 
      statusMessage?: string, 
      data?: { message?: string, error?: string, status?: number, path?: string }, 
      message?: string,
      response?: { status?: number, _data?: unknown }
    }
    
    console.error('[payments/init] Error details:', {
      statusCode: err.statusCode,
      statusMessage: err.statusMessage,
      data: err.data,
      message: err.message,
      responseStatus: err.response?.status,
      responseData: err.response?._data
    })
    
    const statusCode = err.statusCode || err.response?.status || err.data?.status || 500
    const errorMessage = err.data?.message || err.data?.error || err.statusMessage || err.message || 'Failed to initialize payment'
    
    throw createError({
      statusCode,
      statusMessage: errorMessage
    })
  }
})
