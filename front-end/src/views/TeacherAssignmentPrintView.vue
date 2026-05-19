<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatAssetType } from '@/utils/format-labels'
import {
  getAttachmentDisplayName,
  isImageAttachmentLike,
  isPdfAttachmentLike,
  resolveAttachmentUrl
} from '@/utils/attachment-url'
import { formatFullDateTime } from '@/utils/teacher-portal-view'
import type { HomeworkAsset, HomeworkClassSummary, HomeworkTaskListItem } from '@/types/teacher-portal'

type PrintableClass = Pick<HomeworkClassSummary, 'classId' | 'className'> & Partial<HomeworkClassSummary>

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const loading = ref(false)
const printing = ref(false)
const homeworkId = computed(() => `${route.params.id ?? ''}`)
const detail = computed(() => store.getHomeworkDetail(homeworkId.value))
const tasks = computed(() => store.getHomeworkTasks(homeworkId.value))

const printOptions = reactive({
  classId: 'all',
  showDeadline: true,
  showAttachments: true,
  showStudentNameLine: true,
  includeRoster: false
})

const subjectName = computed(() => {
  const subjectCode = detail.value?.baseInfo.subjectCode
  return store.subjectOptions.find((item) => item.subjectCode === subjectCode)?.subjectName || subjectCode || '-'
})

const classOptions = computed(() => [
  { classId: 'all', className: '全部班级' },
  ...(detail.value?.classList ?? []).map((item) => ({
    classId: `${item.classId}`,
    className: item.className
  }))
])

const printableClasses = computed<PrintableClass[]>(() => {
  const classes = detail.value?.classList ?? []
  if (!classes.length) {
    return [{ classId: 'all', className: '全部班级' }]
  }

  if (printOptions.classId === 'all') {
    return classes
  }

  return classes.filter((item) => `${item.classId}` === `${printOptions.classId}`)
})

const imageAttachments = computed(() =>
  (detail.value?.attachments ?? []).filter((item) => isImageAttachment(item))
)

function getAttachmentUrl(item: HomeworkAsset) {
  return resolveAttachmentUrl(item)
}

function taskListByClass(classId: string | number) {
  if (`${classId}` === 'all') {
    return tasks.value
  }

  return tasks.value.filter((item) => `${item.classId}` === `${classId}`)
}

function getAttachmentName(item: HomeworkAsset) {
  return getAttachmentDisplayName(item)
}

function isImageAttachment(item: HomeworkAsset) {
  return isImageAttachmentLike(item)
}

function isPdfAttachment(item: HomeworkAsset) {
  return isPdfAttachmentLike(item)
}

function getMaterialType(item: HomeworkAsset) {
  if (isImageAttachment(item)) return '图片题目'
  if (isPdfAttachment(item)) return 'PDF 题目'
  return formatAssetType(item.assetType) || '题目文件'
}

function preloadImages(urls: string[]) {
  return Promise.all(
    urls.map(
      (url) =>
        new Promise<void>((resolve) => {
          const image = new Image()
          image.onload = () => resolve()
          image.onerror = () => resolve()
          image.src = url
        })
    )
  )
}

async function loadPage() {
  if (!homeworkId.value) {
    return
  }

  loading.value = true
  try {
    await Promise.all([store.loadHomeworkDetail(homeworkId.value), store.loadHomeworkTasks(homeworkId.value)])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业打印数据加载失败')
  } finally {
    loading.value = false
  }
}

async function printHomework() {
  if (!detail.value) {
    ElMessage.warning('作业信息还没有加载完成')
    return
  }

  printing.value = true
  try {
    await nextTick()
    if (printOptions.showAttachments) {
      await preloadImages(imageAttachments.value.map((item) => getAttachmentUrl(item)).filter(Boolean))
    }
    await nextTick()
    window.print()
  } finally {
    printing.value = false
  }
}

watch(homeworkId, () => {
  void loadPage()
})

onMounted(loadPage)
</script>

