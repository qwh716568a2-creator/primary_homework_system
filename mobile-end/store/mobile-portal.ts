import { defineStore } from 'pinia'
import { createPreviewState } from '@/data/preview-mobile'
import { assistParentSubmit, fetchParentChildren, fetchParentHomeworkDetail, fetchParentHomeworkList, fetchParentMessagePage } from '@/services/parent'
import {
  createStudentWrongBook as createStudentWrongBookApi,
  fetchStudentHomeworkDetail,
  fetchStudentHomeworkList,
  fetchStudentMessagePage,
  fetchStudentReviewList,
  fetchStudentSubmissionList,
  fetchStudentWrongBookSubjects,
  fetchStudentWrongBookDetail as fetchStudentWrongBookDetailApi,
  fetchStudentWrongBookPage,
  markStudentWrongBookMastered as markStudentWrongBookMasteredApi,
  submitStudentHomework,
  submitStudentWrongBookFix as submitStudentWrongBookFixApi
} from '@/services/student'
import { useAuthStore } from '@/store/auth'
import type {
  ChildProfile,
  HomeworkRecord,
  HomeworkReview,
  HomeworkSubmission,
  MessageItem,
  MobilePreviewState,
  ParentProfile,
  PageResult,
  SubjectOption,
  StudentProfile,
  StudentWrongBookCreatePayload,
  StudentSubmitPayload,
  UserRole,
  WrongBookFixPayload,
  WrongBookRecord
} from '@/types/mobile'

const USE_PREVIEW_DATA = import.meta.env.VITE_USE_MOBILE_MOCK === 'true'

interface MobilePortalState extends MobilePreviewState {
  studentSubmissionsMap: Record<string, HomeworkSubmission[]>
  studentReviewsMap: Record<string, HomeworkReview[]>
  studentWrongBookDetailMap: Record<string, WrongBookRecord>
  studentWrongBookSubjects: SubjectOption[]
  parentHomeworkDetails: Record<string, Record<string, HomeworkRecord>>
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T
}

function createEmptyStudentProfile(): StudentProfile {
  return {
    id: '',
    name: '',
    school: '',
    className: '',
    studentNo: '',
    headline: '已同步你的作业、提交和老师反馈。'
  }
}

function createEmptyParentProfile(): ParentProfile {
  return {
    id: '',
    name: '',
    mobile: '',
    school: '',
    headline: '已同步孩子作业、协助提交和消息提醒。'
  }
}

function createEmptyState(): MobilePortalState {
  return {
    studentProfile: createEmptyStudentProfile(),
    parentProfile: createEmptyParentProfile(),
    children: [],
    activeChildId: '',
    studentHomeworks: [],
    parentHomeworks: {},
    studentWrongBooks: [],
    studentMessages: [],
    parentMessages: [],
    studentSubmissionsMap: {},
    studentReviewsMap: {},
    studentWrongBookDetailMap: {},
    studentWrongBookSubjects: [],
    parentHomeworkDetails: {}
  }
}

function createPreviewPortalState(): MobilePortalState {
  const preview = clone(createPreviewState())
  return {
    ...preview,
    studentSubmissionsMap: {},
    studentReviewsMap: {},
    studentWrongBookDetailMap: {},
    studentWrongBookSubjects: buildSubjectOptions(preview.studentHomeworks, preview.studentWrongBooks),
    parentHomeworkDetails: {}
  }
}

function shouldUsePreviewData(token?: string) {
  return USE_PREVIEW_DATA || Boolean(token?.startsWith('mobile-local-'))
}

function buildStudentHeadline(homeworks: HomeworkRecord[]) {
  const pendingCount = homeworks.filter((item) => item.status === 'pending').length
  const revisionCount = homeworks.filter((item) => item.status === 'revision').length

  if (pendingCount === 0 && revisionCount === 0) {
    return '当前作业已同步完成，继续保持节奏。'
  }

  if (revisionCount > 0) {
    return `今天有 ${pendingCount} 项待完成、${revisionCount} 项待订正作业。`
  }

  return `今天还有 ${pendingCount} 项作业需要在放学前完成。`
}

