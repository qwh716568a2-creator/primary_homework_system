<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { uploadMobileFile } from '@/services/files'
import { useMobilePortalStore } from '@/store/mobile-portal'
import { ensureMobileRole } from '@/utils/page-guard'

const mobilePortalStore = useMobilePortalStore()
const homeworkIdState = reactive({
  value: ''
})
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  text: '',
  images: [] as string[]
})

onLoad((query) => {
  homeworkIdState.value = query?.homeworkId ?? ''
  const draft = uni.getStorageSync(`student-submit-draft-${homeworkIdState.value}`) as
    | { text: string; images: string[] }
    | undefined

  if (draft) {
    form.text = draft.text
    form.images = draft.images
  }
})

onShow(() => {
  void loadHomework()
})

const homework = computed(() => mobilePortalStore.getStudentHomework(homeworkIdState.value))

async function loadHomework() {
  if (!ensureMobileRole('student')) {
    return
  }

  loading.value = true
  try {
    await mobilePortalStore.loadStudentHomeworkDetail(homeworkIdState.value)
  } finally {
    loading.value = false
  }
}

function goBack() {
  uni.navigateBack()
}

function pickImages() {
  uni.chooseImage({
    count: Math.max(0, 6 - form.images.length),
    success(result) {
      form.images.push(...result.tempFilePaths)
    }
  })
}

function removeImage(target: string) {
  form.images = form.images.filter((item) => item !== target)
}

function saveDraft() {
  uni.setStorageSync(`student-submit-draft-${homeworkIdState.value}`, {
    text: form.text,
    images: form.images
  })
  uni.showToast({ title: '草稿已保存', icon: 'none' })
}

async function uploadImages(images: string[]) {
  const uploaded: string[] = []

  for (let index = 0; index < images.length; index += 1) {
    const current = images[index]

    if (/^https?:\/\//.test(current)) {
      uploaded.push(current)
      continue
    }

    uni.showLoading({ title: `上传图片 ${index + 1}/${images.length}` })
    const result = await uploadMobileFile(current, 'submission_image')
    uploaded.push(result.fileUrl)
  }

  return uploaded
}

async function submitHomework() {
  if (submitting.value) {
    return
  }

  if (!form.text.trim() && form.images.length === 0) {
    uni.showToast({ title: '请先填写内容或上传图片', icon: 'none' })
    return
  }

  submitting.value = true

  try {
    uni.showLoading({ title: '准备提交' })
    const uploadedImages = form.images.length ? await uploadImages(form.images) : []
    uni.showLoading({ title: '正在提交' })

    await mobilePortalStore.submitHomework({
      homeworkId: homeworkIdState.value,
      text: form.text,
      images: uploadedImages
    })

    uni.hideLoading()
    uni.removeStorageSync(`student-submit-draft-${homeworkIdState.value}`)
    uni.showToast({ title: '提交成功', icon: 'none' })

    uni.redirectTo({
      url: `/pages/student/feedback/index?homeworkId=${homeworkIdState.value}`
    })
  } catch (error) {
    uni.hideLoading()
    uni.showToast({
      title: error instanceof Error ? error.message : '提交失败，请稍后重试',
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
      <text class="detail-header__title">提交作业</text>
      <view class="detail-header__back detail-header__back--ghost">返回</view>
    </view>

    <view v-if="loading" class="shell-card empty-state detail-empty">
      <text class="empty-state__title">正在加载作业信息</text>
      <text class="empty-state__copy">加载完成后就可以直接提交。</text>
    </view>

    <view v-else-if="homework" class="submit-stack">
      <view class="shell-card submit-hero">
        <text class="submit-hero__title">{{ homework.title }}</text>
        <text class="submit-hero__copy">{{ homework.summary }}</text>
      </view>

      <view class="shell-card submit-block">
        <text class="section-title">文字说明</text>
        <textarea
          v-model="form.text"
          class="textarea-panel"
          auto-height
          placeholder="可以补充解题思路、朗读说明或观察记录。"
          placeholder-class="field-placeholder"
        />
      </view>

      <view class="shell-card submit-block">
        <view class="submit-block__row">
          <text class="section-title">图片上传</text>
          <text class="submit-block__link" @tap="pickImages">添加图片</text>
        </view>

        <view class="submit-images">
          <view v-for="(image, index) in form.images" :key="`${image}-${index}`" class="submit-image">
            <image :src="image" mode="aspectFill" class="submit-image__content" />
            <text class="submit-image__index">{{ index + 1 }}</text>
            <text class="submit-image__remove" @tap.stop="removeImage(image)">×</text>
          </view>
          <view v-if="form.images.length === 0" class="submit-image submit-image--empty">
            <text>上传后会在这里预览</text>
          </view>
        </view>
      </view>

      <view class="submit-actions">
        <view class="secondary-button" @tap="saveDraft">保存草稿</view>
        <view class="primary-button" @tap="submitHomework">{{ submitting ? '提交中...' : '提交' }}</view>
      </view>
    </view>
  </view>
</template>

<style>
.submit-stack {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-top: 28rpx;
}

.submit-hero,
.submit-block {
  padding: 28rpx;
}

.submit-hero__title {
  display: block;
  font-size: 42rpx;
  line-height: 1.15;
  font-weight: 800;
  color: var(--text-strong);
}

.submit-hero__copy {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: var(--text-secondary);
}

.submit-block__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 18rpx;
}

.submit-block__link {
  font-size: 24rpx;
  color: var(--brand-blue);
  font-weight: 700;
}

.submit-images {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.submit-image {
  position: relative;
  height: 220rpx;
  border-radius: 28rpx;
  overflow: hidden;
  background: rgba(244, 249, 255, 0.92);
}

.submit-image--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  font-size: 22rpx;
}

.submit-image__content {
  width: 100%;
  height: 100%;
}

.submit-image__index,
.submit-image__remove {
  position: absolute;
  min-width: 38rpx;
  height: 38rpx;
  border-radius: 50%;
  color: #ffffff;
  font-size: 20rpx;
  text-align: center;
  line-height: 38rpx;
}

.submit-image__index {
  top: 16rpx;
  right: 16rpx;
  background: rgba(16, 35, 63, 0.74);
}

.submit-image__remove {
  left: 16rpx;
  bottom: 16rpx;
  background: rgba(255, 107, 107, 0.9);
}

.submit-actions {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 16rpx;
}
</style>
