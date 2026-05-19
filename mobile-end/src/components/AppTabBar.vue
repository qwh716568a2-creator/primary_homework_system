<script setup lang="ts">
import { computed } from 'vue'
import type { UserRole } from '@/types/mobile'
import { goToProfile, goToRoleHome, goToRoleMessages } from '@/utils/navigation'

const props = defineProps<{
  role: UserRole
  active: 'home' | 'wrongbook' | 'messages' | 'profile'
}>()

const tabs = computed(() => {
  if (props.role === 'parent') {
    return [
      {
        key: 'home' as const,
        label: '孩子'
      },
      {
        key: 'messages' as const,
        label: '消息'
      },
      {
        key: 'profile' as const,
        label: '我的'
      }
    ]
  }

  return [
    {
      key: 'home' as const,
      label: '作业'
    },
    {
      key: 'wrongbook' as const,
      label: '错题本'
    },
    {
      key: 'messages' as const,
      label: '消息'
    },
    {
      key: 'profile' as const,
      label: '我的'
    }
  ]
})

function handleNavigate(key: 'home' | 'wrongbook' | 'messages' | 'profile') {
  if (key === props.active) {
    return
  }

  if (key === 'home') {
    goToRoleHome(props.role)
    return
  }

  if (key === 'wrongbook') {
    uni.reLaunch({
      url: '/pages/student/wrongbook/home'
    })
    return
  }

  if (key === 'messages') {
    goToRoleMessages(props.role)
    return
  }

  goToProfile()
}
</script>

<template>
  <view class="tabbar-shell">
    <view
      v-for="tab in tabs"
      :key="tab.key"
      :class="['tabbar-item', { 'tabbar-item--active': tab.key === active }]"
      @tap="handleNavigate(tab.key)"
    >
      <view class="tabbar-dot" />
      <text class="tabbar-label">{{ tab.label }}</text>
    </view>
  </view>
</template>

<style>
.tabbar-shell {
  position: fixed;
  left: 26rpx;
  right: 26rpx;
  bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
  padding: 16rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.82);
  border: 2rpx solid rgba(255, 255, 255, 0.84);
  box-shadow: 0 24rpx 54rpx rgba(36, 65, 101, 0.16);
  backdrop-filter: blur(24rpx);
}

.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  min-width: 0;
  padding: 18rpx 8rpx;
  border-radius: 24rpx;
}

.tabbar-item--active {
  background: linear-gradient(135deg, rgba(47, 124, 255, 0.14), rgba(66, 183, 255, 0.12));
}

.tabbar-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: rgba(130, 158, 190, 0.5);
}

.tabbar-item--active .tabbar-dot {
  background: var(--brand-blue);
  box-shadow: 0 0 0 10rpx rgba(47, 124, 255, 0.15);
}

.tabbar-label {
  font-size: 22rpx;
  font-weight: 700;
  color: var(--text-secondary);
  line-height: 1;
  white-space: nowrap;
}

.tabbar-item--active .tabbar-label {
  color: var(--text-strong);
}
</style>
