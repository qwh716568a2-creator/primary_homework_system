import type { ParentHomeworkRecord, ParentMessageItem } from '@/types/parent-portal'

export const parentTaskStatusMap: Record<string, string> = {
  pending: '待完成',
  submitted: '已提交',
  revision: '待订正',
  completed: '已完成',
  overdue: '已逾期'
}

export const parentReviewStatusMap: Record<string, string> = {
  unreviewed: '待批改',
  completed: '已反馈',
  revision_required: '需订正'
}

export const parentMessageKindMap: Record<string, string> = {
  assignment: '作业通知',
  review: '批改反馈',
  remind: '提醒消息',
  system: '系统通知',
  homework_publish: '作业通知',
  review_result: '批改反馈',
  deadline_reminder: '截止提醒',
  submission_reminder: '提交提醒',
  custom_notice: '通知'
}

export function getParentTaskStatusLabel(status?: string) {
  return status ? parentTaskStatusMap[status] || status : '待完成'
}

export function getParentReviewStatusLabel(status?: string) {
  return status ? parentReviewStatusMap[status] || status : '待批改'
}

export function getParentMessageKindLabel(kind?: string) {
  return kind ? parentMessageKindMap[kind] || kind : '消息通知'
}

export function formatParentDateTime(value?: string) {
  if (!value) return '未同步'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(
    date.getMinutes()
  ).padStart(2, '0')}`
}

export function resolveParentDeadlineLabel(value?: string) {
  return getParentDeadlineHint(value)
}

export function getParentDeadlineHint(value?: string) {
  if (!value) return '未设置截止'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  const diff = date.getTime() - Date.now()
  const absHours = Math.ceil(Math.abs(diff) / 3_600_000)

  if (diff <= 0) {
    return absHours <= 24 ? `已过期 ${absHours} 小时` : `已过期 ${Math.ceil(absHours / 24)} 天`
  }

  return absHours <= 24 ? `${absHours} 小时后截止` : `${Math.ceil(absHours / 24)} 天后截止`
}

export function sortParentHomeworksByDeadline(items: ParentHomeworkRecord[]) {
  return [...items].sort((a, b) => {
    const aTime = new Date(a.deadline || '').getTime() || Number.MAX_SAFE_INTEGER
    const bTime = new Date(b.deadline || '').getTime() || Number.MAX_SAFE_INTEGER
    return aTime - bTime
  })
}

export function sortParentMessages(items: ParentMessageItem[]) {
  return [...items].sort((a, b) => {
    const aTime = new Date(a.time || '').getTime() || 0
    const bTime = new Date(b.time || '').getTime() || 0
    return bTime - aTime
  })
}
