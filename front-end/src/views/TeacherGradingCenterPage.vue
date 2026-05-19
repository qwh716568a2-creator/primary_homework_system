<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { uploadTeacherFile } from '@/api/teacher'
import { useTeacherPortalStore } from '@/stores/teacherPortalApi'
import { isImageAttachmentLike, resolveAttachmentUrl } from '@/utils/attachment-url'
import type {
  HomeworkAsset,
  HomeworkSubmission,
  TeacherWrongBookItemInput
} from '@/types/teacher-portal'

type ReviewDecision = 'completed' | 'revision_required'

const route = useRoute()
const router = useRouter()
const store = useTeacherPortalStore()

const selectedHomeworkId = ref('')
const selectedTaskId = ref('')
const selectedSubmissionId = ref('')
const activeTaskTab = ref<'pending' | 'reviewed' | 'missing'>('pending')
const reviewAttachmentInput = ref('')
const reviewAssets = ref<HomeworkAsset[]>([])
const wrongItems = ref<TeacherWrongBookItemInput[]>([])
const reviewAssetFileInput = ref<HTMLInputElement | null>(null)
const wrongItemFileInput = ref<HTMLInputElement | null>(null)
const reviewAssetUploading = ref(false)
const wrongItemAssetUploading = ref(false)
const scoreHydratingTaskIds = new Set<string>()
const activeTool = ref<'pen' | 'text' | 'symbol'>('pen')
const zoomLevel = ref(100)

const filters = reactive({
  keyword: '',
  classId: '' as string | number,
  taskStatus: '',
  reviewStatus: ''
})

const reviewForm = reactive({
  reviewStatus: 'completed' as ReviewDecision,
  score: 90,
  scoreLevel: 'A',
  commentText: ''
})

const reviewTemplates = [
  '字迹工整',
  '思路清晰',
  '粗心大意',
  '格式规范',
  '公式用错了'
]

const scorePresets = [100, 95, 90, 85, 80, 70]

const wrongReasonOptions = [
  { value: 'calc_error', label: '计算错误' },
  { value: 'concept_error', label: '概念不清' },
  { value: 'reading_error', label: '审题偏差' },
  { value: 'writing_error', label: '书写问题' },
  { value: 'careless_error', label: '粗心出错' },
  { value: 'other', label: '其他原因' }
]

const symbolOptions = ['√', '×', '？', '△', '☆', '→']

const gradingDraftStorageKey = computed(
  () =>
    `teacher-grading-draft:${selectedHomeworkId.value || 'none'}:${selectedTaskId.value || 'none'}:${
      selectedSubmissionId.value || 'none'
    }`
)

const taskStatusMap = {
  pending: { label: '未提交', tone: 'slate' },
  submitted: { label: '待批改', tone: 'sky' },
  revision_required: { label: '待订正', tone: 'rose' },
  completed: { label: '已完成', tone: 'teal' },
  overdue: { label: '已逾期', tone: 'amber' }
} as const

const reviewStatusMap = {
  unreviewed: { label: '未批改', tone: 'slate' },
  completed: { label: '已批改', tone: 'teal' },
  revision_required: { label: '待订正', tone: 'rose' }
} as const

function createEmptyWrongItem(): TeacherWrongBookItemInput {
  return {
    questionNo: '',
    questionText: '',
    studentAnswer: '',
    correctAnswer: '',
    analysisText: '',
    wrongReasonCode: 'calc_error',
    assets: []
  }
}

const wrongItemDraft = reactive<TeacherWrongBookItemInput>(createEmptyWrongItem())

const routeHomeworkId = computed(() => `${route.params.id ?? ''}`)

const currentHomework = computed(() =>
  store.homeworks.find((item) => `${item.homeworkId}` === selectedHomeworkId.value)
)

const currentHomeworkDetail = computed(() => store.getHomeworkDetail(selectedHomeworkId.value))
const homeworkTasks = computed(() => store.getHomeworkTasks(selectedHomeworkId.value))
const currentTaskDetail = computed(() => store.getTaskDetail(selectedTaskId.value))

const gradingAssignments = computed(() =>
  [...store.homeworks]
    .filter((item) => item.status === 'published' || item.pendingCount > 0 || item.revisionRequiredCount > 0)
    .sort(
      (left, right) =>
        right.pendingCount + right.revisionRequiredCount - (left.pendingCount + left.revisionRequiredCount)
    )
)

const taskClassOptions = computed(() => {
  const classMap = new Map<string, string>()

  homeworkTasks.value.forEach((item) => {
    if (!classMap.has(`${item.classId}`)) {
      classMap.set(`${item.classId}`, item.className)
    }
  })

  return Array.from(classMap.entries()).map(([value, label]) => ({ value, label }))
})

const filteredTasks = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()

  return homeworkTasks.value.filter((item) => {
    if (filters.classId && `${item.classId}` !== `${filters.classId}`) {
      return false
    }

    if (filters.taskStatus && item.taskStatus !== filters.taskStatus) {
      return false
    }

    if (filters.reviewStatus && item.reviewStatus !== filters.reviewStatus) {
      return false
    }

    if (keyword) {
      const text = `${item.studentName} ${item.className}`.toLowerCase()
      if (!text.includes(keyword)) {
        return false
      }
    }

    return true
  })
})

const pendingQueueTasks = computed(() =>
  filteredTasks.value.filter((item) => item.taskStatus === 'submitted' || item.reviewStatus === 'unreviewed')
)

const reviewedQueueTasks = computed(() =>
  filteredTasks.value.filter((item) => item.reviewStatus === 'completed' || item.reviewStatus === 'revision_required')
)

const missingQueueTasks = computed(() => filteredTasks.value.filter((item) => item.taskStatus === 'pending'))