<template>
  <div class="assignment-print-page" v-loading="loading">
    <section class="print-toolbar">
      <div>
        <p class="eyebrow">作业打印</p>
        <h2>{{ detail?.baseInfo.title || '作业打印页' }}</h2>
      </div>

      <div class="print-toolbar__actions">
        <el-button @click="router.back()">返回详情</el-button>
        <el-button type="primary" :loading="printing" @click="printHomework">打印作业</el-button>
      </div>
    </section>

    <section v-if="detail" class="print-options">
      <label>
        <span>打印范围</span>
        <el-select v-model="printOptions.classId" placeholder="选择班级">
          <el-option
            v-for="item in classOptions"
            :key="item.classId"
            :label="item.className"
            :value="item.classId"
          />
        </el-select>
      </label>

      <div class="option-switches">
        <el-switch v-model="printOptions.showDeadline" active-text="显示截止时间" />
        <el-switch v-model="printOptions.showAttachments" active-text="显示作业内容" />
        <el-switch v-model="printOptions.showStudentNameLine" active-text="姓名填写栏" />
        <el-switch v-model="printOptions.includeRoster" active-text="附学生名单" />
      </div>
    </section>

    <template v-if="detail">
      <article
        v-for="classItem in printableClasses"
        :key="classItem.classId"
        class="print-sheet"
      >
        <header class="sheet-header">
          <div>
            <p>{{ store.teacher?.school || '小课后作业系统' }}</p>
            <h1>{{ detail.baseInfo.title }}</h1>
          </div>
          <div class="sheet-meta">
            <span>学科：{{ subjectName }}</span>
            <span>班级：{{ classItem.className }}</span>
            <span v-if="printOptions.showDeadline">截止：{{ formatFullDateTime(detail.baseInfo.deadlineAt) }}</span>
          </div>
        </header>

        <section v-if="printOptions.showStudentNameLine" class="student-line">
          <span>姓名：</span>
          <i></i>
          <span>学号：</span>
          <i></i>
          <span>日期：</span>
          <i></i>
        </section>

        <section class="sheet-block">
          <h2>作业要求</h2>
          <p class="content-text">{{ detail.baseInfo.contentText || '暂无作业内容。' }}</p>
        </section>

        <section v-if="printOptions.showAttachments" class="sheet-block attachment-print-block">
          <h2>作业内容</h2>
          <div v-if="detail.attachments.length" class="print-attachments">
            <div
              v-for="(item, index) in detail.attachments"
              :key="`${getAttachmentUrl(item)}-${index}`"
              class="print-attachment"
              :class="{ 'is-image': isImageAttachment(item), 'is-pdf': isPdfAttachment(item) }"
            >
              <img v-if="isImageAttachment(item)" :src="getAttachmentUrl(item)" :alt="getAttachmentName(item)" />
              <div v-else-if="isPdfAttachment(item)" class="pdf-material">
                <div class="material-title">
                  <strong>{{ getAttachmentName(item) }}</strong>
                  <span>{{ getMaterialType(item) }}</span>
                </div>
                <iframe class="pdf-frame screen-preview" :src="getAttachmentUrl(item)" :title="getAttachmentName(item)" />
                <p class="print-note">PDF 题目材料：{{ getAttachmentName(item) }}</p>
              </div>
              <div v-else>
                <strong>{{ getAttachmentName(item) }}</strong>
                <span>{{ getMaterialType(item) }}</span>
              </div>
            </div>
          </div>
          <p v-else class="empty-material">老师未上传题目材料，请按上方作业要求完成。</p>
        </section>

        <section v-if="printOptions.includeRoster" class="sheet-block roster-block">
          <h2>学生名单</h2>
          <div v-if="taskListByClass(classItem.classId).length" class="roster-grid">
            <span
              v-for="task in taskListByClass(classItem.classId)"
              :key="task.taskId"
            >
              {{ (task as HomeworkTaskListItem).studentName }}
            </span>
          </div>
          <p v-else class="empty-line">暂无学生名单</p>
        </section>
      </article>
    </template>

    <el-empty v-else description="暂无可打印的作业信息" />
  </div>
</template>

<style scoped>
.assignment-print-page {
  display: grid;
  gap: 18px;
}

.print-toolbar,
.print-options {
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
}

.print-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 26px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #2f7cff;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.print-toolbar h2 {
  margin: 0;
  color: #10233f;
  font-size: 28px;
}

.print-toolbar__actions {
  display: flex;
  gap: 10px;
}

