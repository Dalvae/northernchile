import type { H3Event } from 'h3'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

interface FetchError {
  statusCode?: number
  data?: { message?: string; error?: string }
  message?: string
}

function extractErrorMessage(error: unknown, fallback: string): string {
  const err = error as FetchError
  return err.data?.message || err.data?.error || err.message || fallback
}

function extractStatusCode(error: unknown): number {
  return (error as FetchError).statusCode || 500
}

/**
 * Proxy a GET request to the backend API.
 */
export async function proxyGet<T>(event: H3Event, endpoint: string, errorMessage = 'Request failed'): Promise<T> {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const query = getQuery(event)

  try {
    const result = await $fetch<T>(`${backendUrl}${endpoint}`, {
      headers: { Cookie: cookie },
      query
    })
    return result as T
  } catch (error) {
    throw createError({
      statusCode: extractStatusCode(error),
      statusMessage: extractErrorMessage(error, errorMessage)
    })
  }
}

/**
 * Proxy a POST/PUT/PATCH/DELETE request to the backend API with a body.
 */
export async function proxyWithBody<T>(
  event: H3Event,
  method: HttpMethod,
  endpoint: string,
  errorMessage = 'Request failed'
): Promise<T> {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''
  const body = await readBody(event)

  try {
    const result = await $fetch<T>(`${backendUrl}${endpoint}`, {
      method,
      headers: {
        'Cookie': cookie,
        'Content-Type': 'application/json'
      },
      body
    })
    return result as T
  } catch (error) {
    throw createError({
      statusCode: extractStatusCode(error),
      statusMessage: extractErrorMessage(error, errorMessage)
    })
  }
}

/**
 * Proxy a DELETE request to the backend API (no body).
 */
export async function proxyDelete(event: H3Event, endpoint: string, errorMessage = 'Delete failed'): Promise<void> {
  const config = useRuntimeConfig(event)
  const backendUrl = config.public.apiBase
  const cookie = getHeader(event, 'cookie') || ''

  try {
    await $fetch(`${backendUrl}${endpoint}`, {
      method: 'DELETE',
      headers: { Cookie: cookie }
    })
  } catch (error) {
    throw createError({
      statusCode: extractStatusCode(error),
      statusMessage: extractErrorMessage(error, errorMessage)
    })
  }
}

// Convenience shortcuts
export const proxyPost = <T>(event: H3Event, endpoint: string, errorMessage?: string) =>
  proxyWithBody<T>(event, 'POST', endpoint, errorMessage)

export const proxyPut = <T>(event: H3Event, endpoint: string, errorMessage?: string) =>
  proxyWithBody<T>(event, 'PUT', endpoint, errorMessage)

export const proxyPatch = <T>(event: H3Event, endpoint: string, errorMessage?: string) =>
  proxyWithBody<T>(event, 'PATCH', endpoint, errorMessage)