const visibleTasks = computed(() => {
  if (activeTaskTab.value === 'reviewed') return reviewedQueueTasks.value
  if (activeTaskTab.value === 'missing') return missingQueueTasks.value
  return pendingQueueTasks.value
})

const taskStats = computed(() => ({
  total: homeworkTasks.value.length,
  unreviewed: homeworkTasks.value.filter((item) => item.reviewStatus === 'unreviewed').length,
  revisionRequired: homeworkTasks.value.filter((item) => item.reviewStatus === 'revision_required').length,
  completed: homeworkTasks.value.filter((item) => item.reviewStatus === 'completed').length
}))

const currentTaskIndex = computed(() =>
  visibleTasks.value.findIndex((item) => `${item.taskId}` === selectedTaskId.value)
)

const selectedSubmission = computed(() => {
  const submissions = currentTaskDetail.value?.submissions ?? []

  if (!submissions.length) {
    return undefined
  }

  if (!selectedSubmissionId.value) {
    return submissions.at(-1)
  }

  return submissions.find((item) => `${item.submissionId}` === selectedSubmissionId.value) ?? submissions.at(-1)
})

const selectedSubmissionImageAssets = computed(() =>
  (selectedSubmission.value?.assets ?? []).filter((asset) => isImageAttachmentLike(asset))
)

const selectedSubmissionFileAssets = computed(() =>
  (selectedSubmission.value?.assets ?? []).filter((asset) => !isImageAttachmentLike(asset))
)

const selectedSubmissionImageUrls = computed(() =>
  selectedSubmissionImageAssets.value.map(resolveAttachmentUrl).filter(Boolean)
)

function getSubmissionImageIndex(asset: HomeworkAsset) {
  return Math.max(0, selectedSubmissionImageUrls.value.findIndex((url) => url === resolveAttachmentUrl(asset)))
}

function statusText(value?: string) {
  return value ? taskStatusMap[value as keyof typeof taskStatusMap]?.label ?? value : '未知'
}

function reviewText(value?: string) {
  return value ? reviewStatusMap[value as keyof typeof reviewStatusMap]?.label ?? value : '未知'
}

function toneClass(value?: string, type: 'task' | 'review' = 'task') {
  const map = type === 'task' ? taskStatusMap : reviewStatusMap
  return `tone-${map[value as keyof typeof map]?.tone ?? 'slate'}`
}

