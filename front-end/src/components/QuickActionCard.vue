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
  border-radius: 32px;
  color: white;
  text-align: center;
  text-decoration: none;
  box-shadow: 0 16px 32px -8px rgba(15, 23, 42, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.quick-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 32px;
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.4);
  pointer-events: none;
}

.quick-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 24px 48px -12px rgba(15, 23, 42, 0.4);
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
  background: linear-gradient(135deg, #6366f1, #4f46e5);
}

.quick-teal {
  background: linear-gradient(135deg, #14b8a6, #0d9488);
}

.quick-ink {
  background: linear-gradient(135deg, #334155, #0f172a);
}
</style>
