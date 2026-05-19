<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { resolveReviewLabel } from '@/utils/mobile-format'
import { ensureMobileRole } from '@/utils/page-guard'

const mobilePortalStore = useMobilePortalStore()
const homeworkId = ref('')
const loading = ref(false)
const errorText = ref('')

onLoad((query) => {
  homeworkId.value = query?.homeworkId ?? ''
})

onShow(() => {
  void loadFeedback()
})

const homework = computed(() => mobilePortalStore.getStudentHomework(homeworkId.value))
const reviews = computed(() => mobilePortalStore.studentReviewsMap[homeworkId.value] ?? [])
const latestReview = computed(() => reviews.value[0] ?? homework.value?.review ?? null)
const reviewLabel = computed(() =>
  latestReview.value?.status ? resolveReviewLabel(latestReview.value.status) : '待批改'
)

async function loadFeedback() {
  if (!ensureMobileRole('student')) {
    return
  }

  loading.value = true
  errorText.value = ''

  try {
    await Promise.all([
      mobilePortalStore.loadStudentHomeworkDetail(homeworkId.value),
      mobilePortalStore.loadStudentReviews(homeworkId.value)
    ])
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '反馈加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

function addToWrongBook() {
  const subject = homework.value?.subject || ''
  const questionText = homework.value?.title || ''
  uni.navigateTo({
    url: `/pages/student/wrongbook/create?homeworkId=${homeworkId.value}&subjectName=${encodeURIComponent(subject)}&questionText=${encodeURIComponent(questionText)}`
  })
}
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">反馈结果</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view v-if="loading" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">正在同步老师反馈</text>
      <text class="empty-state__copy">稍等一下，最新批改结果正在拉取。</text>
    </view>

    <view v-else-if="errorText" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">反馈加载失败</text>
      <text class="empty-state__copy">{{ errorText }}</text>
      <view class="secondary-button detail-retry" @tap="loadFeedback">重新加载</view>
    </view>

    <view v-else-if="homework" class="feedback-stack">
      <view class="shell-card feedback-hero">
        <text class="glass-chip">{{ homework.subject }}</text>
        <text class="feedback-hero__title">{{ homework.title }}</text>
        <text class="feedback-hero__status">{{ reviewLabel }}</text>
      </view>

      <view class="shell-card feedback-card">
        <text class="section-title">老师评语</text>
        <text class="feedback-card__comment">
          {{ latestReview?.comment || '老师正在批改中，完成后会第一时间通知你。' }}
        </text>
      </view>

      <view class="shell-card feedback-card">
        <text class="section-title">结果摘要</text>
        <view class="feedback-result">
          <view class="feedback-result__item">
            <text class="feedback-result__label">评级</text>
            <text class="feedback-result__value">{{ latestReview?.level || '待批改' }}</text>
          </view>
          <view class="feedback-result__item">
            <text class="feedback-result__label">得分</text>
            <text class="feedback-result__value">
              {{ latestReview?.score ? `${latestReview.score} 分` : '暂无' }}
            </text>
          </view>
        </view>
      </view>

      <view v-if="reviews.length" class="shell-card feedback-card">
        <text class="section-title">批改记录</text>
        <view class="feedback-history">
          <view v-for="item in reviews" :key="item.id || item.reviewedAt" class="feedback-history__item">
            <view>
              <text class="feedback-history__title">{{ item.level || resolveReviewLabel(item.status) }}</text>
              <text class="feedback-history__meta">{{ item.reviewedAt || '刚刚更新' }}</text>
            </view>
            <text class="feedback-history__score">{{ item.score ? `${item.score} 分` : '暂无分数' }}</text>
          </view>
        </view>
      </view>

      <view class="feedback-actions">
        <view class="secondary-button" @tap="addToWrongBook">加入错题本</view>
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

.feedback-result__item,
.feedback-history__item {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(243, 248, 255, 0.9);
}

.feedback-result__label,
.feedback-history__meta {
  display: block;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.feedback-result__value,
.feedback-history__title,
.feedback-history__score {
  display: block;
  margin-top: 16rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.feedback-history {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 18rpx;
}

.feedback-actions {
  margin-top: 4rpx;
}

.detail-empty,
.detail-retry {
  margin-top: 28rpx;
}
</style>
