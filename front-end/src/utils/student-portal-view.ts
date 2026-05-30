import type { StudentHomeworkRecord, StudentMessageItem, StudentWrongBookRecord } from '@/types/student-portal'

export const studentHomeworkStatusMap = {
  pending: { label: '待完成', tone: 'amber' },
  submitted: { label: '已提交', tone: 'sky' },
  revision: { label: '待订正', tone: 'rose' },
  completed: { label: '已完成', tone: 'teal' },
  overdue: { label: '已逾期', tone: 'rose' }
} as const

export const studentReviewStatusMap = {
  unreviewed: { label: '待批改', tone: 'slate' },
  completed: { label: '已反馈', tone: 'teal' },
  revision_required: { label: '需订正', tone: 'rose' }
} as const

export const studentWrongBookStatusMap = {
  pending_fix: { label: '待订正', tone: 'amber' },
  fixed: { label: '已订正', tone: 'sky' },
  mastered: { label: '已掌握', tone: 'teal' }
} as const

export const studentWrongBookPoolMap = {
  active_wrong: { label: '活跃错题', tone: 'rose' },
  risky_correct: { label: '风险正确', tone: 'amber' },
  mastered_archive: { label: '已掌握归档', tone: 'teal' }
} as const

export const studentWrongBookPracticeResultMap = {
  correct: { label: '答对', tone: 'teal' },
  wrong: { label: '答错', tone: 'rose' },
  unanswered: { label: '未作答', tone: 'slate' }
} as const

export const studentMessageKindMap: Record<string, string> = {
  assignment: '作业通知',
  review: '批改反馈',
  remind: '提醒消息',
  system: '系统通知',
  homework_publish: '作业通知',
  review_result: '批改反馈',
  deadline_reminder: '提醒消息',
  submission_reminder: '提醒消息',
  custom_notice: '系统通知'
}

export const wrongReasonOptions = [
  { label: '计算错误', value: 'calculation' },
  { label: '审题偏差', value: 'reading' },
  { label: '概念不清', value: 'concept' },
  { label: '书写问题', value: 'writing' },
  { label: '其他原因', value: 'other' }
]

export const wrongReasonMap = Object.fromEntries(wrongReasonOptions.map((item) => [item.value, item.label])) as Record<
  string,
  string
>

export function getStudentTaskStatusLabel(status?: string) {
  return status ? studentHomeworkStatusMap[status as keyof typeof studentHomeworkStatusMap]?.label || status : '待完成'
}

export function getStudentReviewStatusLabel(status?: string) {
  return status ? studentReviewStatusMap[status as keyof typeof studentReviewStatusMap]?.label || status : '待批改'
}

export function getStudentWrongBookStatusLabel(status?: string) {
  return status ? studentWrongBookStatusMap[status as keyof typeof studentWrongBookStatusMap]?.label || status : '待订正'
}

export function getStudentWrongBookPoolLabel(poolType?: string) {
  return poolType
    ? studentWrongBookPoolMap[poolType as keyof typeof studentWrongBookPoolMap]?.label || poolType
    : '活跃错题'
}

export function getStudentWrongBookPracticeResultLabel(result?: string) {
  return result
    ? studentWrongBookPracticeResultMap[result as keyof typeof studentWrongBookPracticeResultMap]?.label || result
    : '未作答'
}

export function getStudentMessageKindLabel(kind?: string) {
  return kind ? studentMessageKindMap[kind] || kind : '消息通知'
}

export function formatStudentDateTime(value?: string) {
  if (!value) return '未同步时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getMonth() + 1}月${date.getDate()}日 ${String(date.getHours()).padStart(2, '0')}:${String(
    date.getMinutes()
  ).padStart(2, '0')}`
}

export function formatStudentFullDateTime(value?: string) {
  if (!value) return '未同步时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(
    2,
    '0'
  )} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

export function resolveStudentDeadlineLabel(value?: string) {
  return getStudentDeadlineHint(value)
}

export function getStudentDeadlineHint(value?: string) {
  if (!value) return '未设置截止时间'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  const diff = date.getTime() - Date.now()
  const absHours = Math.ceil(Math.abs(diff) / 3_600_000)

  if (diff < 0) {
    return absHours <= 24 ? `已过期 ${absHours} 小时` : `已过期 ${Math.ceil(absHours / 24)} 天`
  }

  return absHours <= 24 ? `${absHours} 小时后截止` : `${Math.ceil(absHours / 24)} 天后截止`
}

export function sortStudentHomeworksByDeadline(items: StudentHomeworkRecord[]) {
  return [...items].sort((a, b) => {
    const aTime = new Date(a.deadline || '').getTime() || Number.MAX_SAFE_INTEGER
    const bTime = new Date(b.deadline || '').getTime() || Number.MAX_SAFE_INTEGER
    return aTime - bTime
  })
}

export function sortStudentMessages(items: StudentMessageItem[]) {
  return [...items].sort((a, b) => {
    const aTime = new Date(a.time || '').getTime() || 0
    const bTime = new Date(b.time || '').getTime() || 0
    return bTime - aTime
  })
}

export function sortWrongBookItems(items: StudentWrongBookRecord[]) {
  return [...items].sort((a, b) => {
    const aTime = new Date(a.createdAt || '').getTime() || 0
    const bTime = new Date(b.createdAt || '').getTime() || 0
    return bTime - aTime
  })
}
