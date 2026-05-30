import { getAuthSession } from '@/utils/auth-session-clean'
import { requestJson } from '@/api/teacher-http-clean'
import type {
  StudentHomeworkRecord,
  StudentHomeworkReview,
  StudentHomeworkSubmission,
  StudentMessageItem,
  StudentPageResult,
  StudentWrongBookPracticeDetail,
  StudentWrongBookPracticeHistoryRecord,
  StudentWrongBookPracticePlan,
  StudentWrongBookPracticeSubmitPayload,
  StudentWrongBookPracticeSubmitResult,
  StudentSubjectOption,
  StudentSubmitPayload,
  StudentWrongBookCreatePayload,
  StudentWrongBookFixPayload,
  StudentWrongBookRecord
} from '@/types/student-portal'

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

export function fetchStudentHomeworkList(tab = 'all') {
  return requestJson<StudentHomeworkRecord[]>(
    withQuery('/api/student/homeworks', {
      tab
    })
  )
}

export function fetchStudentHomeworkDetail(homeworkId: string) {
  return requestJson<StudentHomeworkRecord>(`/api/student/homeworks/${homeworkId}`)
}

export function fetchStudentSubmissionList(homeworkId: string) {
  return requestJson<StudentHomeworkSubmission[]>(`/api/student/homeworks/${homeworkId}/submissions`)
}

export function fetchStudentReviewList(homeworkId: string) {
  return requestJson<StudentHomeworkReview[]>(`/api/student/homeworks/${homeworkId}/reviews`)
}

export function submitStudentHomework(payload: StudentSubmitPayload) {
  return requestJson<null>(`/api/student/homeworks/${payload.homeworkId}/submit`, {
    method: 'POST',
    body: JSON.stringify({
      text: payload.text,
      images: payload.images,
      assistedByParent: payload.assistedByParent ?? false
    })
  })
}

export function fetchStudentMessagePage(readStatus = 'all', pageNo = 1, pageSize = 50) {
  return requestJson<StudentPageResult<StudentMessageItem>>(
    withQuery('/api/student/notifications', {
      readStatus,
      pageNo,
      pageSize
    })
  )
}

export function fetchStudentMessageDetail(messageId: string) {
  return requestJson<StudentMessageItem>(`/api/student/notifications/${messageId}`)
}

export function markStudentMessageRead(messageId: string) {
  return requestJson<null>(`/api/student/notifications/${messageId}/read`, {
    method: 'POST',
    body: JSON.stringify({})
  })
}

export function fetchStudentWrongBookSubjects() {
  return requestJson<StudentSubjectOption[]>('/api/student/wrong-book/subjects')
}

export function fetchStudentWrongBookPage(subjectCode = 'all', status = 'all', pageNo = 1, pageSize = 50) {
  return requestJson<StudentPageResult<StudentWrongBookRecord>>(
    withQuery('/api/student/wrong-book', {
      subjectCode,
      status,
      pageNo,
      pageSize
    })
  )
}

export function fetchStudentWrongBookDetail(wrongBookId: string) {
  return requestJson<StudentWrongBookRecord>(`/api/student/wrong-book/${wrongBookId}`)
}

export function createStudentWrongBook(payload: StudentWrongBookCreatePayload) {
  return requestJson<{ wrongBookId?: string }>('/api/student/wrong-book', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function submitStudentWrongBookFix(wrongBookId: string, payload: StudentWrongBookFixPayload) {
  return requestJson<null>(`/api/student/wrong-book/${wrongBookId}/fix`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function markStudentWrongBookMastered(wrongBookId: string) {
  return requestJson<null>(`/api/student/wrong-book/${wrongBookId}/mastered`, {
    method: 'POST',
    body: JSON.stringify({})
  })
}

export function generateStudentWrongBookPracticePlan(subjectCode = 'all', questionCount = 10) {
  return requestJson<StudentWrongBookPracticePlan>(
    withQuery('/api/student/wrong-book/practice/plan', {
      subjectCode: subjectCode === 'all' ? undefined : subjectCode,
      questionCount
    })
  )
}

export function submitStudentWrongBookPractice(payload: StudentWrongBookPracticeSubmitPayload) {
  return requestJson<StudentWrongBookPracticeSubmitResult>('/api/student/wrong-book/practice/submit', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export function fetchStudentWrongBookPracticeHistory(pageNo = 1, pageSize = 20) {
  return requestJson<StudentPageResult<StudentWrongBookPracticeHistoryRecord>>(
    withQuery('/api/student/wrong-book/practice/history', {
      pageNo,
      pageSize
    })
  )
}

export function fetchStudentWrongBookPracticeDetail(practiceId: string | number) {
  return requestJson<StudentWrongBookPracticeDetail>(`/api/student/wrong-book/practice/${practiceId}`)
}

export async function uploadStudentFile(file: File, bizType: string) {
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
