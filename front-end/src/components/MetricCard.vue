<script setup lang="ts">
defineProps<{
  label: string
  value: string
  hint: string
  tone: 'sky' | 'teal' | 'amber' | 'rose'
}>()
</script>

<template>
  <article class="metric-card surface-card" :class="`metric-${tone}`">
    <div class="metric-accent"></div>
    <p class="metric-label">{{ label }}</p>
    <div class="metric-row">
      <strong>{{ value }}</strong>
      <slot />
    </div>
    <p class="metric-hint">{{ hint }}</p>
  </article>
</template>

<style scoped>
.metric-card {
  position: relative;
  overflow: hidden;
  padding: 1.2rem;
}

.metric-accent {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  margin-bottom: 0.9rem;
  opacity: 0.95;
}

.metric-label {
  margin: 0;
  font-size: 0.92rem;
  color: #5c6c80;
}

.metric-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 0.75rem;
}

.metric-row strong {
  font-size: 2rem;
  color: #17324d;
  line-height: 1;
}

.metric-hint {
  margin: 0.8rem 0 0;
  font-size: 0.9rem;
  color: #6f8096;
}

.metric-sky .metric-accent {
  background: linear-gradient(135deg, #65b8ff, #2e7cf6);
}

.metric-teal .metric-accent {
  background: linear-gradient(135deg, #64d8c6, #11998e);
}

.metric-amber .metric-accent {
  background: linear-gradient(135deg, #ffd46b, #f59e0b);
}

.metric-rose .metric-accent {
  background: linear-gradient(135deg, #ff9ec6, #ec4899);
}
</style>
