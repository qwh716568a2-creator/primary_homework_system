<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  hint: string
  tone: 'sky' | 'teal' | 'amber' | 'rose'
}>()
</script>

<template>
  <article class="metric-card surface-card" :class="`metric-${tone}`">
    <div class="metric-accent">
      <slot name="icon" />
    </div>
    <p class="metric-label">{{ label }}</p>
    <div class="metric-row">
      <strong>{{ value }}</strong>
    </div>
    <p class="metric-hint">{{ hint }}</p>
  </article>
</template>

<style scoped>
.metric-card {
  position: relative;
  overflow: hidden;
  padding: 1.5rem;
  border-radius: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.metric-accent {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  margin-bottom: 1rem;
  opacity: 0.95;
  box-shadow: 0 8px 16px -4px rgba(0, 0, 0, 0.1);
}

.metric-accent :deep(svg) {
  width: 22px;
  height: 22px;
  color: #ffffff;
}

.metric-label {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 500;
  color: #64748b;
}

.metric-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.metric-row strong {
  font-size: 2.25rem;
  color: #0f172a;
  line-height: 1;
  font-weight: 700;
}

.metric-hint {
  margin: 0.8rem 0 0;
  font-size: 0.9rem;
  color: #94a3b8;
}

.metric-sky .metric-accent {
  background: linear-gradient(135deg, #38bdf8, #0284c7);
  box-shadow: 0 8px 16px -4px rgba(2, 132, 199, 0.25);
}

.metric-teal .metric-accent {
  background: linear-gradient(135deg, #2dd4bf, #0f766e);
  box-shadow: 0 8px 16px -4px rgba(15, 118, 110, 0.25);
}

.metric-amber .metric-accent {
  background: linear-gradient(135deg, #fbbf24, #b45309);
  box-shadow: 0 8px 16px -4px rgba(180, 83, 9, 0.25);
}

.metric-rose .metric-accent {
  background: linear-gradient(135deg, #fb7185, #be123c);
  box-shadow: 0 8px 16px -4px rgba(190, 18, 60, 0.25);
}
</style>
