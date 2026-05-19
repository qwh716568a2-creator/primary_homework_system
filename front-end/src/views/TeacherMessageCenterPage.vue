<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Search } from '@element-plus/icons-vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import type { TeacherMessageBizType, TeacherMessageFormInput } from '@/types/teacher-portal'
import { formatDateTime } from '@/utils/teacher-portal-view'

const route = useRoute()
const store = useTeacherPortalStore()
const formRef = ref<FormInstance>()

const bizTypeOptions = [
  { value: 'custom_notice', label: '普通通知' },
  { value: 'submission_reminder', label: '催交通知' },
  { value: 'deadline_reminder', label: '截止提醒' },
  { value: 'review_result', label: '批改反馈' }
] as const

const quickTemplates = [
  {
    label: '今晚截止提醒',
    bizType: 'deadline_reminder',
    title: '今晚作业截止提醒',
    content: '请在今晚截止前完成并提交《{{homeworkTitle}}》。'
  },
  {
    label: '未交催办',
    bizType: 'submission_reminder',
    title: '作业待提交提醒',
    content: '《{{homeworkTitle}}》当前仍未提交，请尽快完成并上传。'
  },
  {
    label: '批改结果反馈',
    bizType: 'review_result',
    title: '作业批改结果已更新',
    content: '《{{homeworkTitle}}》的批改结果已经更新，请及时查看并根据老师建议订正。'
  }
] as const

const form = reactive<TeacherMessageFormInput>({
  bizType: 'custom_notice',
  scopeType: 'class',
  homeworkId: '',
  classIds: [],
  receiverRole: 'both',
  notifyChannels: ['in_app'],
  notifyTitle: '',
  notifyContent: ''
})

const filters = reactive({
  keyword: '',
  bizType: '',
  sendStatus: ''
})

const deletingMessageId = ref<string | number | null>(null)

