<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import HomeworkCard from '@/components/HomeworkCard.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useAuthStore } from '@/store/auth'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'
import type { HomeworkStatus } from '@/types/mobile'

const authStore = useAuthStore()
const mobilePortalStore = useMobilePortalStore()
const activeFilter = ref<'all' | HomeworkStatus>('all')
const activeSubject = ref('all')
const loading = ref(false)
const errorText = ref('')

const filters: Array<{ key: 'all' | HomeworkStatus; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待完成' },
  { key: 'submitted', label: '已提交' },
  { key: 'revision', label: '待订正' },
  { key: 'completed', label: '已完成' }
]

const subjectFilters = computed(() => [
  { key: 'all', label: '全部科目' },
  ...Array.from(new Set(mobilePortalStore.studentHomeworks.map((item) => item.subject))).map((subject) => ({
    key: subject,
    label: subject
  }))
])

const filteredHomeworks = computed(() => {
  let list = mobilePortalStore.studentHomeworks

  if (activeFilter.value !== 'all') {
    list = list.filter((item) => item.status === activeFilter.value)
  }

  if (activeSubject.value !== 'all') {
    list = list.filter((item) => item.subject === activeSubject.value)
  }

  return list
})

const pendingCount = computed(
  () => mobilePortalStore.studentHomeworks.filter((item) => item.status === 'pending').length
)
const revisionCount = computed(
  () => mobilePortalStore.studentHomeworks.filter((item) => item.status === 'revision').length
)
const profileSchool = computed(() => authStore.session?.schoolName || mobilePortalStore.studentProfile.school || '我的学校')
const profileName = computed(() => authStore.session?.userName || mobilePortalStore.studentProfile.name || '同学')
const profileHeadline = computed(() => mobilePortalStore.studentProfile.headline || '已同步你的作业、提交和老师反馈。')
const profileBadge = computed(() => mobilePortalStore.studentProfile.className || '学生端')

async function loadHomeworks() {
  if (!ensureMobileRole('student')) {
    return
  }

  authStore.bootstrap()
  mobilePortalStore.bootstrap()
  loading.value = true
  errorText.value = ''

  try {
    await mobilePortalStore.loadStudentHomeworks()
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '作业加载失败'
  } finally {
    loading.value = false
  }
}

onShow(() => {
  void loadHomeworks()
})

function openHomework(homeworkId: string) {
  uni.navigateTo({
    url: `/pages/student/detail/index?homeworkId=${homeworkId}`
  })
}
</script>

<template>
  <view class="mobile-page">
    <view class="student-home__hero">
      <view>
        <text class="glass-chip">{{ profileSchool }}</text>
        <text class="student-home__name">{{ profileName }}</text>
        <text class="student-home__headline">{{ profileHeadline }}</text>
      </view>
      <view class="student-home__badge">
        <text>{{ profileBadge }}</text>
      </view>
    </view>

    <view class="student-home__stats">
      <view class="student-home__stat shell-card">
        <text class="student-home__stat-value">{{ pendingCount }}</text>
        <text class="student-home__stat-label">待完成</text>
      </view>
      <view class="student-home__stat shell-card">
        <text class="student-home__stat-value">{{ revisionCount }}</text>
        <text class="student-home__stat-label">待订正</text>
      </view>
    </view>

    <view class="student-home__filters">
      <view
        v-for="filter in filters"
        :key="filter.key"
        :class="['student-home__filter', { 'student-home__filter--active': activeFilter === filter.key }]"
        @tap="activeFilter = filter.key"
      >
        <text>{{ filter.label }}</text>
      </view>
    </view>

    <view class="student-home__section-label">按科目查看</view>

    <view class="student-home__filters student-home__filters--subjects">
      <view
        v-for="subject in subjectFilters"
        :key="subject.key"
        :class="['student-home__filter', { 'student-home__filter--active': activeSubject === subject.key }]"
        @tap="activeSubject = subject.key"
      >
        <text>{{ subject.label }}</text>
      </view>
    </view>

    <view class="student-home__list">
      <view v-if="loading" class="shell-card empty-state student-home__empty">
        <text class="empty-state__title">正在同步作业数据</text>
        <text class="empty-state__copy">稍等一下，最新作业和反馈马上出现。</text>
      </view>

      <view v-else-if="errorText" class="shell-card empty-state student-home__empty">
        <text class="empty-state__title">作业加载失败</text>
        <text class="empty-state__copy">{{ errorText }}</text>
        <view class="secondary-button student-home__retry" @tap="loadHomeworks">重新加载</view>
      </view>

      <template v-else>
        <HomeworkCard
          v-for="item in filteredHomeworks"
          :key="item.id"
          :item="item"
          @tap="openHomework(item.id)"
        />

        <view v-if="filteredHomeworks.length === 0" class="shell-card empty-state student-home__empty">
          <text class="empty-state__title">当前筛选下没有作业</text>
          <text class="empty-state__copy">可以切换状态或科目，看看其他作业安排。</text>
        </view>
      </template>
    </view>

    <AppTabBar role="student" active="home" />
  </view>
</template>

<style>
.student-home__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20rpx;
}

.student-home__name {
  display: block;
  margin-top: 24rpx;
  font-size: 64rpx;
  line-height: 1.04;
  font-weight: 800;
  color: var(--text-strong);
}

.student-home__headline {
  display: block;
  max-width: 500rpx;
  margin-top: 22rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--text-secondary);
}

.student-home__badge {
  padding: 18rpx 22rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.82);
  color: var(--text-primary);
  font-size: 22rpx;
  font-weight: 700;
}

.student-home__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 30rpx;
}

.student-home__stat {
  padding: 24rpx;
}

.student-home__stat-value {
  display: block;
  font-size: 54rpx;
  line-height: 1;
  font-weight: 800;
  color: var(--text-strong);
}

.student-home__stat-label {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.student-home__filters {
  display: flex;
  gap: 12rpx;
  margin-top: 28rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.student-home__filter {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 126rpx;
  height: 70rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.72);
  color: var(--text-secondary);
  font-size: 22rpx;
  font-weight: 700;
}

.student-home__filter--active {
  background: linear-gradient(135deg, #2f7cff 0%, #53b4ff 100%);
  color: #ffffff;
}

.student-home__list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 26rpx;
}

.student-home__section-label {
  margin-top: 20rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: var(--text-secondary);
}

.student-home__filters--subjects {
  margin-top: 16rpx;
}

.student-home__empty {
  margin-top: 4rpx;
}

.student-home__retry {
  margin-top: 18rpx;
}
</style>
