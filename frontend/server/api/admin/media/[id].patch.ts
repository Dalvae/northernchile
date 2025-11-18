export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.apiBase;
  const authToken = getHeader(event, 'Authorization');
  const mediaId = getRouterParam(event, 'id');
  const body = await readBody(event);

  try {
    const updatedMedia = await $fetch(`${backendUrl}/api/admin/media/${mediaId}`, {
      method: 'PATCH',
      headers: {
        'Authorization': authToken || '',
        'Content-Type': 'application/json'
      },
      body: body
    });
    return updatedMedia;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al actualizar media'
    });
  }
});
