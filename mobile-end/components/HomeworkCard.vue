<script setup lang="ts">
import { computed } from 'vue'
import type { HomeworkRecord } from '@/types/mobile'
import { resolveHomeworkStatusClass, resolveHomeworkStatusLabel } from '@/utils/mobile-format'

const props = defineProps<{
  item: HomeworkRecord
  childName?: string
}>()

const statusClass = computed(() => resolveHomeworkStatusClass(props.item.status))
const statusLabel = computed(() => resolveHomeworkStatusLabel(props.item.status))
const submitHint = computed(() =>
  props.item.latestSubmission?.submittedAt
    ? `最近提交 ${props.item.latestSubmission.submittedAt}`
    : `截止 ${props.item.deadline}`
)
</script>

<template>
  <view class="homework-card shell-card">
    <view class="homework-card__top">
      <view>
        <text class="homework-card__subject">{{ item.subject }}</text>
        <text class="homework-card__title">{{ item.title }}</text>
      </view>
      <text :class="['status-chip', statusClass]">{{ statusLabel }}</text>
    </view>

    <text v-if="childName" class="homework-card__child">{{ childName }}</text>
    <text class="homework-card__summary">{{ item.summary }}</text>

    <view class="homework-card__meta">
      <text>{{ submitHint }}</text>
      <text>{{ item.teacherName }}老师</text>
    </view>
  </view>
</template>

<style>
.homework-card {
  padding: 28rpx;
}

.homework-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.homework-card__subject {
  display: inline-flex;
  margin-bottom: 14rpx;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(47, 124, 255, 0.1);
  font-size: 20rpx;
  font-weight: 700;
  color: var(--brand-blue);
}

.homework-card__title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-strong);
  line-height: 1.35;
}

.homework-card__child {
  display: block;
  margin-top: 18rpx;
  font-size: 22rpx;
  color: var(--brand-teal);
  font-weight: 700;
}

.homework-card__summary {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--text-primary);
}

.homework-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 24rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}
</style>
