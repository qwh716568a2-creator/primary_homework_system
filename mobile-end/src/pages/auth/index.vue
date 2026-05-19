<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store/auth'
import { goToRoleHome } from '@/utils/navigation'
import type { LoginPayload, UserRole } from '@/types/mobile'

const authStore = useAuthStore()
const loading = ref(false)
const feedback = reactive({
  message: ''
})

const form = reactive<LoginPayload>({
  role: 'student',
  account: '',
  password: '',
  schoolId: ''
})

const roleOptions: Array<{ key: UserRole; label: string }> = [
  {
    key: 'student',
    label: '我是学生'
  },
  {
    key: 'parent',
    label: '我是家长'
  }
]

const requiresSchoolId = computed(() => form.role === 'student')
const accountLabel = computed(() => (form.role === 'student' ? '学号' : '手机号'))
const accountPlaceholder = computed(() =>
  form.role === 'student' ? '请输入学生学号' : '请输入家长手机号'
)
watch(
  () => form.role,
  (role) => {
    feedback.message = ''
    if (role === 'parent') {
      form.schoolId = ''
    }
  }
)

onShow(() => {
  authStore.bootstrap()
  if (authStore.isLoggedIn && authStore.role) {
    goToRoleHome(authStore.role)
  }
})

function showError(message: string) {
  feedback.message = message
  uni.showToast({ title: message, icon: 'none' })
}

