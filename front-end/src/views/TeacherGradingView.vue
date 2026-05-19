<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Search } from '@element-plus/icons-vue'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { formatAssetType } from '@/utils/format-labels'
import { getAttachmentDisplayName, isImageAttachmentLike, resolveAttachmentUrl } from '@/utils/attachment-url'
import { formatDateTime, getRelativeDeadline } from '@/utils/teacher-portal-view'
import type { HomeworkTaskListItem } from '@/types/teacher-portal'

type ReviewDecision = 'completed' | 'revision_required'
type TaskTab = 'pending' | 'revision_required' | 'missing' | 'done'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const selectedHomeworkId = ref('')
const selectedTaskId = ref('')
const activeTab = ref<TaskTab>('pending')

const filters = reactive({
  keyword: '',
  classId: ''
})

const reviewForm = reactive({
  reviewStatus: 'completed' as ReviewDecision,
  score: 90,
  scoreLevel: 'A',
  commentText: ''
})

const quickComments = ['思路清晰', '书写规范', '步骤完整', '继续保持', '请尽快订正']
const quickScores = [100, 95, 90, 85, 80, 70]

const routeHomeworkId = computed(() => `${route.params.id ?? ''}`)
const routeTaskId = computed(() => `${route.query.task ?? ''}`)

const currentHomework = computed(() =>
  store.homeworks.find((item) => `${item.homeworkId}` === selectedHomeworkId.value)
)

const currentTaskList = computed(() => store.getHomeworkTasks(selectedHomeworkId.value))
const currentTaskDetail = computed(() => store.getTaskDetail(selectedTaskId.value))
const latestSubmission = computed(() => currentTaskDetail.value?.submissions?.at(-1))
const latestReview = computed(() => currentTaskDetail.value?.reviews?.at(-1))

function getAssetName(asset: { assetName?: string; assetUrl?: string }, index: number) {
  return getAttachmentDisplayName(asset, `附件 ${index + 1}`)
}

function isImageAsset(asset: { assetType?: string; assetName?: string; assetUrl?: string }) {
  return isImageAttachmentLike(asset)
}

function getImageAssetUrls(assets: { assetType?: string; assetName?: string; assetUrl?: string }[] = []) {
  return assets.filter(isImageAsset).map(resolveAttachmentUrl).filter(Boolean)
}

function getImageAssetIndex(
  assets: { assetType?: string; assetName?: string; assetUrl?: string }[] = [],
  assetUrl?: string
) {
  return Math.max(0, getImageAssetUrls(assets).findIndex((url) => url === assetUrl))
}

const homeworkQueue = computed(() =>
  [...store.homeworks]
    .filter((item) => item.pendingCount > 0)
    .sort((left, right) => {
      if (left.pendingCount !== right.pendingCount) {
        return right.pendingCount - left.pendingCount
      }
      return `${left.deadlineAt}`.localeCompare(`${right.deadlineAt}`)
    })
)

const classOptions = computed(() => {
  const map = new Map<string, string>()
  currentTaskList.value.forEach((item) => {
    if (!map.has(`${item.classId}`)) {
      map.set(`${item.classId}`, item.className)
    }
  })
  return Array.from(map.entries()).map(([value, label]) => ({ value, label }))
})

const taskStats = computed(() => ({
  pending: currentTaskList.value.filter((item) => item.taskStatus === 'submitted' || item.reviewStatus === 'unreviewed')
    .length,
  revisionRequired: currentTaskList.value.filter((item) => item.reviewStatus === 'revision_required').length,
  missing: currentTaskList.value.filter((item) => item.taskStatus === 'pending').length,
  done: currentTaskList.value.filter((item) => item.reviewStatus === 'completed').length
}))

const summaryCards = computed(() => [
  { key: 'pending', label: '待批改', value: taskStats.value.pending, hint: '优先处理最新提交' },
  { key: 'revision', label: '待订正', value: taskStats.value.revisionRequired, hint: '继续跟进学生订正' },
  { key: 'missing', label: '未提交', value: taskStats.value.missing, hint: '可直接发送催交提醒' },
  { key: 'done', label: '已完成', value: taskStats.value.done, hint: '本次已处理的批改记录' }
])

