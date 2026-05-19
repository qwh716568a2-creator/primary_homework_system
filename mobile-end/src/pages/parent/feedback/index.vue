<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { resolveReviewLabel } from '@/utils/mobile-format'
import { ensureMobileRole } from '@/utils/page-guard'

const mobilePortalStore = useMobilePortalStore()
const childId = ref('')
const homeworkId = ref('')
const loading = ref(false)
const errorText = ref('')

onLoad((query) => {
  childId.value = query?.childId ?? mobilePortalStore.activeChildId
  homeworkId.value = query?.homeworkId ?? ''
})

onShow(() => {
  void loadFeedback()
})

const child = computed(
  () => mobilePortalStore.children.find((item) => item.id === childId.value) ?? mobilePortalStore.currentChild
)
const homework = computed(() => mobilePortalStore.getParentHomework(homeworkId.value, childId.value))
const reviewLabel = computed(() =>
  homework.value?.review?.status ? resolveReviewLabel(homework.value.review.status) : '待批改'
)

async function loadFeedback() {
  if (!ensureMobileRole('parent')) {
    return
  }

  loading.value = true
  errorText.value = ''

  try {
    if (!mobilePortalStore.children.length) {
      await mobilePortalStore.loadParentChildren()
    }
    await mobilePortalStore.loadParentHomeworkDetail(childId.value, homeworkId.value)
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '反馈加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  uni.navigateBack()
}
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">老师反馈</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view v-if="loading" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">正在同步老师反馈</text>
      <text class="empty-state__copy">孩子的最新批改结果正在加载。</text>
    </view>

    <view v-else-if="errorText" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">反馈加载失败</text>
      <text class="empty-state__copy">{{ errorText }}</text>
      <view class="secondary-button detail-retry" @tap="loadFeedback">重新加载</view>
    </view>

    <view v-else-if="homework && child" class="feedback-stack">
      <view class="shell-card feedback-hero">
        <text class="glass-chip">{{ child.name }} · {{ homework.subject }}</text>
        <text class="feedback-hero__title">{{ homework.title }}</text>
        <text class="feedback-hero__status">{{ reviewLabel }}</text>
      </view>

      <view class="shell-card feedback-card">
        <text class="section-title">老师评语</text>
        <text class="feedback-card__comment">
          {{ homework.review?.comment || '老师正在批改中，完成后会第一时间通知家长查看。' }}
        </text>
      </view>

      <view class="shell-card feedback-card">
        <text class="section-title">结果摘要</text>
        <view class="feedback-result">
          <view class="feedback-result__item">
            <text class="feedback-result__label">评级</text>
            <text class="feedback-result__value">{{ homework.review?.level || '待批改' }}</text>
          </view>
          <view class="feedback-result__item">
            <text class="feedback-result__label">得分</text>
            <text class="feedback-result__value">
              {{ homework.review?.score ? `${homework.review.score} 分` : '暂无' }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.feedback-stack {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 28rpx;
}

.feedback-hero,
.feedback-card {
  padding: 28rpx;
}

.feedback-hero__title {
  display: block;
  margin-top: 18rpx;
  font-size: 44rpx;
  line-height: 1.18;
  font-weight: 800;
  color: var(--text-strong);
}

.feedback-hero__status {
  display: block;
  margin-top: 18rpx;
  font-size: 26rpx;
  color: var(--brand-teal);
  font-weight: 700;
}

.feedback-card__comment {
  display: block;
  margin-top: 18rpx;
  font-size: 26rpx;
  line-height: 1.8;
  color: var(--text-primary);
}

.feedback-result {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 18rpx;
}

.feedback-result__item {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(243, 248, 255, 0.9);
}

.feedback-result__label {
  display: block;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.feedback-result__value {
  display: block;
  margin-top: 16rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.detail-empty,
.detail-retry {
  margin-top: 28rpx;
}
</style>