function buildParentHeadline(children: ChildProfile[]) {
  if (!children.length) {
    return '当前没有可查看的孩子档案。'
  }

  const pendingCount = children.reduce((sum, item) => sum + (item.pendingCount ?? 0), 0)
  const revisionCount = children.reduce((sum, item) => sum + (item.revisionCount ?? 0), 0)
  return `已关联 ${children.length} 位孩子，待完成 ${pendingCount} 项、待订正 ${revisionCount} 项。`
}

function upsertHomework(list: HomeworkRecord[], record: HomeworkRecord) {
  const nextList = [...list]
  const targetIndex = nextList.findIndex((item) => item.id === record.id)

  if (targetIndex >= 0) {
    nextList.splice(targetIndex, 1, record)
    return nextList
  }

  nextList.unshift(record)
  return nextList
}

function upsertWrongBook(list: WrongBookRecord[], record: WrongBookRecord) {
  const nextList = [...list]
  const targetIndex = nextList.findIndex((item) => item.id === record.id)

  if (targetIndex >= 0) {
    nextList.splice(targetIndex, 1, record)
    return nextList
  }

  nextList.unshift(record)
  return nextList
}

function nowLabel() {
  const now = new Date()
  const month = now.getMonth() + 1
  const date = now.getDate()
  const hour = `${now.getHours()}`.padStart(2, '0')
  const minute = `${now.getMinutes()}`.padStart(2, '0')
  return `${month} 月 ${date} 日 ${hour}:${minute}`
}

function buildSubjectOptions(homeworks: HomeworkRecord[], wrongBooks: WrongBookRecord[]): SubjectOption[] {
  const optionMap = new Map<string, SubjectOption>()

  wrongBooks.forEach((item) => {
    const subjectCode = `${item.subjectCode || item.subjectName || ''}`.trim()
    const subjectName = `${item.subjectName || item.subjectCode || ''}`.trim()

    if (!subjectCode || !subjectName || optionMap.has(subjectCode)) {
      return
    }

    optionMap.set(subjectCode, {
      subjectCode,
      subjectName
    })
  })

  homeworks.forEach((item) => {
    const subjectName = `${item.subject || ''}`.trim()
    if (!subjectName) {
      return
    }

    const subjectCode = subjectName.toLowerCase()
    if (optionMap.has(subjectCode)) {
      return
    }

    optionMap.set(subjectCode, {
      subjectCode,
      subjectName
    })
  })

  return Array.from(optionMap.values())
}

