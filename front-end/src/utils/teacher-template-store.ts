import type { AssignmentFormInput } from '@/types/teacher-portal'

const TEMPLATE_STORAGE_KEY = 'primary-homework-teacher-templates'

export interface TeacherAssignmentTemplate {
  templateId: string
  templateName: string
  subjectCode: string
  contentText: string
  submitTypes: AssignmentFormInput['submitTypes']
  allowLateSubmit: boolean
  allowResubmit: boolean
  needParentConfirm: boolean
  attachments: AssignmentFormInput['attachments']
}

function canUseStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

const builtInTemplates: TeacherAssignmentTemplate[] = [
  {
    templateId: 'builtin-reading',
    templateName: '课文朗读作业',
    subjectCode: 'chinese',
    contentText: '完成指定课文朗读，拍照上传课后练习，并在家长陪同下检查字词。',
    submitTypes: ['text', 'image'],
    allowLateSubmit: true,
    allowResubmit: true,
    needParentConfirm: true,
    attachments: []
  },
  {
    templateId: 'builtin-math',
    templateName: '数学口算练习',
    subjectCode: 'math',
    contentText: '完成口算卡指定题目，书写清晰，拍照上传。',
    submitTypes: ['image'],
    allowLateSubmit: true,
    allowResubmit: true,
    needParentConfirm: false,
    attachments: []
  },
  {
    templateId: 'builtin-english',
    templateName: '英语跟读打卡',
    subjectCode: 'english',
    contentText: '完成单词跟读和课文朗读，可上传录音或文字打卡记录。',
    submitTypes: ['text', 'file', 'mixed'],
    allowLateSubmit: true,
    allowResubmit: true,
    needParentConfirm: false,
    attachments: []
  }
]

export function getTeacherAssignmentTemplates() {
  if (!canUseStorage()) {
    return builtInTemplates
  }

  try {
    const raw = window.localStorage.getItem(TEMPLATE_STORAGE_KEY)
    const userTemplates = raw ? (JSON.parse(raw) as TeacherAssignmentTemplate[]) : []
    return [...builtInTemplates, ...userTemplates]
  } catch {
    return builtInTemplates
  }
}

export function saveTeacherAssignmentTemplate(template: Omit<TeacherAssignmentTemplate, 'templateId'>) {
  if (!canUseStorage()) {
    return
  }

  const userTemplates = getTeacherAssignmentTemplates().filter(
    (item) => !item.templateId.startsWith('builtin-')
  )

  userTemplates.unshift({
    ...template,
    templateId: `custom-${Date.now()}`
  })

  window.localStorage.setItem(TEMPLATE_STORAGE_KEY, JSON.stringify(userTemplates))
}
