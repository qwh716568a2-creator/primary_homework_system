<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useStudentPortalStore } from '@/stores/studentPortal'
import { formatStudentDateTime, resolveStudentDeadlineLabel } from '@/utils/student-portal-view'
import { formatAssetType, formatSubmitTypes } from '@/utils/format-labels'
import { getAttachmentDisplayName, isImageAttachmentLike, resolveAttachmentUrl } from '@/utils/attachment-url'

const route = useRoute()
const router = useRouter()
const store = useStudentPortalStore()

const homeworkId = computed(() => `${route.params.id ?? ''}`)
const homework = computed(() => store.getHomework(homeworkId.value))
const submissions = computed(() => store.submissionMap[homeworkId.value] ?? [])
const reviews = computed(() => store.reviewMap[homeworkId.value] ?? [])

function getAttachmentName(file: { name?: string; url?: string }) {
  return getAttachmentDisplayName(file)
}

function isImageAttachment(file: { type?: string; name?: string; url?: string }) {
  return isImageAttachmentLike(file)
}

function getAttachmentPreviewUrls() {
  return homework.value?.attachments.filter(isImageAttachment).map(resolveAttachmentUrl).filter(Boolean) ?? []
}

function getAttachmentPreviewIndex(file: { url?: string }) {
  return Math.max(0, getAttachmentPreviewUrls().findIndex((url) => url === resolveAttachmentUrl(file)))
}

function getSubmissionImageName(url: string, index: number) {
  const cleanUrl = url.split('?')[0]
  const fileName = cleanUrl.split('/').filter(Boolean).pop()

  return fileName || `提交图片 ${index + 1}`
}

