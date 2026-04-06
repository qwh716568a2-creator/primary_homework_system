<script setup lang="ts">
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import {
  formatDateTime,
  formatFullDateTime,
  getLatestSubmission,
  getReviewTag,
  getSubmissionText
} from '@/utils/teacher-portal'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const homeworkId = computed(() => `${route.params.id ?? ''}`)
const tasks = computed(() => store.getHomeworkTasks(homeworkId.value))
const detail = computed(() => store.getHomeworkDetail(homeworkId.value))

const filters = reactive({
  classId: '',
  taskStatus: '',
  keyword: ''
})

const selectedTaskId = computed(() => {
  const value = route.query.task

  if (typeof value === 'string' && value) {
    return value
  }

  return tasks.value[0] ? `${tasks.value[0].taskId}` : ''
})

const selectedTask = computed(() =>
  tasks.value.find((item) => `${item.taskId}` === selectedTaskId.value)
)

const taskDetail = computed(() => store.getTaskDetail(selectedTaskId.value))
const latestSubmission = computed(() => getLatestSubmission(taskDetail.value))

const reviewForm = reactive({
  reviewStatus: 'completed' as 'completed' | 'revision_required',
  score: undefined as number | undefined,
  scoreLevel: '',
  commentText: ''
})

const neighbors = computed(() => {
  const index = tasks.value.findIndex((item) => `${item.taskId}` === selectedTaskId.value)

  if (index === -1) {
    return {
      previousId: '',
      nextId: ''
    }
  }

  return {
    previousId: tasks.value[index - 1] ? `${tasks.value[index - 1].taskId}` : '',
    nextId: tasks.value[index + 1] ? `${tasks.value[index + 1].taskId}` : ''
  }
})

function buildTaskQuery() {
  return {
    classId: filters.classId || undefined,
    taskStatus: filters.taskStatus || undefined,
    keyword: filters.keyword.trim() || undefined
  }
}

function chooseTask(taskId: number | string) {
  router.replace({
    query: {
      ...route.query,
      task: `${taskId}`
    }
  })
}

async function loadTaskList() {
  await store.loadHomeworkTasks(homeworkId.value, buildTaskQuery())
}

async function loadPage() {
  try {
    await Promise.all([
      store.loadHomeworkDetail(homeworkId.value),
      store.loadTeachingClasses(),
      loadTaskList()
    ])
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改中心加载失败')
  }
}

async function submitReview() {
  if (!selectedTask.value || !latestSubmission.value) {
    ElMessage.warning('当前任务还没有提交内容，暂时无法批改')
    return
  }

  try {
    await store.submitReview(selectedTask.value.taskId, {
      submissionId: latestSubmission.value.submissionId,
      reviewStatus: reviewForm.reviewStatus,
      score: reviewForm.score,
      scoreLevel: reviewForm.scoreLevel.trim() || undefined,
      commentText: reviewForm.commentText.trim() || undefined
    })
    await Promise.all([
      store.loadHomeworkTasks(homeworkId.value, buildTaskQuery()),
      store.loadHomeworkDetail(homeworkId.value)
    ])
    ElMessage.success(reviewForm.reviewStatus === 'completed' ? '批改已提交' : '已标记为待订正')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改提交失败')
  }
}

watch(
  selectedTaskId,
  async (taskId) => {
    if (!taskId) {
      return
    }

    try {
      await store.loadTaskDetail(taskId)
    } catch (error) {
      ElMessage.error(error instanceof Error ? error.message : '任务详情加载失败')
    }
  },
  { immediate: true }
)

watch(
  taskDetail,
  (value) => {
    const latestReview = value?.reviews.at(-1)
    const normalizedScore =
      typeof latestReview?.score === 'number'
        ? latestReview.score
        : latestReview?.score
          ? Number(latestReview.score)
          : undefined

    reviewForm.reviewStatus =
      latestReview?.reviewStatus === 'revision_required' ? 'revision_required' : 'completed'
    reviewForm.score = Number.isFinite(normalizedScore) ? normalizedScore : undefined
    reviewForm.scoreLevel = latestReview?.scoreLevel ?? ''
    reviewForm.commentText = latestReview?.commentText ?? ''
  },
  { immediate: true }
)

watch(
  tasks,
  (value) => {
    if (!value.length) {
      return
    }

    if (!value.some((item) => `${item.taskId}` === selectedTaskId.value)) {
      chooseTask(value[0].taskId)
    }
  },
  { immediate: true }
)

onMounted(loadPage)
</script>

