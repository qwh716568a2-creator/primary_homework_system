<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useParentPortalStore } from '@/stores/parentPortal'
import { formatParentDateTime, resolveParentDeadlineLabel } from '@/utils/parent-portal-view'
import { formatAssetType, formatSubmitTypes } from '@/utils/format-labels'
import { getAttachmentDisplayName, isImageAttachmentLike, resolveAttachmentUrl } from '@/utils/attachment-url'

const route = useRoute()
const router = useRouter()
const store = useParentPortalStore()

const childId = computed(() => `${route.params.childId ?? ''}`)
const homeworkId = computed(() => `${route.params.id ?? ''}`)
const child = computed(() => store.children.find((item) => item.id === childId.value) ?? null)
const homework = computed(() => store.getHomework(childId.value, homeworkId.value))

const imageAttachmentUrls = computed(
  () => homework.value?.attachments.filter(isImageAttachment).map(resolveAttachmentUrl).filter(Boolean) ?? []
)

function getAttachmentName(file: { name?: string; url?: string }) {
  return getAttachmentDisplayName(file)
}

function isImageAttachment(file: { type?: string; name?: string; url?: string }) {
  return isImageAttachmentLike(file)
}

function getAttachmentPreviewIndex(file: { url?: string }) {
  return Math.max(0, imageAttachmentUrls.value.findIndex((url) => url === resolveAttachmentUrl(file)))
}

function getSubmissionImageName(url: string, index: number) {
  const cleanUrl = url.split('?')[0]
  const fileName = cleanUrl.split('/').filter(Boolean).pop()
  return fileName || `提交图片 ${index + 1}`
}

async function loadPage() {
  try {
    if (!store.children.length) {
      await store.loadChildren()
    }
    if (childId.value) {
      store.selectChild(childId.value)
      await store.loadHomeworkDetail(childId.value, homeworkId.value)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '作业详情加载失败，请稍后重试。')
  }
}

function openAssist() {
  void router.push(`/parent/homeworks/${childId.value}/${homeworkId.value}/assist`)
}

