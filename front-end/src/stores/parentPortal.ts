import { computed, reactive, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  assistParentSubmit,
  fetchParentChildren,
  fetchParentHomeworkDetail,
  fetchParentHomeworkList,
  fetchParentMessageDetail,
  fetchParentMessagePage,
  markParentMessageRead
} from '@/api/parent'
import { clearAuthSession, getAuthSession, persistAuthSession } from '@/utils/auth-session-clean'
import type { AuthSession } from '@/types/auth'
import type {
  ParentAssistSubmitPayload,
  ParentChildProfile,
  ParentHomeworkRecord,
  ParentMessageItem,
  ParentProfileSummary
} from '@/types/parent-portal'

export const useParentPortalStore = defineStore('parent-portal', () => {
  const authUser = ref<AuthSession | null>(getAuthSession())
  const children = ref<ParentChildProfile[]>([])
  const activeChildId = ref('')
  const childHomeworkMap = ref<Record<string, ParentHomeworkRecord[]>>({})
  const homeworkDetailMap = ref<Record<string, ParentHomeworkRecord>>({})
  const messages = ref<ParentMessageItem[]>([])
  const loading = reactive({
    children: false,
    homeworks: false,
    detail: false,
    messages: false,
    action: false
  })

  const isAuthenticated = computed(() => Boolean(authUser.value?.authenticated))

  const currentChild = computed(
    () => children.value.find((item) => item.id === activeChildId.value) ?? children.value[0] ?? null
  )

  const currentChildHomeworks = computed(() =>
    currentChild.value ? childHomeworkMap.value[currentChild.value.id] ?? [] : []
  )

  const profile = computed<ParentProfileSummary>(() => {
    const childCount = children.value.length
    const pending = children.value.reduce((sum, item) => sum + (item.pendingCount ?? 0), 0)
    const headline = childCount > 0 ? `已关联 ${childCount} 位孩子，还有 ${pending} 项作业需要关注。` : '孩子作业与老师反馈已同步。'

    return {
      name: authUser.value?.name || '家长用户',
      school: authUser.value?.school || '校内统一认证',
      account: authUser.value?.account || '未同步',
      headline
    }
  })

  const pendingCount = computed(() => children.value.reduce((sum, item) => sum + (item.pendingCount ?? 0), 0))

  const revisionCount = computed(() => currentChildHomeworks.value.filter((item) => item.status === 'revision').length)

  const submittedCount = computed(() => currentChildHomeworks.value.filter((item) => item.status === 'submitted').length)

  const unreadMessageCount = computed(() => messages.value.filter((item) => item.unread).length)

  const statusOverview = computed(() => [
    {
      label: '待跟进',
      value: pendingCount.value,
      hint: '孩子仍未完成的作业',
      tone: 'amber' as const
    },
    {
      label: '待订正',
      value: revisionCount.value,
      hint: '老师反馈后需要继续修改',
      tone: 'rose' as const
    },
    {
      label: '已提交',
      value: submittedCount.value,
      hint: '当前孩子已提交的作业',
      tone: 'sky' as const
    },
    {
      label: '未读消息',
      value: unreadMessageCount.value,
      hint: '老师反馈与提醒通知',
      tone: 'teal' as const
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
    children.value = []
    activeChildId.value = ''
    childHomeworkMap.value = {}
    homeworkDetailMap.value = {}
    messages.value = []
  }

  function selectChild(childId: string) {
    activeChildId.value = childId
  }

  async function loadChildren() {
    loading.children = true

    try {
      children.value = await fetchParentChildren()
      if (!activeChildId.value && children.value.length > 0) {
        activeChildId.value = children.value[0].id
      }
      return children.value
    } finally {
      loading.children = false
    }
  }

  async function loadHomeworks(studentId = activeChildId.value, tab = 'all') {
    if (!studentId) {
      return []
    }

    loading.homeworks = true

    try {
      const list = await fetchParentHomeworkList(studentId, tab)
      childHomeworkMap.value = {
        ...childHomeworkMap.value,
        [studentId]: list
      }
      return list
    } finally {
      loading.homeworks = false
    }
  }

  async function loadHomeworkDetail(studentId: string, homeworkId: string) {
    loading.detail = true

    try {
      const detail = await fetchParentHomeworkDetail(studentId, homeworkId)
      homeworkDetailMap.value = {
        ...homeworkDetailMap.value,
        [`${studentId}:${homeworkId}`]: detail
      }

      const currentList = childHomeworkMap.value[studentId] ?? []
      const index = currentList.findIndex((item) => item.id === homeworkId)
      if (index >= 0) {
        currentList.splice(index, 1, detail)
        childHomeworkMap.value = {
          ...childHomeworkMap.value,
          [studentId]: [...currentList]
        }
      }

      return detail
    } finally {
      loading.detail = false
    }
  }

  async function loadMessages(readStatus = 'all') {
    loading.messages = true

    try {
      const data = await fetchParentMessagePage(readStatus)
      messages.value = data.list
      return data.list
    } finally {
      loading.messages = false
    }
  }

  async function openMessage(messageId: string) {
    const detail = await fetchParentMessageDetail(messageId)
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
      await markParentMessageRead(messageId)
    } catch {
      // 已读状态先在前端生效，避免接口短暂不可用时阻断查看消息。
    }
  }

  async function assistHomework(payload: ParentAssistSubmitPayload) {
    loading.action = true

    try {
      await assistParentSubmit(payload)
      await Promise.all([loadHomeworks(payload.studentId), loadHomeworkDetail(payload.studentId, payload.homeworkId)])
    } finally {
      loading.action = false
    }
  }

  async function initializeWorkspace() {
    await loadChildren()
    await Promise.all([loadHomeworks(activeChildId.value), loadMessages()])
  }

  function getHomework(studentId: string, homeworkId: string) {
    return (
      homeworkDetailMap.value[`${studentId}:${homeworkId}`] ??
      (childHomeworkMap.value[studentId] ?? []).find((item) => item.id === homeworkId) ??
      null
    )
  }

  return {
    authUser,
    children,
    activeChildId,
    childHomeworkMap,
    homeworkDetailMap,
    messages,
    loading,
    isAuthenticated,
    currentChild,
    currentChildHomeworks,
    profile,
    pendingCount,
    revisionCount,
    submittedCount,
    unreadMessageCount,
    statusOverview,
    setAuthenticatedUser,
    resetState,
    selectChild,
    loadChildren,
    loadHomeworks,
    loadHomeworkDetail,
    loadMessages,
    openMessage,
    markMessageRead,
    assistHomework,
    initializeWorkspace,
    getHomework
  }
})
