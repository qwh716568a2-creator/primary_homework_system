<script setup lang="ts">
import { computed } from 'vue'
import { assignmentStateMap, reviewStatusMap, submissionStatusMap } from '@/utils/teacher-portal'

const props = defineProps<{
  kind: 'assignment' | 'submission' | 'review'
  value: string
}>()

const config = computed(() => {
  if (props.kind === 'assignment') {
    return assignmentStateMap[props.value as keyof typeof assignmentStateMap]
  }

  if (props.kind === 'review') {
    return reviewStatusMap[props.value as keyof typeof reviewStatusMap]
  }

  return submissionStatusMap[props.value as keyof typeof submissionStatusMap]
})
</script>

<template>
  <span class="status-tag" :class="`tone-${config?.tone ?? 'slate'}`">
    {{ config?.label ?? value }}
  </span>
</template>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 74px;
  padding: 0.38rem 0.7rem;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.tone-slate {
  color: #596579;
  background: #edf1f7;
}

.tone-sky {
  color: #155eef;
  background: #e7f0ff;
}

.tone-teal {
  color: #067647;
  background: #e6f8ef;
}

.tone-amber {
  color: #b54708;
  background: #fff3df;
}

.tone-rose {
  color: #c11574;
  background: #ffe6f2;
}
</style>
