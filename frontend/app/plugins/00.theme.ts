export default defineNuxtPlugin({
  name: 'theme-init',
  enforce: 'pre',
  setup() {
    // Initialize theme composable on both server and client
    // This ensures the theme cookie is read and applied during SSR
    useTheme()
  }
})
