import type {
  ChildProfile,
  HomeworkRecord,
  MessageItem,
  PageResult,
  StudentSubmitPayload
} from '@/types/mobile'
import { request } from '@/services/request'

export function fetchParentChildren() {
  return request<ChildProfile[]>({
    url: '/api/parent/students',
    method: 'GET'
  })
}

export function fetchParentHomeworkList(studentId: string, tab = 'all') {
  return request<HomeworkRecord[]>({
    url: `/api/parent/students/${studentId}/homeworks`,
    method: 'GET',
    data: {
      tab
    }
  })
}

export function fetchParentHomeworkDetail(studentId: string, homeworkId: string) {
  return request<HomeworkRecord>({
    url: `/api/parent/students/${studentId}/homeworks/${homeworkId}`,
    method: 'GET'
  })
}

export function assistParentSubmit(payload: StudentSubmitPayload) {
  if (!payload.studentId) {
    throw new Error('协助提交缺少 studentId')
  }

  return request<null, Omit<StudentSubmitPayload, 'homeworkId'>>({
    url: `/api/parent/students/${payload.studentId}/homeworks/${payload.homeworkId}/submissions`,
    method: 'POST',
    data: {
      studentId: payload.studentId,
      text: payload.text,
      images: payload.images,
      assistedByParent: true
    }
  })
}

export function fetchParentMessagePage(readStatus = 'all', pageNo = 1, pageSize = 50) {
  return request<PageResult<MessageItem>>({
    url: '/api/parent/notifications',
    method: 'GET',
    data: {
      readStatus,
      pageNo,
      pageSize
    }
  })
}
