<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useStudentPortalStore } from '@/stores/studentPortal'
import {
  getAttachmentDisplayName,
  isImageAttachmentLike,
  isPdfAttachmentLike,
  resolveAttachmentUrl
} from '@/utils/attachment-url'
import type { StudentHomeworkAttachment } from '@/types/student-portal'

const route = useRoute()
const router = useRouter()
const store = useStudentPortalStore()

const loading = ref(false)
const printing = ref(false)
const homeworkId = computed(() => `${route.params.id ?? ''}`)
const homework = computed(() => store.getHomework(homeworkId.value))

const printOptions = reactive({
  showDeadline: true,
  showAttachments: true,
  showStudentNameLine: true,
  showParentSignLine: false
})

const imageAttachments = computed(() => (homework.value?.attachments ?? []).filter((item) => isImageAttachment(item)))

function getAttachmentUrl(file: StudentHomeworkAttachment) {
  return resolveAttachmentUrl(file)
}

function formatPrintDateTime(value?: string) {
  if (!value) return '未设置'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function getAttachmentName(file: StudentHomeworkAttachment) {
  return getAttachmentDisplayName(file)
}

function isImageAttachment(file: StudentHomeworkAttachment) {
  return isImageAttachmentLike(file)
}

function isPdfAttachment(file: StudentHomeworkAttachment) {
  return isPdfAttachmentLike(file)
}

function getMaterialType(file: StudentHomeworkAttachment) {
  if (isImageAttachment(file)) return '图片题目'
  if (isPdfAttachment(file)) return 'PDF 题目'
  return '题目文件'
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
  if (!homeworkId.value) return
  loading.value = true
  try {
    await store.loadHomeworkDetail(homeworkId.value)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业打印数据加载失败')
  } finally {
    loading.value = false
  }
}

async function printHomework() {
  if (!homework.value) {
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
  <div class="homework-print-page" v-loading="loading">
    <section class="print-toolbar">
      <div>
        <p>作业打印</p>
        <h2>{{ homework?.title || '作业打印页' }}</h2>
      </div>
      <div class="print-toolbar__actions">
        <el-button @click="router.back()">返回详情</el-button>
        <el-button type="primary" :loading="printing" @click="printHomework">打印作业</el-button>
      </div>
    </section>

    <section v-if="homework" class="print-options">
      <el-switch v-model="printOptions.showDeadline" active-text="显示截止时间" />
      <el-switch v-model="printOptions.showAttachments" active-text="显示作业内容" />
      <el-switch v-model="printOptions.showStudentNameLine" active-text="姓名填写栏" />
      <el-switch v-model="printOptions.showParentSignLine" active-text="家长签字栏" />
    </section>

    <article v-if="homework" class="print-sheet">
      <header class="sheet-header">
        <div>
          <p>{{ store.profile.school }}</p>
          <h1>{{ homework.title }}</h1>
        </div>
        <div class="sheet-meta">
          <span>学科：{{ homework.subject }}</span>
          <span>老师：{{ homework.teacherName }}</span>
          <span v-if="printOptions.showDeadline">截止：{{ formatPrintDateTime(homework.deadline) }}</span>
        </div>
      </header>

      <section v-if="printOptions.showStudentNameLine" class="fill-lines">
        <span>姓名：</span>
        <i></i>
        <span>班级：</span>
        <i></i>
        <span>日期：</span>
        <i></i>
      </section>

      <section class="sheet-block">
        <h2>作业要求</h2>
        <p class="content-text">{{ homework.content || homework.summary || '暂无作业内容。' }}</p>
      </section>

      <section v-if="printOptions.showAttachments" class="sheet-block">
        <h2>作业内容</h2>
        <div v-if="homework.attachments.length" class="attachment-grid">
          <div
            v-for="file in homework.attachments"
            :key="file.id || getAttachmentUrl(file)"
            class="print-attachment"
            :class="{ 'is-image': isImageAttachment(file), 'is-pdf': isPdfAttachment(file) }"
          >
            <img v-if="isImageAttachment(file)" :src="getAttachmentUrl(file)" :alt="getAttachmentName(file)" />
            <div v-else-if="isPdfAttachment(file)" class="pdf-material">
              <div class="material-title">
                <strong>{{ getAttachmentName(file) }}</strong>
                <span>{{ getMaterialType(file) }}</span>
              </div>
              <iframe class="pdf-frame screen-preview" :src="getAttachmentUrl(file)" :title="getAttachmentName(file)" />
              <p class="print-note">PDF 题目材料：{{ getAttachmentName(file) }}</p>
            </div>
            <div v-else>
              <strong>{{ getAttachmentName(file) }}</strong>
              <span>{{ getMaterialType(file) }}</span>
            </div>
          </div>
        </div>
        <p v-else class="empty-material">老师未上传题目材料，请按上方作业要求完成。</p>
      </section>

      <section v-if="printOptions.showParentSignLine" class="parent-sign-line">
        <span>家长签字：</span>
        <i></i>
      </section>
    </article>

    <el-empty v-else description="暂无可打印的作业信息" />
  </div>
</template>

<style scoped>
.homework-print-page {
  display: grid;
  gap: 18px;
}

.print-toolbar,
.print-options {
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
}

.print-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 26px;
}

.print-toolbar p {
  margin: 0 0 6px;
  color: #2f7cff;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.print-toolbar h2 {
  margin: 0;
  color: #10233f;
  font-size: 28px;
}

.print-toolbar__actions,
.print-options {
  display: flex;
  flex-wrap: wrap;
  gap: 14px 22px;
}

.print-options {
  padding: 18px 22px;
}

.print-sheet {
  width: 210mm;
  min-height: 297mm;
  margin: 0 auto 24px;
  padding: 18mm;
  color: #162033;
  background: #fff;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.16);
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
  text-align: right;
}

.fill-lines,
.parent-sign-line {
  display: grid;
  grid-template-columns: auto 1fr auto 1fr auto 1fr;
  gap: 10px;
  align-items: end;
  margin: 24px 0;
}

.parent-sign-line {
  grid-template-columns: auto 1fr;
  margin-top: 34px;
}

.fill-lines i,
.parent-sign-line i {
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
  font-size: 16px;
  line-height: 1.9;
  white-space: pre-wrap;
}

.attachment-grid {
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
    background: #fff !important;
  }

  :global(.student-shell) {
    display: block !important;
    padding: 0 !important;
    background: #fff !important;
  }

  :global(.student-sidebar),
  :global(.student-topbar),
  .print-toolbar,
  .print-options {
    display: none !important;
  }

  :global(.student-main),
  :global(.student-content) {
    display: block !important;
    width: auto !important;
    margin: 0 !important;
    padding: 0 !important;
    background: #fff !important;
  }

  .homework-print-page {
    display: block;
  }

  .print-sheet {
    width: 210mm;
    min-height: 297mm;
    margin: 0 !important;
    padding: 14mm 16mm 16mm !important;
    box-shadow: none !important;
    border-radius: 0 !important;
  }

  .content-text,
  .print-attachment {
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