async function handleLogin() {
  if (loading.value) {
    return
  }

  feedback.message = ''

  if (!form.account.trim()) {
    showError(`请输入${accountLabel.value}`)
    return
  }

  if (!form.password.trim()) {
    showError('请输入登录密码')
    return
  }

  if (requiresSchoolId.value && !form.schoolId?.trim()) {
    showError('学生登录请输入学校 ID')
    return
  }

  loading.value = true
  uni.showLoading({ title: '登录中' })

  try {
    const session = await authStore.login(form)
    uni.hideLoading()
    uni.showToast({ title: `欢迎回来，${session.userName}`, icon: 'none' })
    goToRoleHome(session.role)
  } catch (error) {
    uni.hideLoading()
    showError(error instanceof Error ? error.message : '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="mobile-page mobile-page--auth auth-page">
    <view class="auth-backdrop">
      <view class="auth-backdrop__orb auth-backdrop__orb--one"></view>
      <view class="auth-backdrop__orb auth-backdrop__orb--two"></view>
      <view class="auth-backdrop__orb auth-backdrop__orb--three"></view>
      <view class="auth-backdrop__line auth-backdrop__line--one"></view>
      <view class="auth-backdrop__line auth-backdrop__line--two"></view>
    </view>

    <view class="auth-shell">
      <view class="auth-topbar">
        <image class="auth-brandmark auth-brandmark--logo" src="/static/system-logo.png" mode="aspectFit" />
        <view class="auth-topbar__info">
          <text class="auth-topbar__name">小课后</text>
          <text class="auth-topbar__sub">小学课后作业系统</text>
        </view>
        <view class="auth-topbar__badge">安全登录</view>
      </view>

      <view class="auth-hero">
        <text class="auth-hero__eyebrow">统一登录</text>
        <text class="auth-hero__title">登录你的作业空间</text>
      </view>

      <view class="shell-card auth-panel">
        <view class="auth-panel__head">
          <text class="auth-panel__title">账号登录</text>
        </view>

        <view class="auth-role-switcher">
          <view
            v-for="role in roleOptions"
            :key="role.key"
            :class="['auth-role', { 'auth-role--active': form.role === role.key }]"
            hover-class="auth-role--press"
            hover-stay-time="70"
            @tap="form.role = role.key"
          >
            <view class="auth-role__flag" />
            <text class="auth-role__label">{{ role.label }}</text>
          </view>
        </view>

        <view class="auth-fields">
          <view class="field-panel auth-field-panel">
            <text class="field-label">{{ accountLabel }}</text>
            <input
              v-model="form.account"
              class="field-input auth-field-input"
              :placeholder="accountPlaceholder"
              placeholder-class="field-placeholder"
            />
          </view>

          <view v-if="requiresSchoolId" class="field-panel auth-field-panel">
            <text class="field-label">学校 ID</text>
            <input
              v-model="form.schoolId"
              class="field-input auth-field-input"
              placeholder="请输入学校 ID"
              placeholder-class="field-placeholder"
            />
          </view>

          <view class="field-panel auth-field-panel">
            <text class="field-label">登录密码</text>
            <input
              v-model="form.password"
              class="field-input auth-field-input"
              password
              placeholder="请输入登录密码"
              placeholder-class="field-placeholder"
            />
          </view>
        </view>

        <view :class="['primary-button', 'auth-panel__action', { 'auth-panel__action--loading': loading }]" @tap="handleLogin">
          <text>{{ loading ? '正在安全登录...' : '登录' }}</text>
        </view>

        <view v-if="feedback.message" class="auth-feedback auth-feedback--error">
          <text>{{ feedback.message }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.auth-page {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(142, 217, 255, 0.38), transparent 26%),
    radial-gradient(circle at left 18%, rgba(47, 124, 255, 0.14), transparent 32%),
    linear-gradient(180deg, #f3f8ff 0%, #f9fbff 48%, #f3f8ff 100%);
}

.auth-backdrop {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.auth-backdrop__orb,
.auth-backdrop__line {
  position: absolute;
  border-radius: 999rpx;
}

.auth-backdrop__orb--one {
  top: 32rpx;
  right: -56rpx;
  width: 280rpx;
  height: 280rpx;
  background: radial-gradient(circle, rgba(88, 181, 255, 0.28) 0%, rgba(88, 181, 255, 0.03) 68%, transparent 72%);
}

.auth-backdrop__orb--two {
  top: 360rpx;
  left: -98rpx;
  width: 240rpx;
  height: 240rpx;
  background: radial-gradient(circle, rgba(47, 124, 255, 0.16) 0%, rgba(47, 124, 255, 0.02) 68%, transparent 72%);
}

.auth-backdrop__orb--three {
  bottom: 120rpx;
  right: -68rpx;
  width: 220rpx;
  height: 220rpx;
  background: radial-gradient(circle, rgba(28, 184, 163, 0.14) 0%, rgba(28, 184, 163, 0.02) 68%, transparent 72%);
}

.auth-backdrop__line--one {
  top: 154rpx;
  right: 46rpx;
  width: 180rpx;
  height: 180rpx;
  border: 2rpx solid rgba(104, 129, 162, 0.12);
}

.auth-backdrop__line--two {
  bottom: 180rpx;
  left: -24rpx;
  width: 220rpx;
  height: 220rpx;
  border: 2rpx solid rgba(104, 129, 162, 0.1);
}

.auth-shell {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.auth-topbar {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.auth-brandmark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 22rpx;
  background: linear-gradient(135deg, #122947 0%, #2f7cff 100%);
  box-shadow: 0 18rpx 32rpx rgba(22, 54, 106, 0.18);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 800;
}

.auth-brandmark--logo {
  padding: 6rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18rpx 36rpx rgba(22, 54, 106, 0.14);
}

.auth-topbar__info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.auth-topbar__name {
  font-size: 24rpx;
  font-weight: 800;
  color: var(--text-strong);
  letter-spacing: 0.08em;
}

.auth-topbar__sub {
  font-size: 20rpx;
  color: var(--text-secondary);
}

.auth-topbar__badge {
  margin-left: auto;
  min-height: 52rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.72);
  border: 2rpx solid rgba(202, 219, 241, 0.8);
  color: var(--text-secondary);
  font-size: 22rpx;
  line-height: 52rpx;
  font-weight: 700;
}

.auth-hero {
  padding: 8rpx 4rpx 4rpx;
}

.auth-hero__eyebrow {
  display: inline-flex;
  min-height: 48rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: rgba(47, 124, 255, 0.1);
  color: var(--brand-blue);
  font-size: 20rpx;
  font-weight: 700;
  line-height: 48rpx;
}

.auth-hero__title {
  display: block;
  margin-top: 18rpx;
  font-size: 72rpx;
  line-height: 1.02;
  font-weight: 900;
  letter-spacing: -0.04em;
  color: #081a33;
}

.auth-panel {
  padding: 28rpx;
  border-radius: 38rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 28rpx 80rpx rgba(23, 58, 111, 0.14);
}

.auth-panel__head {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.auth-panel__title {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: var(--text-strong);
}

.auth-role-switcher {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 24rpx;
}

.auth-role {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-height: 100rpx;
  padding: 0 22rpx;
  border-radius: 28rpx;
  background: linear-gradient(180deg, rgba(245, 249, 255, 0.94), rgba(240, 246, 255, 0.72));
  border: 2rpx solid rgba(220, 232, 247, 0.92);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.auth-role--active {
  background: linear-gradient(135deg, rgba(47, 124, 255, 0.16), rgba(88, 181, 255, 0.14));
  border-color: rgba(47, 124, 255, 0.28);
  box-shadow: 0 18rpx 34rpx rgba(47, 124, 255, 0.12);
}

.auth-role--press {
  transform: scale(0.985);
}

.auth-role__flag {
  width: 22rpx;
  height: 22rpx;
  border-radius: 50%;
  background: rgba(143, 169, 200, 0.52);
  transition: transform 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.auth-role--active .auth-role__flag {
  background: var(--brand-blue);
  box-shadow: 0 0 0 10rpx rgba(47, 124, 255, 0.12);
  transform: scale(1.08);
}

.auth-role__label {
  font-size: 28rpx;
  font-weight: 800;
  color: var(--text-strong);
}

.auth-fields {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 24rpx;
}

.auth-field-panel {
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, rgba(247, 250, 255, 0.98), rgba(243, 248, 255, 0.86));
  border: 2rpx solid rgba(225, 235, 248, 0.92);
}

.auth-field-input {
  font-size: 30rpx;
  font-weight: 600;
}

.auth-panel__action {
  margin-top: 26rpx;
  min-height: 96rpx;
  border-radius: 30rpx;
}

.auth-panel__action--loading {
  opacity: 0.9;
}

.auth-feedback {
  margin-top: 18rpx;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  line-height: 1.65;
}

.auth-feedback--error {
  background: rgba(255, 100, 118, 0.08);
  color: #c14859;
  border: 2rpx solid rgba(255, 100, 118, 0.14);
}
</style>
