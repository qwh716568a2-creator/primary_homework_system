<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { buildAsset, submissionMethodOptions } from '@/utils/teacher-portal-view'
import { formatAssetType } from '@/utils/format-labels'
import {
  getTeacherAssignmentTemplates,
  saveTeacherAssignmentTemplate
} from '@/utils/teacher-template-store'
import type { AssignmentFormInput, HomeworkAsset } from '@/types/teacher-portal'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const attachmentInput = ref('')
const attachmentType = ref<HomeworkAsset['assetType']>('file')
const templateName = ref('')
const selectedTemplateId = ref('')

const attachmentTypeOptions = [
  { label: '附件链接', value: 'file' },
  { label: '示例图片', value: 'image' }
] as const

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

const templates = computed(() => getTeacherAssignmentTemplates())

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
      if (seen.has(key)) return false
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
  if (!detail) return

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

function applyTemplate(templateId: string) {
  selectedTemplateId.value = templateId
  const template = templates.value.find((item) => item.templateId === templateId)

  if (!template) return

  formData.subjectCode = template.subjectCode
  formData.contentText = template.contentText
  formData.submitTypes = [...template.submitTypes]
  formData.allowLateSubmit = template.allowLateSubmit
  formData.allowResubmit = template.allowResubmit
  formData.needParentConfirm = template.needParentConfirm
  formData.attachments = [...template.attachments]

  if (!formData.title.trim()) {
    formData.title = template.templateName
  }
}

function addAttachment() {
  if (!attachmentInput.value.trim()) return
  formData.attachments.push(buildAsset(attachmentInput.value, attachmentType.value))
  attachmentInput.value = ''
}

function removeAttachment(index: number) {
  formData.attachments.splice(index, 1)
}

function assetTypeLabel(assetType?: HomeworkAsset['assetType']) {
  return assetType === 'image' ? '示例图' : '附件'
}