.print-options {
  display: grid;
  grid-template-columns: minmax(260px, 360px) 1fr;
  gap: 22px;
  align-items: center;
  padding: 18px 22px;
}

.print-options label {
  display: grid;
  gap: 8px;
  color: #52657f;
  font-weight: 700;
}

.option-switches {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 22px;
}

.print-sheet {
  width: 210mm;
  min-height: 297mm;
  margin: 0 auto 24px;
  padding: 18mm;
  color: #162033;
  background:
    linear-gradient(90deg, rgba(47, 124, 255, 0.08), transparent 34%),
    #fff;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.16);
  page-break-after: always;
}

.print-sheet:last-child {
  page-break-after: auto;
}

.sheet-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding-bottom: 14px;
  border-bottom: 3px solid #10233f;
}

.sheet-header p {
  margin: 0 0 8px;
  color: #64748b;
  font-size: 13px;
}

.sheet-header h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
}

.sheet-meta {
  display: grid;
  gap: 8px;
  min-width: 210px;
  color: #334155;
  font-size: 14px;
  text-align: right;
}

.student-line {
  display: grid;
  grid-template-columns: auto 1fr auto 1fr auto 1fr;
  gap: 10px;
  align-items: end;
  margin: 24px 0;
  font-size: 15px;
}

.student-line i {
  display: block;
  height: 28px;
  border-bottom: 1px solid #94a3b8;
}

.sheet-block {
  margin-top: 20px;
}

.sheet-block h2 {
  margin: 0 0 10px;
  color: #10233f;
  font-size: 18px;
}

.content-text {
  min-height: 190px;
  margin: 0;
  padding: 18px;
  border: 1px solid #d7e0ea;
  border-radius: 14px;
  color: #1f2a3d;
  font-size: 16px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.print-attachments {
  display: grid;
  gap: 16px;
}

.print-attachment {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  padding: 14px;
  border: 1px solid #d7e0ea;
  border-radius: 14px;
}

.print-attachment img {
  width: 100%;
  max-height: 620px;
  border-radius: 10px;
  object-fit: contain;
  background: #f8fafc;
}

.print-attachment div {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.print-attachment strong {
  word-break: break-all;
}

.print-attachment span {
  color: #64748b;
  font-size: 13px;
}

.pdf-material {
  display: grid;
  gap: 12px;
}

.material-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.pdf-frame {
  width: 100%;
  height: 520px;
  border: 1px solid #d7e0ea;
  border-radius: 12px;
  background: #fff;
}

.print-note {
  display: none;
  margin: 0;
  color: #334155;
}

.empty-material {
  margin: 0;
  padding: 18px;
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
  color: #64748b;
}

.roster-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border: 1px solid #d7e0ea;
  border-radius: 14px;
  overflow: hidden;
}

.roster-grid span {
  min-height: 42px;
  padding: 10px 12px;
  border-right: 1px solid #d7e0ea;
  border-bottom: 1px solid #d7e0ea;
}

.empty-line {
  color: #64748b;
}

@media print {
  @page {
    size: A4;
    margin: 0;
  }

  :global(html),
  :global(body),
  :global(#app) {
    width: 210mm !important;
    min-height: 297mm !important;
    margin: 0 !important;
    padding: 0 !important;
    min-width: 0 !important;
    background: #fff !important;
  }

  :global(.teacher-shell-next) {
    display: block !important;
    padding: 0 !important;
    background: #fff !important;
  }

  :global(.teacher-shell-next__sidebar),
  :global(.teacher-shell-next__topbar),
  .print-toolbar,
  .print-options {
    display: none !important;
  }

  :global(.teacher-shell-next__main),
  :global(.teacher-shell-next__content) {
    display: block !important;
    width: auto !important;
    margin: 0 !important;
    padding: 0 !important;
    background: #fff !important;
  }

  .assignment-print-page {
    display: block;
  }

  .print-sheet {
    width: 210mm;
    min-height: 297mm;
    margin: 0 !important;
    padding: 14mm 16mm 16mm !important;
    box-shadow: none !important;
    border-radius: 0 !important;
    background: #fff;
  }

  .content-text,
  .print-attachment,
  .roster-grid {
    break-inside: avoid;
  }

  .screen-preview {
    display: none;
  }

  .print-note {
    display: block;
  }
}
</style>