function openFeedback() {
  void router.push(`/parent/homeworks/${childId.value}/${homeworkId.value}/feedback`)
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="page-stack">
    <section v-if="homework" class="parent-detail-hero surface-card">
      <div class="parent-detail-hero__bar">
        <el-button text @click="router.push('/parent/home')">← 返回首页</el-button>
        <div class="chip-row">
          <span class="soft-chip">{{ child?.name || '孩子' }}</span>
          <span class="soft-chip">{{ homework.subject }}</span>
          <StatusTag kind="student-homework" :value="homework.status" />
        </div>
      </div>

      <div class="parent-detail-hero__main">
        <div>
          <h2>{{ homework.title }}</h2>
          <p>{{ homework.content }}</p>
        </div>
        <div class="parent-detail-hero__actions">
          <el-button @click="router.push(`/parent/homeworks/${childId}/${homeworkId}/print`)">打印作业</el-button>
          <el-button @click="openFeedback">查看反馈</el-button>
          <el-button type="primary" @click="openAssist">
            {{ homework.status === 'revision' ? '协助订正' : '协助提交' }}
          </el-button>
        </div>
      </div>

      <div class="parent-detail-meta">
        <div>
          <span>老师</span>
          <strong>{{ homework.teacherName }}</strong>
        </div>
        <div>
          <span>截止</span>
          <strong>{{ formatParentDateTime(homework.deadline) }}</strong>
        </div>
        <div>
          <span>方式</span>
          <strong>{{ formatSubmitTypes(homework.submitTypes) }}</strong>
        </div>
        <div>
          <span>节奏</span>
          <strong>{{ resolveParentDeadlineLabel(homework.deadline) }}</strong>
        </div>
      </div>
    </section>

    <div v-else class="empty-state">当前没有找到这份作业。</div>

    <div v-if="homework" class="content-grid parent-detail-grid">
      <section class="surface-card section-card">
        <div class="dashboard-section-head">
          <h3>附件与示例</h3>
        </div>

        <div v-if="homework.attachments.length" class="attachment-list">
          <div v-for="file in homework.attachments" :key="file.id || resolveAttachmentUrl(file)" class="attachment-card">
            <el-image
              v-if="isImageAttachment(file)"
              class="attachment-preview"
              :src="resolveAttachmentUrl(file)"
              :alt="getAttachmentName(file)"
              :preview-src-list="imageAttachmentUrls"
              :initial-index="getAttachmentPreviewIndex(file)"
              fit="cover"
              preview-teleported
              hide-on-click-modal
            />
            <div v-else class="attachment-file-icon">文件</div>

            <div class="attachment-body">
              <div class="attachment-title-row">
                <strong>{{ getAttachmentName(file) }}</strong>
                <span class="soft-chip">{{ isImageAttachment(file) ? '图片附件' : formatAssetType(file.type) }}</span>
              </div>
              <el-link type="primary" :href="resolveAttachmentUrl(file)" target="_blank" :download="getAttachmentName(file)" :underline="false">
                下载
              </el-link>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">暂无附件。</div>
      </section>

      <div class="page-stack">
        <section class="surface-card section-card">
          <div class="dashboard-section-head">
            <h3>提交记录</h3>
          </div>

          <div v-if="homework.latestSubmission" class="panel-list">
            <div class="panel-list-item">
              <div class="dashboard-row-between">
                <strong>最近一次提交</strong>
                <span class="metric-inline">{{ formatParentDateTime(homework.latestSubmission.submittedAt) }}</span>
              </div>
              <p>{{ homework.latestSubmission.text || '本次提交没有文字说明。' }}</p>
              <div v-if="homework.latestSubmission.images?.length" class="submission-image-list">
                <div
                  v-for="(url, index) in homework.latestSubmission.images"
                  :key="`${url}-${index}`"
                  class="submission-image-card"
                >
                  <el-image
                    class="submission-image-preview"
                    :src="url"
                    :alt="getSubmissionImageName(url, index)"
                    :preview-src-list="homework.latestSubmission.images"
                    :initial-index="index"
                    fit="cover"
                    preview-teleported
                    hide-on-click-modal
                  />
                  <span>{{ getSubmissionImageName(url, index) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-state">暂无提交记录。</div>
        </section>

        <section class="surface-card section-card">
          <div class="dashboard-section-head">
            <h3>老师反馈</h3>
          </div>

          <div v-if="homework.review" class="panel-list">
            <div class="panel-list-item">
              <div class="dashboard-row-between">
                <StatusTag kind="student-review" :value="homework.review.status" />
                <span class="metric-inline">{{ formatParentDateTime(homework.review.reviewedAt) }}</span>
              </div>
              <p>{{ homework.review.comment || '老师暂未填写详细评语。' }}</p>
            </div>
          </div>

          <div v-else class="empty-state">暂无反馈。</div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.parent-detail-hero {
  padding: 24px 28px;
  border-radius: 28px;
}

.parent-detail-hero__bar,
.parent-detail-hero__main,
.parent-detail-meta,
.dashboard-row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.parent-detail-hero__main {
  align-items: flex-end;
  margin-top: 18px;
}

.parent-detail-hero h2 {
  margin: 0;
  color: #08213f;
  font-size: 32px;
  letter-spacing: -0.04em;
}

.parent-detail-hero p {
  margin: 12px 0 0;
  color: #536b86;
  line-height: 1.75;
  max-width: 78ch;
}

.parent-detail-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.parent-detail-meta {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
}

.parent-detail-meta > div {
  min-width: 130px;
}

.parent-detail-meta span {
  display: block;
  color: #6d8196;
  font-size: 13px;
}

.parent-detail-meta strong {
  display: block;
  margin-top: 4px;
  color: #08213f;
}

.parent-detail-grid {
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
  background: linear-gradient(135deg, rgba(248, 251, 255, 0.96), rgba(255, 255, 255, 0.92));
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
  width: 88px;
  height: 72px;
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
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
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

@media (max-width: 1080px) {
  .parent-detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .parent-detail-hero__bar,
  .parent-detail-hero__main,
  .parent-detail-meta {
    align-items: flex-start;
    flex-direction: column;
  }

  .parent-detail-hero h2 {
    font-size: 26px;
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
