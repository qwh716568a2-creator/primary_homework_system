import { getAuthSession } from '@/utils/auth-session-clean'
import { requestJson } from '@/api/teacher-http-clean'
import type {
  ParentAssistSubmitPayload,
  ParentChildProfile,
  ParentHomeworkRecord,
  ParentMessageItem,
  ParentPageResult
} from '@/types/parent-portal'

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

export function fetchParentChildren() {
  return requestJson<ParentChildProfile[]>('/api/parent/students')
}

export function fetchParentHomeworkList(studentId: string, tab = 'all') {
  return requestJson<ParentHomeworkRecord[]>(
    withQuery(`/api/parent/students/${studentId}/homeworks`, {
      tab
    })
  )
}

export function fetchParentHomeworkDetail(studentId: string, homeworkId: string) {
  return requestJson<ParentHomeworkRecord>(`/api/parent/students/${studentId}/homeworks/${homeworkId}`)
}

export function assistParentSubmit(payload: ParentAssistSubmitPayload) {
  return requestJson<null>(`/api/parent/students/${payload.studentId}/homeworks/${payload.homeworkId}/submissions`, {
    method: 'POST',
    body: JSON.stringify({
      studentId: payload.studentId,
      text: payload.text,
      images: payload.images,
      assistedByParent: true
    })
  })
}

export function fetchParentMessagePage(readStatus = 'all', pageNo = 1, pageSize = 50) {
  return requestJson<ParentPageResult<ParentMessageItem>>(
    withQuery('/api/parent/notifications', {
      readStatus,
      pageNo,
      pageSize
    })
  )
}

export function fetchParentMessageDetail(messageId: string) {
  return requestJson<ParentMessageItem>(`/api/parent/notifications/${messageId}`)
}

export function markParentMessageRead(messageId: string) {
  return requestJson<null>(`/api/parent/notifications/${messageId}/read`, {
    method: 'POST',
    body: JSON.stringify({})
  })
}

export async function uploadParentFile(file: File, bizType: string) {
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
    throw new Error(payload?.message || '文件上传失败，请稍后重试。')
  }

  if (typeof payload?.code === 'number' && payload.code !== 0) {
    throw new Error(payload.message || '文件上传失败，请稍后重试。')
  }

  if (!payload?.data?.fileUrl) {
    throw new Error('上传接口未返回文件地址。')
  }

  return payload.data
}
