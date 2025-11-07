import { ref, onMounted, type Ref } from 'vue'

/**
 * Un composable de Vue 3 para interactuar con localStorage de forma segura en Nuxt 3 (SSR).
 * Lee el valor inicial de localStorage solo en el lado del cliente.
 *
 * @param key La clave para usar en localStorage.
 * @param initialValue El valor inicial a usar si no hay nada en localStorage.
 * @returns Un array con un ref reactivo para el valor y una función para actualizarlo.
 */
export function useLocalStorage<T>(key: string, initialValue: T): [Ref<T>, (value: T) => void] {
  // Creamos un ref con el valor inicial. Este valor se usará en el servidor.
  const storedValue: Ref<T> = ref(initialValue) as Ref<T>

  // La función para actualizar el valor en el estado y en localStorage.
  const setValue = (value: T) => {
    try {
      // Actualizamos el estado reactivo.
      storedValue.value = value

      // Nos aseguramos de escribir en localStorage solo en el cliente.
      if (import.meta.client) {
        localStorage.setItem(key, JSON.stringify(value))
      }
    } catch (error) {
      console.error(`Error al guardar en localStorage con la clave "${key}":`, error)
    }
  }

  // onMounted se ejecuta únicamente en el lado del cliente después de que el componente se monta.
  // Es el lugar perfecto para leer de localStorage de forma segura.
  onMounted(() => {
    try {
      const item = localStorage.getItem(key)
      if (item !== null) {
        // Si encontramos un valor, lo parseamos y actualizamos nuestro ref.
        storedValue.value = JSON.parse(item)
      }
    } catch (error) {
      console.error(`Error al leer de localStorage con la clave "${key}":`, error)
    }
  })

  // Devolvemos el valor reactivo y la función para modificarlo.
  return [storedValue, setValue]
}
