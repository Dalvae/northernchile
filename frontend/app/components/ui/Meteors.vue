<script setup lang="ts">
interface Props {
  count?: number
}

withDefaults(defineProps<Props>(), {
  count: 20
})

const getMeteorStyle = () => {
  // Generamos una posición horizontal aleatoria
  const left = Math.floor(Math.random() * 100) + '%'
  // Retraso y duración aleatorios
  const delay = Math.random() * 3 + 's'
  const duration = Math.floor(Math.random() * 3 + 2) + 's'

  return {
    left: left,
    animationDelay: delay,
    animationDuration: duration
  }
}
</script>

<template>
  <div
    class="absolute inset-0 w-full h-full overflow-hidden pointer-events-none rounded-2xl"
  >
    <span
      v-for="index in count"
      :key="'meteor-' + index"
      :style="getMeteorStyle()"
      class="animate-meteor-fall absolute top-0 h-1 w-1 rounded-full bg-white shadow-[0_0_0_4px_rgba(255,255,255,0.1),0_0_8px_rgba(255,255,255,0.2)] z-0"
    >
      <!-- Cola del meteoro -->
      <span
        class="absolute top-1/2 left-1/2 -z-10 h-[1px] w-[100px] -translate-y-1/2 bg-gradient-to-r from-white via-white/40 to-transparent content-['']"
      />
    </span>
  </div>
</template>

<style scoped>
/*
   La animación mueve el elemento físicamente hacia abajo y a la izquierda.
   La rotación visual del meteoro (la cola) se hace con `rotate` en la clase base o aquí.
*/
.animate-meteor-fall {
  /* Rotamos el meteoro para que apunte en diagonal */
  transform: rotate(-35deg);
  opacity: 0;
  animation: meteor-fall linear infinite;
}

@keyframes meteor-fall {
  0% {
    /* Empieza un poco arriba y a la derecha de su posición */
    opacity: 1;
    margin-top: -20px;
    margin-left: 20px;
  }
  100% {
    /* Termina muy abajo y a la izquierda */
    opacity: 0;
    margin-top: 400px;
    margin-left: -400px;
  }
}
</style>