<template>
  <section class="page-stack">
    <header class="page-header">
      <div>
        <h2>批改中心</h2>
        <p>{{ detail?.baseInfo.title || '当前作业' }} · 直接请求任务与提交详情接口完成批改。</p>
      </div>
      <div class="actions-row" style="margin-top: 0;">
        <el-select v-model="filters.classId" clearable placeholder="班级" style="min-width: 180px;">
          <el-option
            v-for="item in detail?.classList ?? []"
            :key="item.classId"
            :label="item.className"
            :value="`${item.classId}`"
          />
        </el-select>
        <el-select v-model="filters.taskStatus" clearable placeholder="任务状态" style="min-width: 180px;">
          <el-option label="未提交" value="pending" />
          <el-option label="已提交" value="submitted" />
          <el-option label="已完成" value="completed" />
          <el-option label="待订正" value="revision_required" />
          <el-option label="逾期" value="overdue" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索学生姓名"
          style="min-width: 220px;"
          @keyup.enter="loadTaskList"
        />
        <el-button type="primary" @click="loadTaskList">查询</el-button>
        <el-button @click="router.push(`/assignments/${homeworkId}`)">返回详情</el-button>
      </div>
    </header>

    <section class="student-review-shell">
      <aside class="section-card surface-card sticky-column">
        <h3>任务列表</h3>
        <p class="section-subtitle">共 {{ tasks.length }} 个学生任务。</p>

        <div v-if="tasks.length" class="student-list">
          <button
            v-for="item in tasks"
            :key="item.taskId"
            type="button"
            class="student-list-item"
            :class="{ 'student-list-item-active': `${item.taskId}` === selectedTaskId }"
            @click="chooseTask(item.taskId)"
          >
            <strong>{{ item.studentName }}</strong>
            <small>{{ item.className }}</small>
            <span>{{ item.latestSubmittedAt ? `最近提交 ${formatDateTime(item.latestSubmittedAt)}` : '暂无提交' }}</span>
          </button>
        </div>
        <div v-else class="empty-state">当前筛选条件下没有任务。</div>
      </aside>

      <div class="review-content">
        <article v-if="selectedTask" class="section-card surface-card">
          <div class="card-row-between">
            <div>
              <h3 style="margin: 0;">{{ selectedTask.studentName }}</h3>
              <p class="section-subtitle" style="margin-top: 0.35rem; margin-bottom: 0;">
                {{ selectedTask.className }} · 最近提交 {{ formatDateTime(selectedTask.latestSubmittedAt) }}
              </p>
            </div>
            <div class="chip-row">
              <StatusTag kind="submission" :value="selectedTask.taskStatus" />
              <StatusTag kind="review" :value="selectedTask.reviewStatus" />
            </div>
          </div>

          <div v-if="latestSubmission" class="split-grid" style="margin-top: 1rem;">
            <div class="section-card surface-card">
              <h3>最近一次提交</h3>
              <p class="section-subtitle">
                第 {{ latestSubmission.versionNo }} 次提交 · {{ formatFullDateTime(latestSubmission.submittedAt) }}
              </p>
              <p style="margin: 0; color: var(--muted);">{{ getSubmissionText(latestSubmission) }}</p>

              <div class="image-grid" v-if="latestSubmission.assets.length">
                <span
                  v-for="(item, index) in latestSubmission.assets"
                  :key="`${item.assetUrl}-${index}`"
                  class="image-pill"
                >
                  {{ item.assetName || item.assetUrl }}
                </span>
              </div>
            </div>

            <div class="section-card surface-card">
              <h3>批改操作</h3>
              <p class="section-subtitle">选择批改结果并填写评语，提交到正式批改接口。</p>
              <div class="form-stack">
                <el-radio-group v-model="reviewForm.reviewStatus">
                  <el-radio-button label="completed">已完成</el-radio-button>
                  <el-radio-button label="revision_required">待订正</el-radio-button>
                </el-radio-group>
                <div class="split-grid">
                  <el-input-number v-model="reviewForm.score" :min="0" :max="100" controls-position="right" />
                  <el-input v-model="reviewForm.scoreLevel" placeholder="等级，例如 A / B+" />
                </div>
                <el-input
                  v-model="reviewForm.commentText"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入批改评语"
                />
                <div class="actions-row">
                  <el-button type="primary" :loading="store.loading.action" @click="submitReview">提交批改</el-button>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-state" style="margin-top: 1rem;">当前任务暂无提交内容。</div>
        </article>

        <article v-if="taskDetail" class="section-card surface-card">
          <h3>历史提交记录</h3>
          <p class="section-subtitle">支持查看学生历次提交结果和已产生的批改记录。</p>

          <div v-if="taskDetail.submissions.length" class="submission-history">
            <div
              v-for="submission in taskDetail.submissions"
              :key="submission.submissionId"
              class="submission-card"
            >
              <strong>{{ formatFullDateTime(submission.submittedAt) }} · 第 {{ submission.versionNo }} 次</strong>
              <p>{{ getSubmissionText(submission) }}</p>
              <div class="image-grid" v-if="submission.assets.length">
                <span
                  v-for="(item, index) in submission.assets"
                  :key="`${item.assetUrl}-${index}`"
                  class="image-pill"
                >
                  {{ item.assetName || item.assetUrl }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">暂无历史提交记录。</div>
        </article>

        <article v-if="taskDetail" class="section-card surface-card">
          <h3>历史批改记录</h3>
          <p class="section-subtitle">如果同一任务被多次批改，这里会按时间顺序展示。</p>

          <div v-if="taskDetail.reviews.length" class="panel-list">
            <div v-for="(item, index) in taskDetail.reviews" :key="`${item.reviewId || index}`" class="panel-list-item">
              <strong>{{ getReviewTag(item) }}</strong>
              <p>
                {{ item.reviewedAt ? formatFullDateTime(item.reviewedAt) : '暂无批改时间' }}
                <span v-if="item.score !== undefined"> · 分数 {{ item.score }}</span>
                <span v-if="item.scoreLevel"> · 等级 {{ item.scoreLevel }}</span>
              </p>
            </div>
          </div>
          <div v-else class="empty-state">暂无批改记录。</div>
        </article>

        <article v-if="selectedTask" class="section-card surface-card">
          <h3>快速切换</h3>
          <p class="section-subtitle">按当前筛选结果在上一位与下一位学生之间切换。</p>
          <div class="actions-row">
            <el-button :disabled="!neighbors.previousId" @click="chooseTask(neighbors.previousId)">上一位</el-button>
            <el-button :disabled="!neighbors.nextId" @click="chooseTask(neighbors.nextId)">下一位</el-button>
          </div>
        </article>
      </div>
    </section>
  </section>
</template>