const tabOptions = computed<{ key: TaskTab; label: string }[]>(() => [
  { key: 'pending', label: `待批改（${taskStats.value.pending}）` },
  { key: 'revision_required', label: `待订正（${taskStats.value.revisionRequired}）` },
  { key: 'missing', label: `未提交（${taskStats.value.missing}）` },
  { key: 'done', label: `已完成（${taskStats.value.done}）` }
])

const filteredTasks = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return currentTaskList.value.filter((item) => {
    if (filters.classId && `${item.classId}` !== filters.classId) return false
    if (keyword) {
      const text = `${item.studentName} ${item.className}`.toLowerCase()
      if (!text.includes(keyword)) return false
    }

    if (activeTab.value === 'pending') {
      return item.taskStatus === 'submitted' || item.reviewStatus === 'unreviewed'
    }
    if (activeTab.value === 'revision_required') {
      return item.reviewStatus === 'revision_required'
    }
    if (activeTab.value === 'missing') {
      return item.taskStatus === 'pending'
    }
    return item.reviewStatus === 'completed'
  })
})

const currentTask = computed(() =>
  currentTaskList.value.find((item) => `${item.taskId}` === selectedTaskId.value)
)

const currentTaskIndex = computed(() =>
  filteredTasks.value.findIndex((item) => `${item.taskId}` === selectedTaskId.value)
)

function taskHint(task: HomeworkTaskListItem) {
  if (task.taskStatus === 'pending') return '未提交'
  if (task.reviewStatus === 'revision_required') return '待订正'
  if (task.reviewStatus === 'completed') return '已完成'
  return '待批改'
}

function scoreToLevel(score: number) {
  if (score >= 90) return 'A'
  if (score >= 80) return 'B'
  if (score >= 70) return 'C'
  if (score >= 60) return 'D'
  return 'E'
}

function applyComment(comment: string) {
  reviewForm.commentText = reviewForm.commentText ? `${reviewForm.commentText} ${comment}` : comment
}

function applyScore(score: number) {
  reviewForm.score = score
  reviewForm.scoreLevel = scoreToLevel(score)
}

function syncReviewForm() {
  const review = latestReview.value
  const score = Number(review?.score ?? 90)
  reviewForm.reviewStatus = review?.reviewStatus === 'revision_required' ? 'revision_required' : 'completed'
  reviewForm.score = score
  reviewForm.scoreLevel = `${review?.scoreLevel ?? scoreToLevel(score)}`
  reviewForm.commentText = review?.commentText ?? ''
}

async function selectTask(taskId: string | number) {
  selectedTaskId.value = `${taskId}`
  try {
    await store.loadTaskDetail(taskId)
    syncReviewForm()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '任务详情加载失败')
  }
}

