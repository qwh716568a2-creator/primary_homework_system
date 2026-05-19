<script setup lang="ts">
import { computed, reactive } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store/auth'
import { useMobilePreferencesStore } from '@/store/mobile-preferences'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileLogin } from '@/utils/page-guard'

const authStore = useAuthStore()
const preferencesStore = useMobilePreferencesStore()
const mobilePortalStore = useMobilePortalStore()

const draft = reactive({
  masterEnabled: true,
  assignmentEnabled: true,
  reviewEnabled: true,
  reminderEnabled: true,
  systemEnabled: true,
  soundEnabled: true,
  vibrationEnabled: true,
  quietHoursEnabled: false,
  quietStart: '22:00',
  quietEnd: '07:00'
})

function syncDraft() {
  const current = preferencesStore.notificationSettings
  draft.masterEnabled = current.masterEnabled
  draft.assignmentEnabled = current.assignmentEnabled
  draft.reviewEnabled = current.reviewEnabled
  draft.reminderEnabled = current.reminderEnabled
  draft.systemEnabled = current.systemEnabled
  draft.soundEnabled = current.soundEnabled
  draft.vibrationEnabled = current.vibrationEnabled
  draft.quietHoursEnabled = current.quietHoursEnabled
  draft.quietStart = current.quietStart
  draft.quietEnd = current.quietEnd
}

onShow(async () => {
  if (!ensureMobileLogin()) {
    return
  }

  authStore.bootstrap()
  await preferencesStore.bootstrap()
  mobilePortalStore.bootstrap()
  syncDraft()
})

const isParent = computed(() => authStore.role === 'parent')
const roleName = computed(() => (isParent.value ? '家长端' : '学生端'))
const notificationSummary = computed(() => {
  const enabledCount = [
    draft.assignmentEnabled,
    draft.reviewEnabled,
    draft.reminderEnabled,
    draft.systemEnabled
  ].filter(Boolean).length

  return draft.masterEnabled ? `已开启 ${enabledCount} 类消息提醒` : '当前已暂停全部消息提醒'
})

const categoryItems = computed(() => [
  {
    key: 'assignmentEnabled' as const,
    title: isParent.value ? '孩子作业动态' : '作业进度提醒',
    copy: isParent.value ? '接收孩子提交、逾期和协助作业动态' : '接收作业发布、待完成和截止提醒'
  },
  {
    key: 'reviewEnabled' as const,
    title: isParent.value ? '批改与反馈' : '老师批改反馈',
    copy: isParent.value ? '接收老师批改结果和评语更新' : '接收评分、评语和订正提醒'
  },
  {
    key: 'reminderEnabled' as const,
    title: '催办提醒',
    copy: isParent.value ? '接收教师催交、订正和再次提醒' : '接收待提交、待订正和错题复习提醒'
  },
  {
    key: 'systemEnabled' as const,
    title: '系统公告',
    copy: '接收账号更新、平台通知和系统公告'
  }
])

function goBack() {
  uni.navigateBack()
}

function updateSwitch(
  key:
    | 'masterEnabled'
    | 'assignmentEnabled'
    | 'reviewEnabled'
    | 'reminderEnabled'
    | 'systemEnabled'
    | 'soundEnabled'
    | 'vibrationEnabled'
    | 'quietHoursEnabled',
  value: boolean
) {
  draft[key] = value
}

async function saveSettings() {
  try {
    await preferencesStore.saveNotificationSettings({
      ...draft
    })
    uni.showToast({ title: '消息设置已保存', icon: 'success' })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '消息设置保存失败',
      icon: 'none'
    })
  }
}

async function resetDefaults() {
  try {
    await preferencesStore.resetNotificationSettings()
    syncDraft()
    uni.showToast({ title: '已恢复默认设置', icon: 'success' })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '恢复默认设置失败',
      icon: 'none'
    })
  }
}

async function markAllRead() {
  if (!authStore.role) {
    return
  }

  try {
    await preferencesStore.markAllMessagesRead()
    mobilePortalStore.markMessagesRead(authStore.role)
    uni.showToast({ title: '消息已全部标记为已读', icon: 'success' })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '全部标记已读失败',
      icon: 'none'
    })
  }
}

