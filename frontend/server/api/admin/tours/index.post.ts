export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.public.apiBase;
  const authToken = getHeader(event, "Authorization");
  const body = await readBody(event);

  try {
    const newTour = await $fetch(`${backendUrl}/api/tours`, {
      method: "POST",
      headers: {
        Authorization: authToken || "",
        "Content-Type": "application/json",
      },
      body: body,
    });
    return newTour;
  } catch (error: any) {
    throw createError({
      statusCode: error.statusCode || 500,
      message: error.message || "Error al crear tour",
    });
  }
});