async function openHomework(homeworkId: string | number) {
  selectedHomeworkId.value = `${homeworkId}`
  selectedTaskId.value = ''
  filters.keyword = ''
  filters.classId = ''

  try {
    await Promise.all([store.loadHomeworkDetail(homeworkId), store.loadHomeworkTasks(homeworkId)])

    const initialTask =
      (routeTaskId.value && currentTaskList.value.find((item) => `${item.taskId}` === routeTaskId.value)) ||
      currentTaskList.value.find((item) => item.taskStatus === 'submitted' || item.reviewStatus === 'unreviewed') ||
      currentTaskList.value[0]

    if (initialTask) {
      await selectTask(initialTask.taskId)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改队列加载失败')
  }
}

async function submitReviewAndNext() {
  if (!selectedTaskId.value || !latestSubmission.value) {
    ElMessage.warning('请先选择一份已提交的学生作业')
    return
  }

  try {
    await store.submitReview(selectedTaskId.value, {
      submissionId: latestSubmission.value.submissionId,
      reviewStatus: reviewForm.reviewStatus,
      score: reviewForm.score,
      scoreLevel: reviewForm.scoreLevel,
      commentText: reviewForm.commentText
    })

    await store.loadHomeworkTasks(selectedHomeworkId.value)
    ElMessage.success('批改结果已提交')

    const nextTask = filteredTasks.value[currentTaskIndex.value + 1]
    if (nextTask) {
      await selectTask(nextTask.taskId)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交批改失败')
  }
}

async function remindCurrentTask() {
  if (!selectedHomeworkId.value || !currentTask.value) {
    ElMessage.warning('请先选择一位学生任务')
    return
  }

  try {
    await store.sendTypedReminder(selectedHomeworkId.value, {
      remindType: currentTask.value.taskStatus === 'pending' ? 'pending' : 'overdue',
      classId: currentTask.value.classId
    })
    ElMessage.success('提醒已发送')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提醒发送失败')
  }
}

function goAssignmentDetail() {
  if (!selectedHomeworkId.value) return
  router.push(`/assignments/${selectedHomeworkId.value}`)
}

function switchTab(tab: TaskTab) {
  activeTab.value = tab
}

function goPrevTask() {
  const target = filteredTasks.value[currentTaskIndex.value - 1]
  if (target) void selectTask(target.taskId)
}

function goNextTask() {
  const target = filteredTasks.value[currentTaskIndex.value + 1]
  if (target) void selectTask(target.taskId)
}

watch(
  () => reviewForm.score,
  (value) => {
    reviewForm.scoreLevel = scoreToLevel(Number(value || 0))
  }
)

watch(
  () => routeHomeworkId.value,
  (value) => {
    if (value && value !== selectedHomeworkId.value) {
      void openHomework(value)
    }
  }
)

watch(
  () => filteredTasks.value.map((item) => `${item.taskId}`),
  (taskIds) => {
    if (!taskIds.length) {
      selectedTaskId.value = ''
      return
    }

    if (!taskIds.includes(selectedTaskId.value)) {
      void selectTask(taskIds[0])
    }
  }
)

onMounted(async () => {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadHomeworkList(), store.loadHomeworkOverview()])
    const initialHomeworkId = routeHomeworkId.value || `${homeworkQueue.value[0]?.homeworkId ?? ''}`
    if (initialHomeworkId) {
      await openHomework(initialHomeworkId)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改中心加载失败')
  }
})
</script>

