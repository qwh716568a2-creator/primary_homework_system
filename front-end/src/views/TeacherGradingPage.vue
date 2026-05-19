<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatAssetType } from '@/utils/format-labels'
import { getAttachmentDisplayName, isImageAttachmentLike, resolveAttachmentUrl } from '@/utils/attachment-url'
import {
  formatDateTime,
  formatFullDateTime,
  getLatestSubmission,
  getReviewTag,
  getSubmissionText
} from '@/utils/teacher-portal-view'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const selectedTaskId = ref<string>('')

const reviewForm = reactive({
  reviewStatus: 'completed' as 'completed' | 'revision_required',
  score: 90,
  scoreLevel: 'A',
  commentText: ''
})

const homeworkId = computed(() => `${route.params.id ?? ''}`)
const homeworkDetail = computed(() => store.getHomeworkDetail(homeworkId.value))
const taskList = computed(() => store.getHomeworkTasks(homeworkId.value))
const taskDetail = computed(() => store.getTaskDetail(selectedTaskId.value))
const latestSubmission = computed(() => getLatestSubmission(taskDetail.value))

function getAssetName(asset: { assetName?: string; assetUrl?: string }, index: number) {
  return getAttachmentDisplayName(asset, `附件 ${index + 1}`)
}

function isImageAsset(asset: { assetType?: string; assetName?: string; assetUrl?: string }) {
  return isImageAttachmentLike(asset)
}

function getImageAssetUrls(assets: { assetType?: string; assetName?: string; assetUrl?: string }[]) {
  return assets.filter(isImageAsset).map(resolveAttachmentUrl).filter(Boolean)
}

function getImageAssetIndex(
  assets: { assetType?: string; assetName?: string; assetUrl?: string }[],
  assetUrl?: string
) {
  return Math.max(0, getImageAssetUrls(assets).findIndex((url) => url === assetUrl))
}

function fillReviewForm() {
  const latestReview = taskDetail.value?.reviews?.at(-1)

  if (!latestReview) {
    reviewForm.reviewStatus = 'completed'
    reviewForm.score = 90
    reviewForm.scoreLevel = 'A'
    reviewForm.commentText = ''
    return
  }

  reviewForm.reviewStatus =
    latestReview.reviewStatus === 'revision_required' ? 'revision_required' : 'completed'
  reviewForm.score = Number(latestReview.score ?? 90)
  reviewForm.scoreLevel = `${latestReview.scoreLevel ?? 'A'}`
  reviewForm.commentText = latestReview.commentText ?? ''
}

async function selectTask(taskId: number | string) {
  selectedTaskId.value = `${taskId}`

  try {
    await store.loadTaskDetail(taskId)
    fillReviewForm()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改详情加载失败')
  }
}

