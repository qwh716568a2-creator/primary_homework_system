<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'

const mobilePortalStore = useMobilePortalStore()
const homeworkId = ref('')
const loading = ref(false)
const errorText = ref('')

onLoad((query) => {
  homeworkId.value = query?.homeworkId ?? ''
})

onShow(() => {
  void loadDetail()
})

const homework = computed(() => mobilePortalStore.getStudentHomework(homeworkId.value))
const submissions = computed(() => mobilePortalStore.studentSubmissionsMap[homeworkId.value] ?? [])
const reviews = computed(() => mobilePortalStore.studentReviewsMap[homeworkId.value] ?? [])

async function loadDetail() {
  if (!ensureMobileRole('student')) {
    return
  }

  loading.value = true
  errorText.value = ''

  try {
    await Promise.all([
      mobilePortalStore.loadStudentHomeworkDetail(homeworkId.value),
      mobilePortalStore.loadStudentSubmissions(homeworkId.value),
      mobilePortalStore.loadStudentReviews(homeworkId.value)
    ])
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '作业详情加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

function openSubmit() {
  uni.navigateTo({
    url: `/pages/student/submit/index?homeworkId=${homeworkId.value}`
  })
}

function openFeedback() {
  uni.navigateTo({
    url: `/pages/student/feedback/index?homeworkId=${homeworkId.value}`
  })
}

function addToWrongBook() {
  const subject = homework.value?.subject || ''
  const title = homework.value?.title || ''
  uni.navigateTo({
    url: `/pages/student/wrongbook/create?homeworkId=${homeworkId.value}&subjectName=${encodeURIComponent(subject)}&questionText=${encodeURIComponent(title)}`
  })
}
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">作业详情</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view v-if="loading" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">正在加载作业详情</text>
      <text class="empty-state__copy">同时会同步提交记录和老师反馈。</text>
    </view>

    <view v-else-if="errorText" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">作业详情加载失败</text>
      <text class="empty-state__copy">{{ errorText }}</text>
      <view class="secondary-button detail-retry" @tap="loadDetail">重新加载</view>
    </view>

    <view v-else-if="homework" class="detail-stack">
      <view class="shell-card detail-hero">
        <text class="glass-chip">{{ homework.subject }}</text>
        <text class="detail-hero__title">{{ homework.title }}</text>
        <text class="detail-hero__meta">{{ homework.teacherName }}老师 · 截止 {{ homework.deadline }}</text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">作业说明</text>
        <text class="muted-copy detail-block__copy">{{ homework.content }}</text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">提交方式</text>
        <view class="detail-block__tags">
          <text v-for="tag in homework.submitTypes" :key="tag" class="detail-block__tag">{{ tag }}</text>
        </view>
        <text class="detail-block__hint">
          {{ homework.allowParentAssist ? '支持家长协助上传' : '需要学生独立完成并提交' }}
        </text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">附件与示例</text>
        <view v-if="homework.attachments.length" class="detail-block__attachments">
          <view v-for="file in homework.attachments" :key="file.id" class="detail-block__attachment">
            <text class="detail-block__attachment-name">{{ file.name }}</text>
            <text class="detail-block__attachment-type">{{ file.type }}</text>
          </view>
        </view>
        <text v-else class="muted-copy">当前作业没有额外附件。</text>
      </view>

      <view v-if="submissions.length" class="shell-card detail-block">
        <text class="section-title">提交记录</text>
        <view class="detail-block__history">
          <view v-for="item in submissions" :key="item.id || item.submittedAt" class="detail-block__history-item">
            <view>
              <text class="detail-block__history-title">第 {{ item.versionNo || 1 }} 次提交</text>
              <text class="detail-block__history-meta">{{ item.submittedAt }}</text>
            </view>
            <text class="detail-block__history-role">{{ item.assistedByParent ? '家长协助' : '学生提交' }}</text>
          </view>
        </view>
      </view>

      <view v-if="reviews.length" class="shell-card detail-block">
        <text class="section-title">最新反馈</text>
        <text class="detail-block__review-level">{{ reviews[0].level || '已反馈' }}</text>
        <text class="muted-copy">{{ reviews[0].comment || '老师暂未填写详细评语。' }}</text>
      </view>
    </view>

    <view v-else class="shell-card empty-state detail-empty">
      <text class="empty-state__title">暂未找到这份作业</text>
      <text class="empty-state__copy">可能已经被删除，或者需要从列表页重新进入。</text>
    </view>

    <view v-if="homework && !loading" class="detail-bottom">
      <view class="secondary-button" @tap="openFeedback">查看反馈</view>
      <view class="secondary-button" @tap="addToWrongBook">加入错题本</view>
      <view class="primary-button" @tap="openSubmit">
        {{ homework.status === 'revision' ? '继续订正' : '提交作业' }}
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
  background: rgba(47, 124, 255, 0.1);
  color: var(--brand-blue);
  font-size: 22rpx;
  font-weight: 700;
}

.detail-block__hint {
  display: block;
  margin-top: 18rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.detail-block__attachments,
.detail-block__history {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 18rpx;
}

.detail-block__attachment,
.detail-block__history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: rgba(245, 249, 255, 0.92);
}

.detail-block__attachment-name,
.detail-block__history-title {
  font-size: 24rpx;
  color: var(--text-primary);
}

.detail-block__attachment-type,
.detail-block__history-meta,
.detail-block__history-role {
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
  grid-template-columns: 1fr 1fr 1.3fr;
  gap: 16rpx;
  margin-top: 24rpx;
}

.detail-empty {
  margin-top: 28rpx;
}

.detail-retry {
  margin-top: 18rpx;
}
</style>