function formatDateTime(input?: string) {
  if (!input) {
    return '暂无'
  }

  const normalized = input.includes('T') ? input : input.replace(' ', 'T')
  const date = new Date(normalized)

  if (Number.isNaN(date.getTime())) {
    return '暂无'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

function formatFullDateTime(input?: string) {
  if (!input) {
    return '暂无'
  }

  const normalized = input.includes('T') ? input : input.replace(' ', 'T')
  const date = new Date(normalized)

  if (Number.isNaN(date.getTime())) {
    return '暂无'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

function relativeDeadline(input?: string) {
  if (!input) {
    return '未设置截止时间'
  }

  const normalized = input.includes('T') ? input : input.replace(' ', 'T')
  const date = new Date(normalized)

  if (Number.isNaN(date.getTime())) {
    return '未设置截止时间'
  }

  const diff = date.getTime() - Date.now()
  const day = 24 * 60 * 60 * 1000
  const hour = 60 * 60 * 1000

  if (diff <= 0) return '已到截止时间'
  if (diff >= day) return `${Math.ceil(diff / day)} 天后截止`
  return `${Math.ceil(diff / hour)} 小时后截止`
}

function latestSubmissionOf(detail = currentTaskDetail.value) {
  return detail?.submissions?.at(-1)
}

function submissionText(submission?: HomeworkSubmission) {
  return submission?.submitText?.trim() || '学生没有填写文字说明，可结合附件继续批改。'
}

function reviewSummary() {
  const latest = currentTaskDetail.value?.reviews?.at(-1)

  if (!latest) return '还没有历史批改记录。'
  if (latest.commentText?.trim()) return latest.commentText.trim()
  if (latest.scoreLevel) return `最近一次批改等级：${latest.scoreLevel}`
  if (latest.score !== undefined && latest.score !== null) return `最近一次批改分数：${latest.score}`
  return '最近一次批改已保存。'
}

function latestReviewOfTask(taskId?: number | string) {
  return store.getTaskDetail(taskId)?.reviews?.at(-1)
}

function hasReviewScore(taskId?: number | string) {
  const latest = latestReviewOfTask(taskId)
  return latest?.score !== undefined && latest.score !== null && latest.score !== ''
}

function reviewScoreText(taskId?: number | string) {
  const latest = latestReviewOfTask(taskId)

  if (!latest) return ''

  const score = latest.score !== undefined && latest.score !== null && latest.score !== '' ? `${latest.score}分` : ''
  const level = latest.scoreLevel ? `${latest.scoreLevel}` : ''

  if (score && level) return `${score} · ${level}`
  return score || level
}

function toScoreLevel(score: number) {
  if (score >= 90) return 'A'
  if (score >= 80) return 'B'
  if (score >= 70) return 'C'
  if (score >= 60) return 'D'
  return 'E'
}

function isImageAsset(asset?: HomeworkAsset) {
  return asset?.assetType === 'image'
}

function cloneWrongItem(item?: TeacherWrongBookItemInput): TeacherWrongBookItemInput {
  return {
    questionNo: item?.questionNo ?? '',
    questionText: item?.questionText ?? '',
    studentAnswer: item?.studentAnswer ?? '',
    correctAnswer: item?.correctAnswer ?? '',
    analysisText: item?.analysisText ?? '',
    wrongReasonCode: item?.wrongReasonCode ?? 'calc_error',
    assets: item?.assets ? [...item.assets] : []
  }
}

function resetWrongItemDraft() {
  Object.assign(wrongItemDraft, createEmptyWrongItem())
}

function addReviewAsset() {
  if (!reviewAttachmentInput.value.trim()) return

  reviewAssets.value.push({
    assetType: 'image',
    assetUrl: reviewAttachmentInput.value.trim(),
    assetName: `批注图片 ${reviewAssets.value.length + 1}`
  })
  reviewAttachmentInput.value = ''
}

function removeReviewAsset(index: number) {
  reviewAssets.value.splice(index, 1)
}

function triggerReviewAssetUpload() {
  reviewAssetFileInput.value?.click()
}

async function handleReviewAssetUpload(event: Event) {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]

  if (!file) {
    return
  }

  reviewAssetUploading.value = true

  try {
    const uploaded = await uploadTeacherFile(file, 'review_asset')

    reviewAssets.value = [
      ...reviewAssets.value,
      {
        assetType: 'image',
        assetUrl: uploaded.fileUrl,
        assetName: uploaded.fileName || `批注图片 ${reviewAssets.value.length + 1}`,
        assetSize: uploaded.fileSize
      }
    ]

    ElMessage.success('批注图片上传成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批注图片上传失败')
  } finally {
    reviewAssetUploading.value = false

    if (input) {
      input.value = ''
    }
  }
}

function triggerWrongItemAssetUpload() {
  wrongItemFileInput.value?.click()
}

async function handleWrongItemAssetUpload(event: Event) {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]

  if (!file) {
    return
  }

  wrongItemAssetUploading.value = true

  try {
    const uploaded = await uploadTeacherFile(file, 'wrong_book_image')

    wrongItemDraft.assets = [
      ...(wrongItemDraft.assets ?? []),
      {
        assetType: 'image',
        assetUrl: uploaded.fileUrl,
        assetName: uploaded.fileName || `错题截图 ${(wrongItemDraft.assets?.length ?? 0) + 1}`,
        assetSize: uploaded.fileSize
      }
    ]

    ElMessage.success('截图上传成功')
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '截图上传失败')
  } finally {
    wrongItemAssetUploading.value = false

    if (input) {
      input.value = ''
    }
  }
}

function removeWrongItemAsset(index: number) {
  wrongItemDraft.assets = (wrongItemDraft.assets ?? []).filter((_, currentIndex) => currentIndex !== index)
}

function prefillWrongItemFromSubmission() {
  if (!selectedSubmission.value) return

  if (!wrongItemDraft.studentAnswer?.trim()) {
    wrongItemDraft.studentAnswer = submissionText(selectedSubmission.value)
  }

  if (!(wrongItemDraft.assets?.length ?? 0)) {
    wrongItemDraft.assets = selectedSubmission.value.assets
      .filter((asset) => asset.assetType === 'image')
      .slice(0, 2)
      .map((asset) => ({ ...asset }))
  }
}

function addWrongItem() {
  if (!wrongItemDraft.questionText?.trim() && !wrongItemDraft.studentAnswer?.trim()) {
    ElMessage.warning('请至少填写题目内容或学生答案后，再加入错题本。')
    return
  }

  wrongItems.value.push(cloneWrongItem(wrongItemDraft))
  resetWrongItemDraft()
}

function removeWrongItem(index: number) {
  wrongItems.value.splice(index, 1)
}

function fillReviewForm() {
  const latestReview = currentTaskDetail.value?.reviews?.at(-1)

  if (!latestReview) {
    reviewForm.reviewStatus = 'completed'
    reviewForm.score = 90
    reviewForm.scoreLevel = 'A'
    reviewForm.commentText = ''
    reviewAssets.value = []
    wrongItems.value = []
    reviewAttachmentInput.value = ''
    resetWrongItemDraft()
    restoreReviewDraft()
    return
  }

  reviewForm.reviewStatus =
    latestReview.reviewStatus === 'revision_required' ? 'revision_required' : 'completed'
  reviewForm.score = Number(latestReview.score ?? 90)
  reviewForm.scoreLevel = `${latestReview.scoreLevel ?? toScoreLevel(Number(latestReview.score ?? 90))}`
  reviewForm.commentText = latestReview.commentText ?? ''
  reviewAssets.value = [...(latestReview.reviewAssets ?? [])]
  wrongItems.value = (latestReview.wrongItems ?? []).map((item) => cloneWrongItem(item))
  reviewAttachmentInput.value = ''
  resetWrongItemDraft()
  restoreReviewDraft()
}

async function selectTask(taskId: number | string) {
  selectedTaskId.value = `${taskId}`

  try {
    const detail = await store.loadTaskDetail(taskId)
    selectedSubmissionId.value = `${latestSubmissionOf(detail)?.submissionId ?? ''}`
    fillReviewForm()
    await router.replace({
      path: selectedHomeworkId.value ? `/assignments/${selectedHomeworkId.value}/grading` : '/grading-center',
      query: selectedTaskId.value ? { task: selectedTaskId.value } : {}
    })
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改详情加载失败')
  }
}

async function hydrateReviewedScores() {
  const tasks = reviewedQueueTasks.value.filter((item) => {
    const taskId = `${item.taskId}`
    return !store.getTaskDetail(taskId) && !scoreHydratingTaskIds.has(taskId)
  })

  await Promise.allSettled(
    tasks.map(async (item) => {
      const taskId = `${item.taskId}`
      scoreHydratingTaskIds.add(taskId)

      try {
        await store.loadTaskDetail(taskId)
      } finally {
        scoreHydratingTaskIds.delete(taskId)
      }
    })
  )
}

async function openHomework(homeworkId: number | string, taskId?: number | string) {
  selectedHomeworkId.value = `${homeworkId}`
  filters.classId = ''
  filters.keyword = ''
  filters.taskStatus = ''
  filters.reviewStatus = ''

  try {
    await Promise.all([store.loadHomeworkDetail(homeworkId), store.loadHomeworkTasks(homeworkId)])

    if (routeHomeworkId.value !== `${homeworkId}`) {
      await router.replace(`/assignments/${homeworkId}/grading`)
    }

    const nextTaskId =
      taskId ||
      `${route.query.task ?? ''}` ||
      homeworkTasks.value.find((item) => item.reviewStatus === 'unreviewed')?.taskId ||
      homeworkTasks.value[0]?.taskId

    if (nextTaskId) {
      await selectTask(nextTaskId)
    } else {
      selectedTaskId.value = ''
      selectedSubmissionId.value = ''
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改工作台加载失败')
  }
}

async function loadPage() {
  try {
    await Promise.all([store.loadTeachingClasses(), store.loadHomeworkList()])
    const initialHomeworkId = routeHomeworkId.value || `${gradingAssignments.value[0]?.homeworkId ?? ''}`

    if (initialHomeworkId) {
      await openHomework(initialHomeworkId, `${route.query.task ?? ''}`)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '批改中心加载失败')
  }
}

function applyTemplate(template: string) {
  if (reviewForm.commentText.includes(template)) {
    return
  }

  reviewForm.commentText = reviewForm.commentText ? `${reviewForm.commentText} ${template}` : template
}

function applyScore(score: number) {
  reviewForm.score = score
  reviewForm.scoreLevel = toScoreLevel(score)
}

function insertCommonSymbol(symbol: string) {
  reviewForm.commentText = `${reviewForm.commentText}${symbol}`
}

function adjustZoom(direction: 'in' | 'out') {
  const next = direction === 'in' ? zoomLevel.value + 10 : zoomLevel.value - 10
  zoomLevel.value = Math.max(80, Math.min(140, next))
}

function saveReviewDraft() {
  if (!selectedTaskId.value) {
    ElMessage.warning('请先选择一位学生再保存草稿。')
    return
  }

  const payload = {
    reviewStatus: reviewForm.reviewStatus,
    score: reviewForm.score,
    scoreLevel: reviewForm.scoreLevel,
    commentText: reviewForm.commentText,
    reviewAssets: reviewAssets.value,
    wrongItems: wrongItems.value,
    wrongItemDraft: cloneWrongItem(wrongItemDraft),
    activeTool: activeTool.value,
    zoomLevel: zoomLevel.value
  }

  window.localStorage.setItem(gradingDraftStorageKey.value, JSON.stringify(payload))
  ElMessage.success('批改草稿已保存')
}

function restoreReviewDraft() {
  if (!selectedTaskId.value) {
    return
  }

  const raw = window.localStorage.getItem(gradingDraftStorageKey.value)

  if (!raw) {
    return
  }

  try {
    const payload = JSON.parse(raw) as {
      reviewStatus?: ReviewDecision
      score?: number
      scoreLevel?: string
      commentText?: string
      reviewAssets?: HomeworkAsset[]
      wrongItems?: TeacherWrongBookItemInput[]
      wrongItemDraft?: TeacherWrongBookItemInput
      activeTool?: 'pen' | 'text' | 'symbol'
      zoomLevel?: number
    }

    reviewForm.reviewStatus = payload.reviewStatus ?? reviewForm.reviewStatus
    reviewForm.score = Number(payload.score ?? reviewForm.score)
    reviewForm.scoreLevel = payload.scoreLevel ?? reviewForm.scoreLevel
    reviewForm.commentText = payload.commentText ?? reviewForm.commentText
    reviewAssets.value = [...(payload.reviewAssets ?? reviewAssets.value)]
    wrongItems.value = (payload.wrongItems ?? wrongItems.value).map((item) => cloneWrongItem(item))
    Object.assign(wrongItemDraft, cloneWrongItem(payload.wrongItemDraft))
    activeTool.value = payload.activeTool ?? activeTool.value
    zoomLevel.value = payload.zoomLevel ?? zoomLevel.value
  } catch {
    window.localStorage.removeItem(gradingDraftStorageKey.value)
  }
}

function clearReviewDraft() {
  if (selectedTaskId.value) {
    window.localStorage.removeItem(gradingDraftStorageKey.value)
  }
}

async function moveTask(step: number) {
  const target = visibleTasks.value[currentTaskIndex.value + step]
  if (!target) return
  await selectTask(target.taskId)
}

async function submitReview() {
  if (!selectedTaskId.value || !selectedSubmission.value) {
    ElMessage.warning('请先选择一条有提交记录的学生任务。')
    return false
  }

  try {
    const wrongItemCount = wrongItems.value.length

    await store.submitReview(selectedTaskId.value, {
      submissionId: selectedSubmission.value.submissionId,
      reviewStatus: reviewForm.reviewStatus,
      score: reviewForm.score,
      scoreLevel: reviewForm.scoreLevel.trim() || undefined,
      commentText: reviewForm.commentText.trim() || undefined,
      reviewAssets: reviewAssets.value.length ? [...reviewAssets.value] : undefined,
      wrongItems: wrongItemCount ? wrongItems.value.map((item) => cloneWrongItem(item)) : undefined
    })

    await Promise.all([
      store.loadHomeworkTasks(selectedHomeworkId.value),
      store.loadHomeworkDetail(selectedHomeworkId.value),
      store.loadTaskDetail(selectedTaskId.value)
    ])

    fillReviewForm()
    clearReviewDraft()
    ElMessage.success(
      wrongItemCount ? `批改结果已提交，并记录 ${wrongItemCount} 条错题。` : '批改结果已提交。'
    )
    return true
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '提交批改失败')
    return false
  }
}

async function submitReviewAndMoveNext() {
  const success = await submitReview()

  if (!success) {
    return
  }

  const nextTask = visibleTasks.value[currentTaskIndex.value + 1]
  if (nextTask) {
    await selectTask(nextTask.taskId)
  }
}

watch(
  () => route.params.id,
  async (value) => {
    const nextHomeworkId = `${value ?? ''}`
    if (nextHomeworkId && nextHomeworkId !== selectedHomeworkId.value) {
      await openHomework(nextHomeworkId, `${route.query.task ?? ''}`)
    }
  }
)

watch(
  () => route.query.task,
  async (value) => {
    const nextTaskId = `${value ?? ''}`
    if (nextTaskId && nextTaskId !== selectedTaskId.value) {
      await selectTask(nextTaskId)
    }
  }
)

watch(
  () => reviewForm.score,
  (value) => {
    reviewForm.scoreLevel = toScoreLevel(Number(value || 0))
  }
)

watch(
  () => activeTaskTab.value,
  async () => {
    if (activeTaskTab.value === 'reviewed') {
      void hydrateReviewedScores()
    }

    if (!visibleTasks.value.length) {
      selectedTaskId.value = ''
      selectedSubmissionId.value = ''
      return
    }

    if (!visibleTasks.value.some((item) => `${item.taskId}` === selectedTaskId.value)) {
      await selectTask(visibleTasks.value[0].taskId)
    }
  }
)

onMounted(loadPage)
</script>

<template>
  <div class="prototype-grade-page">
    <div class="prototype-grade-layout">
      <aside class="prototype-left-pane">
        <div class="prototype-left-pane__summary">
          <h2>{{ currentHomework?.title || '《待处理作业》' }}</h2>
          <p>
            {{ currentHomework?.classNames.join('、') || '待选择班级' }}
            <span v-if="homeworkTasks.length"> | 共 {{ homeworkTasks.length }} 份</span>
          </p>
        </div>

        <div class="prototype-search">
          <el-input v-model="filters.keyword" placeholder="搜索学生姓名..." clearable />
        </div>

        <div class="prototype-tabs">
          <button
            type="button"
            class="prototype-tab"
            :class="{ 'is-active': activeTaskTab === 'pending' }"
            @click="activeTaskTab = 'pending'"
          >
            待批改 ({{ pendingQueueTasks.length }})
          </button>
          <button
            type="button"
            class="prototype-tab"
            :class="{ 'is-active': activeTaskTab === 'reviewed' }"
            @click="activeTaskTab = 'reviewed'"
          >
            已批改 ({{ reviewedQueueTasks.length }})
          </button>
          <button
            type="button"
            class="prototype-tab"
            :class="{ 'is-active': activeTaskTab === 'missing' }"
            @click="activeTaskTab = 'missing'"
          >
            未提交 ({{ missingQueueTasks.length }})
          </button>
        </div>

        <div class="prototype-student-list">
          <button
            v-for="item in visibleTasks"
            :key="item.taskId"
            type="button"
            class="prototype-student-item"
            :class="{ 'is-active': `${item.taskId}` === selectedTaskId }"
            @click="selectTask(item.taskId)"
          >
            <div class="prototype-student-item__main">
              <div class="prototype-status-dot" :class="toneClass(item.taskStatus)"></div>
              <div>
                <strong>{{ item.studentName }}</strong>
                <p>{{ item.latestSubmittedAt ? formatDateTime(item.latestSubmittedAt) : '暂无提交记录' }}</p>
              </div>
            </div>

            <div v-if="hasReviewScore(item.taskId)" class="prototype-student-review-score">
              <strong>{{ reviewScoreText(item.taskId) }}</strong>
              <span>批改分</span>
            </div>

            <span class="prototype-student-score" :class="{ 'is-working': `${item.taskId}` === selectedTaskId }">
              {{
                `${item.taskId}` === selectedTaskId
                  ? '批改中'
                  : item.reviewStatus === 'completed'
                    ? '已批改'
                    : item.taskStatus === 'pending'
                      ? '未提交'
                      : '待处理'
              }}
            </span>
          </button>

          <div v-if="!visibleTasks.length" class="prototype-empty-side">
            当前列表没有学生任务
          </div>
        </div>
      </aside>

      <section class="prototype-main-pane">
        <header class="prototype-main-header">
          <div>
            <h1>{{ currentTaskDetail?.taskInfo.studentName || '学生' }}的作业</h1>
            <p>
              耗时: {{ selectedSubmission ? `${Math.max(8, (selectedSubmission.versionNo || 1) * 12)}分钟` : '--' }}
              <span> | </span>
              查重率: 0%
            </p>
          </div>

          <button type="button" class="prototype-reload-btn" @click="router.push('/assignments')">返回重做</button>
        </header>

        <div class="prototype-main-body">
          <div class="prototype-paper-column">
            <div class="prototype-tool-bar">
              <div class="prototype-tool-group">
                <button
                  type="button"
                  class="prototype-tool-btn"
                  :class="{ 'is-active': activeTool === 'pen' }"
                  @click="activeTool = 'pen'"
                >
                  红笔
                </button>
                <button
                  type="button"
                  class="prototype-tool-btn"
                  :class="{ 'is-active': activeTool === 'text' }"
                  @click="activeTool = 'text'"
                >
                  插入文字
                </button>
                <el-popover placement="bottom-start" :width="220" trigger="click">
                  <template #reference>
                    <button
                      type="button"
                      class="prototype-tool-btn"
                      :class="{ 'is-active': activeTool === 'symbol' }"
                      @click="activeTool = 'symbol'"
                    >
                      常见符号
                    </button>
                  </template>

                  <div class="prototype-symbol-grid">
                    <button
                      v-for="symbol in symbolOptions"
                      :key="symbol"
                      type="button"
                      class="prototype-symbol-btn"
                      @click="insertCommonSymbol(symbol)"
                    >
                      {{ symbol }}
                    </button>
                  </div>
                </el-popover>
              </div>
              <div class="prototype-zoom-controls">
                <button type="button" class="prototype-tool-btn" @click="adjustZoom('out')">缩小</button>
                <span>{{ zoomLevel }}%</span>
                <button type="button" class="prototype-tool-btn" @click="adjustZoom('in')">放大</button>
              </div>
            </div>

            <div class="prototype-paper-stage">
              <div class="prototype-paper-sheet" :style="{ transform: `scale(${zoomLevel / 100})` }">
                <div class="prototype-paper-sheet__title">
                  {{ currentHomework?.title || '分数乘法练习题' }}
                </div>

                <div class="prototype-paper-meta">
                  <span>{{ currentTaskDetail?.taskInfo.studentName || '当前学生' }}</span>
                  <span>{{ currentHomework?.classNames.join('、') || '当前班级' }}</span>
                  <span>{{ selectedSubmission ? formatFullDateTime(selectedSubmission.submittedAt) : '暂无提交时间' }}</span>
                </div>

                <div class="prototype-paper-sheet__content" :class="{ 'is-empty': !selectedSubmission }">
                  <p>{{ submissionText(selectedSubmission) }}</p>
                </div>

                <div v-if="selectedSubmissionImageAssets.length" class="prototype-paper-images">
                  <el-image
                    v-for="asset in selectedSubmissionImageAssets"
                    :key="resolveAttachmentUrl(asset)"
                    class="prototype-paper-image"
                    :src="resolveAttachmentUrl(asset)"
                    :alt="asset.assetName || '提交图片'"
                    :preview-src-list="selectedSubmissionImageUrls"
                    :initial-index="getSubmissionImageIndex(asset)"
                    fit="cover"
                    preview-teleported
                    hide-on-click-modal
                  />
                </div>

                <div v-if="selectedSubmissionFileAssets.length" class="prototype-file-list">
                  <a
                    v-for="asset in selectedSubmissionFileAssets"
                    :key="resolveAttachmentUrl(asset)"
                    :href="resolveAttachmentUrl(asset)"
                    :download="asset.assetName || '附件'"
                    target="_blank"
                    rel="noreferrer"
                    class="prototype-file-item"
                  >
                    {{ asset.assetName || '查看附件' }}
                  </a>
                </div>

                <div v-if="activeTool === 'pen'" class="prototype-annotation prototype-annotation--circle">?</div>
                <div v-if="activeTool === 'text'" class="prototype-annotation prototype-annotation--text">
                  请关注关键步骤与单位书写
                </div>
              </div>
            </div>
          </div>

          <aside class="prototype-score-pane">
            <section class="prototype-score-block">
              <h3>综合评分</h3>
              <div class="prototype-score-inline">
                <el-input-number
                  v-model="reviewForm.score"
                  :min="0"
                  :max="100"
                  :step="1"
                  class="prototype-score-input"
                  controls-position="right"
                />
                <span>/ 100</span>
              </div>
            </section>

            <section class="prototype-score-block">
              <h3>快捷评语</h3>
              <div class="prototype-quick-tags">
                <button
                  v-for="template in reviewTemplates"
                  :key="template"
                  type="button"
                  class="prototype-quick-tag"
                  @click="applyTemplate(template)"
                >
                  {{ template }}
                </button>
              </div>
            </section>

            <section class="prototype-score-block">
              <h3>详细评语</h3>
              <el-input
                v-model="reviewForm.commentText"
                type="textarea"
                :rows="6"
                resize="none"
                placeholder="写下本次批改意见"
              />
            </section>

            <section class="prototype-score-block">
              <div class="prototype-score-grid">
                <div>
                  <label>批改结论</label>
                  <el-radio-group v-model="reviewForm.reviewStatus" class="prototype-radio-group">
                    <el-radio-button label="completed">完成</el-radio-button>
                    <el-radio-button label="revision_required">待订正</el-radio-button>
                  </el-radio-group>
                </div>
                <div>
                  <label>等级</label>
                  <el-input v-model="reviewForm.scoreLevel" placeholder="A / B / C" />
                </div>
              </div>
            </section>

            <section class="prototype-score-block">
              <h3>批注图片</h3>
              <div class="prototype-upload-actions">
                <input
                  ref="reviewAssetFileInput"
                  type="file"
                  accept="image/*"
                  class="prototype-hidden-input"
                  @change="handleReviewAssetUpload"
                />
                <el-button :loading="reviewAssetUploading" @click="triggerReviewAssetUpload">
                  选择批注图片
                </el-button>
                <el-input v-model="reviewAttachmentInput" placeholder="也可粘贴批注图片链接" />
                <el-button plain @click="addReviewAsset">加入链接</el-button>
              </div>
              <div v-if="reviewAssets.length" class="prototype-link-list">
                <button
                  v-for="(asset, index) in reviewAssets"
                  :key="`${asset.assetUrl}-${index}`"
                  type="button"
                  class="prototype-link-chip"
                  @click="removeReviewAsset(index)"
                >
                  {{ asset.assetName || `批注图 ${index + 1}` }} · 删除
                </button>
              </div>
            </section>

            <section class="prototype-score-block">
              <div class="prototype-score-head">
                <h3>错题本</h3>
                <button type="button" class="prototype-text-btn" @click="prefillWrongItemFromSubmission">
                  从提交带入
                </button>
              </div>

              <div class="prototype-score-grid">
                <el-input v-model="wrongItemDraft.questionNo" placeholder="题号" />
                <el-select v-model="wrongItemDraft.wrongReasonCode" placeholder="错因标签">
                  <el-option
                    v-for="option in wrongReasonOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </div>

              <div class="prototype-wrongbook-stack">
                <el-input
                  v-model="wrongItemDraft.questionText"
                  type="textarea"
                  :rows="2"
                  resize="none"
                  placeholder="题目内容"
                />
                <el-input
                  v-model="wrongItemDraft.studentAnswer"
                  type="textarea"
                  :rows="2"
                  resize="none"
                  placeholder="学生答案"
                />
                <el-input
                  v-model="wrongItemDraft.correctAnswer"
                  type="textarea"
                  :rows="2"
                  resize="none"
                  placeholder="正确答案"
                />
                <el-input
                  v-model="wrongItemDraft.analysisText"
                  type="textarea"
                  :rows="2"
                  resize="none"
                  placeholder="分析与订正提示"
                />
              </div>

              <div class="prototype-upload-actions">
                <input
                  ref="wrongItemFileInput"
                  type="file"
                  accept="image/*"
                  class="prototype-hidden-input"
                  @change="handleWrongItemAssetUpload"
                />
                <el-button :loading="wrongItemAssetUploading" @click="triggerWrongItemAssetUpload">
                  选择截图并上传
                </el-button>
                <el-button type="primary" plain @click="addWrongItem">加入错题本</el-button>
              </div>

              <div v-if="wrongItemDraft.assets?.length" class="prototype-link-list">
                <button
                  v-for="(asset, index) in wrongItemDraft.assets"
                  :key="`${asset.assetUrl}-${index}`"
                  type="button"
                  class="prototype-link-chip"
                  @click="removeWrongItemAsset(index)"
                >
                  {{ asset.assetName || `截图 ${index + 1}` }} · 删除
                </button>
              </div>

              <div v-if="wrongItems.length" class="prototype-wrong-list">
                <article
                  v-for="(item, index) in wrongItems"
                  :key="`${item.questionNo || 'q'}-${index}`"
                  class="prototype-wrong-item"
                >
                  <div class="prototype-score-head">
                    <strong>{{ item.questionNo ? `题号 ${item.questionNo}` : `错题 ${index + 1}` }}</strong>
                    <button type="button" class="prototype-text-btn prototype-text-btn--danger" @click="removeWrongItem(index)">
                      删除
                    </button>
                  </div>
                  <p>{{ item.questionText || item.studentAnswer || '已加入错题本，待补充详细内容。' }}</p>
                </article>
              </div>
            </section>

            <div class="prototype-footer-actions">
              <button type="button" class="prototype-secondary-btn" @click="saveReviewDraft">保存草稿</button>
              <button
                type="button"
                class="prototype-primary-btn"
                :disabled="!selectedSubmission || store.loading.action"
                @click="submitReviewAndMoveNext"
              >
                {{ store.loading.action ? '提交中...' : '提交评分并看下一份' }}
              </button>
            </div>
          </aside>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.prototype-grade-page {
  min-height: calc(100vh - 92px);
  background: transparent;
}

.prototype-grade-layout {
  display: grid;
  grid-template-columns: 400px minmax(0, 1fr);
  min-height: calc(100vh - 92px);
  border: 1px solid #d7d0c3;
  overflow: hidden;
  background: #fffdf8;
}

.prototype-left-pane {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #d7d0c3;
  background: #f8f4ea;
}

.prototype-left-pane__summary {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #ddd5c8;
}

.prototype-left-pane__summary h2 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  line-height: 1.35;
}

