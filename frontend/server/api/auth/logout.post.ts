export default defineEventHandler(async (event) => {
  // Borramos la cookie.
  deleteCookie(event, 'auth_token', {
    httpOnly: true,
    path: '/',
    sameSite: 'lax',
    secure: process.env.NODE_ENV === 'production',
  });

  return { message: 'Logged out' };
});