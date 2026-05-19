<script setup lang="ts">
import StatusTag from '@/components/StatusTag.vue'
import { formatParentDateTime, resolveParentDeadlineLabel } from '@/utils/parent-portal-view'
import type { ParentHomeworkRecord } from '@/types/parent-portal'

defineProps<{
  item: ParentHomeworkRecord
  childName?: string
}>()

const emit = defineEmits<{
  open: [homeworkId: string]
}>()
</script>

<template>
  <button type="button" class="parent-homework-card" @click="emit('open', item.id)">
    <div class="parent-homework-card__top">
      <div class="chip-row">
        <span class="soft-chip">{{ childName || '孩子作业' }}</span>
        <span class="soft-chip">{{ item.subject }}</span>
      </div>
      <StatusTag kind="student-homework" :value="item.status" />
    </div>

    <strong>{{ item.title }}</strong>
    <p>{{ item.summary || item.content }}</p>

    <div class="parent-homework-card__footer">
      <div>
        <small>老师</small>
        <span>{{ item.teacherName }}</span>
      </div>
      <div>
        <small>截止</small>
        <span>{{ formatParentDateTime(item.deadline) }}</span>
      </div>
      <span class="metric-inline">{{ resolveParentDeadlineLabel(item.deadline) }}</span>
    </div>
  </button>
</template>

<style scoped>
.parent-homework-card {
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 230px;
  padding: 1.15rem 1.2rem;
  border: 1px solid rgba(190, 205, 224, 0.58);
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(247, 251, 255, 0.96)),
    radial-gradient(circle at 90% 12%, rgba(47, 124, 255, 0.1), transparent 32%);
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.parent-homework-card:hover {
  transform: translateY(-2px);
  border-color: rgba(47, 124, 255, 0.55);
  box-shadow: 0 20px 38px -28px rgba(36, 66, 112, 0.45);
}

.parent-homework-card__top,
.parent-homework-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.parent-homework-card strong {
  margin-top: 0.95rem;
  font-size: 1.12rem;
  color: #08213f;
}

.parent-homework-card p {
  display: -webkit-box;
  margin: 0.55rem 0 0;
  overflow: hidden;
  color: #56708c;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.parent-homework-card__footer {
  margin-top: auto;
  padding-top: 0.9rem;
  border-top: 1px solid rgba(194, 205, 221, 0.55);
}

.parent-homework-card__footer small,
.parent-homework-card__footer span {
  display: block;
}

.parent-homework-card__footer small {
  color: #7b8da0;
  font-size: 0.78rem;
}

.parent-homework-card__footer span {
  margin-top: 0.18rem;
  color: #19324e;
  font-weight: 800;
}

@media (max-width: 760px) {
  .parent-homework-card__top,
  .parent-homework-card__footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
