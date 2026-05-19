import { computed, reactive, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createTeacherClassBinding,
  createTeacherHomework,
  deleteTeacherHomework,
  deleteTeacherMessage,
  fetchTeacherBindingCandidates,
  fetchTeacherHomeworkDetail,
  fetchTeacherHomeworkOverview,
  fetchTeacherMessageRecords,
  fetchTeacherHomeworks,
  fetchTeacherHomeworkTasks,
  fetchTeacherTaskDetail,
  fetchTeachingClasses,
  remindTeacherHomework,
  revokeTeacherHomework,
  sendTeacherMessage,
  submitTeacherTaskReview,
  updateTeacherHomework
} from '@/api/teacher'
import { clearAuthSession, getAuthSession, persistAuthSession, roleMeta } from '@/utils/auth-session-clean'
import { buildDashboardCards } from '@/utils/teacher-portal-view'
import type { AuthSession } from '@/types/auth'
import type {
  AssignmentFormInput,
  DashboardCard,
  HomeworkAsset,
  HomeworkDetail,
  HomeworkListItem,
  HomeworkListQuery,
  HomeworkOverviewStats,
  TeacherWrongBookItemInput,
  TeacherMessageFormInput,
  TeacherMessageQuery,
  TeacherMessageRecord,
  HomeworkStatsQuery,
  HomeworkTaskDetail,
  HomeworkTaskListItem,
  HomeworkTaskQuery,
  SubjectOption,
  TeacherClassBindingCandidate,
  TeacherClassBindingPayload,
  TeacherProfile,
  TeachingClassRelation
} from '@/types/teacher-portal'

const emptyOverview: HomeworkOverviewStats = {
  publishCount: 0,
  submissionRate: 0,
  onTimeRate: 0,
  reviewRate: 0,
  revisionRequiredRate: 0
}

