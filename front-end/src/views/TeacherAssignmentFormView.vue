<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Delete, Paperclip, UploadFilled } from '@element-plus/icons-vue'
import { uploadTeacherFile } from '@/api/teacher'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatAssetType } from '@/utils/format-labels'
import type { HomeworkAsset, SubmissionMethod } from '@/types/teacher-portal'

const router = useRouter()
const route = useRoute()
const store = useTeacherPortalStore()

const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadingAttachments = ref(false)

const isEdit = ref(false)
const editHomeworkId = ref<string | number | undefined>(undefined)

const formData = ref({
  title: '',
  subjectCode: '',
  classIds: [] as Array<string | number>,
  contentText: '',
  deadlineAt: '',
  allowLateSubmit: false,
  needParentConfirm: false,
  submitTypes: ['text', 'image'] as SubmissionMethod[],
  allowResubmit: true,
  attachments: [] as HomeworkAsset[]
})

function isSameDay(a: Date, b: Date) {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
}

function parseDeadlineDate(value: string) {
  if (!value) return null
  const date = new Date(value.replace(/-/g, '/'))
  return Number.isNaN(date.getTime()) ? null : date
}

function disabledPastDate(date: Date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

function shouldLimitTodayTime() {
  const selected = parseDeadlineDate(formData.value.deadlineAt)
  return !selected || isSameDay(selected, new Date())
}

function disabledPastHours() {
  if (!shouldLimitTodayTime()) return []
  const currentHour = new Date().getHours()
  return Array.from({ length: currentHour }, (_, index) => index)
}

function disabledPastMinutes(hour: number) {
  if (!shouldLimitTodayTime()) return []
  const now = new Date()
  return hour === now.getHours() ? Array.from({ length: now.getMinutes() }, (_, index) => index) : []
}

function disabledPastSeconds(hour: number, minute: number) {
  if (!shouldLimitTodayTime()) return []
  const now = new Date()
  return hour === now.getHours() && minute === now.getMinutes()
    ? Array.from({ length: now.getSeconds() + 1 }, (_, index) => index)
    : []
}

function detectAssetType(file: File): HomeworkAsset['assetType'] {
  if (file.type.startsWith('image/')) return 'image'
  if (file.type.startsWith('audio/')) return 'audio'
  if (file.type.startsWith('video/')) return 'video'
  return 'file'
}

function triggerAttachmentSelect() {
  fileInputRef.value?.click()
}

function removeAttachment(index: number) {
  formData.value.attachments.splice(index, 1)
}

async function handleAttachmentChange(event: Event) {
  const target = event.target as HTMLInputElement
  const files = Array.from(target.files ?? [])

  if (!files.length) {
    return
  }

  uploadingAttachments.value = true
  try {
    for (const file of files) {
      const uploaded = await uploadTeacherFile(file, 'teacher-homework-attachment')
      formData.value.attachments.push({
        assetType: detectAssetType(file),
        assetUrl: uploaded.fileUrl,
        assetName: uploaded.fileName || file.name,
        assetSize: uploaded.fileSize || file.size
      })
    }
    ElMessage.success('附件上传成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '附件上传失败，请稍后重试。')
  } finally {
    uploadingAttachments.value = false
    target.value = ''
  }
}

onMounted(async () => {
  if (!store.classOptions.length) {
    await store.loadTeachingClasses()
  }

  if (route.query.edit) {
    isEdit.value = true
    editHomeworkId.value = route.query.edit as string

    try {
      const detail = await store.loadHomeworkDetail(editHomeworkId.value)
      if (detail) {
        formData.value.title = detail.baseInfo.title
        formData.value.subjectCode = detail.baseInfo.subjectCode
        formData.value.classIds = detail.classList.map((item) => item.classId)
        formData.value.contentText = detail.baseInfo.contentText || ''
        formData.value.deadlineAt = detail.baseInfo.deadlineAt || ''
        formData.value.allowLateSubmit = detail.baseInfo.allowLateSubmit || false
        formData.value.needParentConfirm = detail.baseInfo.needParentConfirm || false
        formData.value.submitTypes = detail.baseInfo.submitTypes || ['text', 'image']
        formData.value.allowResubmit = detail.baseInfo.allowResubmit !== false
        formData.value.attachments = detail.attachments || []
      }
    } catch {
      ElMessage.error('无法加载作业数据进行编辑')
      router.back()
    }
  }
})

async function submit(publishNow: boolean) {
  if (!formData.value.title || !formData.value.subjectCode || !formData.value.classIds.length) {
    ElMessage.warning('请填写完整的必要信息')
    return
  }

  const deadline = parseDeadlineDate(formData.value.deadlineAt)
  if (deadline && deadline.getTime() <= Date.now()) {
    ElMessage.warning('截止时间不能早于当前时间')
    return
  }

  try {
    const action = publishNow ? 'published' : 'draft'
    await store.saveAssignment(
      {
        title: formData.value.title,
        subjectCode: formData.value.subjectCode,
        classIds: formData.value.classIds,
        contentText: formData.value.contentText,
        deadlineAt: formData.value.deadlineAt,
        allowLateSubmit: formData.value.allowLateSubmit,
        needParentConfirm: formData.value.needParentConfirm,
        submitTypes: formData.value.submitTypes,
        allowResubmit: formData.value.allowResubmit,
        attachments: formData.value.attachments
      },
      action,
      editHomeworkId.value
    )

    ElMessage.success(publishNow ? '作业已成功发布' : '草稿已保存')
    router.push('/assignments')
  } catch (error) {
    ElMessage.error((error as Error).message || '保存失败')
  }
}
</script>

<template>
  <div class="page-stack">
    <header class="page-header header-glow mb-4">
      <div>
        <h2 class="hero-title">{{ isEdit ? '编辑作业' : '发布新作业' }}</h2>
        <p class="subtitle mt-2">填写以下信息为班级派发任务，支持多班级发布与附件上传。</p>
      </div>
      <el-button @click="router.back()">返回上一步</el-button>
    </header>

    <div class="surface-card section-card">
      <el-form class="custom-form mt-2" label-position="top" @submit.prevent>
        <el-row :gutter="32">
          <el-col :span="12">
            <el-form-item label="作业标题" required>
              <el-input v-model="formData.title" size="large" placeholder="如：第三单元词语抄写与背诵" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属学科" required>
              <el-select v-model="formData.subjectCode" class="w-full" size="large" placeholder="请选择学科">
                <el-option
                  v-for="subject in store.subjectOptions"
                  :key="subject.subjectCode"
                  :label="subject.subjectName"
                  :value="subject.subjectCode"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="下发至教学班级（支持多选组合发布）" required>
              <el-select
                v-model="formData.classIds"
                class="w-full deadline-picker"
                size="large"
                multiple
                placeholder="请在此处选择一个或多个班级"
              >
                <el-option
                  v-for="item in store.classOptions"
                  :key="item.classId"
                  :label="item.className"
                  :value="item.classId"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="作业要求详述" required>
              <el-input
                v-model="formData.contentText"
                type="textarea"
                :rows="6"
                resize="none"
                placeholder="详细输入您的作业说明，也支持复制文本。"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider class="my-4" border-style="dashed" />

        <el-row :gutter="32">
          <el-col :span="12">
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="formData.deadlineAt"
                class="w-full"
                size="large"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY年MM月DD日 HH:mm"
                :disabled-date="disabledPastDate"
                :disabled-hours="disabledPastHours"
                :disabled-minutes="disabledPastMinutes"
                :disabled-seconds="disabledPastSeconds"
                popper-class="homework-deadline-popper"
                placeholder="设定最晚提交时间"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="其他策略配置">
              <div class="checkbox-group glass-panel">
                <el-checkbox v-model="formData.allowLateSubmit">允许逾期补交</el-checkbox>
                <el-checkbox v-model="formData.needParentConfirm">需要家长协助确认</el-checkbox>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="作业附件">
          <div class="attachment-panel">
            <div class="attachment-toolbar">
              <el-button
                type="primary"
                plain
                :icon="UploadFilled"
                :loading="uploadingAttachments"
                @click="triggerAttachmentSelect"
              >
                上传附件
              </el-button>
              <span class="attachment-hint">支持上传图片、文档、音频或视频，用于补充题单、参考资料和示例。</span>
            </div>

            <input
              ref="fileInputRef"
              class="hidden-input"
              type="file"
              multiple
              @change="handleAttachmentChange"
            />

            <div v-if="formData.attachments.length" class="attachment-list">
              <div v-for="(item, index) in formData.attachments" :key="`${item.assetUrl}-${index}`" class="attachment-item">
                <div class="attachment-meta">
                  <el-icon class="attachment-icon"><Paperclip /></el-icon>
                  <div>
                    <div class="attachment-name">{{ item.assetName || '未命名附件' }}</div>
                    <div class="attachment-subtitle">{{ formatAssetType(item.assetType) }} · {{ item.assetUrl }}</div>
                  </div>
                </div>
                <el-button text type="danger" :icon="Delete" @click="removeAttachment(index)">移除</el-button>
              </div>
            </div>
            <div v-else class="attachment-empty">当前还没有上传附件。</div>
          </div>
        </el-form-item>

        <div class="form-actions mt-4">
          <el-button size="large" type="primary" class="submit-btn custom-shadow" :loading="store.loading.action" @click="submit(true)">
            {{ isEdit ? '保存并发布' : '确认并立即下发' }}
          </el-button>
          <el-button size="large" :loading="store.loading.action" @click="submit(false)">保存为草稿</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.mb-4 { margin-bottom: 24px; }
.mt-2 { margin-top: 8px; }
.mt-4 { margin-top: 24px; }
.my-4 { margin: 24px 0; }
.w-full { width: 100%; }

.header-glow {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtitle {
  color: #64748b;
  font-size: 0.95rem;
}

.custom-form {
  max-width: 980px;
  padding: 10px;
}

:deep(.deadline-picker.el-date-editor.el-input) {
  height: 54px;
}

:deep(.deadline-picker .el-input__wrapper) {
  min-height: 54px;
  border-radius: 16px;
  border: 1px solid #d8e3f2;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(246, 250, 255, 0.9)),
    radial-gradient(circle at 12% 20%, rgba(64, 158, 255, 0.12), transparent 32%);
  box-shadow: 0 14px 36px rgba(15, 23, 42, 0.06);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

:deep(.deadline-picker .el-input__wrapper:hover),
:deep(.deadline-picker .el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 18px 42px rgba(64, 158, 255, 0.18);
  transform: translateY(-1px);
}

.checkbox-group.glass-panel {
  display: flex;
  gap: 16px;
  align-items: center;
  min-height: 48px;
  background: rgba(241, 245, 249, 0.5);
  border-radius: 8px;
  padding: 0 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.attachment-panel {
  width: 100%;
  padding: 16px;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.72);
}

.attachment-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.attachment-hint {
  color: #64748b;
  font-size: 0.88rem;
}

.hidden-input {
  display: none;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #dbe4f0;
  background: white;
}

.attachment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.attachment-icon {
  font-size: 18px;
  color: #409eff;
}

.attachment-name {
  color: #0f172a;
  font-weight: 600;
}

.attachment-subtitle {
  color: #64748b;
  font-size: 0.82rem;
  word-break: break-all;
}

.attachment-empty {
  color: #94a3b8;
  font-size: 0.92rem;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.submit-btn {
  padding: 0 32px;
  font-weight: 600;
}

.custom-shadow {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}
</style>
