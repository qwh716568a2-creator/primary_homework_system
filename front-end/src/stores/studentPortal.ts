import { computed, reactive, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createStudentWrongBook,
  fetchStudentHomeworkDetail,
  fetchStudentHomeworkList,
  fetchStudentMessageDetail,
  fetchStudentMessagePage,
  fetchStudentReviewList,
  fetchStudentSubmissionList,
  fetchStudentWrongBookDetail,
  fetchStudentWrongBookPage,
  fetchStudentWrongBookSubjects,
  markStudentMessageRead,
  markStudentWrongBookMastered,
  submitStudentHomework,
  submitStudentWrongBookFix
} from '@/api/student'
import { clearAuthSession, getAuthSession, persistAuthSession } from '@/utils/auth-session-clean'
import type { AuthSession } from '@/types/auth'
import type {
  StudentHomeworkRecord,
  StudentHomeworkReview,
  StudentHomeworkSubmission,
  StudentMessageItem,
  StudentProfileSummary,
  StudentSubmitPayload,
  StudentSubjectOption,
  StudentWrongBookCreatePayload,
  StudentWrongBookFixPayload,
  StudentWrongBookRecord
} from '@/types/student-portal'
import { studentWrongBookStatusMap } from '@/utils/student-portal-view'

export const useStudentPortalStore = defineStore('student-portal', () => {
  const authUser = ref<AuthSession | null>(getAuthSession())
  const homeworks = ref<StudentHomeworkRecord[]>([])
  const messages = ref<StudentMessageItem[]>([])
  const wrongBooks = ref<StudentWrongBookRecord[]>([])
  const wrongBookSubjects = ref<StudentSubjectOption[]>([])
  const homeworkDetails = ref<Record<string, StudentHomeworkRecord>>({})
  const submissionMap = ref<Record<string, StudentHomeworkSubmission[]>>({})
  const reviewMap = ref<Record<string, StudentHomeworkReview[]>>({})
  const wrongBookDetailMap = ref<Record<string, StudentWrongBookRecord>>({})
  const loading = reactive({
    homeworks: false,
    messages: false,
    wrongBooks: false,
    detail: false,
    submissions: false,
    reviews: false,
    wrongBookDetail: false,
    subjects: false,
    action: false
  })

  const isAuthenticated = computed(() => Boolean(authUser.value?.authenticated))

  const profile = computed<StudentProfileSummary>(() => {
    const pending = homeworks.value.filter((item) => item.status === 'pending').length
    const revision = homeworks.value.filter((item) => item.status === 'revision').length

    let headline = '今天的学习进度已经同步完成。'
    if (pending > 0 || revision > 0) {
      headline = `还有 ${pending} 项待完成、${revision} 项待订正作业，继续保持节奏。`
    }

    return {
      name: authUser.value?.name || '学生用户',
      school: authUser.value?.school || '校内统一认证',
      account: authUser.value?.account || '未同步',
      headline
    }
  })

  const pendingCount = computed(() => homeworks.value.filter((item) => item.status === 'pending').length)
  const revisionCount = computed(() => homeworks.value.filter((item) => item.status === 'revision').length)
  const completedCount = computed(() => homeworks.value.filter((item) => item.status === 'completed').length)
  const unreadMessageCount = computed(() => messages.value.filter((item) => item.unread).length)
  const pendingWrongBookCount = computed(
    () => wrongBooks.value.filter((item) => item.status === 'pending_fix').length
  )

  const statusOverview = computed(() => [
    {
      label: '待完成',
      value: pendingCount.value,
      hint: '今天仍需按时完成的作业数量',
      tone: 'amber' as const
    },
    {
      label: '待订正',
      value: revisionCount.value,
      hint: '老师已反馈并要求继续修改的内容',
      tone: 'rose' as const
    },
    {
      label: '已掌握错题',
      value: wrongBooks.value.filter((item) => item.status === 'mastered').length,
      hint: '已经完成订正并标记掌握的题目',
      tone: 'teal' as const
    },
    {
      label: '未读消息',
      value: unreadMessageCount.value,
      hint: '老师反馈和提醒通知',
      tone: 'sky' as const
    }
  ])

  function setAuthenticatedUser(user: AuthSession | null, options: { remember?: boolean } = {}) {
    authUser.value = user

    if (user) {
      persistAuthSession(user, options.remember !== false)
    } else {
      clearAuthSession()
    }
  }

  function resetState() {
    homeworks.value = []
    messages.value = []
    wrongBooks.value = []
    wrongBookSubjects.value = []
    homeworkDetails.value = {}
    submissionMap.value = {}
    reviewMap.value = {}
    wrongBookDetailMap.value = {}
  }

  async function initializeWorkspace() {
    await Promise.all([loadHomeworks(), loadMessages(), loadWrongBooks(), loadWrongBookSubjects()])
  }

  async function loadHomeworks(tab = 'all') {
    loading.homeworks = true
    try {
      homeworks.value = await fetchStudentHomeworkList(tab)
      return homeworks.value
    } finally {
      loading.homeworks = false
    }
  }

  async function loadMessages(readStatus = 'all') {
    loading.messages = true
    try {
      const data = await fetchStudentMessagePage(readStatus)
      messages.value = data.list
      return messages.value
    } finally {
      loading.messages = false
    }
  }

  async function openMessage(messageId: string) {
    const detail = await fetchStudentMessageDetail(messageId)
    const index = messages.value.findIndex((item) => item.id === messageId)
    if (index >= 0) {
      messages.value.splice(index, 1, detail)
    }
    return detail
  }

  async function markMessageRead(messageId: string) {
    const index = messages.value.findIndex((item) => item.id === messageId)
    if (index < 0 || !messages.value[index].unread) {
      return
    }

    messages.value.splice(index, 1, {
      ...messages.value[index],
      unread: false
    })

    try {
      await markStudentMessageRead(messageId)
    } catch {
      // 已读状态先在前端生效，避免接口短暂不可用时阻断查看消息。
    }
  }

  async function loadWrongBooks(subjectCode = 'all', status = 'all') {
    loading.wrongBooks = true
    try {
      const data = await fetchStudentWrongBookPage(subjectCode, status)
      wrongBooks.value = data.list
      return wrongBooks.value
    } finally {
      loading.wrongBooks = false
    }
  }

  async function loadWrongBookSubjects() {
    loading.subjects = true
    try {
      wrongBookSubjects.value = await fetchStudentWrongBookSubjects()
      return wrongBookSubjects.value
    } finally {
      loading.subjects = false
    }
  }

  async function loadHomeworkDetail(homeworkId: string) {
    loading.detail = true
    try {
      const detail = await fetchStudentHomeworkDetail(homeworkId)
      homeworkDetails.value = { ...homeworkDetails.value, [homeworkId]: detail }

      const index = homeworks.value.findIndex((item) => item.id === homeworkId)
      if (index >= 0) {
        homeworks.value.splice(index, 1, detail)
      }

      return detail
    } finally {
      loading.detail = false
    }
  }

  async function loadSubmissions(homeworkId: string) {
    loading.submissions = true
    try {
      const list = await fetchStudentSubmissionList(homeworkId)
      submissionMap.value = { ...submissionMap.value, [homeworkId]: list }
      return list
    } finally {
      loading.submissions = false
    }
  }

  async function loadReviews(homeworkId: string) {
    loading.reviews = true
    try {
      const list = await fetchStudentReviewList(homeworkId)
      reviewMap.value = { ...reviewMap.value, [homeworkId]: list }
      return list
    } finally {
      loading.reviews = false
    }
  }

  async function loadWrongBookDetail(wrongBookId: string) {
    loading.wrongBookDetail = true
    try {
      const detail = await fetchStudentWrongBookDetail(wrongBookId)
      wrongBookDetailMap.value = { ...wrongBookDetailMap.value, [wrongBookId]: detail }
      return detail
    } finally {
      loading.wrongBookDetail = false
    }
  }

  async function submitHomework(payload: StudentSubmitPayload) {
    loading.action = true
    try {
      await submitStudentHomework(payload)
      await Promise.all([loadHomeworks(), loadHomeworkDetail(payload.homeworkId), loadSubmissions(payload.homeworkId)])
    } finally {
      loading.action = false
    }
  }

  async function addWrongBook(payload: StudentWrongBookCreatePayload) {
    loading.action = true
    try {
      const result = await createStudentWrongBook(payload)
      await Promise.all([loadWrongBooks(), loadWrongBookSubjects()])
      if (result.wrongBookId) {
        await loadWrongBookDetail(`${result.wrongBookId}`)
      }
      return result
    } finally {
      loading.action = false
    }
  }

  async function fixWrongBook(wrongBookId: string, payload: StudentWrongBookFixPayload) {
    loading.action = true
    try {
      await submitStudentWrongBookFix(wrongBookId, payload)
      await Promise.all([loadWrongBooks(), loadWrongBookDetail(wrongBookId)])
    } finally {
      loading.action = false
    }
  }

  async function masteredWrongBook(wrongBookId: string) {
    loading.action = true
    try {
      await markStudentWrongBookMastered(wrongBookId)
      await Promise.all([loadWrongBooks(), loadWrongBookDetail(wrongBookId)])
    } finally {
      loading.action = false
    }
  }

  function getHomework(homeworkId: string) {
    return homeworkDetails.value[homeworkId] ?? homeworks.value.find((item) => item.id === homeworkId) ?? null
  }

  function getWrongBook(wrongBookId: string) {
    return wrongBookDetailMap.value[wrongBookId] ?? wrongBooks.value.find((item) => item.id === wrongBookId) ?? null
  }

  const wrongBookSummary = computed(() =>
    wrongBooks.value.map((item) => ({
      id: item.id,
      subjectName: item.subjectName,
      questionText: item.questionText,
      statusLabel: studentWrongBookStatusMap[item.status]?.label ?? item.status
    }))
  )

  return {
    authUser,
    homeworks,
    messages,
    wrongBooks,
    wrongBookSubjects,
    homeworkDetails,
    submissionMap,
    reviewMap,
    wrongBookDetailMap,
    loading,
    isAuthenticated,
    profile,
    pendingCount,
    revisionCount,
    completedCount,
    unreadMessageCount,
    pendingWrongBookCount,
    statusOverview,
    wrongBookSummary,
    setAuthenticatedUser,
    resetState,
    initializeWorkspace,
    loadHomeworks,
    loadMessages,
    openMessage,
    markMessageRead,
    loadWrongBooks,
    loadWrongBookSubjects,
    loadHomeworkDetail,
    loadSubmissions,
    loadReviews,
    loadWrongBookDetail,
    submitHomework,
    addWrongBook,
    fixWrongBook,
    masteredWrongBook,
    getHomework,
    getWrongBook
  }
})
