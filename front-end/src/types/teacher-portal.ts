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
  classId: number | string
  className: string
  subjectCode: string
  subjectName: string
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
