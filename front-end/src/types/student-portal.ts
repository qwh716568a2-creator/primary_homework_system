export type StudentHomeworkStatus = 'pending' | 'submitted' | 'revision' | 'completed' | 'overdue'
export type StudentReviewStatus = 'unreviewed' | 'completed' | 'revision_required'
export type StudentMessageKind = 'assignment' | 'review' | 'remind' | 'system'
export type StudentWrongBookStatus = 'pending_fix' | 'fixed' | 'mastered'
export type StudentWrongBookSourceType = 'teacher_mark' | 'student_manual' | 'system_auto'
export type StudentWrongBookPoolType = 'active_wrong' | 'risky_correct' | 'mastered_archive'
export type StudentWrongBookPracticeStatus = 'generated' | 'in_progress' | 'completed' | 'abandoned'
export type StudentWrongBookPracticeResultStatus = 'correct' | 'wrong' | 'unanswered'

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
  poolType?: StudentWrongBookPoolType
  correctStreak?: number
  masteryScore?: number
  practiceCount?: number
  lastPracticedAt?: string
  lastPracticeResult?: StudentWrongBookPracticeResultStatus
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

export interface StudentWrongBookPracticeItem {
  practiceItemId: string | number
  wrongBookId: string | number
  questionNo?: string
  subjectCode?: string
  subjectName?: string
  questionText: string
  correctAnswer?: string
  itemSourceType: StudentWrongBookPoolType
  itemWeight?: number
  sortNo: number
}

export interface StudentWrongBookPracticePlan {
  practiceId: string | number
  practiceName: string
  questionCount: number
  wrongQuestionCount: number
  riskyQuestionCount: number
  items: StudentWrongBookPracticeItem[]
}

export interface StudentWrongBookPracticeSubmitItem {
  practiceItemId: string | number
  wrongBookId: string | number
  studentAnswer: string
  resultStatus: StudentWrongBookPracticeResultStatus
  usedDurationSeconds: number
}

export interface StudentWrongBookPracticeSubmitPayload {
  practiceId: string | number
  items: StudentWrongBookPracticeSubmitItem[]
}

export interface StudentWrongBookPracticeSubmitResult {
  practiceId: string | number
  correctCount: number
  wrongCount: number
  accuracyRate: number
  masteredCount: number
  returnedToActiveCount: number
}

export interface StudentWrongBookPracticeReviewItem extends StudentWrongBookPracticeItem {
  studentAnswer: string
  resultStatus: StudentWrongBookPracticeResultStatus
  usedDurationSeconds: number
}

export interface StudentWrongBookPracticeHistoryRecord {
  id?: string | number
  practiceId?: string | number
  practiceName: string
  practiceType?: string
  questionCount: number
  wrongQuestionCount?: number
  riskyQuestionCount?: number
  submittedCount?: number
  correctCount: number
  wrongCount: number
  accuracyRate: number
  status: StudentWrongBookPracticeStatus
  generatedAt?: string
  startedAt?: string
  submittedAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface StudentWrongBookPracticeDetail extends StudentWrongBookPracticeHistoryRecord {
  items: StudentWrongBookPracticeReviewItem[]
}