<template>
  <div class="grading-page">
    <section class="grading-page__summary">
      <article v-for="card in summaryCards" :key="card.key" class="grading-page__summary-card">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.hint }}</small>
      </article>
    </section>

    <section class="grading-page__queue">
      <header class="grading-page__section-head">
        <div>
          <span class="grading-page__eyebrow">作业队列</span>
          <h2>先选作业，再处理学生任务</h2>
        </div>
      </header>

      <div class="grading-page__queue-list">
        <button
          v-for="item in homeworkQueue"
          :key="item.homeworkId"
          type="button"
          class="grading-page__queue-card"
          :class="{ 'is-active': `${item.homeworkId}` === selectedHomeworkId }"
          @click="openHomework(item.homeworkId)"
        >
          <div class="grading-page__queue-head">
            <strong>{{ item.title }}</strong>
            <StatusTag kind="assignment" :value="item.status" />
          </div>
          <p>{{ item.subjectName }} · {{ item.classNames.join('、') || '暂无班级' }}</p>
          <div class="grading-page__queue-meta">
            <span>{{ item.pendingCount }} 待批改</span>
            <span>{{ item.submittedCount }} 已提交</span>
          </div>
        </button>
      </div>
    </section>

    <section class="grading-page__workspace">
      <article class="grading-page__tasks">
        <header class="grading-page__section-head grading-page__section-head--compact">
          <div>
            <span class="grading-page__eyebrow">学生任务</span>
            <h3>{{ currentHomework?.title || '请选择作业' }}</h3>
            <p>{{ currentHomework ? getRelativeDeadline(currentHomework.deadlineAt) : '从上方队列选择作业开始处理' }}</p>
          </div>
          <div class="grading-page__tools">
            <el-input
              v-model="filters.keyword"
              :prefix-icon="Search"
              clearable
              placeholder="搜索学生姓名"
              style="width: 210px"
            />
            <el-select v-model="filters.classId" clearable placeholder="全部班级" style="width: 140px">
              <el-option
                v-for="item in classOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-button plain @click="goAssignmentDetail">作业详情</el-button>
          </div>
        </header>

        <div class="grading-page__tabs">
          <button
            v-for="tab in tabOptions"
            :key="tab.key"
            type="button"
            class="grading-page__tab"
            :class="{ 'is-active': activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="grading-page__task-list">
          <button
            v-for="task in filteredTasks"
            :key="task.taskId"
            type="button"
            class="grading-page__task-card"
            :class="{ 'is-active': `${task.taskId}` === selectedTaskId }"
            @click="selectTask(task.taskId)"
          >
            <div class="grading-page__task-card-head">
              <div>
                <strong>{{ task.studentName }}</strong>
                <p>{{ task.className }} · {{ taskHint(task) }}</p>
              </div>
              <StatusTag kind="submission" :value="task.taskStatus" />
            </div>
            <div class="grading-page__task-card-foot">
              <span>{{ formatDateTime(task.latestSubmittedAt) || '暂无提交时间' }}</span>
              <span>提交 {{ task.submissionCount }} 次</span>
            </div>
          </button>

          <div v-if="!filteredTasks.length" class="grading-page__empty">
            当前筛选下没有学生任务。
          </div>
        </div>
      </article>

      <article class="grading-page__review">
        <header class="grading-page__section-head grading-page__section-head--compact">
          <div>
            <span class="grading-page__eyebrow">当前批改</span>
            <h3>{{ currentTask?.studentName || '请选择学生任务' }}</h3>
            <p>{{ currentTask ? `${currentTask.className} · ${taskHint(currentTask)}` : '先从左侧选中一位学生' }}</p>
          </div>
          <div class="grading-page__tools">
            <el-button circle plain :icon="ArrowLeft" :disabled="currentTaskIndex <= 0" @click="goPrevTask" />
            <el-button
              circle
              plain
              :icon="ArrowRight"
              :disabled="currentTaskIndex < 0 || currentTaskIndex >= filteredTasks.length - 1"
              @click="goNextTask"
            />
          </div>
        </header>

        <div class="grading-page__review-grid">
          <section class="grading-page__paper">
            <div class="grading-page__paper-card">
              <span class="grading-page__eyebrow">提交内容</span>
              <p>{{ latestSubmission?.submitText?.trim() || '当前学生没有填写文字说明，请结合附件继续查看。' }}</p>
            </div>

            <div v-if="latestSubmission?.assets?.length" class="grading-page__asset-list">
              <div
                v-for="(asset, index) in latestSubmission.assets"
                :key="`${resolveAttachmentUrl(asset)}-${index}`"
                class="grading-page__asset"
                :class="{ 'grading-page__asset--image': isImageAsset(asset) }"
              >
                <el-image
                  v-if="isImageAsset(asset)"
                  class="grading-page__asset-preview"
                  :src="resolveAttachmentUrl(asset)"
                  :alt="getAssetName(asset, index)"
                  :preview-src-list="getImageAssetUrls(latestSubmission.assets)"
                  :initial-index="getImageAssetIndex(latestSubmission.assets, resolveAttachmentUrl(asset))"
                  fit="cover"
                  preview-teleported
                  hide-on-click-modal
                />
                <strong>{{ asset.assetName || `附件 ${index + 1}` }}</strong>
                <span>{{ asset.assetType }}</span>
                <a :href="resolveAttachmentUrl(asset)" :download="getAssetName(asset, index)" target="_blank" rel="noreferrer">下载</a>
              </div>
            </div>

            <div class="grading-page__paper-card grading-page__paper-card--muted">
              <span class="grading-page__eyebrow">最近一次批改</span>
              <p>{{ latestReview?.commentText || '还没有历史批改记录。' }}</p>
            </div>
          </section>

          <aside class="grading-page__review-panel">
            <div class="grading-page__field-grid">
              <label class="grading-page__field">
                <span>批改结论</span>
                <el-radio-group v-model="reviewForm.reviewStatus">
                  <el-radio-button label="completed">完成</el-radio-button>
                  <el-radio-button label="revision_required">待订正</el-radio-button>
                </el-radio-group>
              </label>

              <label class="grading-page__field">
                <span>分数</span>
                <el-input-number v-model="reviewForm.score" :min="0" :max="100" :step="5" controls-position="right" />
              </label>

              <label class="grading-page__field">
                <span>等级</span>
                <el-input v-model="reviewForm.scoreLevel" />
              </label>
            </div>

            <div class="grading-page__quick-row">
              <span>快捷分数</span>
              <div class="grading-page__chip-row">
                <button
                  v-for="score in quickScores"
                  :key="score"
                  type="button"
                  class="grading-page__chip"
                  :class="{ 'is-active': reviewForm.score === score }"
                  @click="applyScore(score)"
                >
                  {{ score }} 分
                </button>
              </div>
            </div>

            <div class="grading-page__quick-row">
              <span>快捷评语</span>
              <div class="grading-page__chip-row">
                <button
                  v-for="comment in quickComments"
                  :key="comment"
                  type="button"
                  class="grading-page__chip grading-page__chip--soft"
                  @click="applyComment(comment)"
                >
                  {{ comment }}
                </button>
              </div>
            </div>

            <label class="grading-page__field grading-page__field--full">
              <span>详细评语</span>
              <el-input
                v-model="reviewForm.commentText"
                type="textarea"
                :rows="7"
                maxlength="300"
                show-word-limit
                placeholder="写给学生的批改建议、提醒或鼓励。"
              />
            </label>

            <div class="grading-page__actions">
              <el-button plain @click="remindCurrentTask">提醒学生</el-button>
              <el-button type="primary" :loading="store.loading.action" @click="submitReviewAndNext">
                提交评分并看下一份
              </el-button>
            </div>
          </aside>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.grading-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.grading-page__summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.grading-page__summary-card,
