<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMobilePortalStore } from '@/store/mobile-portal'
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
  void loadDetail()
})

const child = computed(
  () => mobilePortalStore.children.find((item) => item.id === childId.value) ?? mobilePortalStore.currentChild
)
const homework = computed(() => mobilePortalStore.getParentHomework(homeworkId.value, childId.value))

async function loadDetail() {
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
    errorText.value = error instanceof Error ? error.message : '孩子作业加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

function openAssist() {
  uni.navigateTo({
    url: `/pages/parent/assist/index?childId=${childId.value}&homeworkId=${homeworkId.value}`
  })
}

function openFeedback() {
  uni.navigateTo({
    url: `/pages/parent/feedback/index?childId=${childId.value}&homeworkId=${homeworkId.value}`
  })
}
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">孩子作业</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view v-if="loading" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">正在加载孩子作业</text>
      <text class="empty-state__copy">页面会同步孩子最新提交与老师反馈。</text>
    </view>

    <view v-else-if="errorText" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">孩子作业加载失败</text>
      <text class="empty-state__copy">{{ errorText }}</text>
      <view class="secondary-button detail-retry" @tap="loadDetail">重新加载</view>
    </view>

    <view v-else-if="homework && child" class="detail-stack">
      <view class="shell-card detail-hero">
        <text class="glass-chip">{{ child.name }} · {{ child.gradeName }} {{ child.className }}</text>
        <text class="detail-hero__title">{{ homework.title }}</text>
        <text class="detail-hero__meta">{{ homework.teacherName }}老师 · 截止 {{ homework.deadline }}</text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">作业说明</text>
        <text class="muted-copy detail-block__copy">{{ homework.content }}</text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">提交要求</text>
        <view class="detail-block__tags">
          <text v-for="tag in homework.submitTypes" :key="tag" class="detail-block__tag">{{ tag }}</text>
        </view>
        <text class="detail-block__hint">
          {{ homework.allowParentAssist ? '这份作业支持家长协助上传。' : '这份作业要求学生独立完成。' }}
        </text>
      </view>

      <view v-if="homework.latestSubmission" class="shell-card detail-block">
        <text class="section-title">孩子最近一次提交</text>
        <text class="muted-copy">{{ homework.latestSubmission.text || '本次提交未填写文字说明。' }}</text>
        <text class="detail-block__hint">提交时间 {{ homework.latestSubmission.submittedAt }}</text>
      </view>

      <view v-if="homework.review" class="shell-card detail-block">
        <text class="section-title">老师反馈预览</text>
        <text class="detail-block__review-level">{{ homework.review.level || '已反馈' }}</text>
        <text class="muted-copy">{{ homework.review.comment || '老师暂未填写详细评语。' }}</text>
      </view>
    </view>

    <view v-else class="shell-card empty-state detail-empty">
      <text class="empty-state__title">暂未找到孩子这份作业</text>
      <text class="empty-state__copy">请返回首页重新选择，或确认当前孩子是否正确。</text>
    </view>

    <view v-if="homework && child && !loading" class="detail-bottom">
      <view class="secondary-button" @tap="openFeedback">查看反馈</view>
      <view class="primary-button" @tap="openAssist">
        {{ homework.status === 'revision' ? '协助订正' : '协助提交' }}
      </view>
    </view>
  </view>
</template>

<style>
.detail-stack {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 28rpx;
}

.detail-hero {
  padding: 32rpx;
}

.detail-hero__title {
  display: block;
  margin-top: 22rpx;
  font-size: 48rpx;
  line-height: 1.12;
  font-weight: 800;
  color: var(--text-strong);
}

.detail-hero__meta {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.detail-block {
  padding: 28rpx;
}

.detail-block__copy {
  display: block;
  margin-top: 18rpx;
}

.detail-block__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.detail-block__tag {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(28, 184, 163, 0.12);
  color: var(--brand-teal);
  font-size: 22rpx;
  font-weight: 700;
}

.detail-block__hint {
  display: block;
  margin-top: 18rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.detail-block__review-level {
  display: block;
  margin-top: 18rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: var(--brand-teal);
}

.detail-bottom {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 16rpx;
  margin-top: 24rpx;
}

.detail-empty,
.detail-retry {
  margin-top: 28rpx;
}
</style>
