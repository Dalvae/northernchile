<template>
  <div class="bg-stars">
    <div class="stars-layer stars-1" />
    <div class="stars-layer stars-2" />
    <div class="stars-layer stars-3" />
  </div>
</template>

<script setup lang="ts">
// Componente de fondo animado de estrellas
// Inspirado en el efecto de partículas para tours astronómicos
</script>

<style scoped>
.bg-stars {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background: linear-gradient(
    to bottom,
    #000000 0%,
    #0a0a1a 50%,
    #1a1a2e 100%
  );
}

.stars-layer {
  position: absolute;
  inset: 0;
  background-repeat: repeat;
  animation: twinkle linear infinite;
}

/* Layer 1: Estrellas pequeñas y rápidas */
.stars-1 {
  background-image:
    radial-gradient(2px 2px at 20% 30%, white, transparent),
    radial-gradient(2px 2px at 60% 70%, white, transparent),
    radial-gradient(1px 1px at 50% 50%, white, transparent),
    radial-gradient(1px 1px at 80% 10%, white, transparent),
    radial-gradient(2px 2px at 90% 60%, white, transparent),
    radial-gradient(1px 1px at 33% 80%, white, transparent),
    radial-gradient(1px 1px at 15% 90%, white, transparent);
  background-size: 200px 200px;
  background-position: 0 0;
  animation-duration: 200s;
  opacity: 0.8;
}

/* Layer 2: Estrellas medianas y más lentas */
.stars-2 {
  background-image:
    radial-gradient(3px 3px at 40% 20%, rgba(255, 255, 255, 0.9), transparent),
    radial-gradient(2px 2px at 70% 40%, rgba(255, 255, 255, 0.8), transparent),
    radial-gradient(2px 2px at 25% 60%, rgba(255, 255, 255, 0.85), transparent),
    radial-gradient(3px 3px at 85% 80%, rgba(255, 255, 255, 0.9), transparent),
    radial-gradient(2px 2px at 10% 50%, rgba(255, 255, 255, 0.8), transparent);
  background-size: 300px 300px;
  background-position: 50px 50px;
  animation-duration: 300s;
  opacity: 0.6;
}

/* Layer 3: Estrellas grandes y muy lentas */
.stars-3 {
  background-image:
    radial-gradient(4px 4px at 30% 35%, rgba(255, 255, 255, 1), transparent),
    radial-gradient(3px 3px at 65% 75%, rgba(200, 200, 255, 0.9), transparent),
    radial-gradient(4px 4px at 45% 15%, rgba(255, 255, 255, 0.95), transparent),
    radial-gradient(3px 3px at 75% 55%, rgba(255, 200, 200, 0.9), transparent);
  background-size: 400px 400px;
  background-position: 100px 100px;
  animation-duration: 400s;
  opacity: 0.5;
}

@keyframes twinkle {
  from {
    transform: translate3d(0, 0, 0);
  }
  to {
    transform: translate3d(-200px, -200px, 0);
  }
}

/* Efecto de parpadeo para algunas estrellas */
.stars-1::before,
.stars-2::before,
.stars-3::before {
  content: '';
  position: absolute;
  inset: 0;
  background: inherit;
  animation: blink 2s ease-in-out infinite alternate;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Responsive: reducir cantidad de estrellas en mobile para performance */
@media (max-width: 768px) {
  .stars-2,
  .stars-3 {
    display: none;
  }
}
</style>
