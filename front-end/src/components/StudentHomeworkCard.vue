<script setup lang="ts">
import StatusTag from '@/components/StatusTag.vue'
import type { StudentHomeworkRecord } from '@/types/student-portal'
import { formatStudentDateTime, resolveStudentDeadlineLabel } from '@/utils/student-portal-view'
import { formatSubmitTypes } from '@/utils/format-labels'

defineProps<{
  item: StudentHomeworkRecord
}>()

defineEmits<{
  open: [homeworkId: string]
}>()
</script>

<template>
  <article class="student-homework-card surface-card" @click="$emit('open', item.id)">
    <div class="student-homework-card__top">
      <span class="soft-chip">{{ item.subject }}</span>
      <StatusTag kind="student-homework" :value="item.status" />
    </div>

    <h3>{{ item.title }}</h3>
    <p class="student-homework-card__summary">{{ item.summary || item.content }}</p>

    <div class="student-homework-card__meta">
      <div>
        <small>最近截止</small>
        <strong>{{ formatStudentDateTime(item.deadline) }}</strong>
      </div>
      <div>
        <small>老师</small>
        <strong>{{ item.teacherName }}</strong>
      </div>
      <div>
        <small>当前节奏</small>
        <strong>{{ resolveStudentDeadlineLabel(item.deadline) }}</strong>
      </div>
    </div>

    <div class="student-homework-card__footer">
      <span>{{ formatSubmitTypes(item.submitTypes) }}</span>
      <span>{{ item.allowParentAssist ? '支持家长协助' : '独立完成' }}</span>
    </div>
  </article>
</template>

<style scoped>
.student-homework-card {
  padding: 1.2rem;
  border-radius: 26px;
  cursor: pointer;
}

.student-homework-card__top,
.student-homework-card__meta,
.student-homework-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
}

.student-homework-card h3 {
  margin: 1rem 0 0;
  font-size: 1.25rem;
  color: #11263f;
}

.student-homework-card__summary {
  margin: 0.7rem 0 0;
  color: #617589;
  line-height: 1.7;
}

.student-homework-card__meta {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(148, 163, 184, 0.18);
}

.student-homework-card__meta > div {
  min-width: 0;
}

.student-homework-card__meta small,
.student-homework-card__footer {
  color: #6d8196;
}

.student-homework-card__meta strong {
  display: block;
  margin-top: 0.2rem;
  color: #152b43;
  font-size: 0.95rem;
}

.student-homework-card__footer {
  margin-top: 1rem;
  font-size: 0.88rem;
}

@media (max-width: 760px) {
  .student-homework-card__meta {
    display: grid;
    grid-template-columns: 1fr;
  }

  .student-homework-card__footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
