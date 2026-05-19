import { getAuthSession } from '@/utils/auth-session-clean'
import { requestJson } from '@/api/teacher-http-clean'
import type {
  AssignmentFormInput,
  HomeworkAsset,
  HomeworkDetail,
  HomeworkListItem,
  HomeworkListQuery,
  HomeworkOverviewStats,
  TeacherWrongBookItemInput,
  HomeworkStatsQuery,
  TeacherMessageFormInput,
  TeacherMessageQuery,
  TeacherMessageRecord,
  HomeworkTaskDetail,
  HomeworkTaskListItem,
  HomeworkTaskQuery,
  TeacherClassBindingCandidate,
  TeacherClassBindingPayload,
  TeachingClassRelation
} from '@/types/teacher-portal'

interface PagedData<T> {
  list: T[]
  total: number
  pageNo?: number
  pageSize?: number
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? ''

function buildUrl(path: string) {
  return API_BASE_URL ? `${API_BASE_URL}${path}` : path
}

function withQuery(path: string, params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, `${value}`)
    }
  })

  const queryString = search.toString()
  return queryString ? `${path}?${queryString}` : path
}

function buildHomeworkPayload(form: AssignmentFormInput, publishNow: boolean) {
  return {
    title: form.title.trim(),
    subjectCode: form.subjectCode,
    contentText: form.contentText.trim(),
    classIds: form.classIds,
    deadlineAt: form.deadlineAt,
    allowLateSubmit: form.allowLateSubmit,
    allowResubmit: form.allowResubmit,
    needParentConfirm: form.needParentConfirm,
    submitTypes: form.submitTypes,
    attachments: form.attachments,
    publishNow
  }
}

function buildTeacherMessagePayload(form: TeacherMessageFormInput) {
  return {
    bizType: form.bizType,
    scopeType: form.scopeType,
    homeworkId: form.homeworkId,
    classIds: form.classIds,
    receiverRole: form.receiverRole,
    notifyChannels: form.notifyChannels,
    notifyTitle: form.notifyTitle.trim(),
    notifyContent: form.notifyContent.trim()
  }
}

export async function fetchTeachingClasses(subjectCode?: string) {
  return requestJson<TeachingClassRelation[]>(
    withQuery('/api/teacher/classes', {
      subjectCode
    })
  )
}

export async function fetchTeacherBindingCandidates(keyword?: string) {
  return requestJson<TeacherClassBindingCandidate[]>(
    withQuery('/api/teacher/class-bindings/candidates', {
      keyword
    })
  )
}

