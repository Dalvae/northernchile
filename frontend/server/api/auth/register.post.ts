export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.apiBase;

  const body = await readBody(event);

  try {
    const response = await $fetch(`${backendUrl}/api/auth/register`, {
      method: "POST",
      body: body,
      headers: {
        "Content-Type": "application/json",
      },
    });
    return response;
  } catch (error: any) {
    throw createError({
      statusCode: error.response?.status || 500,
      statusMessage: "Failed to register",
    });
  }
});
