<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'

const mobilePortalStore = useMobilePortalStore()
const wrongBookId = ref('')
const loading = ref(false)
const errorText = ref('')
const correctionAssetInput = ref('')

const fixForm = reactive({
  fixedText: '',
  assets: [] as Array<{
    assetRole: 'correction_image'
    assetType: 'image'
    assetUrl: string
    assetName?: string
  }>
})

const wrongBook = computed(() => mobilePortalStore.getWrongBook(wrongBookId.value))

function resolveStatusLabel(status?: string) {
  if (status === 'pending_fix') return '待订正'
  if (status === 'fixed') return '已订正'
  if (status === 'mastered') return '已掌握'
  return '未同步'
}

function resolveStatusClass(status?: string) {
  if (status === 'pending_fix') return 'status-revision'
  if (status === 'fixed') return 'status-submitted'
  if (status === 'mastered') return 'status-completed'
  return 'status-overdue'
}

async function loadDetail() {
  if (!ensureMobileRole('student')) return

  loading.value = true
  errorText.value = ''

  try {
    await mobilePortalStore.loadStudentWrongBookDetail(wrongBookId.value)
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '错题详情加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

function addCorrectionAsset() {
  if (!correctionAssetInput.value.trim()) return

  fixForm.assets.push({
    assetRole: 'correction_image',
    assetType: 'image',
    assetUrl: correctionAssetInput.value.trim(),
    assetName: `订正图片 ${fixForm.assets.length + 1}`
  })
  correctionAssetInput.value = ''
}

function removeCorrectionAsset(index: number) {
  fixForm.assets.splice(index, 1)
}

async function submitFix() {
  if (!fixForm.fixedText.trim()) {
    uni.showToast({ title: '请先填写订正说明', icon: 'none' })
    return
  }

  try {
    await mobilePortalStore.submitWrongBookFix(wrongBookId.value, {
      fixedText: fixForm.fixedText.trim(),
      assets: fixForm.assets
    })
    fixForm.fixedText = ''
    fixForm.assets = []
    correctionAssetInput.value = ''
    await loadDetail()
    uni.showToast({ title: '订正已提交', icon: 'success' })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '订正提交失败',
      icon: 'none'
    })
  }
}

async function markMastered() {
  try {
    await mobilePortalStore.markWrongBookMastered(wrongBookId.value)
    await loadDetail()
    uni.showToast({ title: '已标记掌握', icon: 'success' })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '状态更新失败',
      icon: 'none'
    })
  }
}

onLoad((query) => {
  wrongBookId.value = query?.id ?? ''
})

