export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event);
  const backendUrl = config.backendApiUrl;
  const body = await readBody(event);

  try {
    const response = await $fetch<{ token: string; user: any }>(`${backendUrl}/api/auth/login`, {
      method: "POST",
      body: body,
    });

    // ¡Aquí está la magia!
    // Usamos setCookie de Nitro para crear la cookie httpOnly.
    setCookie(event, 'auth_token', response.token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 60 * 60 * 24 * 7, // 7 días en segundos
      path: '/',
    });

    // Devolvemos el resto de la información (el usuario) al cliente.
    return { user: response.user };

  } catch (error: any) {
    // Si el login falla en el backend, limpiamos cualquier cookie antigua.
    deleteCookie(event, 'auth_token');
    throw createError({
      statusCode: error.response?.status || 500,
      statusMessage: "Error de autenticación",
    });
  }
});
