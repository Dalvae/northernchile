export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.backendApiUrl;
  const authToken = getHeader(event, 'Authorization');
  const userId = getRouterParam(event, 'id');
  const body = await readBody(event);

  try {
    const updatedUser = await $fetch(`${backendUrl}/api/users/${userId}`, {
      method: 'PUT',
      headers: { 'Authorization': authToken || '', 'Content-Type': 'application/json' },
      body: body,
    });
    return updatedUser;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || 'Error al actualizar usuario',
    });
  }
});