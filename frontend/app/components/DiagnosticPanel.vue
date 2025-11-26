<template>
  <ClientOnly>
    <div
      v-if="show"
      class="fixed bottom-4 right-4 bg-black/90 text-white p-4 rounded-lg shadow-xl max-w-md z-50 text-xs font-mono"
    >
      <div class="flex justify-between items-center mb-2">
        <h3 class="font-bold text-sm">
          🔍 Diagnostic Panel
        </h3>
        <button
          class="text-white/60 hover:text-white"
          @click="show = false"
        >
          ✕
        </button>
      </div>

      <div class="space-y-2">
        <div>
          <strong>API Base:</strong>
          <code class="block bg-white/10 p-1 rounded mt-1 break-all">
            {{ config.public.apiBase }}
          </code>
        </div>

        <div>
          <strong>Base URL:</strong>
          <code class="block bg-white/10 p-1 rounded mt-1 break-all">
            {{ config.public.baseUrl }}
          </code>
        </div>

        <div>
          <strong>Environment:</strong>
          <code class="block bg-white/10 p-1 rounded mt-1">
            {{ import.meta.env.MODE }}
          </code>
        </div>

        <div>
          <strong>Window Location:</strong>
          <code class="block bg-white/10 p-1 rounded mt-1 break-all">
            {{ windowLocation }}
          </code>
        </div>

        <div>
          <strong>Auth Store:</strong>
          <code class="block bg-white/10 p-1 rounded mt-1">
            Loading: {{ authStore.loading }}<br>
            Authenticated: {{ authStore.isAuthenticated }}<br>
            User: {{ authStore.user?.email || 'None' }}
          </code>
        </div>

        <button
          class="w-full mt-2 bg-blue-600 hover:bg-blue-700 px-3 py-2 rounded text-white font-bold"
          @click="testApiConnection"
        >
          Test API Connection
        </button>

        <div v-if="apiTestResult">
          <strong>API Test:</strong>
          <code
            class="block p-1 rounded mt-1 break-all"
            :class="apiTestResult.success ? 'bg-green-900/50' : 'bg-red-900/50'"
          >
            {{ apiTestResult.message }}
          </code>
        </div>
      </div>
    </div>

    <!-- Toggle Button -->
    <button
      v-if="!show"
      class="fixed bottom-4 right-4 bg-blue-600 hover:bg-blue-700 text-white p-3 rounded-full shadow-xl z-50"
      @click="show = true"
      title="Show Diagnostic Panel"
    >
      🔍
    </button>
  </ClientOnly>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const config = useRuntimeConfig()
const authStore = useAuthStore()
const show = ref(false)
const apiTestResult = ref<{ success: boolean, message: string } | null>(null)

const windowLocation = computed(() => {
  if (import.meta.client) {
    return window.location.href
  }
  return 'N/A'
})

async function testApiConnection() {
  apiTestResult.value = null

  try {
    const response = await $fetch(`${config.public.apiBase}/api/index`, {
      method: 'GET'
    })

    apiTestResult.value = {
      success: true,
      message: `✅ Connected! Response: ${JSON.stringify(response)}`
    }
  } catch (error: any) {
    apiTestResult.value = {
      success: false,
      message: `❌ Error: ${error.message || error.toString()}`
    }
  }
}
</script>
