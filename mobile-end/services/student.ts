import type {
  HomeworkRecord,
  HomeworkReview,
  HomeworkSubmission,
  MessageItem,
  PageResult,
  SubjectOption,
  StudentWrongBookCreatePayload,
  StudentSubmitPayload,
  WrongBookFixPayload,
  WrongBookRecord
} from '@/types/mobile'
import { request } from '@/services/request'

export function fetchStudentHomeworkList(tab = 'all') {
  return request<HomeworkRecord[]>({
    url: '/api/student/homeworks',
    method: 'GET',
    data: {
      tab
    }
  })
}

export function fetchStudentHomeworkDetail(homeworkId: string) {
  return request<HomeworkRecord>({
    url: `/api/student/homeworks/${homeworkId}`,
    method: 'GET'
  })
}

export function submitStudentHomework(payload: StudentSubmitPayload) {
  return request<null, Omit<StudentSubmitPayload, 'homeworkId'>>({
    url: `/api/student/homeworks/${payload.homeworkId}/submit`,
    method: 'POST',
    data: {
      text: payload.text,
      images: payload.images,
      assistedByParent: payload.assistedByParent
    }
  })
}

export function fetchStudentSubmissionList(homeworkId: string) {
  return request<HomeworkSubmission[]>({
    url: `/api/student/homeworks/${homeworkId}/submissions`,
    method: 'GET'
  })
}

export function fetchStudentReviewList(homeworkId: string) {
  return request<HomeworkReview[]>({
    url: `/api/student/homeworks/${homeworkId}/reviews`,
    method: 'GET'
  })
}

export function fetchStudentMessagePage(readStatus = 'all', pageNo = 1, pageSize = 50) {
  return request<PageResult<MessageItem>>({
    url: '/api/student/notifications',
    method: 'GET',
    data: {
      readStatus,
      pageNo,
      pageSize
    }
  })
}

export function fetchStudentWrongBookSubjects() {
  return request<SubjectOption[]>({
    url: '/api/student/wrong-book/subjects',
    method: 'GET'
  })
}

export function fetchStudentWrongBookPage(subjectCode = 'all', status = 'all', pageNo = 1, pageSize = 50) {
  return request<PageResult<WrongBookRecord>>({
    url: '/api/student/wrong-book',
    method: 'GET',
    data: {
      subjectCode,
      status,
      pageNo,
      pageSize
    }
  })
}

export function fetchStudentWrongBookDetail(wrongBookId: string) {
  return request<WrongBookRecord>({
    url: `/api/student/wrong-book/${wrongBookId}`,
    method: 'GET'
  })
}

export function createStudentWrongBook(payload: StudentWrongBookCreatePayload) {
  return request<{ wrongBookId?: string }, StudentWrongBookCreatePayload>({
    url: '/api/student/wrong-book',
    method: 'POST',
    data: payload
  })
}

export function submitStudentWrongBookFix(wrongBookId: string, payload: WrongBookFixPayload) {
  return request<null, WrongBookFixPayload>({
    url: `/api/student/wrong-book/${wrongBookId}/fix`,
    method: 'POST',
    data: payload
  })
}

export function markStudentWrongBookMastered(wrongBookId: string) {
  return request<null, Record<string, never>>({
    url: `/api/student/wrong-book/${wrongBookId}/mastered`,
    method: 'POST',
    data: {}
  })
}
