<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import AppTabBar from '@/components/AppTabBar.vue'
import { useAuthStore } from '@/store/auth'
import { useMobilePreferencesStore } from '@/store/mobile-preferences'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileLogin } from '@/utils/page-guard'
import { goToAccountSecurity, goToMessageSettings, goToStudentWrongBook } from '@/utils/navigation'

const authStore = useAuthStore()
const preferencesStore = useMobilePreferencesStore()
const mobilePortalStore = useMobilePortalStore()

const COPY = {
  parent: '\u5bb6\u957f',
  student: '\u5b66\u751f',
  noSchool: '\u672a\u540c\u6b65\u5b66\u6821\u4fe1\u606f',
  parentAccount: '\u5bb6\u957f\u8d26\u53f7',
  studentAccount: '\u5b66\u751f\u8d26\u53f7',
  noIdentifier: '\u672a\u540c\u6b65\u8d26\u53f7\u6807\u8bc6',
  parentSide: '\u5bb6\u957f\u7aef',
  studentSide: '\u5b66\u751f\u7aef'
} as const

onShow(async () => {
  if (ensureMobileLogin()) {
    authStore.bootstrap()
    await preferencesStore.bootstrap()
    mobilePortalStore.bootstrap()
  }
})

const isParent = computed(() => authStore.role === 'parent')
const profileName = computed(() => authStore.session?.userName || (isParent.value ? COPY.parent : COPY.student))
const profileSchool = computed(() => authStore.session?.schoolName || COPY.noSchool)
const profileSubline = computed(() => (isParent.value ? COPY.parentAccount : COPY.studentAccount))
const profileIdentifier = computed(() => authStore.session?.userId || COPY.noIdentifier)
const profileIdentifierDisplay = computed(() => {
  if (!preferencesStore.securitySettings.hideAccountIdentifier) {
    return profileIdentifier.value
  }

  const raw = profileIdentifier.value
  if (raw.length <= 4) {
    return `${raw.slice(0, 1)}***`
  }

  return `${raw.slice(0, 2)}****${raw.slice(-2)}`
})
const currentRole = computed(() => (isParent.value ? 'parent' : 'student'))
const roleName = computed(() => (isParent.value ? COPY.parentSide : COPY.studentSide))
const roleSummary = computed(() => {
  if (isParent.value) {
    const childCount = mobilePortalStore.children.length
    const pendingCount = mobilePortalStore.children.reduce((sum, item) => sum + (item.pendingCount ?? 0), 0)
    return `\u5df2\u5173\u8054 ${childCount} \u4f4d\u5b69\u5b50\uff0c\u5f53\u524d\u5f85\u5b8c\u6210\u4f5c\u4e1a ${pendingCount} \u9879\u3002`
  }

  return `\u5f53\u524d\u540c\u6b65 ${mobilePortalStore.studentHomeworks.length} \u4efd\u4f5c\u4e1a\uff0c\u6d88\u606f ${mobilePortalStore.studentMessages.length} \u6761\u3002`
})
const securitySummary = computed(() => {
  const enabledItems = [
    preferencesStore.securitySettings.rememberAccount,
    preferencesStore.securitySettings.loginAlertEnabled,
    preferencesStore.securitySettings.appLockEnabled,
    preferencesStore.securitySettings.biometricEnabled
  ].filter(Boolean).length

  return `\u5df2\u542f\u7528 ${enabledItems} \u9879\u5b89\u5168\u4fdd\u62a4`
})

function logout() {
  authStore.logout()
  mobilePortalStore.resetPortalData()
  uni.reLaunch({ url: '/pages/auth/index' })
}
</script>

<template>
  <view class="mobile-page">
    <view class="shell-card profile-hero">
      <view class="profile-hero__brand">
        <image class="profile-hero__logo" src="/static/system-logo.png" mode="aspectFit" />
        <view class="profile-hero__avatar">{{ profileName.slice(0, 1) }}</view>
      </view>
      <text class="profile-hero__name">{{ profileName }}</text>
      <text class="profile-hero__meta">{{ profileSchool }}</text>
      <text class="profile-hero__subline">{{ profileSubline }} · {{ profileIdentifierDisplay }}</text>
      <view class="profile-hero__role">{{ roleName }}</view>
      <text class="profile-hero__summary">{{ roleSummary }}</text>
    </view>

    <view class="shell-card profile-block">
      <text class="section-title">账号信息</text>
      <view class="profile-list">
        <view class="profile-item">
          <text class="profile-item__label">当前身份</text>
          <text class="profile-item__value">{{ roleName }}</text>
        </view>
        <view class="profile-item">
          <text class="profile-item__label">所属学校</text>
          <text class="profile-item__value">{{ profileSchool }}</text>
        </view>
        <view class="profile-item">
          <text class="profile-item__label">账号标识</text>
          <text class="profile-item__value">{{ profileIdentifierDisplay }}</text>
        </view>
      </view>
    </view>

    <view class="shell-card profile-block">
      <text class="section-title">常用操作</text>
      <view class="profile-actions">
        <view v-if="!isParent" class="secondary-button" @tap="goToStudentWrongBook">我的错题本</view>
        <view class="profile-entry" @tap="goToMessageSettings">
          <view>
            <text class="profile-entry__title">消息设置</text>
            <text class="profile-entry__copy">提醒偏好、免打扰和消息整理</text>
          </view>
          <text class="profile-entry__meta">去设置</text>
        </view>
        <view class="profile-entry" @tap="goToAccountSecurity">
          <view>
            <text class="profile-entry__title">账号与安全</text>
            <text class="profile-entry__copy">{{ securitySummary }}</text>
          </view>
          <text class="profile-entry__meta">去管理</text>
        </view>
        <view class="primary-button" @tap="logout">退出登录</view>
      </view>
    </view>

    <AppTabBar :role="currentRole" active="profile" />
  </view>
</template>

<style>
.profile-hero,
.profile-block {
  padding: 30rpx;
}

.profile-hero__brand {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.profile-hero__logo {
  width: 108rpx;
  height: 108rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 18rpx 34rpx rgba(47, 124, 255, 0.14);
}

.profile-hero__avatar {
  width: 108rpx;
  height: 108rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #2f7cff 0%, #5ec7ff 100%);
  color: #ffffff;
  font-size: 40rpx;
  font-weight: 800;
  text-align: center;
  line-height: 108rpx;
}

.profile-hero__name {
  display: block;
  margin-top: 22rpx;
  font-size: 46rpx;
  font-weight: 800;
  color: var(--text-strong);
}

.profile-hero__meta,
.profile-hero__subline,
.profile-hero__summary {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.profile-hero__role {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 22rpx;
  min-height: 52rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: rgba(47, 124, 255, 0.1);
  color: var(--brand-blue);
  font-size: 22rpx;
  font-weight: 700;
}

.profile-block {
  margin-top: 18rpx;
}

.profile-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 20rpx;
}

.profile-item,
.profile-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.profile-item__label,
.profile-entry__copy {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.profile-item__value,
.profile-entry__title,
.profile-entry__meta {
  font-size: 24rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.profile-entry {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(244, 248, 255, 0.78);
}

.profile-entry__title,
.profile-entry__copy {
  display: block;
}

.profile-entry__copy {
  margin-top: 10rpx;
}

.profile-entry__meta {
  color: var(--brand-blue);
}

.profile-actions {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16rpx;
  margin-top: 18rpx;
}
</style>