onShow(() => {
  void loadDetail()
})
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">错题详情</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view v-if="loading" class="shell-card empty-state detail-state">
      <text class="empty-state__title">正在同步错题详情</text>
      <text class="empty-state__copy">题目、答案和订正记录马上出现。</text>
    </view>

    <view v-else-if="errorText" class="shell-card empty-state detail-state">
      <text class="empty-state__title">错题详情加载失败</text>
      <text class="empty-state__copy">{{ errorText }}</text>
      <view class="secondary-button detail-retry" @tap="loadDetail">重新加载</view>
    </view>

    <view v-else-if="wrongBook" class="detail-stack">
      <view class="shell-card wrongbook-hero">
        <view class="wrongbook-hero__top">
          <text class="glass-chip">{{ wrongBook.subjectName }}</text>
          <text :class="['status-chip', resolveStatusClass(wrongBook.status)]">
            {{ resolveStatusLabel(wrongBook.status) }}
          </text>
        </view>
        <text class="wrongbook-hero__title">
          {{ wrongBook.questionNo ? `第 ${wrongBook.questionNo} 题 · ` : '' }}{{ wrongBook.questionText }}
        </text>
        <text class="wrongbook-hero__meta">
          {{ wrongBook.teacherName || '我的错题本' }} · {{ wrongBook.createdAt }}
        </text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">错误答案</text>
        <text class="muted-copy detail-block__copy">{{ wrongBook.studentAnswer || '未记录' }}</text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">正确答案</text>
        <text class="muted-copy detail-block__copy">{{ wrongBook.correctAnswer || '待补充' }}</text>
      </view>

      <view class="shell-card detail-block">
        <text class="section-title">解析与提醒</text>
        <text class="muted-copy detail-block__copy">{{ wrongBook.analysisText || '暂未填写解析。' }}</text>
      </view>

      <view v-if="wrongBook.assets.length" class="shell-card detail-block">
        <text class="section-title">题目与订正图片</text>
        <view class="detail-assets">
          <view
            v-for="asset in wrongBook.assets"
            :key="`${asset.assetRole}-${asset.assetUrl}`"
            class="detail-asset"
          >
            <text class="detail-asset__name">{{ asset.assetName || asset.assetRole }}</text>
            <text class="detail-asset__meta">{{ asset.assetRole }}</text>
          </view>
        </view>
      </view>

      <view v-if="wrongBook.lastFixedText || wrongBook.lastFixedAt" class="shell-card detail-block">
        <text class="section-title">最近一次订正</text>
        <text class="muted-copy detail-block__copy">{{ wrongBook.lastFixedText || '已更新状态' }}</text>
        <text class="detail-fixed-time">{{ wrongBook.lastFixedAt || '刚刚' }}</text>
      </view>

      <view v-if="wrongBook.status !== 'mastered'" class="shell-card detail-block">
        <text class="section-title">提交订正</text>
        <view class="field-panel">
          <text class="field-label">订正说明</text>
          <textarea
            v-model="fixForm.fixedText"
            class="textarea-panel"
            placeholder="写下这次重新理解后的答案或方法"
            placeholder-class="field-placeholder"
          />
        </view>

        <view class="detail-asset-editor">
          <view class="field-panel detail-asset-editor__input">
            <text class="field-label">订正图片链接</text>
            <input
              v-model="correctionAssetInput"
              class="field-input"
              placeholder="输入图片链接后添加"
              placeholder-class="field-placeholder"
            />
          </view>
          <view class="secondary-button" @tap="addCorrectionAsset">添加图片</view>
        </view>

        <view v-if="fixForm.assets.length" class="detail-assets detail-assets--draft">
          <view
            v-for="(asset, index) in fixForm.assets"
            :key="asset.assetUrl"
            class="detail-asset"
            @tap="removeCorrectionAsset(index)"
          >
            <text class="detail-asset__name">{{ asset.assetName }}</text>
            <text class="detail-asset__meta">点击移除</text>
          </view>
        </view>

        <view class="detail-actions">
          <view class="secondary-button" @tap="markMastered">已完全掌握</view>
          <view class="primary-button" @tap="submitFix">提交订正</view>
        </view>
      </view>
    </view>
  </view>
</template>

<style>
.detail-stack {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 28rpx;
}

.detail-state,
.detail-block,
.wrongbook-hero {
  padding: 28rpx;
}

.wrongbook-hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.wrongbook-hero__title {
  display: block;
  margin-top: 22rpx;
  font-size: 40rpx;
  line-height: 1.2;
  font-weight: 800;
  color: var(--text-strong);
}

.wrongbook-hero__meta,
.detail-fixed-time {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.detail-block__copy {
  display: block;
  margin-top: 18rpx;
}

.detail-assets {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 18rpx;
}

.detail-assets--draft {
  margin-top: 18rpx;
}

.detail-asset {
  padding: 22rpx;
  border-radius: 24rpx;
  background: rgba(245, 249, 255, 0.92);
}

.detail-asset__name {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.detail-asset__meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.detail-asset-editor {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180rpx;
  gap: 16rpx;
  margin-top: 18rpx;
  align-items: stretch;
}

.detail-asset-editor__input {
  min-width: 0;
}

.detail-actions {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 16rpx;
  margin-top: 24rpx;
}

.detail-retry {
  margin-top: 18rpx;
}
</style>
