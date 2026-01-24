// Shared fetch options for admin API calls
export const useAdminFetch = () => {
  const baseFetchOptions = {
    credentials: 'include' as RequestCredentials
  }

  const jsonHeaders = {
    'Content-Type': 'application/json'
  }

  return {
    baseFetchOptions,
    jsonHeaders
  }
}
