<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppTabBar from '@/components/AppTabBar.vue'
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

const pendingCount = computed(
  () => mobilePortalStore.studentWrongBooks.filter((item) => item.status === 'pending_fix').length
)
const fixedCount = computed(
  () => mobilePortalStore.studentWrongBooks.filter((item) => item.status === 'fixed').length
)
const masteredCount = computed(
  () => mobilePortalStore.studentWrongBooks.filter((item) => item.status === 'mastered').length
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
  if (!ensureMobileRole('student')) {
    return
  }

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
    <view class="wrongbook-home__hero">
      <view>
        <text class="glass-chip">错题本</text>
        <text class="wrongbook-home__title">把错题变成下一次的稳分题</text>
        <text class="wrongbook-home__copy">老师标记和自己整理的错题都会集中沉淀，方便反复订正和回顾。</text>
      </view>
      <view class="wrongbook-home__cta" @tap="goCreate">新增</view>
    </view>

    <view class="wrongbook-home__stats">
      <view class="shell-card wrongbook-home__stat">
        <text class="wrongbook-home__stat-value">{{ pendingCount }}</text>
        <text class="wrongbook-home__stat-label">待订正</text>
      </view>
      <view class="shell-card wrongbook-home__stat">
        <text class="wrongbook-home__stat-value">{{ fixedCount }}</text>
        <text class="wrongbook-home__stat-label">已订正</text>
      </view>
      <view class="shell-card wrongbook-home__stat">
        <text class="wrongbook-home__stat-value">{{ masteredCount }}</text>
        <text class="wrongbook-home__stat-label">已掌握</text>
      </view>
    </view>

    <view class="wrongbook-home__filters">
      <view
        v-for="item in statusFilters"
        :key="item.key"
        :class="['wrongbook-home__filter', { 'wrongbook-home__filter--active': activeStatus === item.key }]"
        @tap="activeStatus = item.key"
      >
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="wrongbook-home__section-label">按科目查看</view>

    <view class="wrongbook-home__filters wrongbook-home__filters--subjects">
      <view
        v-for="item in subjectFilters"
        :key="item.key"
        :class="['wrongbook-home__filter', { 'wrongbook-home__filter--active': activeSubject === item.key }]"
        @tap="activeSubject = item.key"
      >
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="wrongbook-home__list">
      <view v-if="loading" class="shell-card empty-state">
        <text class="empty-state__title">正在同步错题本</text>
        <text class="empty-state__copy">稍等一下，最新错题和订正状态马上出现。</text>
      </view>

      <view v-else-if="errorText" class="shell-card empty-state">
        <text class="empty-state__title">错题本加载失败</text>
        <text class="empty-state__copy">{{ errorText }}</text>
        <view class="secondary-button wrongbook-home__retry" @tap="loadWrongBooks">重新加载</view>
      </view>

      <template v-else>
        <view
          v-for="item in filteredList"
          :key="item.id"
          class="shell-card wrongbook-home__card"
          @tap="openDetail(item.id)"
        >
          <view class="wrongbook-home__card-top">
            <view class="wrongbook-home__card-tags">
              <text class="glass-chip">{{ item.subjectName }}</text>
              <text :class="['status-chip', resolveStatusClass(item.status)]">{{ resolveStatusLabel(item.status) }}</text>
            </view>
            <text class="wrongbook-home__card-source">{{ item.sourceType === 'teacher_mark' ? '老师标记' : '主动整理' }}</text>
          </view>

          <text class="wrongbook-home__card-title">
            {{ item.questionNo ? `第 ${item.questionNo} 题 · ` : '' }}{{ item.questionText }}
          </text>

          <text class="wrongbook-home__card-meta">
            {{ item.wrongReasonLabel || item.wrongReasonCode || '未分类' }} · {{ item.createdAt }}
          </text>
        </view>

        <view v-if="!filteredList.length" class="shell-card empty-state">
          <text class="empty-state__title">当前筛选下没有错题</text>
          <text class="empty-state__copy">可以切换状态、科目，或者自己新增一条错题。</text>
        </view>
      </template>
    </view>

    <AppTabBar role="student" active="wrongbook" />
  </view>
</template>

<style>
.wrongbook-home__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20rpx;
}

.wrongbook-home__title {
  display: block;
  margin-top: 24rpx;
  font-size: 58rpx;
  line-height: 1.08;
  font-weight: 800;
  color: var(--text-strong);
}

.wrongbook-home__copy {
  display: block;
  max-width: 520rpx;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--text-secondary);
}

.wrongbook-home__cta {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120rpx;
  min-height: 74rpx;
  padding: 0 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #2f7cff 0%, #58b5ff 100%);
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 700;
  box-shadow: 0 20rpx 48rpx rgba(47, 124, 255, 0.24);
}

.wrongbook-home__stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 28rpx;
}

.wrongbook-home__stat {
  padding: 24rpx;
}

.wrongbook-home__stat-value {
  display: block;
  font-size: 52rpx;
  line-height: 1;
  font-weight: 800;
  color: var(--text-strong);
}

.wrongbook-home__stat-label {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.wrongbook-home__section-label {
  margin-top: 24rpx;
  font-size: 24rpx;
  font-weight: 700;
  color: var(--text-secondary);
}

.wrongbook-home__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 22rpx;
}

.wrongbook-home__filters--subjects {
  margin-top: 16rpx;
}

.wrongbook-home__filter {
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

.wrongbook-home__filter--active {
  background: linear-gradient(135deg, #2f7cff 0%, #58b5ff 100%);
  color: #ffffff;
  box-shadow: 0 16rpx 36rpx rgba(47, 124, 255, 0.24);
}

.wrongbook-home__list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 24rpx;
}

.wrongbook-home__card {
  padding: 28rpx;
}

.wrongbook-home__card-top,
.wrongbook-home__card-tags {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}

.wrongbook-home__card-tags {
  justify-content: flex-start;
  flex-wrap: wrap;
}

.wrongbook-home__card-source {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.wrongbook-home__card-title {
  display: block;
  margin-top: 18rpx;
  font-size: 30rpx;
  line-height: 1.5;
  font-weight: 700;
  color: var(--text-strong);
}

.wrongbook-home__card-meta {
  display: block;
  margin-top: 14rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.wrongbook-home__retry {
  margin-top: 18rpx;
}
</style>
