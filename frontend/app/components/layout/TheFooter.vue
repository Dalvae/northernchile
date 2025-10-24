<template>
  <UFooter>
    <template #left>
      <p class="text-sm text-gray-500 dark:text-gray-400">
        Built with Nuxt UI • © {{ new Date().getFullYear() }} Northern Chile
      </p>
    </template>

    <template #right>
      <UButton
        to="https://github.com/nuxt-ui-templates/starter"
        target="_blank"
        icon="i-simple-icons-github"
        aria-label="GitHub"
        color="gray"
        variant="ghost"
      />
    </template>
  </UFooter>
</template>
