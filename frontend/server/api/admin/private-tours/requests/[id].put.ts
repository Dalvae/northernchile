import { ofetch } from 'ofetch'

export default defineEventHandler(async (event): Promise<unknown> => {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const id = getRouterParam(event, 'id')
  const body = await readBody(event)

  try {
    const result: unknown = await ofetch(`${backendUrl}/api/admin/private-tours/requests/${id}`, {
      method: 'PUT',
      headers: { Cookie: cookie },
      body
    })
    return result
  } catch (error: unknown) {
    const err = error as { statusCode?: number, data?: { message?: string, error?: string }, message?: string }
    throw createError({
      statusCode: err.statusCode || 500,
      statusMessage: err.data?.message || err.data?.error || err.message || 'Failed to update private tour request'
    })
  }
})
