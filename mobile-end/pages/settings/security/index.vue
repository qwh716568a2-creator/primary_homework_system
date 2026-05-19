<script setup lang="ts">
import { computed, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store/auth'
import { useMobilePreferencesStore } from '@/store/mobile-preferences'
import { ensureMobileLogin } from '@/utils/page-guard'

const authStore = useAuthStore()
const preferencesStore = useMobilePreferencesStore()

const securityDraft = reactive({
  hideAccountIdentifier: false,
  rememberAccount: true,
  loginAlertEnabled: true,
  appLockEnabled: false,
  biometricEnabled: false
})

const passwordForm = reactive({
  currentPassword: '',
  nextPassword: '',
  confirmPassword: ''
})

function syncDraft() {
  const current = preferencesStore.securitySettings
  securityDraft.hideAccountIdentifier = current.hideAccountIdentifier
  securityDraft.rememberAccount = current.rememberAccount
  securityDraft.loginAlertEnabled = current.loginAlertEnabled
  securityDraft.appLockEnabled = current.appLockEnabled
  securityDraft.biometricEnabled = current.biometricEnabled
}

onShow(() => {
  if (!ensureMobileLogin()) {
    return
  }

  authStore.bootstrap()
  preferencesStore.bootstrap()
  syncDraft()
})

const roleName = computed(() => (authStore.role === 'parent' ? '家长端' : '学生端'))
const profileName = computed(() => authStore.session?.userName || '未同步用户')
const profileSchool = computed(() => authStore.session?.schoolName || '未同步学校信息')
const profileIdentifier = computed(() => authStore.session?.userId || '未同步账号标识')
const profileIdentifierDisplay = computed(() => {
  if (!securityDraft.hideAccountIdentifier) {
    return profileIdentifier.value
  }

  const raw = profileIdentifier.value
  if (raw.length <= 4) {
    return `${raw.slice(0, 1)}***`
  }

  return `${raw.slice(0, 2)}****${raw.slice(-2)}`
})
const passwordStrength = computed(() => {
  const value = passwordForm.nextPassword
  if (!value) {
    return '未输入新密码'
  }

  const score = [
    value.length >= 8,
    /[A-Za-z]/.test(value),
    /\d/.test(value),
    /[^A-Za-z0-9]/.test(value)
  ].filter(Boolean).length

  if (score >= 4) {
    return '强'
  }
  if (score >= 3) {
    return '中'
  }
  return '弱'
})

function goBack() {
  uni.navigateBack()
}

function updateSwitch(
  key:
    | 'hideAccountIdentifier'
    | 'rememberAccount'
    | 'loginAlertEnabled'
    | 'appLockEnabled'
    | 'biometricEnabled',
  value: boolean
) {
  securityDraft[key] = value
}

function saveSecuritySettings() {
  preferencesStore.saveSecuritySettings({
    ...preferencesStore.securitySettings,
    ...securityDraft
  })
  uni.showToast({ title: '安全设置已保存', icon: 'success' })
}

function resetSecuritySettings() {
  preferencesStore.resetSecuritySettings()
  syncDraft()
  uni.showToast({ title: '已恢复默认安全设置', icon: 'success' })
}

function validatePasswordForm() {
  if (!passwordForm.currentPassword || !passwordForm.nextPassword || !passwordForm.confirmPassword) {
    return '请完整填写密码信息'
  }

  if (passwordForm.nextPassword.length < 6) {
    return '新密码至少 6 位'
  }

  if (!/[A-Za-z]/.test(passwordForm.nextPassword) || !/\d/.test(passwordForm.nextPassword)) {
    return '新密码需同时包含字母和数字'
  }

  if (passwordForm.currentPassword === passwordForm.nextPassword) {
    return '新密码不能与当前密码相同'
  }

  if (passwordForm.nextPassword !== passwordForm.confirmPassword) {
    return '两次输入的新密码不一致'
  }

  return ''
}

function savePasswordCheck() {
  const validationError = validatePasswordForm()
  if (validationError) {
    uni.showToast({ title: validationError, icon: 'none' })
    return
  }

  preferencesStore.markPasswordChecked()
  passwordForm.currentPassword = ''
  passwordForm.nextPassword = ''
  passwordForm.confirmPassword = ''
  uni.showToast({ title: '密码校验已保存', icon: 'success' })
}
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">账号与安全</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view class="shell-card security-hero">
      <text class="glass-chip">{{ roleName }}</text>
      <text class="security-hero__title">守住账号、设备与登录偏好</text>
      <text class="security-hero__copy">隐私展示、登录保护和密码校验都会保存在当前小程序环境。</text>
    </view>

    <view class="shell-card security-block">
      <text class="section-title">账号信息</text>
      <view class="security-list">
        <view class="security-item">
          <text class="security-item__label">账号名称</text>
          <text class="security-item__value">{{ profileName }}</text>
        </view>
        <view class="security-item">
          <text class="security-item__label">所属学校</text>
          <text class="security-item__value">{{ profileSchool }}</text>
        </view>
        <view class="security-item">
          <text class="security-item__label">账号标识</text>
          <text class="security-item__value">{{ profileIdentifierDisplay }}</text>
        </view>
      </view>
    </view>

    <view class="shell-card security-block">
      <text class="section-title">隐私与登录保护</text>
      <view class="security-switch-list">
        <view class="security-switch-row">
          <view>
            <text class="security-switch-row__title">隐藏账号标识</text>
            <text class="security-switch-row__copy">在个人中心和设置页中隐藏完整账号标识</text>
          </view>
          <switch
            :checked="securityDraft.hideAccountIdentifier"
            color="#2f7cff"
            @change="updateSwitch('hideAccountIdentifier', $event.detail.value)"
          />
        </view>
        <view class="security-switch-row">
          <view>
            <text class="security-switch-row__title">记住本机账号</text>
            <text class="security-switch-row__copy">下次打开时保留最近一次登录账号信息</text>
          </view>
          <switch
            :checked="securityDraft.rememberAccount"
            color="#2f7cff"
            @change="updateSwitch('rememberAccount', $event.detail.value)"
          />
        </view>
        <view class="security-switch-row">
          <view>
            <text class="security-switch-row__title">登录变动提醒</text>
            <text class="security-switch-row__copy">当设备登录状态发生变化时提醒你检查账号安全</text>
          </view>
          <switch
            :checked="securityDraft.loginAlertEnabled"
            color="#2f7cff"
            @change="updateSwitch('loginAlertEnabled', $event.detail.value)"
          />
        </view>
        <view class="security-switch-row">
          <view>
            <text class="security-switch-row__title">进入应用前校验</text>
            <text class="security-switch-row__copy">打开小程序时启用本机安全校验保护</text>
          </view>
          <switch
            :checked="securityDraft.appLockEnabled"
            color="#2f7cff"
            @change="updateSwitch('appLockEnabled', $event.detail.value)"
          />
        </view>
        <view class="security-switch-row">
          <view>
            <text class="security-switch-row__title">快速验证</text>
            <text class="security-switch-row__copy">启用后可在支持的设备上使用快捷验证方式</text>
          </view>
          <switch
            :checked="securityDraft.biometricEnabled"
            color="#2f7cff"
            @change="updateSwitch('biometricEnabled', $event.detail.value)"
          />
        </view>
      </view>
    </view>

    <view class="shell-card security-block">
      <text class="section-title">密码校验</text>
      <view class="security-password-grid">
        <view class="field-panel">
          <text class="field-label">当前密码</text>
          <input
            v-model="passwordForm.currentPassword"
            class="field-input"
            password
            placeholder="请输入当前密码"
            placeholder-class="field-placeholder"
          />
        </view>
        <view class="field-panel">
          <text class="field-label">新密码</text>
          <input
            v-model="passwordForm.nextPassword"
            class="field-input"
            password
            placeholder="至少 6 位，需包含字母和数字"
            placeholder-class="field-placeholder"
          />
        </view>
        <view class="field-panel">
          <text class="field-label">确认新密码</text>
          <input
            v-model="passwordForm.confirmPassword"
            class="field-input"
            password
            placeholder="请再次输入新密码"
            placeholder-class="field-placeholder"
          />
        </view>
      </view>
      <view class="security-password-meta">
        <text class="security-password-meta__item">密码强度：{{ passwordStrength }}</text>
        <text class="security-password-meta__item">
          最近校验：{{ preferencesStore.securitySettings.passwordCheckedAt || '暂未保存' }}
        </text>
      </view>
      <view class="primary-button security-password-button" @tap="savePasswordCheck">校验并保存密码设置</view>
    </view>

    <view class="shell-card security-block">
      <text class="section-title">安全操作</text>
      <view class="security-actions">
        <view class="secondary-button" @tap="resetSecuritySettings">恢复默认安全设置</view>
        <view class="primary-button" @tap="saveSecuritySettings">保存账号与安全</view>
      </view>
    </view>
  </view>
</template>

<style>
.security-hero,
.security-block {
  margin-top: 18rpx;
  padding: 30rpx;
}

.security-hero__title {
  display: block;
  margin-top: 20rpx;
  font-size: 42rpx;
  line-height: 1.18;
  font-weight: 800;
  color: var(--text-strong);
}

.security-hero__copy {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--text-secondary);
}

.security-list,
.security-switch-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 20rpx;
}

.security-item,
.security-switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.security-item__label,
.security-switch-row__copy {
  font-size: 24rpx;
  color: var(--text-secondary);
}

.security-item__value,
.security-switch-row__title {
  font-size: 24rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.security-switch-row__title,
.security-switch-row__copy {
  display: block;
}

.security-switch-row__copy {
  margin-top: 10rpx;
  line-height: 1.6;
}

.security-password-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16rpx;
  margin-top: 20rpx;
}

.security-password-meta {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-top: 18rpx;
}

.security-password-meta__item {
  font-size: 22rpx;
  color: var(--text-secondary);
}

.security-password-button {
  margin-top: 18rpx;
}

.security-actions {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16rpx;
  margin-top: 20rpx;
}
</style>
