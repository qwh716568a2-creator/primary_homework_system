<script setup lang="ts">
defineProps<{
  title: string
  subtitle: string
  to: string
  tone: 'sky' | 'teal' | 'ink'
}>()
</script>

<template>
  <router-link class="quick-card" :class="`quick-${tone}`" :to="to">
    <div class="quick-icon">
      <slot />
    </div>
    <strong>{{ title }}</strong>
    <p>{{ subtitle }}</p>
  </router-link>
</template>

<style scoped>
.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
  min-height: 220px;
  padding: 1.8rem;
  border-radius: 999px;
  color: white;
  text-align: center;
  text-decoration: none;
  box-shadow: 0 28px 45px rgba(14, 70, 120, 0.24);
  transition:
    transform 0.24s ease,
    box-shadow 0.24s ease;
}

.quick-card:hover {
  transform: translateY(-6px) scale(1.01);
  box-shadow: 0 36px 55px rgba(14, 70, 120, 0.3);
}

.quick-icon {
  width: 72px;
  height: 72px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  font-size: 2rem;
}

.quick-card strong {
  font-size: 1.45rem;
  line-height: 1.15;
}

.quick-card p {
  margin: 0;
  max-width: 10rem;
  font-size: 0.92rem;
  color: rgba(255, 255, 255, 0.88);
}

.quick-sky {
  background: radial-gradient(circle at top, #71d1ff, #2b7cff 58%, #1d4ed8);
}

.quick-teal {
  background: radial-gradient(circle at top, #7de8df, #1d9b8b 58%, #0f766e);
}

.quick-ink {
  background: radial-gradient(circle at top, #7aa3ff, #2d5fab 58%, #14345b);
}
</style>