async function loadPage() {
  try {
    await Promise.all([store.loadHomeworkDetail(homeworkId.value), store.loadHomeworkTasks(homeworkId.value)])

    const targetTask = `${route.query.task ?? ''}` || `${taskList.value[0]?.taskId ?? ''}`
    if (targetTask) {
      await selectTask(targetTask)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改页加载失败')
  }
}

async function submitReview() {
  if (!selectedTaskId.value || !latestSubmission.value) {
    ElMessage.warning('请先选择一条学生任务并确认有提交记录')
    return
  }

  try {
    await store.submitReview(selectedTaskId.value, {
      submissionId: latestSubmission.value.submissionId,
      reviewStatus: reviewForm.reviewStatus,
      score: reviewForm.score,
      scoreLevel: reviewForm.scoreLevel.trim() || undefined,
      commentText: reviewForm.commentText.trim() || undefined
    })

    await Promise.all([
      store.loadHomeworkTasks(homeworkId.value),
      store.loadHomeworkDetail(homeworkId.value),
      store.loadTaskDetail(selectedTaskId.value)
    ])

    fillReviewForm()
    ElMessage.success('批改已提交')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交批改失败')
  }
}

watch(
  () => route.query.task,
  async (value) => {
    if (value) {
      await selectTask(`${value}`)
    }
  }
)

onMounted(loadPage)
</script>

<template>
  <div class="page-stack">
    <header class="page-header grading-header">
      <div>
        <h2 class="hero-title">批改中心</h2>
        <p class="page-subtitle">
          {{ homeworkDetail?.baseInfo.title || '当前作业批改工作台' }}
        </p>
      </div>

      <el-button @click="router.back()">返回</el-button>
    </header>

    <div class="grading-layout">
      <section class="surface-card section-card task-panel">
        <div class="panel-head">
          <h3>学生任务</h3>
          <span>{{ taskList.length }} 条</span>
        </div>

        <div v-if="taskList.length" class="task-list">
          <button
            v-for="item in taskList"
            :key="item.taskId"
            type="button"
            class="task-item"
            :class="{ active: `${item.taskId}` === selectedTaskId }"
            @click="selectTask(item.taskId)"
          >
            <div class="task-item-top">
              <strong>{{ item.studentName }}</strong>
              <StatusTag kind="submission" :value="item.taskStatus" />
            </div>
            <p>{{ item.className }}</p>
            <div class="task-item-bottom">
              <span>{{ formatDateTime(item.latestSubmittedAt) }}</span>
              <StatusTag kind="review" :value="item.reviewStatus" />
            </div>
          </button>
        </div>

        <el-empty v-else description="当前没有可批改任务" />
      </section>

      <section class="surface-card section-card detail-panel">
        <div class="panel-head">
          <h3>提交内容</h3>
          <span v-if="taskDetail?.taskInfo">
            {{ taskDetail.taskInfo.studentName }}
          </span>
        </div>

        <template v-if="taskDetail?.taskInfo">
          <div class="detail-metas">
            <StatusTag kind="submission" :value="taskDetail.taskInfo.taskStatus" />
            <StatusTag kind="review" :value="taskDetail.taskInfo.reviewStatus" />
          </div>

          <div v-if="taskDetail.submissions.length" class="timeline-list">
            <article
              v-for="item in taskDetail.submissions"
              :key="item.submissionId"
              class="timeline-card"
            >
              <div class="timeline-head">
                <strong>第 {{ item.versionNo }} 次提交</strong>
                <span>{{ formatFullDateTime(item.submittedAt) }}</span>
              </div>

              <p>{{ getSubmissionText(item) }}</p>

              <div v-if="item.assets.length" class="asset-list">
                <div
                  v-for="asset in item.assets"
                  :key="resolveAttachmentUrl(asset)"
                  class="asset-item"
                  :class="{ 'asset-item--image': isImageAsset(asset) }"
                >
                  <el-image
                    v-if="isImageAsset(asset)"
                    class="asset-preview"
                    :src="resolveAttachmentUrl(asset)"
                    :alt="getAssetName(asset, 0)"
                    :preview-src-list="getImageAssetUrls(item.assets)"
                    :initial-index="getImageAssetIndex(item.assets, resolveAttachmentUrl(asset))"
                    fit="cover"
                    preview-teleported
                    hide-on-click-modal
                  />
                  <div class="asset-info">
                    <strong>{{ getAssetName(asset, 0) }}</strong>
                    <span>{{ isImageAsset(asset) ? '图片附件' : asset.assetType }}</span>
                    <a :href="resolveAttachmentUrl(asset)" :download="getAssetName(asset, 0)" target="_blank" rel="noreferrer">下载</a>
                  </div>
                </div>
              </div>
            </article>
          </div>

          <el-empty v-else description="学生还没有提交内容" />
        </template>

        <el-empty v-else description="请选择一条学生任务" />
      </section>

      <section class="surface-card section-card review-panel">
        <div class="panel-head">
          <h3>批改结果</h3>
          <span>{{ latestSubmission ? '可提交批改' : '暂无提交' }}</span>
        </div>

        <template v-if="taskDetail?.taskInfo">
          <el-form label-position="top">
            <el-form-item label="批改结论">
              <el-radio-group v-model="reviewForm.reviewStatus">
                <el-radio label="completed">完成</el-radio>
                <el-radio label="revision_required">待订正</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="分数">
              <el-input-number v-model="reviewForm.score" :min="0" :max="100" class="w-full" />
            </el-form-item>

            <el-form-item label="等级">
              <el-input v-model="reviewForm.scoreLevel" placeholder="例如 A / B / 优 / 良" />
            </el-form-item>

            <el-form-item label="评语">
              <el-input
                v-model="reviewForm.commentText"
                type="textarea"
                :rows="7"
                resize="none"
                placeholder="请输入本次批改反馈"
              />
            </el-form-item>

            <el-button
              type="primary"
              class="w-full"
              :loading="store.loading.action"
              @click="submitReview"
            >
              提交批改
            </el-button>
          </el-form>

          <div class="history-block">
            <div class="panel-head small">
              <h3>历史批改</h3>
            </div>

            <div v-if="taskDetail.reviews.length" class="history-list">
              <article
                v-for="item in taskDetail.reviews"
                :key="item.reviewId || `${item.reviewedAt}-${item.reviewStatus}`"
                class="history-card"
              >
                <div class="timeline-head">
                  <StatusTag kind="review" :value="item.reviewStatus" />
                  <span>{{ formatDateTime(item.reviewedAt) }}</span>
                </div>
                <p>{{ getReviewTag(item) }}</p>
              </article>
            </div>

            <el-empty v-else description="还没有历史批改记录" />
          </div>
        </template>

        <el-empty v-else description="请选择左侧任务后开始批改" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.grading-header {
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

.grading-layout {
  display: grid;
  grid-template-columns: 320px 1fr 360px;
  gap: 20px;
  align-items: start;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.panel-head.small {
  margin-bottom: 12px;
}

.panel-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

.panel-head span {
  color: #64748b;
  font-size: 13px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  width: 100%;
  padding: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: 0.2s ease;
}

.task-item:hover,
.task-item.active {
  border-color: #60a5fa;
  box-shadow: 0 16px 36px rgba(59, 130, 246, 0.12);
}

.task-item-top,
.task-item-bottom,
.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.task-item p,
.timeline-card p,
.history-card p {
  margin: 10px 0 0;
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}

.task-item-bottom {
  margin-top: 12px;
}

.detail-metas {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.timeline-list,
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-card,
.history-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.95);
}

.asset-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.asset-item {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 360px;
  padding: 10px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  border-radius: 14px;
  background: #ffffff;
}

.asset-preview {
  width: 96px;
  height: 72px;
  flex: 0 0 96px;
  overflow: hidden;
  border-radius: 10px;
  background: #eef5ff;
}

.asset-preview :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.asset-info {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.asset-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.asset-info span {
  color: #64748b;
  font-size: 12px;
}

.asset-list a {
  color: #2563eb;
  text-decoration: none;
}

.history-block {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.w-full {
  width: 100%;
}

@media (max-width: 1280px) {
  .grading-layout {
    grid-template-columns: 1fr;
  }
}
</style>