const rules: FormRules = {
  bizType: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  notifyTitle: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  notifyContent: [{ required: true, message: '请输入消息正文', trigger: 'blur' }],
  classIds: [
    {
      validator: (_rule, value, callback) => {
        if (!Array.isArray(value) || value.length === 0) {
          callback(new Error('至少选择一个接收班级'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

const selectedHomework = computed(() =>
  store.homeworks.find((item) => `${item.homeworkId}` === `${form.homeworkId}`)
)

const selectedHomeworkDetail = computed(() =>
  form.homeworkId ? store.getHomeworkDetail(form.homeworkId) : undefined
)

const classOptions = computed(() => {
  if (form.scopeType !== 'homework' || !form.homeworkId) {
    return store.classOptions
  }

  if (selectedHomeworkDetail.value?.classList?.length) {
    return selectedHomeworkDetail.value.classList.map((item) => ({
      classId: item.classId,
      className: item.className
    }))
  }

  if (selectedHomework.value?.classNames?.length) {
    return selectedHomework.value.classNames
      .map((className) => store.classRelations.find((item) => item.className === className))
      .filter(Boolean)
      .map((item) => ({
        classId: item!.classId,
        className: item!.className
      }))
  }

  return []
})

const stats = computed(() => {
  const total = store.messageRecords.length
  const success = store.messageRecords.filter((item) => item.sendStatus === 'success').length
  const failed = store.messageRecords.filter((item) => item.sendStatus === 'failed').length
  const todayKey = new Date().toISOString().slice(0, 10)
  const today = store.messageRecords.filter((item) => (item.sentAt || item.createdAt || '').slice(0, 10) === todayKey)
    .length

  return [
    { label: '累计发送', value: total, hint: '当前列表中的全部消息记录' },
    { label: '今日发送', value: today, hint: '今天已经发出的消息' },
    { label: '发送成功', value: success, hint: '成功写入通知记录' },
    { label: '发送失败', value: failed, hint: '需要重新核查的记录' }
  ]
})

const previewText = computed(() => {
  const selectedClasses = classOptions.value
    .filter((item) => form.classIds.some((classId) => `${classId}` === `${item.classId}`))
    .map((item) => item.className)

  if (!selectedClasses.length) {
    return '选择班级后，这里会显示本次消息的触达范围。'
  }

  if (form.scopeType === 'homework' && selectedHomework.value) {
    return `将围绕《${selectedHomework.value.title}》向 ${selectedClasses.join('、')} 发送消息。`
  }

  return `将向 ${selectedClasses.join('、')} 发送消息。`
})

const bizTypeLabelMap: Record<string, string> = {
  custom_notice: '普通通知',
  homework_publish: '作业发布',
  deadline_reminder: '截止提醒',
  submission_reminder: '催交通知',
  review_result: '批改反馈'
}

const sendStatusLabelMap: Record<string, string> = {
  pending: '发送中',
  success: '发送成功',
  failed: '发送失败'
}

const receiverRoleLabelMap: Record<string, string> = {
  student: '仅学生',
  parent: '仅家长',
  both: '学生和家长'
}

const channelLabelMap: Record<string, string> = {
  in_app: '站内消息',
  wechat: '微信通知',
  sms: '短信提醒'
}

function fillTemplate(text: string) {
  const homeworkTitle = selectedHomework.value?.title || '当前作业'
  return text.replace(/\{\{homeworkTitle\}\}/g, homeworkTitle)
}

function applyTemplate(template: (typeof quickTemplates)[number]) {
  form.bizType = template.bizType as TeacherMessageBizType
  form.scopeType = 'homework'
  form.notifyTitle = fillTemplate(template.title)
  form.notifyContent = fillTemplate(template.content)
}

function resetComposeForm() {
  form.bizType = 'custom_notice'
  form.scopeType = 'class'
  form.homeworkId = ''
  form.classIds = []
  form.receiverRole = 'both'
  form.notifyChannels = ['in_app']
  form.notifyTitle = ''
  form.notifyContent = ''
  formRef.value?.clearValidate()
}

async function loadRecords() {
  try {
    await store.loadMessageRecords({
      keyword: filters.keyword.trim() || undefined,
      bizType: filters.bizType || undefined,
      sendStatus: filters.sendStatus || undefined
    })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '消息记录加载失败，请稍后重试。')
  }
}

async function loadPage() {
  try {
    await Promise.all([
      store.classRelations.length ? Promise.resolve() : store.loadTeachingClasses(),
      store.homeworks.length ? Promise.resolve() : store.loadHomeworkList(),
      loadRecords()
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '消息中心初始化失败，请稍后重试。')
  }
}

async function sendMessage() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    await store.sendMessage({
      bizType: form.bizType,
      scopeType: form.scopeType,
      homeworkId: form.scopeType === 'homework' ? form.homeworkId : undefined,
      classIds: form.classIds,
      receiverRole: form.receiverRole,
      notifyChannels: form.notifyChannels,
      notifyTitle: form.notifyTitle.trim(),
      notifyContent: form.notifyContent.trim()
    })
    ElMessage.success('消息已发送。')
    resetComposeForm()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '消息发送失败，请稍后重试。')
  }
}

async function handleDelete(messageId: string | number) {
  try {
    await ElMessageBox.confirm('删除后该条消息记录将不再展示，是否继续？', '确认删除消息记录', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  deletingMessageId.value = messageId
  try {
    await store.deleteMessage(messageId)
    ElMessage.success('消息记录已删除。')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '删除失败，请确认后端已提供删除接口。')
  } finally {
    deletingMessageId.value = null
  }
}

function applyRoutePrefill() {
  const scope = `${route.query.scope ?? ''}`
  const homeworkId = `${route.query.homeworkId ?? ''}`
  const classId = `${route.query.classId ?? ''}`
  const bizType = `${route.query.bizType ?? ''}`

  if (scope === 'homework') form.scopeType = 'homework'
  if (homeworkId) form.homeworkId = homeworkId
  if (classId) form.classIds = [classId]
  if (bizTypeOptions.some((item) => item.value === bizType)) {
    form.bizType = bizType as TeacherMessageBizType
  }
}

watch(
  () => route.query,
  () => applyRoutePrefill(),
  { immediate: true }
)

watch(
  () => [form.scopeType, form.homeworkId],
  async ([scopeType, homeworkId]) => {
    if (scopeType === 'homework' && homeworkId && !selectedHomeworkDetail.value) {
      try {
        await store.loadHomeworkDetail(homeworkId)
      } catch (error) {
        ElMessage.error(error instanceof Error ? error.message : '关联作业信息加载失败，请稍后重试。')
      }
    }

    const allowedIds = classOptions.value.map((item) => `${item.classId}`)
    form.classIds = form.classIds.filter((item) => allowedIds.includes(`${item}`))

    if (!form.classIds.length && classOptions.value.length && scopeType === 'homework') {
      form.classIds = classOptions.value.map((item) => item.classId)
    }
  },
  { immediate: true }
)

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="message-page">
    <section class="message-page__summary">
      <article v-for="item in stats" :key="item.label" class="message-page__metric">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <section class="message-page__layout">
      <article class="message-page__compose">
        <header class="message-page__section-head">
          <div>
            <span class="message-page__eyebrow">发送面板</span>
            <h2>创建并发送消息</h2>
            <p>先选模板，再定范围，最后确认内容与渠道。</p>
          </div>
          <div class="message-page__head-actions">
            <el-button @click="resetComposeForm">重置</el-button>
            <el-button type="primary" :loading="store.loading.action" @click="sendMessage">发送消息</el-button>
          </div>
        </header>

        <section class="message-page__templates">
          <button
            v-for="template in quickTemplates"
            :key="template.label"
            type="button"
            class="message-page__template"
            @click="applyTemplate(template)"
          >
            {{ template.label }}
          </button>
        </section>

        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="message-page__form">
          <div class="message-page__grid">
            <el-form-item label="消息类型" prop="bizType">
              <el-select v-model="form.bizType" placeholder="请选择消息类型">
                <el-option
                  v-for="item in bizTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="发送范围">
              <el-radio-group v-model="form.scopeType">
                <el-radio-button label="class">按班级</el-radio-button>
                <el-radio-button label="homework">按作业</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="form.scopeType === 'homework'" label="关联作业" class="message-page__wide">
              <el-select v-model="form.homeworkId" filterable clearable placeholder="请选择关联作业">
                <el-option
                  v-for="item in store.homeworks"
                  :key="item.homeworkId"
                  :label="`${item.title} · ${item.subjectName}`"
                  :value="item.homeworkId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="接收班级" prop="classIds" class="message-page__wide">
              <el-select
                v-model="form.classIds"
                multiple
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择接收班级"
              >
                <el-option
                  v-for="item in classOptions"
                  :key="item.classId"
                  :label="item.className"
                  :value="item.classId"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="接收对象">
              <el-radio-group v-model="form.receiverRole">
                <el-radio-button label="student">仅学生</el-radio-button>
                <el-radio-button label="parent">仅家长</el-radio-button>
                <el-radio-button label="both">学生和家长</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="发送渠道">
              <el-checkbox-group v-model="form.notifyChannels">
                <el-checkbox label="in_app">站内消息</el-checkbox>
                <el-checkbox label="wechat">微信通知</el-checkbox>
                <el-checkbox label="sms">短信提醒</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="消息标题" prop="notifyTitle" class="message-page__wide">
              <el-input
                v-model="form.notifyTitle"
                maxlength="128"
                show-word-limit
                placeholder="例如：作业截止提醒"
              />
            </el-form-item>

            <el-form-item label="消息正文" prop="notifyContent" class="message-page__wide">
              <el-input
                v-model="form.notifyContent"
                type="textarea"
                :rows="7"
                maxlength="500"
                show-word-limit
                placeholder="请输入发送给学生或家长的消息内容"
              />
            </el-form-item>
          </div>
        </el-form>

        <section class="message-page__preview">
          <span class="message-page__eyebrow">发送预览</span>
          <h3>{{ form.notifyTitle || '消息标题将在这里预览' }}</h3>
          <p>{{ form.notifyContent || '消息正文预览区域。' }}</p>
          <div class="message-page__preview-meta">
            <span>{{ previewText }}</span>
            <span>
              {{ receiverRoleLabelMap[form.receiverRole] }} ·
              {{ form.notifyChannels.map((item) => channelLabelMap[item]).join('、') }}
            </span>
          </div>
        </section>
      </article>

      <article class="message-page__records">
        <header class="message-page__section-head">
          <div>
            <span class="message-page__eyebrow">发送记录</span>
            <h2>查看每一次触达</h2>
            <p>每条消息独立成卡，状态、范围、时间和内容一眼看清。</p>
          </div>
          <el-button @click="loadRecords">刷新记录</el-button>
        </header>

        <div class="message-page__filters">
          <el-input
            v-model="filters.keyword"
            :prefix-icon="Search"
            clearable
            placeholder="搜索标题或内容"
          />
          <el-select v-model="filters.bizType" clearable placeholder="全部类型">
            <el-option
              v-for="item in bizTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-select v-model="filters.sendStatus" clearable placeholder="全部状态">
            <el-option label="发送成功" value="success" />
            <el-option label="发送中" value="pending" />
            <el-option label="发送失败" value="failed" />
          </el-select>
          <el-button type="primary" plain @click="loadRecords">筛选</el-button>
        </div>

        <div v-if="store.messageRecords.length" class="message-page__record-list">
          <article
            v-for="item in store.messageRecords"
            :key="item.messageId"
            class="message-page__record"
            :class="`is-${item.sendStatus}`"
          >
            <header class="message-page__record-head">
              <div class="message-page__record-main">
                <h3>{{ item.notifyTitle }}</h3>
                <div class="message-page__record-tags">
                  <span>{{ bizTypeLabelMap[item.bizType] || item.bizType }}</span>
                  <span>{{ receiverRoleLabelMap[item.receiverRole] || item.receiverRole }}</span>
                  <span>{{ (item.notifyChannels || []).map((channel) => channelLabelMap[channel] || channel).join('、') }}</span>
                </div>
              </div>
              <div class="message-page__record-side">
                <strong :class="`is-${item.sendStatus}`">{{ sendStatusLabelMap[item.sendStatus] || item.sendStatus }}</strong>
                <el-button
                  text
                  type="danger"
                  :icon="Delete"
                  :loading="deletingMessageId === item.messageId"
                  @click="handleDelete(item.messageId)"
                >
                  删除
                </el-button>
              </div>
            </header>

            <p class="message-page__record-content">{{ item.notifyContent }}</p>

            <footer class="message-page__record-footer">
              <span>{{ item.classNames?.join('、') || '未标注班级' }}</span>
              <span v-if="item.homeworkTitle">关联作业：{{ item.homeworkTitle }}</span>
              <span>成功 {{ item.successCount ?? 0 }} / 失败 {{ item.failedCount ?? 0 }}</span>
              <span>{{ formatDateTime(item.sentAt || item.createdAt) }}</span>
            </footer>
          </article>
        </div>

        <el-empty v-else description="暂无消息记录" />
      </article>
    </section>
  </div>
</template>

<style scoped>
.message-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.message-page__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.message-page__metric,
.message-page__compose,
.message-page__records {
  border: 1px solid #dde6f2;
  border-radius: 26px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.97) 0%, rgba(248, 251, 255, 0.97) 100%);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.05);
}

.message-page__metric {
  padding: 20px 22px;
  display: grid;
  gap: 8px;
}

.message-page__metric span,
.message-page__metric small,
.message-page__eyebrow {
  color: #6d7d96;
}

.message-page__metric strong {
  font-size: 40px;
  line-height: 1;
  color: #13253d;
}

.message-page__layout {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
  gap: 18px;
  align-items: start;
}

.message-page__compose,
.message-page__records {
  padding: 20px;
}

.message-page__section-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.message-page__section-head h2 {
  margin: 6px 0 0;
  color: #13253d;
}

.message-page__section-head p {
  margin: 8px 0 0;
  color: #70819a;
  line-height: 1.6;
}

.message-page__head-actions,
.message-page__templates,
.message-page__filters,
.message-page__record-tags,
.message-page__preview-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.message-page__template {
  padding: 10px 14px;
  border: 1px solid #dce5f2;
  border-radius: 999px;
  background: #fff;
  color: #35506f;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.message-page__template:hover {
  border-color: #5b84ff;
  background: #eef4ff;
}

.message-page__form {
  margin-top: 18px;
}

.message-page__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px 18px;
}

.message-page__wide {
  grid-column: 1 / -1;
}

.message-page__preview {
  margin-top: 18px;
  padding: 18px;
  border: 1px solid #e1e8f3;
  border-radius: 22px;
  background: #f8fbff;
}

.message-page__preview h3 {
  margin: 8px 0 0;
  color: #162b45;
}

.message-page__preview p {
  margin: 10px 0 0;
  color: #334e6e;
  line-height: 1.8;
}

.message-page__preview-meta {
  margin-top: 14px;
  color: #6f8198;
  font-size: 13px;
}

.message-page__filters {
  margin-bottom: 18px;
}

.message-page__filters > * {
  min-width: 0;
}

.message-page__record-list {
  display: grid;
  gap: 14px;
}

.message-page__record {
  padding: 18px;
  border: 1px solid #dce5f2;
  border-radius: 22px;
  background: #fff;
  border-left-width: 5px;
}

.message-page__record.is-success {
  border-left-color: #1dbf73;
}

.message-page__record.is-failed {
  border-left-color: #f04438;
}

.message-page__record.is-pending {
  border-left-color: #f59e0b;
}

.message-page__record-head,
.message-page__record-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.message-page__record-main {
  min-width: 0;
  display: grid;
  gap: 10px;
}

.message-page__record-main h3 {
  margin: 0;
  color: #13253d;
  font-size: 22px;
}

.message-page__record-tags span,
.message-page__record-footer span {
  padding: 6px 10px;
  border-radius: 999px;
  background: #f5f8fd;
  color: #5f718a;
  font-size: 13px;
}

.message-page__record-side {
  display: grid;
  justify-items: end;
  gap: 6px;
}

.message-page__record-side strong {
  font-size: 14px;
}

.message-page__record-side strong.is-success {
  color: #1d7f49;
}

.message-page__record-side strong.is-failed {
  color: #d92d20;
}

.message-page__record-side strong.is-pending {
  color: #b26a00;
}

.message-page__record-content {
  margin: 16px 0;
  color: #243b59;
  line-height: 1.85;
  white-space: pre-wrap;
}

.message-page__record-footer {
  flex-wrap: wrap;
}

@media (max-width: 1380px) {
  .message-page__layout,
  .message-page__summary,
  .message-page__grid {
    grid-template-columns: 1fr;
  }
}
</style>
