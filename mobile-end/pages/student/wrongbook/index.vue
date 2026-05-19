<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'
import type { WrongBookStatus } from '@/types/mobile'

const mobilePortalStore = useMobilePortalStore()
const loading = ref(false)
const errorText = ref('')
const activeStatus = ref<'all' | WrongBookStatus>('all')
const activeSubject = ref('all')

const statusFilters: Array<{ key: 'all' | WrongBookStatus; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'pending_fix', label: '待订正' },
  { key: 'fixed', label: '已订正' },
  { key: 'mastered', label: '已掌握' }
]

const subjectFilters = computed(() => [
  { key: 'all', label: '全部科目' },
  ...Array.from(
    new Map(
      mobilePortalStore.studentWrongBooks.map((item) => [item.subjectCode || item.subjectName, item.subjectName])
    ).entries()
  ).map(([key, label]) => ({ key, label }))
])

const filteredList = computed(() =>
  mobilePortalStore.studentWrongBooks.filter((item) => {
    if (activeStatus.value !== 'all' && item.status !== activeStatus.value) {
      return false
    }

    if (activeSubject.value !== 'all' && `${item.subjectCode || item.subjectName}` !== activeSubject.value) {
      return false
    }

    return true
  })
)

function resolveStatusLabel(status: WrongBookStatus) {
  if (status === 'pending_fix') return '待订正'
  if (status === 'fixed') return '已订正'
  return '已掌握'
}

function resolveStatusClass(status: WrongBookStatus) {
  if (status === 'pending_fix') return 'status-revision'
  if (status === 'fixed') return 'status-submitted'
  return 'status-completed'
}

async function loadWrongBooks() {
  if (!ensureMobileRole('student')) return

  loading.value = true
  errorText.value = ''

  try {
    await mobilePortalStore.loadStudentWrongBooks()
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '错题本加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

function goCreate() {
  uni.navigateTo({
    url: '/pages/student/wrongbook/create'
  })
}

function openDetail(id: string) {
  uni.navigateTo({
    url: `/pages/student/wrongbook/detail?id=${id}`
  })
}

onShow(() => {
  void loadWrongBooks()
})
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">错题本</text>
      <view class="detail-header__back" @tap="goCreate">新增</view>
    </view>

    <view class="wrongbook-hero shell-card">
      <text class="glass-chip">错题管理</text>
      <text class="wrongbook-hero__title">把错题整理成自己的提分清单</text>
      <text class="wrongbook-hero__meta">老师标记和自己主动加入的错题都会汇总在这里。</text>
    </view>

    <view class="wrongbook-filters">
      <view
        v-for="item in statusFilters"
        :key="item.key"
        :class="['wrongbook-filter', { 'wrongbook-filter--active': activeStatus === item.key }]"
        @tap="activeStatus = item.key"
      >
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="wrongbook-section-label">按科目查看</view>

    <view class="wrongbook-filters wrongbook-filters--subjects">
      <view
        v-for="item in subjectFilters"
        :key="item.key"
        :class="['wrongbook-filter', { 'wrongbook-filter--active': activeSubject === item.key }]"
        @tap="activeSubject = item.key"
      >
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="wrongbook-list">
      <view v-if="loading" class="shell-card empty-state">
        <text class="empty-state__title">正在同步错题本</text>
        <text class="empty-state__copy">稍等一下，最新错题和订正状态马上出现。</text>
      </view>

      <view v-else-if="errorText" class="shell-card empty-state">
        <text class="empty-state__title">错题本加载失败</text>
        <text class="empty-state__copy">{{ errorText }}</text>
        <view class="secondary-button wrongbook-retry" @tap="loadWrongBooks">重新加载</view>
      </view>

      <template v-else>
        <view
          v-for="item in filteredList"
          :key="item.id"
          class="shell-card wrongbook-card"
          @tap="openDetail(item.id)"
        >
          <view class="wrongbook-card__top">
            <view class="wrongbook-card__tags">
              <text class="glass-chip">{{ item.subjectName }}</text>
              <text :class="['status-chip', resolveStatusClass(item.status)]">{{ resolveStatusLabel(item.status) }}</text>
            </view>
            <text class="wrongbook-card__source">{{ item.sourceType === 'teacher_mark' ? '老师标记' : '主动整理' }}</text>
          </view>

          <text class="wrongbook-card__title">
            {{ item.questionNo ? `第 ${item.questionNo} 题 · ` : '' }}{{ item.questionText }}
          </text>

          <text class="wrongbook-card__meta">
            {{ item.wrongReasonLabel || item.wrongReasonCode || '未分类' }} · {{ item.createdAt }}
          </text>
        </view>

        <view v-if="!filteredList.length" class="shell-card empty-state">
          <text class="empty-state__title">当前筛选下没有错题</text>
          <text class="empty-state__copy">可以切换状态、科目，或者自己新增一条错题。</text>
        </view>
      </template>
    </view>
  </view>
</template>

<style>
.wrongbook-hero {
  margin-top: 28rpx;
  padding: 30rpx;
}

.wrongbook-hero__title {
  display: block;
  margin-top: 22rpx;
  font-size: 44rpx;
  line-height: 1.15;
  font-weight: 800;
  color: var(--text-strong);
}

.wrongbook-hero__meta {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.wrongbook-section-label {
  margin-top: 24rpx;
  font-size: 24rpx;
  font-weight: 700;
  color: var(--text-secondary);
}

.wrongbook-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 22rpx;
}

.wrongbook-filters--subjects {
  margin-top: 16rpx;
}

.wrongbook-filter {
  min-height: 68rpx;
  padding: 0 24rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-primary);
  font-size: 24rpx;
  font-weight: 700;
}

.wrongbook-filter--active {
  background: linear-gradient(135deg, #2f7cff 0%, #58b5ff 100%);
  color: #ffffff;
  box-shadow: 0 16rpx 36rpx rgba(47, 124, 255, 0.24);
}

.wrongbook-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 24rpx;
}

.wrongbook-card {
  padding: 28rpx;
}

.wrongbook-card__top,
.wrongbook-card__tags {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.wrongbook-card__tags {
  justify-content: flex-start;
  flex-wrap: wrap;
}

.wrongbook-card__source {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.wrongbook-card__title {
  display: block;
  margin-top: 18rpx;
  font-size: 30rpx;
  line-height: 1.5;
  font-weight: 700;
  color: var(--text-strong);
}

.wrongbook-card__meta {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.wrongbook-retry {
  margin-top: 18rpx;
}
</style>
