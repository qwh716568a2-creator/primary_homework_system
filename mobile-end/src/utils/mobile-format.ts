import type { HomeworkStatus, ReviewStatus } from '@/types/mobile'

const statusLabelMap: Record<HomeworkStatus, string> = {
  pending: '待完成',
  submitted: '已提交',
  revision: '待订正',
  completed: '已完成',
  overdue: '已逾期'
}

const statusClassMap: Record<HomeworkStatus, string> = {
  pending: 'status-pending',
  submitted: 'status-submitted',
  revision: 'status-revision',
  completed: 'status-completed',
  overdue: 'status-overdue'
}

const reviewLabelMap: Record<ReviewStatus, string> = {
  unreviewed: '待批改',
  completed: '已批改',
  revision_required: '待订正'
}

export function resolveHomeworkStatusLabel(status: HomeworkStatus) {
  return statusLabelMap[status]
}

export function resolveHomeworkStatusClass(status: HomeworkStatus) {
  return statusClassMap[status]
}

export function resolveReviewLabel(status: ReviewStatus) {
  return reviewLabelMap[status]
}
