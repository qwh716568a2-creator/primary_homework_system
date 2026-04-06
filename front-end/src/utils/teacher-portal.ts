import type {
  AssignmentFormInput,
  AssignmentState,
  HomeworkAsset,
  HomeworkDetail,
  HomeworkListItem,
  HomeworkOverviewStats,
  HomeworkReview,
  HomeworkSubmission,
  HomeworkTaskDetail,
  HomeworkTaskListItem,
  ReviewStatus,
  SubmissionMethod,
  SubmissionStatus
} from '@/types/teacher-portal'

export const assignmentStateMap: Record<AssignmentState, { label: string; tone: string }> = {
  draft: { label: '草稿', tone: 'slate' },
  published: { label: '进行中', tone: 'teal' },
  revoked: { label: '已撤回', tone: 'amber' },
  closed: { label: '已结束', tone: 'amber' }
}

export const submissionStatusMap: Record<SubmissionStatus, { label: string; tone: string }> = {
  pending: { label: '未提交', tone: 'slate' },
  submitted: { label: '待批改', tone: 'sky' },
  completed: { label: '已完成', tone: 'teal' },
  revision_required: { label: '待订正', tone: 'rose' },
  overdue: { label: '逾期未交', tone: 'amber' }
}

export const reviewStatusMap: Record<ReviewStatus, { label: string; tone: string }> = {
  unreviewed: { label: '未批改', tone: 'slate' },
  completed: { label: '已完成', tone: 'teal' },
  revision_required: { label: '待订正', tone: 'rose' }
}

export const submissionMethodMap: Record<SubmissionMethod, string> = {
  text: '文字',
  image: '图片',
  file: '附件',
  mixed: '文字 + 图片'
}

export const submissionMethodOptions = [
  { label: '文字', value: 'text' },
  { label: '图片', value: 'image' },
  { label: '附件', value: 'file' },
  { label: '文字 + 图片', value: 'mixed' }
] as const

export function formatDateTime(input?: string) {
  if (!input) {
    return '暂无'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(input))
}

export function formatFullDateTime(input?: string) {
  if (!input) {
    return '暂无'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(input))
}

export function getRelativeDeadline(input?: string) {
  if (!input) {
    return '未设置截止时间'
  }

  const now = Date.now()
  const diff = new Date(input).getTime() - now
  const day = 24 * 60 * 60 * 1000
  const hour = 60 * 60 * 1000

  if (diff <= 0) {
    return '已到截止时间'
  }

  if (diff >= day) {
    return `${Math.ceil(diff / day)} 天后截止`
  }

  return `${Math.ceil(diff / hour)} 小时后截止`
}

export function percentFromRate(rate?: number) {
  return Math.round((rate ?? 0) * 100)
}

export function buildDashboardCards(overview: HomeworkOverviewStats, homeworks: HomeworkListItem[]) {
  const publishedCount = homeworks.filter((item) => item.status === 'published').length
  const pendingCount = homeworks.reduce((count, item) => count + item.pendingCount, 0)
  const revisionCount = homeworks.reduce((count, item) => count + item.revisionRequiredCount, 0)

  return [
    {
      label: '已发布作业',
      value: `${publishedCount}`,
      hint: `统计范围内共发布 ${overview.publishCount} 条作业`,
      tone: 'sky' as const
    },
    {
      label: '待批改',
      value: `${pendingCount}`,
      hint: '优先处理最近有提交记录的作业',
      tone: 'rose' as const
    },
    {
      label: '待订正',
      value: `${revisionCount}`,
      hint: `待订正率 ${percentFromRate(overview.revisionRequiredRate)}%`,
      tone: 'amber' as const
    },
    {
      label: '已批改率',
      value: `${percentFromRate(overview.reviewRate)}%`,
      hint: `提交率 ${percentFromRate(overview.submissionRate)}%`,
      tone: 'teal' as const
    }
  ]
}

export function getLatestSubmission(detail?: HomeworkTaskDetail | null) {
  if (!detail?.submissions.length) {
    return undefined
  }

  return detail.submissions.at(-1)
}

export function buildAsset(assetText: string, assetType: HomeworkAsset['assetType'] = 'file') {
  return {
    assetType,
    assetUrl: assetText.trim(),
    assetName: assetText.trim()
  } satisfies HomeworkAsset
}

export function toAssignmentForm(detail?: HomeworkDetail): AssignmentFormInput {
  if (!detail) {
    return {
      title: '',
      subjectCode: '',
      classIds: [],
      deadlineAt: '',
      contentText: '',
      submitTypes: ['text', 'image'],
      allowLateSubmit: true,
      allowResubmit: true,
      needParentConfirm: false,
      attachments: []
    }
  }

  return {
    title: detail.baseInfo.title,
    subjectCode: detail.baseInfo.subjectCode,
    classIds: detail.classList.map((item) => item.classId),
    deadlineAt: detail.baseInfo.deadlineAt,
    contentText: detail.baseInfo.contentText,
    submitTypes: detail.baseInfo.submitTypes?.length ? [...detail.baseInfo.submitTypes] : ['text', 'image'],
    allowLateSubmit: detail.baseInfo.allowLateSubmit ?? true,
    allowResubmit: detail.baseInfo.allowResubmit ?? true,
    needParentConfirm: detail.baseInfo.needParentConfirm ?? false,
    attachments: [...detail.attachments]
  }
}

export function getHomeworkDisplayClasses(homework: HomeworkListItem) {
  return homework.classNames.join('、')
}

export function getReviewTag(review?: HomeworkReview) {
  if (!review) {
    return '暂无批改记录'
  }

  return review.commentText || review.scoreLevel || `${review.score ?? ''}`.trim() || '已完成批改'
}

export function getSubmissionText(submission?: HomeworkSubmission) {
  return submission?.submitText?.trim() || '学生未填写文字说明。'
}

export function taskNeedsReview(task: HomeworkTaskListItem) {
  return task.taskStatus === 'submitted' && task.reviewStatus === 'unreviewed'
}