.prototype-left-pane__summary p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
}

.prototype-search {
  padding: 12px 24px 16px;
  border-bottom: 1px solid #ddd5c8;
}

.prototype-tabs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-bottom: 1px solid #ddd5c8;
  background: #f8f4ea;
}

.prototype-tab {
  height: 50px;
  border: 0;
  background: transparent;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.prototype-tab.is-active {
  color: #4f46e5;
  box-shadow: inset 0 -2px 0 #4f46e5;
}

.prototype-student-list {
  flex: 1;
  overflow: auto;
  background: #f8f4ea;
}

.prototype-student-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 18px 24px;
  border: 0;
  border-bottom: 1px solid #e4dccf;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.prototype-student-item.is-active {
  background: #fefbf5;
  box-shadow: inset 3px 0 0 #2753e8;
}

.prototype-student-item__main {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.prototype-status-dot {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  margin-top: 5px;
  border-radius: 50%;
}

.prototype-student-item strong {
  display: block;
  color: #0f172a;
  font-size: 15px;
}

.prototype-student-item p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.prototype-student-review-score {
  display: grid;
  place-items: center;
  min-width: 74px;
  padding: 8px 10px;
  border: 1px solid #bfdbfe;
  border-radius: 14px;
  background:
    radial-gradient(circle at 20% 10%, rgba(255, 255, 255, 0.9), transparent 34%),
    linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #1d4ed8;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.12);
}

.prototype-student-review-score strong {
  color: #1d4ed8;
  font-size: 16px;
  line-height: 1;
}

.prototype-student-review-score span {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.prototype-student-score {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 64px;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  background: #f1f5f9;
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.prototype-student-score.is-working {
  background: #eef2ff;
  color: #4f46e5;
}

.prototype-empty-side {
  padding: 28px 24px;
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
}

.prototype-main-pane {
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f4f0e8;
}

.prototype-main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 28px;
  border-bottom: 1px solid #d7d0c3;
  background: #fffdf8;
}

.prototype-main-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: 22px;
  line-height: 1.2;
}

.prototype-main-header p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
}