export const useTeacherPortalStore = defineStore('teacher-portal-api', () => {
  const authUser = ref<AuthSession | null>(getAuthSession())
  const classRelations = ref<TeachingClassRelation[]>([])
  const bindingCandidates = ref<TeacherClassBindingCandidate[]>([])
  const homeworks = ref<HomeworkListItem[]>([])
  const messageRecords = ref<TeacherMessageRecord[]>([])
  const homeworkOverview = ref<HomeworkOverviewStats>({ ...emptyOverview })
  const homeworkDetails = ref<Record<string, HomeworkDetail>>({})
  const homeworkTasks = ref<Record<string, HomeworkTaskListItem[]>>({})
  const taskDetails = ref<Record<string, HomeworkTaskDetail>>({})
  const loading = reactive({
    classes: false,
    bindingCandidates: false,
    homeworks: false,
    overview: false,
    detail: false,
    tasks: false,
    taskDetail: false,
    messages: false,
    action: false
  })

  const isAuthenticated = computed(() => Boolean(authUser.value?.authenticated))

  const teacher = computed<TeacherProfile>(() => {
    const currentName = authUser.value?.name || '教师用户'

    return {
      name: currentName,
      role: authUser.value ? `${roleMeta[authUser.value.role].label}账号` : '教师账号',
      school: authUser.value?.school || '校内统一认证',
      avatar: currentName.slice(0, 1)
    }
  })

  const subjectOptions = computed<SubjectOption[]>(() => {
    const subjectMap = new Map<string, SubjectOption>()

    classRelations.value.forEach((item) => {
      if (!subjectMap.has(item.subjectCode)) {
        subjectMap.set(item.subjectCode, {
          subjectCode: item.subjectCode,
          subjectName: item.subjectName
        })
      }
    })

    return Array.from(subjectMap.values())
  })

  const classOptions = computed(() => {
    const classMap = new Map<string, { classId: number | string; className: string }>()

    classRelations.value.forEach((item) => {
      const key = `${item.classId}`

      if (!classMap.has(key)) {
        classMap.set(key, {
          classId: item.classId,
          className: item.className
        })
      }
    })

    return Array.from(classMap.values())
  })

  const dashboardCards = computed<DashboardCard[]>(() =>
    buildDashboardCards(homeworkOverview.value, homeworks.value)
  )

  const pendingHomeworkList = computed(() =>
    [...homeworks.value]
      .filter((item) => item.pendingCount > 0 || item.revisionRequiredCount > 0)
      .sort(
        (left, right) =>
          right.pendingCount + right.revisionRequiredCount - (left.pendingCount + left.revisionRequiredCount)
      )
  )

  const recentAssignments = computed(() =>
    [...homeworks.value].sort((left, right) => `${right.deadlineAt}`.localeCompare(`${left.deadlineAt}`))
  )

  const reviewQueue = computed(() =>
    homeworks.value.flatMap((item) =>
      Array.from({ length: item.pendingCount + item.revisionRequiredCount }, () => ({
        homeworkId: item.homeworkId
      }))
    )
  )

  function setAuthenticatedUser(user: AuthSession | null, options: { remember?: boolean } = {}) {
    authUser.value = user

    if (user) {
      persistAuthSession(user, options.remember !== false)
    } else {
      clearAuthSession()
    }
  }

  async function initializeWorkspace() {
    await Promise.all([loadTeachingClasses(), loadHomeworkList(), loadHomeworkOverview()])
  }

  async function loadTeachingClasses(subjectCode?: string) {
    loading.classes = true

    try {
      classRelations.value = await fetchTeachingClasses(subjectCode)
      return classRelations.value
    } finally {
      loading.classes = false
    }
  }

  async function loadBindingCandidates(keyword?: string) {
    loading.bindingCandidates = true

    try {
      bindingCandidates.value = await fetchTeacherBindingCandidates(keyword)
      return bindingCandidates.value
    } finally {
      loading.bindingCandidates = false
    }
  }

  async function loadHomeworkList(query: HomeworkListQuery = {}) {
    loading.homeworks = true

    try {
      const data = await fetchTeacherHomeworks(query)
      homeworks.value = data.list
      return data.list
    } finally {
      loading.homeworks = false
    }
  }

  async function loadHomeworkOverview(query: HomeworkStatsQuery = {}) {
    loading.overview = true

    try {
      homeworkOverview.value = await fetchTeacherHomeworkOverview(query)
      return homeworkOverview.value
    } finally {
      loading.overview = false
    }
  }

  async function loadMessageRecords(query: TeacherMessageQuery = {}) {
    loading.messages = true

    try {
      const data = await fetchTeacherMessageRecords(query)
      messageRecords.value = data.list
      return data.list
    } finally {
      loading.messages = false
    }
  }

  async function loadHomeworkDetail(homeworkId: number | string) {
    loading.detail = true

    try {
      const detail = await fetchTeacherHomeworkDetail(homeworkId)
      homeworkDetails.value[`${homeworkId}`] = detail
      return detail
    } finally {
      loading.detail = false
    }
  }

  async function loadHomeworkTasks(homeworkId: number | string, query: HomeworkTaskQuery = {}) {
    loading.tasks = true

    try {
      const data = await fetchTeacherHomeworkTasks(homeworkId, query)
      homeworkTasks.value[`${homeworkId}`] = data.list
      return data.list
    } finally {
      loading.tasks = false
    }
  }

  async function loadTaskDetail(taskId: number | string) {
    loading.taskDetail = true

    try {
      const detail = await fetchTeacherTaskDetail(taskId)
      taskDetails.value[`${taskId}`] = detail
      return detail
    } finally {
      loading.taskDetail = false
    }
  }

  async function saveAssignment(
    form: AssignmentFormInput,
    nextState: 'draft' | 'published',
    homeworkId?: number | string
  ) {
    loading.action = true

    try {
      if (homeworkId) {
        await updateTeacherHomework(homeworkId, form, nextState === 'published')
        await Promise.all([loadHomeworkList(), loadHomeworkOverview(), loadHomeworkDetail(homeworkId)])
        return `${homeworkId}`
      }

      const result = await createTeacherHomework(form, nextState === 'published')
      await Promise.all([loadHomeworkList(), loadHomeworkOverview(), loadHomeworkDetail(result.homeworkId)])
      return `${result.homeworkId}`
    } finally {
      loading.action = false
    }
  }

  async function revokeAssignment(homeworkId: number | string, reason: string) {
    loading.action = true

    try {
      await revokeTeacherHomework(homeworkId, reason)
      await Promise.all([loadHomeworkList(), loadHomeworkOverview(), loadHomeworkDetail(homeworkId)])
    } finally {
      loading.action = false
    }
  }

  async function deleteAssignment(homeworkId: number | string) {
    loading.action = true

    try {
      await deleteTeacherHomework(homeworkId)
      delete homeworkDetails.value[`${homeworkId}`]
      delete homeworkTasks.value[`${homeworkId}`]
      homeworks.value = homeworks.value.filter((item) => `${item.homeworkId}` !== `${homeworkId}`)
      await Promise.all([loadHomeworkList(), loadHomeworkOverview()])
    } finally {
      loading.action = false
    }
  }

  async function sendReminder(homeworkId: number | string, classId?: number | string) {
    return sendTypedReminder(homeworkId, {
      classId
    })
  }

  async function sendTypedReminder(
    homeworkId: number | string,
    payload: {
      remindType?: 'pending' | 'overdue'
      classId?: number | string
    } = {}
  ) {
    loading.action = true

    try {
      await remindTeacherHomework(homeworkId, payload)
      await loadHomeworkTasks(homeworkId)
      await Promise.allSettled([loadHomeworkList(), loadHomeworkDetail(homeworkId)])
    } finally {
      loading.action = false
    }
  }

  async function submitReview(
    taskId: number | string,
    payload: {
      submissionId: number | string
      reviewStatus: 'completed' | 'revision_required'
      score?: number
      scoreLevel?: string
      commentText?: string
      reviewAssets?: HomeworkAsset[]
      wrongItems?: TeacherWrongBookItemInput[]
    }
  ) {
    loading.action = true

    try {
      await submitTeacherTaskReview(taskId, payload)
      await loadTaskDetail(taskId)
    } finally {
      loading.action = false
    }
  }

  async function saveClassBinding(payload: TeacherClassBindingPayload) {
    loading.action = true

    try {
      await createTeacherClassBinding(payload)
      await Promise.all([loadTeachingClasses(), loadBindingCandidates()])
    } finally {
      loading.action = false
    }
  }

  async function sendMessage(payload: TeacherMessageFormInput) {
    loading.action = true

    try {
      const result = await sendTeacherMessage(payload)
      await loadMessageRecords()
      return result
    } finally {
      loading.action = false
    }
  }

  async function deleteMessage(messageId: number | string) {
    loading.action = true

    try {
      await deleteTeacherMessage(messageId)
      messageRecords.value = messageRecords.value.filter((item) => `${item.messageId}` !== `${messageId}`)
    } finally {
      loading.action = false
    }
  }

  function getHomeworkDetail(homeworkId?: number | string) {
    if (homeworkId === undefined || homeworkId === null) {
      return undefined
    }

    return homeworkDetails.value[`${homeworkId}`]
  }

  function getHomeworkTasks(homeworkId?: number | string) {
    if (homeworkId === undefined || homeworkId === null) {
      return []
    }

    return homeworkTasks.value[`${homeworkId}`] ?? []
  }

  function getTaskDetail(taskId?: number | string) {
    if (taskId === undefined || taskId === null) {
      return undefined
    }

    return taskDetails.value[`${taskId}`]
  }

  return {
    authUser,
    classRelations,
    bindingCandidates,
    homeworks,
    messageRecords,
    homeworkOverview,
    loading,
    isAuthenticated,
    teacher,
    subjectOptions,
    classOptions,
    dashboardCards,
    pendingHomeworkList,
    recentAssignments,
    reviewQueue,
    setAuthenticatedUser,
    initializeWorkspace,
    loadTeachingClasses,
    loadBindingCandidates,
    loadHomeworkList,
    loadHomeworkOverview,
    loadMessageRecords,
    loadHomeworkDetail,
    loadHomeworkTasks,
    loadTaskDetail,
    saveAssignment,
    revokeAssignment,
    deleteAssignment,
    sendReminder,
    submitReview,
    sendTypedReminder,
    sendMessage,
    deleteMessage,
    saveClassBinding,
    getHomeworkDetail,
    getHomeworkTasks,
    getTaskDetail
  }
})
