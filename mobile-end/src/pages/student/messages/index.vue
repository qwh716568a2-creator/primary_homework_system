<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppTabBar from '@/components/AppTabBar.vue'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'

const mobilePortalStore = useMobilePortalStore()
const readStatus = ref<'all' | 'unread' | 'read'>('all')
const loading = ref(false)
const errorText = ref('')

const filters: Array<{ key: 'all' | 'unread' | 'read'; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'unread', label: '未读' },
  { key: 'read', label: '已读' }
]

onShow(() => {
  void loadMessages()
})

async function loadMessages() {
  if (!ensureMobileRole('student')) {
    return
  }

  loading.value = true
  errorText.value = ''

  try {
    await mobilePortalStore.loadStudentMessages(readStatus.value)
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '消息加载失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="mobile-page">
    <view>
      <text class="glass-chip">学生消息</text>
      <text class="messages-title">所有提醒都汇总在这里</text>
      <text class="messages-copy">新作业、催交提醒、批改反馈会按时间更新。</text>
    </view>

    <view class="messages-filters">
      <view
        v-for="item in filters"
        :key="item.key"
        :class="['messages-filter', { 'messages-filter--active': readStatus === item.key }]"
        @tap="readStatus = item.key; loadMessages()"
      >
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="messages-list">
      <view v-if="loading" class="shell-card empty-state">
        <text class="empty-state__title">正在同步消息</text>
        <text class="empty-state__copy">消息提醒马上就会加载出来。</text>
      </view>

      <view v-else-if="errorText" class="shell-card empty-state">
        <text class="empty-state__title">消息加载失败</text>
        <text class="empty-state__copy">{{ errorText }}</text>
        <view class="secondary-button messages-retry" @tap="loadMessages">重新加载</view>
      </view>

      <template v-else>
        <view v-for="item in mobilePortalStore.studentMessages" :key="item.id" class="shell-card message-item">
          <view class="message-item__top">
            <text class="message-item__title">{{ item.title }}</text>
            <view v-if="item.unread" class="message-item__dot" />
          </view>
          <text class="message-item__content">{{ item.content }}</text>
          <text class="message-item__time">{{ item.time }}</text>
        </view>

        <view v-if="!mobilePortalStore.studentMessages.length" class="shell-card empty-state">
          <text class="empty-state__title">当前没有消息</text>
          <text class="empty-state__copy">作业提醒和老师反馈会显示在这里。</text>
        </view>
      </template>
    </view>

    <AppTabBar role="student" active="messages" />
  </view>
</template>

<style>
.messages-title {
  display: block;
  margin-top: 24rpx;
  font-size: 56rpx;
  line-height: 1.1;
  font-weight: 800;
  color: var(--text-strong);
}

.messages-copy {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
  line-height: 1.7;
}

.messages-filters {
  display: flex;
  gap: 12rpx;
  margin-top: 26rpx;
}

.messages-filter {
  min-width: 120rpx;
  height: 66rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.74);
  color: var(--text-secondary);
  font-size: 22rpx;
  font-weight: 700;
}

.messages-filter--active {
  background: linear-gradient(135deg, #2f7cff 0%, #53b4ff 100%);
  color: #ffffff;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 28rpx;
}

.message-item {
  padding: 26rpx;
}

.message-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.message-item__title {
  font-size: 28rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.message-item__dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: var(--brand-blue);
}

.message-item__content {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--text-primary);
}

.message-item__time {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.messages-retry {
  margin-top: 18rpx;
}
</style>
