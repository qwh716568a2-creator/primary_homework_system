export type ParentHomeworkStatus = 'pending' | 'submitted' | 'revision' | 'completed' | 'overdue'
export type ParentReviewStatus = 'unreviewed' | 'completed' | 'revision_required'
export type ParentMessageKind = 'assignment' | 'review' | 'remind' | 'system'

export interface ParentHomeworkAttachment {
  id?: string
  name: string
  type: string
  url: string
}

export interface ParentHomeworkReview {
  id?: string
  status: ParentReviewStatus
  score?: number | string
  level?: string
  comment?: string
  reviewedAt?: string
  images?: string[]
}

export interface ParentHomeworkSubmission {
  id?: string
  operatorRole?: string
  text: string
  images: string[]
  submittedAt: string
  assistedByParent?: boolean
  versionNo?: number
}

export interface ParentHomeworkRecord {
  id: string
  taskId?: string
  title: string
  subject: string
  teacherName: string
  deadline: string
  status: ParentHomeworkStatus
  summary: string
  content: string
  allowParentAssist: boolean
  attachments: ParentHomeworkAttachment[]
  submitTypes: string[]
  hasFeedback: boolean
  latestSubmission?: ParentHomeworkSubmission | null
  review?: ParentHomeworkReview | null
}

export interface ParentChildProfile {
  id: string
  name: string
  className: string
  gradeName: string
  pendingCount: number
  submittedCount: number
  revisionCount: number
}

export interface ParentMessageItem {
  id: string
  title: string
  content: string
  time: string
  kind: ParentMessageKind
  unread: boolean
  childName?: string
}

export interface ParentPageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
}

export interface ParentProfileSummary {
  name: string
  school: string
  account: string
  headline: string
}

export interface ParentAssistSubmitPayload {
  studentId: string
  homeworkId: string
  text: string
  images: string[]
}