async function saveTemplate() {
  if (!templateName.value.trim()) {
    ElMessage.warning('请先填写模板名称')
    return
  }

  if (!formData.subjectCode || !formData.contentText.trim()) {
    ElMessage.warning('请先完善学科和作业说明后再保存模板')
    return
  }

  saveTeacherAssignmentTemplate({
    templateName: templateName.value.trim(),
    subjectCode: formData.subjectCode,
    contentText: formData.contentText.trim(),
    submitTypes: [...formData.submitTypes],
    allowLateSubmit: formData.allowLateSubmit,
    allowResubmit: formData.allowResubmit,
    needParentConfirm: formData.needParentConfirm,
    attachments: [...formData.attachments]
  })

  templateName.value = ''
  ElMessage.success('当前配置已保存为模板')
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
    ElMessage.warning('请填写作业标题')
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

async function resetForm() {
  const confirmed = await ElMessageBox.confirm('确定清空当前表单内容吗？', '清空表单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).catch(() => false)

  if (!confirmed) return

  formData.title = ''
  formData.subjectCode = ''
  formData.classIds = []
  formData.deadlineAt = ''
  formData.contentText = ''
  formData.submitTypes = ['text', 'image']
  formData.allowLateSubmit = true
  formData.allowResubmit = true
  formData.needParentConfirm = false
  formData.attachments = []
  selectedTemplateId.value = ''
  attachmentType.value = 'file'
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="teacher-page teacher-builder-next">
    <section class="teacher-page__hero">
      <span class="teacher-page__eyebrow">{{ isEdit ? 'Edit Assignment' : 'New Assignment' }}</span>
      <h2>{{ isEdit ? '调整已有作业并保持原有执行链路。' : '像填一张业务工单一样，把一次作业完整配置清楚。' }}</h2>
      <p>先确定学科、班级和截止时间，再写清任务说明，最后决定是保存草稿还是直接发布。</p>
    </section>

    <section class="teacher-two-column">
      <article class="teacher-panel">
        <header class="teacher-panel__head">
          <div>
            <span class="teacher-kicker">Assignment Form</span>
            <h3>基础设置</h3>
            <p>标题、学科、班级和任务说明决定了这份作业如何被执行。</p>
          </div>
          <div class="teacher-builder-next__head-actions">
            <button type="button" class="teacher-button" @click="resetForm">清空</button>
            <button type="button" class="teacher-button" @click="router.back()">返回</button>
          </div>
        </header>

        <div class="teacher-builder-next__body">
          <div class="teacher-builder-next__grid">
            <label class="teacher-builder-next__field teacher-builder-next__field--wide">
              <span>作业标题</span>
              <el-input v-model="formData.title" placeholder="例如：第三单元口算练习" />
            </label>

            <label class="teacher-builder-next__field">
              <span>所属学科</span>
              <el-select v-model="formData.subjectCode" placeholder="请选择学科">
                <el-option
                  v-for="item in store.subjectOptions"
                  :key="item.subjectCode"
                  :label="item.subjectName"
                  :value="item.subjectCode"
                />
              </el-select>
            </label>

            <label class="teacher-builder-next__field">
              <span>截止时间</span>
              <el-date-picker
                v-model="formData.deadlineAt"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="设置截止时间"
                class="teacher-builder-next__full"
              />
            </label>

            <label class="teacher-builder-next__field teacher-builder-next__field--wide">
              <span>下发班级</span>
              <el-select
                v-model="formData.classIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="选择一个或多个班级"
              >
                <el-option
                  v-for="item in availableClasses"
                  :key="item.classId"
                  :label="item.className"
                  :value="item.classId"
                />
              </el-select>
            </label>

            <label class="teacher-builder-next__field teacher-builder-next__field--wide">
              <span>作业说明</span>
              <el-input
                v-model="formData.contentText"
                type="textarea"
                :rows="9"
                resize="none"
                placeholder="写清楚任务要求、提交方式和评分标准"
              />
            </label>
          </div>

          <div class="teacher-builder-next__subgrid">
            <section class="teacher-builder-next__subpanel">
              <div class="teacher-builder-next__subhead">
                <strong>提交要求</strong>
                <small>学生需要怎么交作业</small>
              </div>
              <div class="teacher-builder-next__checks">
                <el-checkbox-group v-model="formData.submitTypes">
                  <el-checkbox v-for="item in submissionMethodOptions" :key="item.value" :label="item.value">
                    {{ item.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
              <div class="teacher-builder-next__checks teacher-builder-next__checks--stack">
                <el-checkbox v-model="formData.allowLateSubmit">允许逾期提交</el-checkbox>
                <el-checkbox v-model="formData.allowResubmit">允许重复提交</el-checkbox>
                <el-checkbox v-model="formData.needParentConfirm">需要家长确认</el-checkbox>
              </div>
            </section>

            <section class="teacher-builder-next__subpanel">
              <div class="teacher-builder-next__subhead">
                <strong>附件与示例</strong>
                <small>补充材料、范例图或参考文件</small>
              </div>
              <div class="teacher-builder-next__attachment-editor">
                <el-select v-model="attachmentType">
                  <el-option
                    v-for="item in attachmentTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
                <el-input v-model="attachmentInput" placeholder="输入附件地址后点击添加" />
                <button type="button" class="teacher-button" @click="addAttachment">添加</button>
              </div>

              <div v-if="formData.attachments.length" class="teacher-builder-next__attachment-list">
                <div
                  v-for="(item, index) in formData.attachments"
                  :key="`${item.assetUrl}-${index}`"
                  class="teacher-builder-next__attachment"
                >
                  <span>{{ assetTypeLabel(item.assetType) }} · {{ item.assetName || item.assetUrl }}</span>
                  <button type="button" class="teacher-link-button" @click="removeAttachment(index)">移除</button>
                </div>
              </div>
            </section>
          </div>
        </div>
      </article>

      <aside class="teacher-stack">
        <article class="teacher-panel">
          <header class="teacher-panel__head">
            <div>
              <span class="teacher-kicker">Templates</span>
              <h3>复用模板</h3>
              <p>把常用作业快速套用到当前表单。</p>
            </div>
          </header>

          <div class="teacher-builder-next__side">
            <el-select v-model="selectedTemplateId" clearable placeholder="选择模板">
              <el-option
                v-for="item in templates"
                :key="item.templateId"
                :label="item.templateName"
                :value="item.templateId"
              />
            </el-select>
            <button type="button" class="teacher-button" :disabled="!selectedTemplateId" @click="applyTemplate(selectedTemplateId)">
              套用模板
            </button>
            <el-input v-model="templateName" placeholder="给当前配置起一个模板名" />
            <button type="button" class="teacher-button" @click="saveTemplate">保存为模板</button>
          </div>
        </article>

        <article class="teacher-panel">
          <header class="teacher-panel__head">
            <div>
              <span class="teacher-kicker">Action</span>
              <h3>发布动作</h3>
              <p>确认班级、时间和提交方式无误后，再选择草稿或发布。</p>
            </div>
          </header>

          <div class="teacher-builder-next__side">
            <button type="button" class="teacher-button" :disabled="store.loading.action" @click="submit('draft')">
              保存草稿
            </button>
            <button type="button" class="teacher-button--primary" :disabled="store.loading.action" @click="submit('published')">
              {{ isEdit ? '保存并发布' : '立即发布' }}
            </button>
          </div>
        </article>
      </aside>
    </section>
  </div>
</template>

<style scoped>
.teacher-builder-next__head-actions,
.teacher-builder-next__attachment-editor {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.teacher-builder-next__body {
  display: grid;
  gap: 18px;
  padding: 18px 20px 20px;
}

.teacher-builder-next__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.teacher-builder-next__field {
  display: grid;
  gap: 8px;
}

.teacher-builder-next__field span,
.teacher-builder-next__subhead strong {
  color: var(--teacher-ink);
  font-size: 13px;
  font-weight: 700;
}

.teacher-builder-next__field--wide {
  grid-column: 1 / -1;
}

.teacher-builder-next__full {
  width: 100%;
}

.teacher-builder-next__subgrid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.teacher-builder-next__subpanel {
  display: grid;
  gap: 14px;
  padding-top: 16px;
  border-top: 1px solid #ede7db;
}

.teacher-builder-next__subhead {
  display: grid;
  gap: 4px;
}

.teacher-builder-next__subhead small {
  color: var(--teacher-muted);
  font-size: 12px;
}

.teacher-builder-next__checks {
  color: var(--teacher-muted);
}

.teacher-builder-next__checks--stack {
  display: grid;
  gap: 8px;
}

.teacher-builder-next__attachment-list,
.teacher-builder-next__side {
  display: grid;
  gap: 10px;
  padding: 18px 20px 20px;
}

.teacher-builder-next__attachment {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #ede7db;
}

@media (max-width: 1180px) {
  .teacher-builder-next__grid,
  .teacher-builder-next__subgrid {
    grid-template-columns: 1fr;
  }
}
</style>
