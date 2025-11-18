export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || 'http://localhost:8080';

  // Get query params
  const query = getQuery(event);

  // Get auth header
  const authHeader = getHeader(event, 'authorization');
  const headers: Record<string, string> = {};
  if (authHeader) {
    headers['Authorization'] = authHeader;
  }

  try {
    const response = await $fetch(`${backendUrl}/api/admin/media`, {
      method: 'GET',
      params: query,
      headers
    });

    return response;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Failed to fetch media'
    });
  }
});