.prototype-reload-btn,
.prototype-tool-btn,
.prototype-secondary-btn,
.prototype-primary-btn,
.prototype-link-chip,
.prototype-text-btn {
  border: 1px solid #d7dde6;
  border-radius: 0;
  background: transparent;
  color: #0f172a;
  cursor: pointer;
}

.prototype-reload-btn {
  height: 38px;
  padding: 0 18px;
  font-size: 14px;
}

.prototype-main-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 16px;
  padding: 28px;
  min-height: 0;
}

.prototype-paper-column {
  display: flex;
  flex-direction: column;
  gap: 0;
  min-width: 0;
}

.prototype-tool-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid #d7d0c3;
  border-bottom: 0;
  border-radius: 0;
  background: #fffdf8;
}

.prototype-tool-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.prototype-tool-btn {
  height: 40px;
  padding: 0 18px;
  font-size: 14px;
}

.prototype-tool-btn.is-active {
  border-color: #4f46e5;
  color: #4f46e5;
  background: #eef2ff;
}

.prototype-paper-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px;
  border: 1px solid #d7d0c3;
  border-radius: 0;
  background: #e7e0d2;
  min-height: 680px;
}

.prototype-paper-sheet {
  width: min(100%, 720px);
  min-height: 560px;
  padding: 40px 48px;
  background: #ffffff;
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.08);
  transform-origin: top center;
  transition: transform 0.18s ease;
  position: relative;
}

