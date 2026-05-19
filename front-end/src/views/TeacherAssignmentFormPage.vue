<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { buildAsset, submissionMethodOptions } from '@/utils/teacher-portal-view'
import type { AssignmentFormInput } from '@/types/teacher-portal'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const attachmentInput = ref('')
const isEdit = computed(() => Boolean(route.query.edit))
const editingId = computed(() => route.query.edit as string | undefined)

const formData = reactive<AssignmentFormInput>({
  title: '',
  subjectCode: '',
  classIds: [],
  deadlineAt: '',
  contentText: '',
  submitTypes: ['text', 'image'],
  allowLateSubmit: true,
  allowResubmit: true,
  needParentConfirm: false,
  attachments: []
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
  const selected = parseDeadlineDate(formData.deadlineAt)
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

const availableClasses = computed(() => {
  if (!formData.subjectCode) {
    return store.classOptions
  }

  const seen = new Set<string>()
  return store.classRelations
    .filter((item) => item.subjectCode === formData.subjectCode)
    .map((item) => ({
      classId: item.classId,
      className: item.className
    }))
    .filter((item) => {
      const key = `${item.classId}`
      if (seen.has(key)) {
        return false
      }
      seen.add(key)
      return true
    })
})

watch(
  () => formData.subjectCode,
  () => {
    const validIds = new Set(availableClasses.value.map((item) => `${item.classId}`))
    formData.classIds = formData.classIds.filter((item) => validIds.has(`${item}`))
  }
)

function applyDetail(detail: Awaited<ReturnType<typeof store.loadHomeworkDetail>>) {
  if (!detail) {
    return
  }

  formData.title = detail.baseInfo.title
  formData.subjectCode = detail.baseInfo.subjectCode
  formData.classIds = detail.classList.map((item) => item.classId)
  formData.contentText = detail.baseInfo.contentText || ''
  formData.deadlineAt = detail.baseInfo.deadlineAt || ''
  formData.allowLateSubmit = detail.baseInfo.allowLateSubmit ?? true
  formData.allowResubmit = detail.baseInfo.allowResubmit ?? true
  formData.needParentConfirm = detail.baseInfo.needParentConfirm ?? false
  formData.submitTypes = detail.baseInfo.submitTypes?.length ? [...detail.baseInfo.submitTypes] : ['text', 'image']
  formData.attachments = [...detail.attachments]
}

function addAttachment() {
  if (!attachmentInput.value.trim()) {
    return
  }

  formData.attachments.push(buildAsset(attachmentInput.value))
  attachmentInput.value = ''
}

function removeAttachment(index: number) {
  formData.attachments.splice(index, 1)
}

async function loadPage() {
  try {
    await store.loadTeachingClasses()

    if (editingId.value) {
      const detail = await store.loadHomeworkDetail(editingId.value)
      applyDetail(detail)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '页面数据加载失败')
  }
}

async function submit(nextState: 'draft' | 'published') {
  if (!formData.title.trim()) {
    ElMessage.warning('请先填写作业标题')
    return
  }

  if (!formData.subjectCode) {
    ElMessage.warning('请选择学科')
    return
  }

  if (!formData.classIds.length) {
    ElMessage.warning('请至少选择一个班级')
    return
  }

  if (!formData.deadlineAt) {
    ElMessage.warning('请设置截止时间')
    return
  }

  const deadline = parseDeadlineDate(formData.deadlineAt)
  if (deadline && deadline.getTime() <= Date.now()) {
    ElMessage.warning('截止时间不能早于当前时间')
    return
  }

  if (!formData.contentText.trim()) {
    ElMessage.warning('请填写作业要求')
    return
  }

  try {
    await store.saveAssignment(
      {
        ...formData,
        title: formData.title.trim(),
        contentText: formData.contentText.trim()
      },
      nextState,
      editingId.value
    )

    ElMessage.success(nextState === 'published' ? '作业已发布' : '草稿已保存')
    await router.push('/assignments')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败')
  }
}

onMounted(loadPage)
</script>

<template>
  <div class="page-stack">
    <header class="page-header form-page-header">
      <div>
        <h2 class="hero-title">{{ isEdit ? '编辑作业' : '发布新作业' }}</h2>
        <p class="page-subtitle">支持按学科给多个班级同时发布，保存草稿后也可继续编辑。</p>
      </div>

      <el-button @click="router.back()">返回</el-button>
    </header>

    <section class="surface-card section-card">
      <el-form label-position="top" @submit.prevent>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="作业标题" required>
              <el-input v-model="formData.title" placeholder="例如：第三单元口算练习" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="所属学科" required>
              <el-select v-model="formData.subjectCode" placeholder="请选择学科" class="w-full">
                <el-option
                  v-for="item in store.subjectOptions"
                  :key="item.subjectCode"
                  :label="item.subjectName"
                  :value="item.subjectCode"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="下发班级" required>
              <el-select v-model="formData.classIds" multiple placeholder="选择一个或多个班级" class="w-full">
                <el-option
                  v-for="item in availableClasses"
                  :key="item.classId"
                  :label="item.className"
                  :value="item.classId"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="作业要求" required>
              <el-input v-model="formData.contentText" type="textarea" :rows="6" resize="none" placeholder="请输入作业说明、步骤和提交要求" />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="截止时间" required>
              <el-date-picker
                v-model="formData.deadlineAt"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                format="YYYY年MM月DD日 HH:mm"
                placeholder="设置截止时间"
                class="w-full deadline-picker"
                :disabled-date="disabledPastDate"
                :disabled-hours="disabledPastHours"
                :disabled-minutes="disabledPastMinutes"
                :disabled-seconds="disabledPastSeconds"
                popper-class="homework-deadline-popper"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="提交方式">
              <el-checkbox-group v-model="formData.submitTypes" class="method-group">
                <el-checkbox
                  v-for="item in submissionMethodOptions"
                  :key="item.value"
                  :label="item.value"
                >
                  {{ item.label }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <div class="policy-grid">
              <el-checkbox v-model="formData.allowLateSubmit">允许逾期提交</el-checkbox>
              <el-checkbox v-model="formData.allowResubmit">允许重复提交</el-checkbox>
              <el-checkbox v-model="formData.needParentConfirm">需要家长确认</el-checkbox>
            </div>
          </el-col>

          <el-col :span="24">
            <el-form-item label="附件链接">
              <div class="attachment-editor">
                <el-input v-model="attachmentInput" placeholder="输入附件地址后点击添加" />
                <el-button @click="addAttachment">添加附件</el-button>
              </div>

              <div v-if="formData.attachments.length" class="attachment-list">
                <div
                  v-for="(item, index) in formData.attachments"
                  :key="`${item.assetUrl}-${index}`"
                  class="attachment-chip"
                >
                  <span>{{ item.assetName || item.assetUrl }}</span>
                  <button type="button" @click="removeAttachment(index)">移除</button>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button :loading="store.loading.action" @click="submit('draft')">保存草稿</el-button>
          <el-button type="primary" :loading="store.loading.action" @click="submit('published')">
            {{ isEdit ? '保存并发布' : '发布作业' }}
          </el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.form-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.page-subtitle {
  margin: 10px 0 0;
  color: #64748b;
  font-size: 14px;
}

.w-full {
  width: 100%;
}

.method-group {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.policy-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin: 8px 0 20px;
}

.attachment-editor {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
}

.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 14px;
}

.attachment-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1e3a8a;
  font-size: 13px;
}

.attachment-chip button {
  border: 0;
  background: transparent;
  color: #2563eb;
  cursor: pointer;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
</style>
