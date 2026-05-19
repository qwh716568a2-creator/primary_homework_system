export type StudentHomeworkStatus = 'pending' | 'submitted' | 'revision' | 'completed' | 'overdue'
export type StudentReviewStatus = 'unreviewed' | 'completed' | 'revision_required'
export type StudentMessageKind = 'assignment' | 'review' | 'remind' | 'system'
export type StudentWrongBookStatus = 'pending_fix' | 'fixed' | 'mastered'
export type StudentWrongBookSourceType = 'teacher_mark' | 'student_manual' | 'system_auto'

export interface StudentHomeworkAttachment {
  id?: string
  name: string
  type: string
  url: string
}

export interface StudentHomeworkReview {
  id?: string
  status: StudentReviewStatus
  score?: number | string
  level?: string
  comment?: string
  reviewedAt?: string
  images?: string[]
}

export interface StudentHomeworkSubmission {
  id?: string
  operatorRole?: string
  text: string
  images: string[]
  submittedAt: string
  assistedByParent?: boolean
  versionNo?: number
}

export interface StudentHomeworkRecord {
  id: string
  taskId?: string
  title: string
  subject: string
  teacherName: string
  deadline: string
  status: StudentHomeworkStatus
  summary: string
  content: string
  allowParentAssist: boolean
  attachments: StudentHomeworkAttachment[]
  submitTypes: string[]
  hasFeedback: boolean
  latestSubmission?: StudentHomeworkSubmission | null
  review?: StudentHomeworkReview | null
}

export interface StudentMessageItem {
  id: string
  title: string
  content: string
  time: string
  kind: StudentMessageKind
  unread: boolean
}

export interface StudentSubjectOption {
  subjectCode: string
  subjectName: string
}

export interface StudentWrongBookAsset {
  id?: string
  assetRole: 'question_image' | 'answer_image' | 'correction_image' | 'analysis_image'
  assetType: string
  assetUrl: string
  assetName?: string
}

export interface StudentWrongBookRecord {
  id: string
  homeworkId?: string
  taskId?: string
  reviewId?: string
  subjectCode?: string
  subjectName: string
  sourceType: StudentWrongBookSourceType
  status: StudentWrongBookStatus
  questionNo?: string
  questionText: string
  studentAnswer?: string
  correctAnswer?: string
  analysisText?: string
  wrongReasonCode?: string
  wrongReasonLabel?: string
  teacherName?: string
  createdAt: string
  lastFixedAt?: string
  lastFixedText?: string
  fixCount?: number
  assets: StudentWrongBookAsset[]
}

export interface StudentPageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
}

export interface StudentProfileSummary {
  name: string
  school: string
  account: string
  className?: string
  headline: string
}

export interface StudentSubmitPayload {
  homeworkId: string
  text: string
  images: string[]
  assistedByParent?: boolean
}

export interface StudentWrongBookCreatePayload {
  homeworkId?: string
  taskId?: string
  reviewId?: string
  subjectCode?: string
  subjectName?: string
  questionNo?: string
  questionText: string
  studentAnswer?: string
  correctAnswer?: string
  analysisText?: string
  wrongReasonCode?: string
  assets?: StudentWrongBookAsset[]
}

export interface StudentWrongBookFixPayload {
  fixedText: string
  assets?: StudentWrongBookAsset[]
}
