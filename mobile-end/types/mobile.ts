export type UserRole = 'student' | 'parent'

export type HomeworkStatus = 'pending' | 'submitted' | 'revision' | 'completed' | 'overdue'

export type ReviewStatus = 'unreviewed' | 'completed' | 'revision_required'

export type MessageKind = 'assignment' | 'review' | 'remind' | 'system'
export type WrongBookStatus = 'pending_fix' | 'fixed' | 'mastered'
export type WrongBookSourceType = 'teacher_mark' | 'student_manual' | 'system_auto'

export interface HomeworkAttachment {
  id: string
  name: string
  type: string
  url: string
}

export interface HomeworkReview {
  id?: string
  status: ReviewStatus
  score?: number | string
  level?: string
  comment?: string
  reviewedAt?: string
  images?: string[]
}

export interface HomeworkSubmission {
  id?: string
  operatorRole?: string
  text: string
  images: string[]
  submittedAt: string
  assistedByParent?: boolean
  versionNo?: number
}

export interface HomeworkRecord {
  id: string
  taskId?: string
  title: string
  subject: string
  teacherName: string
  deadline: string
  status: HomeworkStatus
  summary: string
  content: string
  allowParentAssist: boolean
  attachments: HomeworkAttachment[]
  submitTypes: string[]
  hasFeedback: boolean
  latestSubmission?: HomeworkSubmission | null
  review?: HomeworkReview | null
}

export interface StudentProfile {
  id: string
  name: string
  school: string
  className?: string
  studentNo?: string
  headline: string
}

export interface ChildProfile {
  id: string
  name: string
  className: string
  gradeName: string
  pendingCount: number
  submittedCount: number
  revisionCount: number
}

export interface ParentProfile {
  id: string
  name: string
  mobile?: string
  school: string
  headline: string
}

export interface MessageItem {
  id: string
  title: string
  content: string
  time: string
  kind: MessageKind
  unread: boolean
  childName?: string
}

export interface SubjectOption {
  subjectCode: string
  subjectName: string
}

export interface WrongBookAsset {
  id?: string
  assetRole: 'question_image' | 'answer_image' | 'correction_image' | 'analysis_image'
  assetType: string
  assetUrl: string
  assetName?: string
}

export interface WrongBookRecord {
  id: string
  homeworkId?: string
  taskId?: string
  reviewId?: string
  subjectCode?: string
  subjectName: string
  sourceType: WrongBookSourceType
  status: WrongBookStatus
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
  assets: WrongBookAsset[]
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNo: number
  pageSize: number
}

export interface MobileSession {
  role: UserRole
  userId: string
  userName: string
  schoolId?: string
  schoolName: string
  token: string
  permissions?: string[]
}

export interface LoginPayload {
  role: UserRole
  account: string
  password: string
  schoolId?: string
}

export interface StudentSubmitPayload {
  homeworkId: string
  studentId?: string
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
  assets?: WrongBookAsset[]
}

export interface WrongBookFixPayload {
  fixedText: string
  assets?: WrongBookAsset[]
}

export interface MobilePreviewState {
  studentProfile: StudentProfile
  parentProfile: ParentProfile
  children: ChildProfile[]
  activeChildId: string
  studentHomeworks: HomeworkRecord[]
  parentHomeworks: Record<string, HomeworkRecord[]>
  studentWrongBooks: WrongBookRecord[]
  studentMessages: MessageItem[]
  parentMessages: MessageItem[]
}

export interface MobileNotificationSettings {
  masterEnabled: boolean
  assignmentEnabled: boolean
  reviewEnabled: boolean
  reminderEnabled: boolean
  systemEnabled: boolean
  soundEnabled: boolean
  vibrationEnabled: boolean
  quietHoursEnabled: boolean
  quietStart: string
  quietEnd: string
}

export interface MobileSecuritySettings {
  hideAccountIdentifier: boolean
  rememberAccount: boolean
  loginAlertEnabled: boolean
  appLockEnabled: boolean
  biometricEnabled: boolean
  passwordCheckedAt: string
}