.prototype-paper-sheet__title {
  color: #111827;
  text-align: center;
  font-size: 20px;
  font-weight: 800;
}

.prototype-paper-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
  color: #64748b;
  font-size: 12px;
}

.prototype-paper-sheet__content {
  margin-top: 36px;
}

.prototype-paper-sheet__content.is-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  border: 1px dashed #cbd5e1;
  border-radius: 18px;
  color: #64748b;
  text-align: center;
}

.prototype-paper-sheet__content p {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 1.95;
  white-space: pre-wrap;
}

.prototype-paper-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-top: 28px;
}

.prototype-paper-image {
  display: block;
  overflow: hidden;
  border-radius: 12px;
  background: #f8fafc;
}

.prototype-paper-image :deep(.el-image__inner) {
  display: block;
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.prototype-file-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.prototype-file-item {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border: 1px solid #dbe1ea;
  border-radius: 999px;
  color: #334155;
  font-size: 13px;
  text-decoration: none;
}

.prototype-annotation {
  position: absolute;
  color: #ef4444;
}

.prototype-annotation--circle {
  left: 180px;
  top: 260px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 74px;
  height: 74px;
  border: 3px solid #ef4444;
  border-radius: 999px;
  font-size: 44px;
  font-weight: 700;
  transform: rotate(-10deg);
}

.prototype-annotation--text {
  right: 56px;
  top: 332px;
  max-width: 220px;
  font-size: 18px;
  line-height: 1.45;
  transform: rotate(-6deg);
}

.prototype-score-pane {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 28px;
  border: 1px solid #d7d0c3;
  border-radius: 0;
  background: #fffdf8;
}

.prototype-score-block {
  display: grid;
  gap: 14px;
}

.prototype-score-block h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

.prototype-score-inline {
  display: flex;
  align-items: center;
  gap: 14px;
}

.prototype-score-input {
  width: 132px;
}

.prototype-score-inline span {
  color: #64748b;
  font-size: 18px;
}

.prototype-score-input :deep(.el-input__wrapper) {
  min-height: 68px;
  border-radius: 0;
  box-shadow: 0 0 0 1px #cfc6b6 inset;
}

.prototype-score-input :deep(.el-input__inner) {
  font-size: 30px;
  font-weight: 600;
  text-align: center;
}

.prototype-quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.prototype-quick-tag {
  padding: 10px 14px;
  border: 1px solid #d7d0c3;
  border-radius: 0;
  background: transparent;
  color: #334155;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.18s ease, border-color 0.18s ease, color 0.18s ease;
}

.prototype-quick-tag:hover {
  border-color: #2753e8;
  background: #e8eeff;
  color: #2753e8;
}

.prototype-score-grid,
.prototype-upload-inline {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.prototype-score-grid label {
  display: block;
  margin-bottom: 8px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

.prototype-radio-group :deep(.el-radio-button__inner) {
  min-width: 96px;
}

.prototype-score-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.prototype-text-btn {
  padding: 0;
  border: 0;
  background: transparent;
  color: #4f46e5;
  font-size: 13px;
}

.prototype-text-btn--danger {
  color: #ef4444;
}

.prototype-wrongbook-stack,
.prototype-link-list,
.prototype-wrong-list {
  display: grid;
  gap: 10px;
}

.prototype-upload-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.prototype-symbol-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.prototype-symbol-btn {
  height: 42px;
  border: 1px solid #dbe1ea;
  border-radius: 10px;
  background: #ffffff;
  color: #0f172a;
  font-size: 18px;
  cursor: pointer;
}

.prototype-zoom-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.prototype-zoom-controls span {
  min-width: 48px;
  color: #64748b;
  font-size: 13px;
  text-align: center;
}

.prototype-link-chip {
  padding: 8px 12px;
  text-align: left;
  font-size: 13px;
}

.prototype-wrong-item {
  padding: 12px 14px;
  border: 1px solid #ddd5c8;
  border-radius: 0;
  background: #f8f4ea;
}

.prototype-wrong-item p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.prototype-footer-actions {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 12px;
  margin-top: auto;
}

.prototype-secondary-btn,
.prototype-primary-btn {
  height: 58px;
  font-size: 15px;
  font-weight: 700;
}

.prototype-primary-btn {
  border-color: #4f46e5;
  background: #2753e8;
  color: #ffffff;
}

.prototype-hidden-input {
  display: none;
}

.tone-slate {
  background: #e2e8f0;
}

.tone-sky {
  background: #ef4444;
}

.tone-teal {
  background: #10b981;
}

.tone-rose {
  background: #f59e0b;
}

.tone-amber {
  background: #f59e0b;
}

@media (max-width: 1500px) {
  .prototype-main-body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1180px) {
  .prototype-grade-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .prototype-main-body,
  .prototype-main-header,
  .prototype-score-grid,
  .prototype-upload-inline,
  .prototype-footer-actions {
    grid-template-columns: 1fr;
  }

  .prototype-main-header,
  .prototype-tool-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .prototype-paper-stage,
  .prototype-score-pane {
    padding: 16px;
  }

  .prototype-paper-sheet {
    padding: 24px;
    transform: none !important;
  }

  .prototype-paper-sheet__content p {
    font-size: 20px;
  }
}
</style>