.grading-page__queue,
.grading-page__tasks,
.grading-page__review {
  border: 1px solid #dde6f2;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 251, 255, 0.98) 100%);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.05);
}

.grading-page__summary-card {
  padding: 16px 18px;
  display: grid;
  gap: 6px;
  min-height: 132px;
}

.grading-page__summary-card span,
.grading-page__summary-card small,
.grading-page__eyebrow {
  color: #6d7d96;
}

.grading-page__summary-card strong {
  font-size: 34px;
  line-height: 1;
  color: #13253d;
}

.grading-page__queue {
  padding: 18px 18px 16px;
}

.grading-page__section-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
  margin-bottom: 14px;
}

.grading-page__section-head--compact {
  margin-bottom: 14px;
}

.grading-page__section-head h2,
.grading-page__section-head h3 {
  margin: 4px 0 0;
  color: #13253d;
}

.grading-page__section-head p {
  margin: 8px 0 0;
  color: #71829a;
  line-height: 1.6;
}

.grading-page__queue-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 220px));
  gap: 12px;
  justify-content: start;
}

.grading-page__queue-card {
  display: grid;
  gap: 10px;
  padding: 14px;
  width: 220px;
  min-height: 132px;
  border: 1px solid #dce5f3;
  border-radius: 18px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.grading-page__queue-card:hover,
.grading-page__queue-card.is-active {
  border-color: #5b84ff;
  box-shadow: 0 12px 22px rgba(59, 100, 223, 0.12);
  transform: translateY(-2px);
}

.grading-page__queue-head,
.grading-page__task-card-head,
.grading-page__task-card-foot,
.grading-page__actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.grading-page__queue-card p,
.grading-page__task-card p {
  margin: 0;
  color: #71829a;
  font-size: 13px;
}

.grading-page__queue-meta,
.grading-page__chip-row,
.grading-page__tools,
.grading-page__tabs,
.grading-page__asset-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.grading-page__queue-meta span,
.grading-page__tab,
.grading-page__chip,
.grading-page__asset {
  border-radius: 999px;
  font-size: 12px;
}

.grading-page__queue-meta span {
  padding: 5px 9px;
  background: #f2f6fc;
  color: #52637b;
}

.grading-page__workspace {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.grading-page__tasks,
.grading-page__review {
  padding: 20px;
}

.grading-page__task-list {
  display: grid;
  gap: 12px;
}

.grading-page__task-card {
  display: grid;
  gap: 12px;
  padding: 16px;
  border: 1px solid #dde6f2;
  border-radius: 20px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.grading-page__task-card:hover,
.grading-page__task-card.is-active {
  border-color: #5b84ff;
  box-shadow: 0 12px 22px rgba(59, 100, 223, 0.12);
  transform: translateY(-1px);
}

.grading-page__task-card strong,
.grading-page__queue-card strong {
  color: #10233b;
  font-size: 16px;
}

.grading-page__task-card-foot {
  font-size: 13px;
  color: #7b8da7;
}

.grading-page__tab {
  padding: 8px 13px;
  border: 1px solid #dbe5f2;
  background: #f8fbff;
  color: #4c5e76;
  cursor: pointer;
}

.grading-page__tab.is-active {
  border-color: #5b84ff;
  background: #eef4ff;
  color: #2352c8;
  font-weight: 700;
}

.grading-page__review-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 18px;
}

.grading-page__paper,
.grading-page__review-panel {
  display: grid;
  gap: 16px;
}

.grading-page__paper-card,
.grading-page__review-panel {
  border: 1px solid #e1e8f3;
  border-radius: 22px;
  background: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.grading-page__paper-card {
  padding: 18px;
}

.grading-page__paper-card--muted {
  background: #f8fbff;
}

.grading-page__paper-card p {
  margin: 10px 0 0;
  color: #223b58;
  line-height: 1.8;
  font-size: 15px;
}

.grading-page__asset-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.grading-page__asset {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border: 1px solid #dce6f4;
  border-radius: 18px;
  background: #f8fbff;
  color: #173252;
}

.grading-page__asset--image {
  grid-template-columns: 112px minmax(0, 1fr);
  align-items: center;
}

.grading-page__asset-preview {
  grid-row: span 3;
  width: 112px;
  height: 84px;
  overflow: hidden;
  border-radius: 14px;
  background: #eef5ff;
}

.grading-page__asset-preview :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.grading-page__asset span {
  color: #71829a;
}

.grading-page__asset a {
  color: #2563eb;
  font-size: 13px;
  text-decoration: none;
}

.grading-page__review-panel {
  padding: 18px;
}

.grading-page__field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.grading-page__field {
  display: grid;
  gap: 8px;
}

.grading-page__field--full {
  grid-column: 1 / -1;
}

.grading-page__field span,
.grading-page__quick-row > span {
  font-size: 13px;
  color: #61738d;
  font-weight: 700;
}

.grading-page__quick-row {
  display: grid;
  gap: 10px;
}

.grading-page__chip {
  padding: 8px 12px;
  border: 1px solid #dbe5f2;
  background: #fff;
  color: #42556d;
  cursor: pointer;
}

.grading-page__chip.is-active {
  border-color: #2f74ff;
  background: #2f74ff;
  color: #fff;
}

.grading-page__chip--soft {
  background: #f6f9fd;
}

.grading-page__empty {
  padding: 28px 20px;
  border: 1px dashed #d7e1ef;
  border-radius: 18px;
  color: #7a8da7;
  text-align: center;
}

.grading-page__actions {
  align-items: center;
}

@media (max-width: 1420px) {
  .grading-page__workspace,
  .grading-page__review-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .grading-page__summary,
  .grading-page__field-grid {
    grid-template-columns: 1fr;
  }
}
</style>