export async function createTeacherClassBinding(payload: TeacherClassBindingPayload) {
  return requestJson<void>('/api/teacher/class-bindings', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export async function fetchTeacherHomeworks(query: HomeworkListQuery = {}) {
  return requestJson<PagedData<HomeworkListItem>>(
    withQuery('/api/teacher/homeworks', {
      keyword: query.keyword,
      classId: query.classId,
      subjectCode: query.subjectCode,
      status: query.status,
      pageNo: query.pageNo ?? 1,
      pageSize: query.pageSize ?? 200
    })
  )
}

export async function createTeacherHomework(form: AssignmentFormInput, publishNow: boolean) {
  return requestJson<{ homeworkId: number | string; status: string }>('/api/teacher/homeworks', {
    method: 'POST',
    body: JSON.stringify(buildHomeworkPayload(form, publishNow))
  })
}

export async function updateTeacherHomework(
  homeworkId: number | string,
  form: AssignmentFormInput,
  publishAfterSave: boolean
) {
  const data = await requestJson<void>(`/api/teacher/homeworks/${homeworkId}`, {
    method: 'PUT',
    body: JSON.stringify(buildHomeworkPayload(form, false))
  })

  if (publishAfterSave) {
    await publishTeacherHomework(homeworkId)
  }

  return data
}

export async function publishTeacherHomework(homeworkId: number | string) {
  return requestJson<void>(`/api/teacher/homeworks/${homeworkId}/publish`, {
    method: 'POST',
    body: JSON.stringify({})
  })
}

export async function revokeTeacherHomework(homeworkId: number | string, reason: string) {
  return requestJson<void>(`/api/teacher/homeworks/${homeworkId}/revoke`, {
    method: 'POST',
    body: JSON.stringify({
      reason
    })
  })
}

export async function deleteTeacherHomework(homeworkId: number | string) {
  return requestJson<void>(`/api/teacher/homeworks/${homeworkId}`, {
    method: 'DELETE'
  })
}

export async function fetchTeacherHomeworkDetail(homeworkId: number | string) {
  return requestJson<HomeworkDetail>(`/api/teacher/homeworks/${homeworkId}`)
}

export async function fetchTeacherHomeworkTasks(homeworkId: number | string, query: HomeworkTaskQuery = {}) {
  return requestJson<PagedData<HomeworkTaskListItem>>(
    withQuery(`/api/teacher/homeworks/${homeworkId}/tasks`, {
      classId: query.classId,
      taskStatus: query.taskStatus,
      keyword: query.keyword,
      pageNo: query.pageNo ?? 1,
      pageSize: query.pageSize ?? 200
    })
  )
}

export async function fetchTeacherTaskDetail(taskId: number | string) {
  return requestJson<HomeworkTaskDetail>(`/api/teacher/tasks/${taskId}`)
}

export async function submitTeacherTaskReview(
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
  return requestJson<void>(`/api/teacher/tasks/${taskId}/reviews`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export async function remindTeacherHomework(
  homeworkId: number | string,
  payload: {
    remindType?: 'pending' | 'overdue'
    classId?: number | string
  } = {}
) {
  return requestJson<void>(`/api/teacher/homeworks/${homeworkId}/remind`, {
    method: 'POST',
    body: JSON.stringify({
      remindType: payload.remindType ?? 'pending',
      classId: payload.classId
    })
  })
}

export async function fetchTeacherHomeworkOverview(query: HomeworkStatsQuery = {}) {
  return requestJson<HomeworkOverviewStats>(
    withQuery('/api/teacher/stats/homework-overview', {
      classId: query.classId,
      subjectCode: query.subjectCode,
      startDate: query.startDate,
      endDate: query.endDate
    })
  )
}

export async function fetchTeacherMessageRecords(query: TeacherMessageQuery = {}) {
  return requestJson<PagedData<TeacherMessageRecord>>(
    withQuery('/api/teacher/messages', {
      keyword: query.keyword,
      bizType: query.bizType,
      sendStatus: query.sendStatus,
      pageNo: query.pageNo ?? 1,
      pageSize: query.pageSize ?? 20
    })
  )
}

export async function createTeacherMessage(form: TeacherMessageFormInput) {
  return requestJson<{ messageId: number | string }>('/api/teacher/messages', {
    method: 'POST',
    body: JSON.stringify(buildTeacherMessagePayload(form))
  })
}

export async function sendTeacherMessage(form: TeacherMessageFormInput) {
  return requestJson<{ messageId?: number | string } | void>('/api/teacher/messages/send', {
    method: 'POST',
    body: JSON.stringify(buildTeacherMessagePayload(form))
  })
}

export async function deleteTeacherMessage(messageId: number | string) {
  return requestJson<void>(`/api/teacher/messages/${messageId}`, {
    method: 'DELETE'
  })
}

export async function uploadTeacherFile(file: File, bizType: string) {
  const session = getAuthSession()
  const formData = new FormData()

  formData.set('file', file)
  formData.set('bizType', bizType)

  let response: Response

  try {
    response = await fetch(buildUrl('/api/files/upload'), {
      method: 'POST',
      headers: session?.token ? { Authorization: `Bearer ${session.token}` } : undefined,
      body: formData
    })
  } catch {
    throw new Error('无法连接文件上传服务，请稍后重试。')
  }

  const payload = (await response.json().catch(() => null)) as
    | { code?: number; message?: string; data?: { fileUrl: string; fileName?: string; fileSize?: number } }
    | null

  if (!response.ok) {
    throw new Error(payload?.message || '图片上传失败，请稍后重试。')
  }

  if (typeof payload?.code === 'number' && payload.code !== 0) {
    throw new Error(payload.message || '图片上传失败，请稍后重试。')
  }

  if (!payload?.data?.fileUrl) {
    throw new Error('上传接口未返回文件地址。')
  }

  return payload.data
}
