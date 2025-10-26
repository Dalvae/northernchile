export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.backendApiUrl;

  const body = await readBody(event);

  try {
    const response = await $fetch(`${backendUrl}/api/auth/login`, {
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
      statusMessage: "Failed to login",
    });
  }
});