async function loadPage() {
  try {
    await Promise.all([
      store.loadHomeworkDetail(homeworkId.value),
      store.loadSubmissions(homeworkId.value),
      store.loadReviews(homeworkId.value)
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业详情加载失败，请稍后重试。')
  }
}

function openSubmit() {
  void router.push(`/student/homeworks/${homeworkId.value}/submit`)
}

function openFeedback() {
  void router.push(`/student/homeworks/${homeworkId.value}/feedback`)
}

function openWrongBook() {
  void router.push({
    path: '/student/wrong-book',
    query: {
      homeworkId: homeworkId.value,
      subjectName: homework.value?.subject || '',
      questionText: homework.value?.title || ''
    }
  })
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="page-stack">
    <header class="page-header">
      <div>
        <h2>作业详情</h2>
        <p>查看作业要求、最近提交记录和老师反馈，再决定继续提交还是整理错题。</p>
      </div>
      <el-button @click="router.push('/student/home')">返回学习台</el-button>
    </header>

    <section v-if="homework" class="student-detail-hero surface-card">
      <div class="student-detail-hero__top">
        <div class="chip-row">
          <span class="soft-chip">{{ homework.subject }}</span>
          <StatusTag kind="student-homework" :value="homework.status" />
        </div>
        <span class="metric-inline">{{ resolveStudentDeadlineLabel(homework.deadline) }}</span>
      </div>

      <h3>{{ homework.title }}</h3>
      <p>{{ homework.content }}</p>

      <div class="student-detail-hero__meta">
        <div>
          <small>授课老师</small>
          <strong>{{ homework.teacherName }}</strong>
        </div>
        <div>
          <small>截止时间</small>
          <strong>{{ formatStudentDateTime(homework.deadline) }}</strong>
        </div>
        <div>
          <small>提交方式</small>
          <strong>{{ formatSubmitTypes(homework.submitTypes) }}</strong>
        </div>
      </div>

      <div class="student-detail-hero__actions">
        <el-button @click="router.push(`/student/homeworks/${homeworkId}/print`)">打印作业</el-button>
        <el-button @click="openFeedback">查看反馈</el-button>
        <el-button @click="openWrongBook">加入错题本</el-button>
        <el-button type="primary" @click="openSubmit">
          {{ homework.status === 'revision' ? '继续订正' : '提交作业' }}
        </el-button>
      </div>
    </section>

    <div v-else class="empty-state">当前未找到这份作业，请返回学习台重新进入。</div>

    <div v-if="homework" class="content-grid student-detail-grid">
      <section class="surface-card section-card">
        <h3>附件与示例</h3>
        <p class="section-subtitle">老师补充的题目材料、参考资料和学习要求。</p>

        <div v-if="homework.attachments.length" class="attachment-list">
          <div v-for="file in homework.attachments" :key="file.id || resolveAttachmentUrl(file)" class="attachment-card">
            <el-image
              v-if="isImageAttachment(file)"
              class="attachment-preview"
              :src="resolveAttachmentUrl(file)"
              :alt="getAttachmentName(file)"
              :preview-src-list="getAttachmentPreviewUrls()"
              :initial-index="getAttachmentPreviewIndex(file)"
              fit="cover"
              preview-teleported
              hide-on-click-modal
            />
            <div v-else class="attachment-file-icon">文</div>

            <div class="attachment-body">
              <div class="dashboard-row-between attachment-title-row">
                <strong>{{ getAttachmentName(file) }}</strong>
                <span class="soft-chip">{{ isImageAttachment(file) ? '图片附件' : formatAssetType(file.type) }}</span>
              </div>
              <el-link type="primary" :href="resolveAttachmentUrl(file)" target="_blank" :download="getAttachmentName(file)" :underline="false">下载</el-link>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">当前作业没有额外附件。</div>
      </section>

      <div class="page-stack">
        <section class="surface-card section-card">
          <h3>提交记录</h3>
          <p class="section-subtitle">查看自己最近几次上传内容和提交时间。</p>

          <div v-if="submissions.length" class="panel-list">
            <div v-for="item in submissions" :key="item.id || item.submittedAt" class="panel-list-item">
              <div class="dashboard-row-between">
                <strong>第 {{ item.versionNo || 1 }} 次提交</strong>
                <span class="metric-inline">{{ formatStudentDateTime(item.submittedAt) }}</span>
              </div>
              <p>{{ item.text || '本次提交未填写文字说明。' }}</p>
              <div v-if="item.images?.length" class="submission-image-list">
                <div
                  v-for="(url, index) in item.images"
                  :key="`${url}-${index}`"
                  class="submission-image-card"
                >
                  <el-image
                    class="submission-image-preview"
                    :src="url"
                    :alt="getSubmissionImageName(url, index)"
                    :preview-src-list="item.images"
                    :initial-index="index"
                    fit="cover"
                    preview-teleported
                    hide-on-click-modal
                  />
                  <span>{{ getSubmissionImageName(url, index) }}</span>
                </div>
              </div>
              <div v-else class="submission-empty-media">本次提交没有图片附件</div>
            </div>
          </div>

          <div v-else class="empty-state">还没有提交记录，开始第一次提交吧。</div>
        </section>

        <section class="surface-card section-card">
          <h3>老师反馈</h3>
          <p class="section-subtitle">最新批改结果和需要继续订正的提醒会显示在这里。</p>

          <div v-if="reviews.length" class="panel-list">
            <div v-for="item in reviews" :key="item.id || item.reviewedAt" class="panel-list-item">
              <div class="dashboard-row-between">
                <StatusTag kind="student-review" :value="item.status" />
                <span class="metric-inline">{{ formatStudentDateTime(item.reviewedAt) }}</span>
              </div>
              <p>{{ item.comment || '老师暂未填写详细评语。' }}</p>
            </div>
          </div>

          <div v-else class="empty-state">老师完成批改后，这里会出现最新反馈。</div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.student-detail-hero {
  padding: 1.5rem;
  border-radius: 30px;
}

.student-detail-hero__top,
.student-detail-hero__meta,
.student-detail-hero__actions,
.dashboard-row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.8rem;
}

.student-detail-hero h3 {
  margin: 1rem 0 0;
  font-size: 2rem;
  color: #14263d;
}

.student-detail-hero p {
  margin: 0.75rem 0 0;
  color: #607589;
  line-height: 1.8;
  max-width: 70ch;
}

.student-detail-hero__meta {
  margin-top: 1.2rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(148, 163, 184, 0.18);
}

.student-detail-hero__meta > div small {
  color: #6d8196;
}

.student-detail-hero__meta > div strong {
  display: block;
  margin-top: 0.22rem;
  color: #17304a;
}

.student-detail-hero__actions {
  margin-top: 1.25rem;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.student-detail-grid {
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
}

.attachment-list {
  display: grid;
  gap: 14px;
}

.attachment-card {
  display: grid;
  grid-template-columns: 168px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
  padding: 14px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(248, 251, 255, 0.96), rgba(255, 255, 255, 0.9));
}

.attachment-preview {
  display: block;
  width: 168px;
  height: 112px;
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid rgba(203, 213, 225, 0.92);
  background: #f4f8fc;
}

.attachment-preview :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.attachment-file-icon {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  color: #1d4ed8;
  font-weight: 800;
  background: #eaf3ff;
}

.attachment-body {
  min-width: 0;
}

.attachment-title-row {
  align-items: flex-start;
}

.attachment-title-row strong {
  word-break: break-all;
}

.submission-image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(128px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.submission-image-card {
  display: block;
  overflow: hidden;
  border-radius: 14px;
  border: 1px solid rgba(148, 163, 184, 0.28);
  background: #f8fbff;
  color: #31506d;
  text-decoration: none;
}

.submission-image-preview {
  display: block;
}

.submission-image-preview :deep(.el-image__inner) {
  width: 100%;
  height: 108px;
  display: block;
  object-fit: cover;
  background: #eef5ff;
}

.submission-image-card span {
  display: block;
  padding: 8px 10px;
  overflow: hidden;
  font-size: 12px;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.submission-empty-media {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  color: #7b8fa6;
  background: rgba(241, 245, 249, 0.72);
}

@media (max-width: 1080px) {
  .student-detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .student-detail-hero__top,
  .student-detail-hero__meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .student-detail-hero h3 {
    font-size: 1.55rem;
  }

  .attachment-card {
    grid-template-columns: 1fr;
  }

  .attachment-preview {
    width: 100%;
    height: 180px;
  }
}
</style>