export const useMobilePortalStore = defineStore('mobile-portal', {
  state: (): MobilePortalState => createEmptyState(),
  getters: {
    currentChild(state) {
      return state.children.find((item) => item.id === state.activeChildId) ?? state.children[0] ?? null
    },
    currentChildHomeworks(state) {
      return state.parentHomeworks[state.activeChildId] ?? []
    }
  },
  actions: {
    bootstrap() {
      const authStore = useAuthStore()
      authStore.bootstrap()

      if (shouldUsePreviewData(authStore.session?.token)) {
        if (!this.studentHomeworks.length && !this.children.length && !this.studentMessages.length && !this.parentMessages.length) {
          Object.assign(this.$state, createPreviewPortalState())
        }
        return
      }

      this.syncProfilesFromSession()
    },
    syncProfilesFromSession() {
      const authStore = useAuthStore()
      authStore.bootstrap()
      const session = authStore.session

      if (!session) {
        return
      }

      if (session.role === 'student') {
        this.studentProfile = {
          ...this.studentProfile,
          id: session.userId,
          name: session.userName,
          school: session.schoolName,
          studentNo: session.userId,
          headline: this.studentHomeworks.length
            ? buildStudentHeadline(this.studentHomeworks)
            : '已同步你的作业、提交和老师反馈。'
        }
        return
      }

      this.parentProfile = {
        ...this.parentProfile,
        id: session.userId,
        name: session.userName,
        school: session.schoolName,
        mobile: session.userId,
        headline: this.children.length
          ? buildParentHeadline(this.children)
          : '已同步孩子作业、协助提交和消息提醒。'
      }
    },
    resetPortalData() {
      Object.assign(this.$state, createEmptyState())
    },
    resetPreviewData() {
      this.resetPortalData()
    },
    selectChild(childId: string) {
      this.activeChildId = childId
    },
    getStudentHomework(homeworkId: string) {
      return this.studentHomeworks.find((item) => item.id === homeworkId) ?? null
    },
    getParentHomework(homeworkId: string, childId?: string) {
      const targetChildId = childId ?? this.activeChildId
      return this.parentHomeworkDetails[targetChildId]?.[homeworkId]
        ?? this.parentHomeworks[targetChildId]?.find((item) => item.id === homeworkId)
        ?? null
    },
    getWrongBook(wrongBookId: string) {
      return this.studentWrongBookDetailMap[wrongBookId]
        ?? this.studentWrongBooks.find((item) => item.id === wrongBookId)
        ?? null
    },
    cacheStudentHomework(record: HomeworkRecord) {
      this.studentHomeworks = upsertHomework(this.studentHomeworks, record)
      this.studentProfile.headline = buildStudentHeadline(this.studentHomeworks)
    },
    cacheWrongBook(record: WrongBookRecord) {
      this.studentWrongBooks = upsertWrongBook(this.studentWrongBooks, record)
      this.studentWrongBookDetailMap = {
        ...this.studentWrongBookDetailMap,
        [record.id]: record
      }
    },
    cacheParentHomework(studentId: string, record: HomeworkRecord) {
      this.parentHomeworks = {
        ...this.parentHomeworks,
        [studentId]: upsertHomework(this.parentHomeworks[studentId] ?? [], record)
      }

      this.parentHomeworkDetails = {
        ...this.parentHomeworkDetails,
        [studentId]: {
          ...(this.parentHomeworkDetails[studentId] ?? {}),
          [record.id]: record
        }
      }
    },
    applyPreviewWrongBookCreate(payload: StudentWrongBookCreatePayload) {
      const newRecord: WrongBookRecord = {
        id: `wb-${Date.now()}`,
        homeworkId: payload.homeworkId,
        taskId: payload.taskId,
        reviewId: payload.reviewId,
        subjectCode: payload.subjectCode || 'other',
        subjectName: payload.subjectName || '错题本',
        sourceType: 'student_manual',
        status: 'pending_fix',
        questionNo: payload.questionNo,
        questionText: payload.questionText,
        studentAnswer: payload.studentAnswer,
        correctAnswer: payload.correctAnswer,
        analysisText: payload.analysisText,
        wrongReasonCode: payload.wrongReasonCode,
        wrongReasonLabel: payload.wrongReasonCode || '学生整理',
        teacherName: undefined,
        createdAt: nowLabel(),
        fixCount: 0,
        assets: payload.assets ?? []
      }

      this.cacheWrongBook(newRecord)
      return newRecord
    },
    applyPreviewSubmit(payload: StudentSubmitPayload) {
      const updateRecord = (record: HomeworkRecord | undefined) => {
        if (!record) {
          return
        }

        record.status = 'submitted'
        record.hasFeedback = false
        record.latestSubmission = {
          text: payload.text,
          images: payload.images,
          submittedAt: nowLabel(),
          assistedByParent: payload.assistedByParent
        }
        record.review = {
          status: 'unreviewed'
        }
      }

      updateRecord(this.studentHomeworks.find((item) => item.id === payload.homeworkId))
      Object.keys(this.parentHomeworks).forEach((childId) => {
        updateRecord(this.parentHomeworks[childId]?.find((item) => item.id === payload.homeworkId))
      })

      this.studentMessages.unshift({
        id: `msg-stu-${Date.now()}`,
        title: '作业提交成功',
        content: '已收到你的最新提交，老师批改后会通知你查看反馈。',
        time: '刚刚',
        kind: 'assignment',
        unread: true
      })

      this.parentMessages.unshift({
        id: `msg-par-${Date.now()}`,
        title: payload.assistedByParent ? '家长协助提交已完成' : '孩子已提交作业',
        content: '作业状态已经更新为已提交，可以等待老师批改。',
        time: '刚刚',
        kind: 'assignment',
        unread: true,
        childName: this.currentChild?.name ?? '孩子'
      })
    },
    async loadStudentHomeworks(tab = 'all') {
      this.syncProfilesFromSession()

      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        this.studentProfile.headline = buildStudentHeadline(this.studentHomeworks)
        return tab === 'all' ? this.studentHomeworks : this.studentHomeworks.filter((item) => item.status === tab)
      }

      const list = await fetchStudentHomeworkList(tab)
      this.studentHomeworks = list
      this.studentProfile.headline = buildStudentHeadline(list)
      return list
    },
    async loadStudentHomeworkDetail(homeworkId: string) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        return this.getStudentHomework(homeworkId)
      }

      const detail = await fetchStudentHomeworkDetail(homeworkId)
      this.cacheStudentHomework(detail)
      return detail
    },
    async loadStudentSubmissions(homeworkId: string) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        const homework = this.getStudentHomework(homeworkId)
        const previewList = homework?.latestSubmission ? [homework.latestSubmission] : []
        this.studentSubmissionsMap = {
          ...this.studentSubmissionsMap,
          [homeworkId]: previewList
        }
        return previewList
      }

      const list = await fetchStudentSubmissionList(homeworkId)
      this.studentSubmissionsMap = {
        ...this.studentSubmissionsMap,
        [homeworkId]: list
      }
      return list
    },
    async loadStudentReviews(homeworkId: string) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        const homework = this.getStudentHomework(homeworkId)
        const previewList = homework?.review ? [homework.review] : []
        this.studentReviewsMap = {
          ...this.studentReviewsMap,
          [homeworkId]: previewList
        }
        return previewList
      }

      const list = await fetchStudentReviewList(homeworkId)
      this.studentReviewsMap = {
        ...this.studentReviewsMap,
        [homeworkId]: list
      }

      if (list[0]) {
        const homework = this.getStudentHomework(homeworkId)
        if (homework) {
          this.cacheStudentHomework({
            ...homework,
            hasFeedback: true,
            review: list[0]
          })
        }
      }

      return list
    },
    async loadStudentWrongBooks(subjectCode = 'all', status = 'all') {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        return this.studentWrongBooks.filter((item) => {
          if (subjectCode !== 'all' && item.subjectCode !== subjectCode) {
            return false
          }
          if (status !== 'all' && item.status !== status) {
            return false
          }
          return true
        })
      }

      const page = await fetchStudentWrongBookPage(subjectCode, status)
      this.studentWrongBooks = page.list
      return page.list
    },
    async loadStudentWrongBookSubjects() {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        const options = buildSubjectOptions(this.studentHomeworks, this.studentWrongBooks)
        this.studentWrongBookSubjects = options
        return options
      }

      const list = await fetchStudentWrongBookSubjects()
      this.studentWrongBookSubjects = list
      return list
    },
    async loadStudentWrongBookDetail(wrongBookId: string) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        return this.getWrongBook(wrongBookId)
      }

      const detail = await fetchStudentWrongBookDetailApi(wrongBookId)
      this.cacheWrongBook(detail)
      return detail
    },
    async createStudentWrongBook(payload: StudentWrongBookCreatePayload) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        const record = this.applyPreviewWrongBookCreate(payload)
        return record
      }

      const result = await createStudentWrongBookApi(payload)
      if (result?.wrongBookId) {
        await this.loadStudentWrongBookDetail(`${result.wrongBookId}`)
      }
      await this.loadStudentWrongBooks()
      return result
    },
    async submitWrongBookFix(wrongBookId: string, payload: WrongBookFixPayload) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        const current = this.getWrongBook(wrongBookId)
        if (current) {
          this.cacheWrongBook({
            ...current,
            status: 'fixed',
            lastFixedText: payload.fixedText,
            lastFixedAt: nowLabel(),
            fixCount: (current.fixCount ?? 0) + 1,
            assets: payload.assets?.length ? [...current.assets, ...payload.assets] : current.assets
          })
        }
        return
      }

      await submitStudentWrongBookFixApi(wrongBookId, payload)
      await Promise.allSettled([this.loadStudentWrongBookDetail(wrongBookId), this.loadStudentWrongBooks()])
    },
    async markWrongBookMastered(wrongBookId: string) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        const current = this.getWrongBook(wrongBookId)
        if (current) {
          this.cacheWrongBook({
            ...current,
            status: 'mastered'
          })
        }
        return
      }

      await markStudentWrongBookMasteredApi(wrongBookId)
      await Promise.allSettled([this.loadStudentWrongBookDetail(wrongBookId), this.loadStudentWrongBooks()])
    },
    async submitHomework(payload: StudentSubmitPayload) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        this.applyPreviewSubmit(payload)
        return
      }

      await submitStudentHomework(payload)
      await Promise.allSettled([
        this.loadStudentHomeworks(),
        this.loadStudentHomeworkDetail(payload.homeworkId),
        this.loadStudentSubmissions(payload.homeworkId),
        this.loadStudentReviews(payload.homeworkId)
      ])
    },
    async loadStudentMessages(readStatus = 'all') {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        return {
          list: this.studentMessages,
          total: this.studentMessages.length,
          pageNo: 1,
          pageSize: this.studentMessages.length || 20
        } satisfies PageResult<MessageItem>
      }

      const page = await fetchStudentMessagePage(readStatus)
      this.studentMessages = page.list
      return page
    },
    async loadParentChildren() {
      this.syncProfilesFromSession()

      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        this.parentProfile.headline = buildParentHeadline(this.children)
        if (!this.activeChildId && this.children[0]) {
          this.activeChildId = this.children[0].id
        }
        return this.children
      }

      const list = await fetchParentChildren()
      this.children = list
      this.parentProfile.headline = buildParentHeadline(list)

      if (!this.activeChildId || !list.some((item) => item.id === this.activeChildId)) {
        this.activeChildId = list[0]?.id ?? ''
      }

      return list
    },
    async ensureParentActiveChild() {
      if (this.currentChild) {
        return this.currentChild
      }

      const children = await this.loadParentChildren()
      return children[0] ?? null
    },
    async loadParentHomeworks(studentId?: string, tab = 'all') {
      let targetStudentId = studentId ?? this.activeChildId
      if (!targetStudentId) {
        const currentChild = await this.ensureParentActiveChild()
        if (!currentChild) {
          return []
        }
        targetStudentId = currentChild.id
      }

      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        return tab === 'all'
          ? (this.parentHomeworks[targetStudentId] ?? [])
          : (this.parentHomeworks[targetStudentId] ?? []).filter((item) => item.status === tab)
      }

      const list = await fetchParentHomeworkList(targetStudentId, tab)
      this.parentHomeworks = {
        ...this.parentHomeworks,
        [targetStudentId]: list
      }
      return list
    },
    async loadParentHomeworkDetail(studentId: string, homeworkId: string) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        return this.getParentHomework(homeworkId, studentId)
      }

      const detail = await fetchParentHomeworkDetail(studentId, homeworkId)
      this.cacheParentHomework(studentId, detail)
      return detail
    },
    async assistSubmitHomework(payload: StudentSubmitPayload) {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        this.applyPreviewSubmit(payload)
        return
      }

      await assistParentSubmit(payload)

      if (payload.studentId) {
        await Promise.allSettled([
          this.loadParentHomeworks(payload.studentId),
          this.loadParentHomeworkDetail(payload.studentId, payload.homeworkId)
        ])
      }
    },
    async loadParentMessages(readStatus = 'all') {
      if (shouldUsePreviewData(useAuthStore().session?.token)) {
        return {
          list: this.parentMessages,
          total: this.parentMessages.length,
          pageNo: 1,
          pageSize: this.parentMessages.length || 20
        } satisfies PageResult<MessageItem>
      }

      const page = await fetchParentMessagePage(readStatus)
      this.parentMessages = page.list
      return page
    },
    markMessagesRead(role: UserRole) {
      const targetList: MessageItem[] = role === 'student' ? this.studentMessages : this.parentMessages
      targetList.forEach((item) => {
        item.unread = false
      })
    }
  }
})