function updateQuietTime(key: 'quietStart' | 'quietEnd', value: string) {
  draft[key] = value
}
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">消息设置</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view class="shell-card settings-hero">
      <text class="glass-chip">{{ roleName }}</text>
      <text class="settings-hero__title">按你的节奏接收提醒</text>
      <text class="settings-hero__copy">{{ notificationSummary }}</text>
    </view>

    <view class="shell-card settings-block">
      <view class="settings-row settings-row--headline">
        <view>
          <text class="settings-row__title">总开关</text>
          <text class="settings-row__copy">统一控制消息接收状态</text>
        </view>
        <switch
          :checked="draft.masterEnabled"
          color="#2f7cff"
          @change="updateSwitch('masterEnabled', $event.detail.value)"
        />
      </view>
    </view>

    <view class="shell-card settings-block">
      <text class="section-title">提醒范围</text>
      <view class="settings-list">
        <view v-for="item in categoryItems" :key="item.key" class="settings-row">
          <view>
            <text class="settings-row__title">{{ item.title }}</text>
            <text class="settings-row__copy">{{ item.copy }}</text>
          </view>
          <switch
            :disabled="!draft.masterEnabled"
            :checked="draft[item.key]"
            color="#2f7cff"
            @change="updateSwitch(item.key, $event.detail.value)"
          />
        </view>
      </view>
    </view>

    <view class="shell-card settings-block">
      <text class="section-title">提醒方式</text>
      <view class="settings-list">
        <view class="settings-row">
          <view>
            <text class="settings-row__title">声音提醒</text>
            <text class="settings-row__copy">收到新消息时播放提示音</text>
          </view>
          <switch
            :disabled="!draft.masterEnabled"
            :checked="draft.soundEnabled"
            color="#2f7cff"
            @change="updateSwitch('soundEnabled', $event.detail.value)"
          />
        </view>
        <view class="settings-row">
          <view>
            <text class="settings-row__title">震动提醒</text>
            <text class="settings-row__copy">收到新消息时进行轻震提醒</text>
          </view>
          <switch
            :disabled="!draft.masterEnabled"
            :checked="draft.vibrationEnabled"
            color="#2f7cff"
            @change="updateSwitch('vibrationEnabled', $event.detail.value)"
          />
        </view>
      </view>
    </view>

    <view class="shell-card settings-block">
      <text class="section-title">免打扰</text>
      <view class="settings-list">
        <view class="settings-row">
          <view>
            <text class="settings-row__title">开启免打扰</text>
            <text class="settings-row__copy">在设定时间内静默提醒</text>
          </view>
          <switch
            :checked="draft.quietHoursEnabled"
            color="#2f7cff"
            @change="updateSwitch('quietHoursEnabled', $event.detail.value)"
          />
        </view>
        <view v-if="draft.quietHoursEnabled" class="settings-time-grid">
          <picker mode="time" :value="draft.quietStart" @change="updateQuietTime('quietStart', $event.detail.value)">
            <view class="field-panel settings-time">
              <text class="field-label">开始时间</text>
              <text class="settings-time__value">{{ draft.quietStart }}</text>
            </view>
          </picker>
          <picker mode="time" :value="draft.quietEnd" @change="updateQuietTime('quietEnd', $event.detail.value)">
            <view class="field-panel settings-time">
              <text class="field-label">结束时间</text>
              <text class="settings-time__value">{{ draft.quietEnd }}</text>
            </view>
          </picker>
        </view>
      </view>
    </view>

    <view class="shell-card settings-block">
      <text class="section-title">快捷操作</text>
      <view class="settings-actions">
        <view class="secondary-button" @tap="markAllRead">全部标记已读</view>
        <view class="secondary-button" @tap="resetDefaults">恢复默认</view>
        <view class="primary-button" @tap="saveSettings">保存消息设置</view>
      </view>
    </view>
  </view>
</template>

<style>
.settings-hero,
.settings-block {
  margin-top: 18rpx;
  padding: 30rpx;
}

.settings-hero__title {
  display: block;
  margin-top: 20rpx;
  font-size: 42rpx;
  line-height: 1.18;
  font-weight: 800;
  color: var(--text-strong);
}

.settings-hero__copy {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: var(--text-secondary);
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-top: 20rpx;
}

.settings-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.settings-row--headline {
  min-height: 92rpx;
}

.settings-row__title,
.settings-time__value {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.settings-row__copy {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: var(--text-secondary);
}

.settings-time-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.settings-time {
  min-height: 126rpx;
}

.settings-time__value {
  margin-top: 10rpx;
}

.settings-actions {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16rpx;
  margin-top: 20rpx;
}
</style>
