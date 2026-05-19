<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import HomeworkCard from '@/components/HomeworkCard.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { useAuthStore } from '@/store/auth'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'

const authStore = useAuthStore()
const mobilePortalStore = useMobilePortalStore()
const activeSubject = ref('all')
const loading = ref(false)
const errorText = ref('')

onShow(() => {
  void loadHomePage()
})

const currentChild = computed(() => mobilePortalStore.currentChild)
const pendingCount = computed(() => currentChild.value?.pendingCount ?? 0)
const subjectFilters = computed(() => [
  { key: 'all', label: '全部科目' },
  ...Array.from(new Set(mobilePortalStore.currentChildHomeworks.map((item) => item.subject))).map((subject) => ({
    key: subject,
    label: subject
  }))
])
const filteredHomeworks = computed(() => {
  if (activeSubject.value === 'all') {
    return mobilePortalStore.currentChildHomeworks
  }

  return mobilePortalStore.currentChildHomeworks.filter((item) => item.subject === activeSubject.value)
})
const profileSchool = computed(() => authStore.session?.schoolName || mobilePortalStore.parentProfile.school || '我的学校')
const profileName = computed(() => authStore.session?.userName || mobilePortalStore.parentProfile.name || '家长')
const profileHeadline = computed(() => mobilePortalStore.parentProfile.headline || '已同步孩子作业、协助提交和消息提醒。')

watch(subjectFilters, (filters) => {
  if (!filters.some((item) => item.key === activeSubject.value)) {
    activeSubject.value = 'all'
  }
})

async function loadHomePage() {
  if (!ensureMobileRole('parent')) {
    return
  }

  authStore.bootstrap()
  mobilePortalStore.bootstrap()
  loading.value = true
  errorText.value = ''

  try {
    await mobilePortalStore.loadParentChildren()
    if (mobilePortalStore.activeChildId) {
      await mobilePortalStore.loadParentHomeworks(mobilePortalStore.activeChildId)
    }
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '孩子作业加载失败'
  } finally {
    loading.value = false
  }
}

async function selectChild(childId: string) {
  mobilePortalStore.selectChild(childId)
  activeSubject.value = 'all'
  loading.value = true
  errorText.value = ''

  try {
    await mobilePortalStore.loadParentHomeworks(childId)
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '作业加载失败'
  } finally {
    loading.value = false
  }
}

function openHomework(homeworkId: string) {
  if (!currentChild.value) {
    return
  }

  uni.navigateTo({
    url: `/pages/parent/detail/index?childId=${currentChild.value.id}&homeworkId=${homeworkId}`
  })
}
</script>

<template>
  <view class="mobile-page">
    <view>
      <text class="glass-chip">{{ profileSchool }}</text>
      <text class="parent-home__title">{{ profileName }}</text>
      <text class="parent-home__copy">{{ profileHeadline }}</text>
    </view>

    <view class="parent-home__children">
      <view
        v-for="child in mobilePortalStore.children"
        :key="child.id"
        :class="['parent-home__child', { 'parent-home__child--active': child.id === currentChild?.id }]"
        @tap="selectChild(child.id)"
      >
        <text class="parent-home__child-name">{{ child.name }}</text>
        <text class="parent-home__child-meta">{{ child.gradeName }} · {{ child.className }}</text>
      </view>
    </view>

    <view v-if="currentChild" class="parent-home__insight shell-card">
      <text class="parent-home__insight-value">{{ pendingCount }}</text>
      <text class="parent-home__insight-label">{{ currentChild.name }} 当前待完成作业</text>
    </view>

    <view class="parent-home__section-label">按科目查看</view>

    <view class="parent-home__subjects">
      <view
        v-for="subject in subjectFilters"
        :key="subject.key"
        :class="['parent-home__subject', { 'parent-home__subject--active': activeSubject === subject.key }]"
        @tap="activeSubject = subject.key"
      >
        <text>{{ subject.label }}</text>
      </view>
    </view>

    <view class="parent-home__list">
      <view v-if="loading" class="shell-card empty-state parent-home__empty">
        <text class="empty-state__title">正在同步孩子作业</text>
        <text class="empty-state__copy">最新作业和老师反馈正在加载。</text>
      </view>

      <view v-else-if="errorText" class="shell-card empty-state parent-home__empty">
        <text class="empty-state__title">孩子作业加载失败</text>
        <text class="empty-state__copy">{{ errorText }}</text>
        <view class="secondary-button parent-home__retry" @tap="loadHomePage">重新加载</view>
      </view>

      <template v-else>
        <HomeworkCard
          v-for="item in filteredHomeworks"
          :key="item.id"
          :item="item"
          :child-name="currentChild?.name"
          @tap="openHomework(item.id)"
        />

        <view v-if="!mobilePortalStore.children.length" class="shell-card empty-state parent-home__empty">
          <text class="empty-state__title">当前还没有绑定孩子</text>
          <text class="empty-state__copy">请先在管理端完成家长与学生绑定关系。</text>
        </view>

        <view
          v-else-if="filteredHomeworks.length === 0"
          class="shell-card empty-state parent-home__empty"
        >
          <text class="empty-state__title">这位孩子当前没有该科目的作业</text>
          <text class="empty-state__copy">换一个科目看看，或者切换到其他孩子。</text>
        </view>
      </template>
    </view>

    <AppTabBar role="parent" active="home" />
  </view>
</template>

<style>
.parent-home__title {
  display: block;
  margin-top: 24rpx;
  font-size: 58rpx;
  line-height: 1.08;
  font-weight: 800;
  color: var(--text-strong);
}

.parent-home__copy {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--text-secondary);
}

.parent-home__children {
  display: flex;
  gap: 14rpx;
  margin-top: 30rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.parent-home__child {
  min-width: 220rpx;
  padding: 20rpx 22rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.78);
}

.parent-home__child--active {
  background: linear-gradient(135deg, rgba(28, 184, 163, 0.16), rgba(90, 203, 255, 0.12));
}

.parent-home__child-name {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.parent-home__child-meta {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.parent-home__insight {
  margin-top: 26rpx;
  padding: 26rpx;
}

.parent-home__insight-value {
  display: block;
  font-size: 56rpx;
  line-height: 1;
  font-weight: 800;
  color: var(--text-strong);
}

.parent-home__insight-label {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.parent-home__list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 26rpx;
}

.parent-home__section-label {
  margin-top: 22rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: var(--text-secondary);
}

.parent-home__subjects {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.parent-home__subject {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 146rpx;
  height: 68rpx;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.74);
  color: var(--text-secondary);
  font-size: 22rpx;
  font-weight: 700;
}

.parent-home__subject--active {
  background: linear-gradient(135deg, rgba(28, 184, 163, 0.92), rgba(90, 203, 255, 0.92));
  color: #ffffff;
}

.parent-home__empty {
  margin-top: 4rpx;
}

.parent-home__retry {
  margin-top: 18rpx;
}
</style>
