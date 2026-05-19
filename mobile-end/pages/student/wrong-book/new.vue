<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'

const mobilePortalStore = useMobilePortalStore()
const assetInput = ref('')
const submitting = ref(false)

const reasonOptions = [
  { value: 'calc_error', label: '计算错误' },
  { value: 'concept_error', label: '概念不清' },
  { value: 'reading_error', label: '审题错误' },
  { value: 'writing_error', label: '书写问题' },
  { value: 'careless_error', label: '粗心出错' },
  { value: 'other', label: '其他' }
]

const form = reactive({
  homeworkId: '',
  subjectCode: '',
  subjectName: '',
  questionNo: '',
  questionText: '',
  studentAnswer: '',
  correctAnswer: '',
  analysisText: '',
  wrongReasonCode: 'calc_error',
  assets: [] as Array<{
    assetRole: 'question_image'
    assetType: 'image'
    assetUrl: string
    assetName?: string
  }>
})

function safeDecode(value?: string) {
  if (!value) {
    return ''
  }

  try {
    return decodeURIComponent(value)
  } catch {
    return value
  }
}

onLoad((query) => {
  if (!ensureMobileRole('student')) {
    return
  }

  form.homeworkId = query?.homeworkId ?? ''
  form.subjectName = safeDecode(query?.subjectName)
  form.questionText = safeDecode(query?.questionText)

  if (form.homeworkId) {
    const homework = mobilePortalStore.getStudentHomework(form.homeworkId)
    if (homework) {
      form.subjectName = form.subjectName || homework.subject
      form.questionText = form.questionText || homework.title
    }
  }
})

function goBack() {
  uni.navigateBack()
}

function addAsset() {
  if (!assetInput.value.trim()) {
    return
  }

  form.assets.push({
    assetRole: 'question_image',
    assetType: 'image',
    assetUrl: assetInput.value.trim(),
    assetName: `题目图片 ${form.assets.length + 1}`
  })
  assetInput.value = ''
}

function removeAsset(index: number) {
  form.assets.splice(index, 1)
}

