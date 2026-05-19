export type AssignmentState = 'draft' | 'published' | 'revoked' | 'closed'
export type SubmissionStatus = 'pending' | 'submitted' | 'revision_required' | 'completed' | 'overdue'
export type ReviewStatus = 'unreviewed' | 'completed' | 'revision_required'
export type SubmissionMethod = 'text' | 'image' | 'file' | 'mixed'

export interface TeacherProfile {
  name: string
  role: string
  school: string
  avatar: string
}

export interface DashboardCard {
  label: string
  value: string
  hint: string
  tone: 'sky' | 'teal' | 'amber' | 'rose'
}

export interface TeachingClassRelation {
  relationId?: number | string
  classId: number | string
  className: string
  gradeId?: number | string
  gradeName?: string
  schoolId?: number | string
  schoolName?: string
  subjectCode: string
  subjectName: string
  studentCount?: number
  isHeadTeacher?: boolean
  status?: string
}

export interface TeacherClassBindingSubject {
  subjectCode: string
  subjectName?: string
  teacherId?: number | string
  teacherName?: string
}

export interface TeacherClassBindingCandidate {
  classId: number | string
  className: string
  gradeId?: number | string
  gradeName?: string
  schoolId?: number | string
  schoolName?: string
  studentCount?: number
  status?: string
  subjectBindings?: TeacherClassBindingSubject[]
}

export interface TeacherClassBindingPayload {
  classId: number | string
  subjectCode: string
  isHeadTeacher?: boolean
}

export interface SubjectOption {
  subjectCode: string
  subjectName: string
}

export interface HomeworkAsset {
  assetType: 'file' | 'image' | 'audio' | 'video' | string
  assetUrl: string
  assetName?: string
  assetSize?: number
}

export type TeacherWrongReasonCode =
  | 'calc_error'
  | 'concept_error'
  | 'reading_error'
  | 'writing_error'
  | 'careless_error'
  | 'other'

export interface TeacherWrongBookItemInput {
  questionNo?: string
  questionText?: string
  studentAnswer?: string
  correctAnswer?: string
  analysisText?: string
  wrongReasonCode?: TeacherWrongReasonCode | string
  assets?: HomeworkAsset[]
}

export interface HomeworkListItem {
  homeworkId: number | string
  title: string
  subjectCode: string
  subjectName: string
  classNames: string[]
  deadlineAt: string
  status: AssignmentState
  submittedCount: number
  pendingCount: number
  revisionRequiredCount: number
}

export interface HomeworkBaseInfo {
  homeworkId: number | string
  title: string
  subjectCode: string
  contentText: string
  deadlineAt: string
  status: AssignmentState
  allowLateSubmit?: boolean
  allowResubmit?: boolean
  needParentConfirm?: boolean
  submitTypes?: SubmissionMethod[]
}

export interface HomeworkClassSummary {
  classId: number | string
  className: string
  studentCount: number
  submittedCount: number
  completedCount: number
  revisionRequiredCount: number
  overdueCount: number
}

export interface HomeworkDetail {
  baseInfo: HomeworkBaseInfo
  classList: HomeworkClassSummary[]
  attachments: HomeworkAsset[]
}

export interface HomeworkTaskListItem {
  taskId: number | string
  studentId: number | string
  studentName: string
  classId: number | string
  className: string
  taskStatus: SubmissionStatus
  reviewStatus: ReviewStatus
  latestSubmittedAt?: string
  submissionCount: number
  isLate: boolean
}

export interface HomeworkTaskInfo {
  taskId: number | string
  studentId: number | string
  studentName: string
  taskStatus: SubmissionStatus
  reviewStatus: ReviewStatus
}

export interface HomeworkSubmission {
  submissionId: number | string
  versionNo: number
  operatorRole: 'student' | 'parent' | string
  submittedAt: string
  submitText?: string
  assets: HomeworkAsset[]
}

export interface HomeworkReview {
  reviewId?: number | string
  reviewStatus: ReviewStatus
  score?: number | string
  scoreLevel?: string
  commentText?: string
  reviewedAt?: string
  reviewAssets?: HomeworkAsset[]
  wrongItems?: TeacherWrongBookItemInput[]
}

export interface HomeworkTaskDetail {
  taskInfo: HomeworkTaskInfo
  submissions: HomeworkSubmission[]
  reviews: HomeworkReview[]
}

export interface HomeworkOverviewStats {
  publishCount: number
  submissionRate: number
  onTimeRate: number
  reviewRate: number
  revisionRequiredRate: number
}

export interface AssignmentFormInput {
  title: string
  subjectCode: string
  classIds: Array<number | string>
  deadlineAt: string
  contentText: string
  submitTypes: SubmissionMethod[]
  allowLateSubmit: boolean
  allowResubmit: boolean
  needParentConfirm: boolean
  attachments: HomeworkAsset[]
}

export interface HomeworkListQuery {
  keyword?: string
  classId?: number | string
  subjectCode?: string
  status?: string
  pageNo?: number
  pageSize?: number
}

export interface HomeworkTaskQuery {
  classId?: number | string
  taskStatus?: string
  keyword?: string
  pageNo?: number
  pageSize?: number
}

export interface HomeworkStatsQuery {
  classId?: number | string
  subjectCode?: string
  startDate?: string
  endDate?: string
}

export type TeacherMessageBizType =
  | 'custom_notice'
  | 'homework_publish'
  | 'deadline_reminder'
  | 'submission_reminder'
  | 'review_result'

export type TeacherMessageScopeType = 'class' | 'homework'
export type TeacherMessageReceiverRole = 'student' | 'parent' | 'both'
export type TeacherMessageChannel = 'in_app' | 'wechat' | 'sms'
export type TeacherMessageSendStatus = 'pending' | 'success' | 'failed'

export interface TeacherMessageFormInput {
  bizType: TeacherMessageBizType
  scopeType: TeacherMessageScopeType
  homeworkId?: number | string
  classIds: Array<number | string>
  receiverRole: TeacherMessageReceiverRole
  notifyChannels: TeacherMessageChannel[]
  notifyTitle: string
  notifyContent: string
}

export interface TeacherMessageRecord {
  messageId: number | string
  bizType: TeacherMessageBizType | string
  scopeType: TeacherMessageScopeType | string
  notifyTitle: string
  notifyContent: string
  receiverRole: TeacherMessageReceiverRole | string
  notifyChannels: TeacherMessageChannel[]
  classIds?: Array<number | string>
  classNames?: string[]
  homeworkId?: number | string
  homeworkTitle?: string
  receiverCount?: number
  successCount?: number
  failedCount?: number
  sendStatus: TeacherMessageSendStatus | string
  sentAt?: string
  createdAt?: string
}

export interface TeacherMessageQuery {
  keyword?: string
  bizType?: string
  sendStatus?: string
  pageNo?: number
  pageSize?: number
}
