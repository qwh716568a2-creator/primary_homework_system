<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { submissionMethodOptions, toAssignmentForm } from '@/utils/teacher-portal'
import type { AssignmentFormInput, HomeworkAsset } from '@/types/teacher-portal'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const formRef = ref<FormInstance>()
const attachmentDraft = reactive({
  assetName: '',
  assetUrl: '',
  assetType: 'file'
})

const editId = computed(() => {
  const value = route.query.edit
  return typeof value === 'string' ? value : ''
})

const isEdit = computed(() => Boolean(editId.value))

const form = reactive<AssignmentFormInput>(toAssignmentForm())

const availableClasses = computed(() =>
  form.subjectCode
    ? store.classRelations.filter((item) => item.subjectCode === form.subjectCode)
    : []
)

const selectedClasses = computed(() => {
  const selectedSet = new Set(form.classIds.map((item) => `${item}`))
  return store.classRelations.filter((item) => selectedSet.has(`${item.classId}`))
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  subjectCode: [{ required: true, message: '请选择学科', trigger: 'change' }],
  classIds: [
    {
      validator: (_, value, callback) => {
        if (Array.isArray(value) && value.length > 0) {
          callback()
          return
        }

        callback(new Error('请至少选择一个班级'))
      },
      trigger: 'change'
    }
  ],
  deadlineAt: [
    { required: true, message: '请选择截止时间', trigger: 'change' },
    {
      validator: (_, value, callback) => {
        if (!value) {
          callback()
          return
        }

        if (new Date(value).getTime() <= Date.now()) {
          callback(new Error('截止时间必须晚于当前时间'))
          return
        }

        callback()
      },
      trigger: 'change'
    }
  ],
  contentText: [{ required: true, message: '请输入作业内容', trigger: 'blur' }],
  submitTypes: [
    {
      validator: (_, value, callback) => {
        if (Array.isArray(value) && value.length > 0) {
          callback()
          return
        }

        callback(new Error('请至少选择一种提交方式'))
      },
      trigger: 'change'
    }
  ]
}

function applyForm(next: AssignmentFormInput) {
  form.title = next.title
  form.subjectCode = next.subjectCode
  form.classIds = [...next.classIds]
  form.deadlineAt = next.deadlineAt
  form.contentText = next.contentText
  form.submitTypes = [...next.submitTypes]
  form.allowLateSubmit = next.allowLateSubmit
  form.allowResubmit = next.allowResubmit
  form.needParentConfirm = next.needParentConfirm
  form.attachments = [...next.attachments]
}

function isClassSelected(classId: number | string) {
  return form.classIds.some((item) => `${item}` === `${classId}`)
}

function toggleClass(classId: number | string) {
  if (isClassSelected(classId)) {
    form.classIds = form.classIds.filter((item) => `${item}` !== `${classId}`)
    return
  }

  form.classIds = [...form.classIds, classId]
}

function selectAllVisibleClasses() {
  const merged = new Set(form.classIds.map((item) => `${item}`))

  availableClasses.value.forEach((item) => {
    merged.add(`${item.classId}`)
  })

  form.classIds = availableClasses.value
    .map((item) => item.classId)
    .filter((item) => merged.has(`${item}`))
}

function clearSelectedClasses() {
  form.classIds = []
}

function removeClass(classId: number | string) {
  form.classIds = form.classIds.filter((item) => `${item}` !== `${classId}`)
}

function addAttachment() {
  const assetUrl = attachmentDraft.assetUrl.trim()

  if (!assetUrl) {
    ElMessage.warning('请先填写附件地址')
    return
  }

  const asset: HomeworkAsset = {
    assetType: attachmentDraft.assetType,
    assetUrl,
    assetName: attachmentDraft.assetName.trim() || assetUrl
  }

  form.attachments = [...form.attachments, asset]
  attachmentDraft.assetName = ''
  attachmentDraft.assetUrl = ''
  attachmentDraft.assetType = 'file'
}

function removeAttachment(index: number) {
  form.attachments.splice(index, 1)
}

async function loadForm() {
  try {
    await store.loadTeachingClasses()

    if (editId.value) {
      const detail = await store.loadHomeworkDetail(editId.value)
      applyForm(toAssignmentForm(detail))
      return
    }

    applyForm(toAssignmentForm())
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业表单加载失败')
  }
}

async function submit(nextState: 'draft' | 'published') {
  const valid = await formRef.value?.validate().catch(() => false)

  if (!valid) {
    return
  }

  try {
    const homeworkId = await store.saveAssignment(form, nextState, editId.value || undefined)
    ElMessage.success(nextState === 'published' ? '作业已发布' : '草稿已保存')
    await router.push(`/assignments/${homeworkId}`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业保存失败')
  }
}

watch(
  () => form.subjectCode,
  (subjectCode) => {
    if (!subjectCode) {
      form.classIds = []
      return
    }

    const allowedIds = new Set(
      store.classRelations
        .filter((item) => item.subjectCode === subjectCode)
        .map((item) => `${item.classId}`)
    )

    form.classIds = form.classIds.filter((item) => allowedIds.has(`${item}`))
  }
)

watch(editId, () => {
  void loadForm()
})

onMounted(loadForm)
</script>

<template>
  <section class="page-stack">
    <header class="page-header">
      <div>
        <h2>{{ isEdit ? '编辑作业' : '发布作业' }}</h2>
        <p>已切换为正式接口表单，发布时可同时选择多个班级，并直接提交 `classIds`。</p>
      </div>
    </header>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="page-stack">
      <section class="split-grid">
        <article class="section-card surface-card">
          <h3>基础信息</h3>
          <p class="section-subtitle">先确定标题、学科和截止时间，再批量选择目标班级。</p>

          <el-form-item label="作业标题" prop="title">
            <el-input
              v-model="form.title"
              maxlength="60"
              show-word-limit
              placeholder="例如：三年级数学口算练习"
            />
          </el-form-item>

          <div class="split-grid">
            <el-form-item label="所属学科" prop="subjectCode">
              <el-select v-model="form.subjectCode" placeholder="请选择学科">
                <el-option
                  v-for="item in store.subjectOptions"
                  :key="item.subjectCode"
                  :label="item.subjectName"
                  :value="item.subjectCode"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="截止时间" prop="deadlineAt">
              <el-date-picker
                v-model="form.deadlineAt"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择截止时间"
                style="width: 100%;"
              />
            </el-form-item>
          </div>

          <el-form-item label="发布班级" prop="classIds">
            <div class="class-selector-shell">
              <div class="card-row-between">
                <p class="section-subtitle" style="margin: 0;">
                  {{ form.subjectCode ? '点击卡片可快速多选班级。' : '请先选择学科，再加载可发布班级。' }}
                </p>
                <div class="actions-row" style="margin-top: 0;">
                  <el-button text :disabled="!availableClasses.length" @click="selectAllVisibleClasses">全选本学科</el-button>
                  <el-button text :disabled="!form.classIds.length" @click="clearSelectedClasses">清空</el-button>
                </div>
              </div>

              <div v-if="availableClasses.length" class="class-selector-grid">
                <button
                  v-for="item in availableClasses"
                  :key="item.classId"
                  type="button"
                  class="class-selector-card"
                  :class="{ 'class-selector-card-active': isClassSelected(item.classId) }"
                  @click="toggleClass(item.classId)"
                >
                  <span class="class-selector-mark">{{ item.className.slice(0, 1) }}</span>
                  <strong>{{ item.className }}</strong>
                  <small>{{ item.isHeadTeacher ? '班主任班级' : '任课班级' }}</small>
                </button>
              </div>
              <div v-else class="empty-state">当前学科下暂无可发布班级。</div>
            </div>
          </el-form-item>

          <div class="chip-row" v-if="selectedClasses.length">
            <span v-for="item in selectedClasses" :key="item.classId" class="soft-chip">
              {{ item.className }}
              <button type="button" class="chip-close-button" @click="removeClass(item.classId)">×</button>
            </span>
          </div>
        </article>

        <article class="section-card surface-card">
          <h3>发布规则</h3>
          <p class="section-subtitle">提交方式和作业规则都会按真实接口字段提交给后端。</p>

          <el-form-item label="提交方式" prop="submitTypes">
            <el-checkbox-group v-model="form.submitTypes">
              <el-checkbox
                v-for="option in submissionMethodOptions"
                :key="option.value"
                :label="option.value"
              >
                {{ option.label }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>

          <div class="form-stack">
            <el-switch v-model="form.allowLateSubmit" active-text="允许逾期提交" inactive-text="截止后不可提交" />
            <el-switch v-model="form.allowResubmit" active-text="允许重复提交" inactive-text="只允许提交一次" />
            <el-switch v-model="form.needParentConfirm" active-text="需要家长确认" inactive-text="无需家长确认" />
          </div>
        </article>
      </section>

      <section class="split-grid">
        <article class="section-card surface-card">
          <h3>作业内容</h3>
          <p class="section-subtitle">填写作业要求，并准备和后端对接的附件结构。</p>

          <el-form-item label="作业内容" prop="contentText">
            <el-input
              v-model="form.contentText"
              type="textarea"
              :rows="10"
              placeholder="请输入作业说明、提交要求、评分重点等内容"
            />
          </el-form-item>

          <div class="attachment-editor">
            <div class="attachment-editor-grid">
              <el-input v-model="attachmentDraft.assetName" placeholder="附件名称，例如：练习册.pdf" />
              <el-select v-model="attachmentDraft.assetType" placeholder="类型">
                <el-option label="文件" value="file" />
                <el-option label="图片" value="image" />
                <el-option label="音频" value="audio" />
                <el-option label="视频" value="video" />
              </el-select>
            </div>
            <div class="attachment-editor-grid">
              <el-input v-model="attachmentDraft.assetUrl" placeholder="附件地址或对象存储链接" />
              <el-button @click="addAttachment">添加附件</el-button>
            </div>
          </div>

          <div class="panel-list" v-if="form.attachments.length">
            <div v-for="(item, index) in form.attachments" :key="`${item.assetUrl}-${index}`" class="panel-list-item">
              <div class="card-row-between">
                <div>
                  <strong>{{ item.assetName || item.assetUrl }}</strong>
                  <p>{{ item.assetType }} · {{ item.assetUrl }}</p>
                </div>
                <el-button text type="danger" @click="removeAttachment(index)">移除</el-button>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">当前没有附件，可在后端上传接口完成后直接接入。</div>
        </article>

        <article class="section-card surface-card">
          <h3>发布检查</h3>
          <p class="section-subtitle">正式开发阶段，推荐在这里确认接口必填项是否齐全。</p>

          <div class="insight-list">
            <div class="insight-item">已选班级 {{ form.classIds.length }} 个，发布时将直接提交 `classIds` 数组。</div>
            <div class="insight-item">学科代码使用后端返回的 `subjectCode`，避免前后端枚举不一致。</div>
            <div class="insight-item">附件已改为对象结构，后续只需将上传结果映射为 `assetUrl/assetName/assetType`。</div>
          </div>
        </article>
      </section>

      <article class="section-card surface-card">
        <h3>提交操作</h3>
        <p class="section-subtitle">支持保存草稿和立即发布两种操作。</p>
        <div class="actions-row">
          <el-button size="large" :loading="store.loading.action" @click="submit('draft')">保存草稿</el-button>
          <el-button type="primary" size="large" :loading="store.loading.action" @click="submit('published')">
            {{ isEdit ? '保存并发布' : '发布作业' }}
          </el-button>
        </div>
      </article>
    </el-form>
  </section>
</template>