async function submitForm() {
  if (!form.questionText.trim()) {
    uni.showToast({ title: '请填写题目内容', icon: 'none' })
    return
  }

  submitting.value = true

  try {
    const result = await mobilePortalStore.createStudentWrongBook({
      homeworkId: form.homeworkId || undefined,
      subjectCode: form.subjectCode || undefined,
      subjectName: form.subjectName || undefined,
      questionNo: form.questionNo || undefined,
      questionText: form.questionText.trim(),
      studentAnswer: form.studentAnswer.trim() || undefined,
      correctAnswer: form.correctAnswer.trim() || undefined,
      analysisText: form.analysisText.trim() || undefined,
      wrongReasonCode: form.wrongReasonCode,
      assets: form.assets
    })

    const wrongBookId = typeof result === 'object' && result && 'id' in result
      ? `${result.id}`
      : typeof result === 'object' && result && 'wrongBookId' in result && result.wrongBookId
        ? `${result.wrongBookId}`
        : ''

    uni.showToast({ title: '已加入错题本', icon: 'success' })

    if (wrongBookId) {
      uni.redirectTo({
        url: `/pages/student/wrong-book/detail?id=${wrongBookId}`
      })
      return
    }

    uni.redirectTo({
      url: '/pages/student/wrong-book/index'
    })
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '添加失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view class="mobile-page">
    <view class="detail-header">
      <view class="detail-header__back" @tap="goBack">返回</view>
      <text class="detail-header__title">新增错题</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view class="shell-card create-card">
      <text class="glass-chip">主动整理</text>
      <text class="create-card__title">把这道题加入错题本</text>

      <view class="field-panel create-field">
        <text class="field-label">科目</text>
        <input
          v-model="form.subjectName"
          class="field-input"
          placeholder="例如 数学 / 语文"
          placeholder-class="field-placeholder"
        />
      </view>

      <view class="create-grid">
        <view class="field-panel create-field">
          <text class="field-label">题号</text>
          <input
            v-model="form.questionNo"
            class="field-input"
            placeholder="例如 1 / 2-1"
            placeholder-class="field-placeholder"
          />
        </view>

        <view class="field-panel create-field">
          <text class="field-label">错因标签</text>
          <picker :range="reasonOptions" range-key="label" :value="reasonOptions.findIndex((item) => item.value === form.wrongReasonCode)" @change="form.wrongReasonCode = reasonOptions[$event.detail.value]?.value || 'calc_error'">
            <view class="picker-value">
              {{ reasonOptions.find((item) => item.value === form.wrongReasonCode)?.label || '请选择' }}
            </view>
          </picker>
        </view>
      </view>

      <view class="field-panel create-field">
        <text class="field-label">题目内容</text>
        <textarea
          v-model="form.questionText"
          class="textarea-panel"
          placeholder="输入题干、题目截图说明或错题描述"
          placeholder-class="field-placeholder"
        />
      </view>

      <view class="field-panel create-field">
        <text class="field-label">我的错误答案</text>
        <textarea
          v-model="form.studentAnswer"
          class="textarea-panel"
          placeholder="写下自己原来的错误答案"
          placeholder-class="field-placeholder"
        />
      </view>

      <view class="field-panel create-field">
        <text class="field-label">正确答案</text>
        <textarea
          v-model="form.correctAnswer"
          class="textarea-panel"
          placeholder="补充正确答案，方便后面回顾"
          placeholder-class="field-placeholder"
        />
      </view>

      <view class="field-panel create-field">
        <text class="field-label">错因分析</text>
        <textarea
          v-model="form.analysisText"
          class="textarea-panel"
          placeholder="记录为什么做错、下次怎么避免"
          placeholder-class="field-placeholder"
        />
      </view>

      <view class="create-grid">
        <view class="field-panel create-field">
          <text class="field-label">题目图片链接</text>
          <input
            v-model="assetInput"
            class="field-input"
            placeholder="输入图片链接后添加"
            placeholder-class="field-placeholder"
          />
        </view>
        <view class="secondary-button" @tap="addAsset">添加图片</view>
      </view>

      <view v-if="form.assets.length" class="asset-draft-list">
        <view
          v-for="(asset, index) in form.assets"
          :key="asset.assetUrl"
          class="asset-draft-item"
          @tap="removeAsset(index)"
        >
          <text class="asset-draft-item__title">{{ asset.assetName }}</text>
          <text class="asset-draft-item__copy">点击移除</text>
        </view>
      </view>

      <view class="create-actions">
        <view class="secondary-button" @tap="goBack">取消</view>
        <view class="primary-button" @tap="submitForm">{{ submitting ? '提交中...' : '加入错题本' }}</view>
      </view>
    </view>
  </view>
</template>

<style>
.create-card {
  margin-top: 28rpx;
  padding: 30rpx;
}

.create-card__title {
  display: block;
  margin-top: 22rpx;
  font-size: 42rpx;
  font-weight: 800;
  color: var(--text-strong);
}

.create-field {
  margin-top: 18rpx;
}

.create-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180rpx;
  gap: 16rpx;
  margin-top: 18rpx;
  align-items: end;
}

.picker-value {
  min-height: 52rpx;
  display: flex;
  align-items: center;
  font-size: 28rpx;
  color: var(--text-strong);
}

.asset-draft-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 18rpx;
}

.asset-draft-item {
  padding: 22rpx;
  border-radius: 24rpx;
  background: rgba(245, 249, 255, 0.92);
}

.asset-draft-item__title {
  display: block;
  font-size: 24rpx;
  font-weight: 700;
  color: var(--text-strong);
}

.asset-draft-item__copy {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: var(--text-secondary);
}

.create-actions {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 16rpx;
  margin-top: 24rpx;
}
</style>
