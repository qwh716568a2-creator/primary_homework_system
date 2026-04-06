import { requestJson } from '@/api/teacher-http'
import type {
  AssignmentFormInput,
  HomeworkDetail,
  HomeworkListItem,
  HomeworkListQuery,
  HomeworkOverviewStats,
  HomeworkStatsQuery,
  HomeworkTaskDetail,
  HomeworkTaskListItem,
  HomeworkTaskQuery,
  TeachingClassRelation
} from '@/types/teacher-portal'

interface PagedData<T> {
  list: T[]
  total: number
  pageNo?: number
  pageSize?: number
}

function withQuery(path: string, params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, `${value}`)
    }
  })

  const queryString = search.toString()
  return queryString ? `${path}?${queryString}` : path
}

function buildHomeworkPayload(form: AssignmentFormInput, publishNow: boolean) {
  return {
    title: form.title.trim(),
    subjectCode: form.subjectCode,
    contentText: form.contentText.trim(),
    classIds: form.classIds,
    deadlineAt: form.deadlineAt,
    allowLateSubmit: form.allowLateSubmit,
    allowResubmit: form.allowResubmit,
    needParentConfirm: form.needParentConfirm,
    submitTypes: form.submitTypes,
    attachments: form.attachments,
    publishNow
  }
}

export async function fetchTeachingClasses(subjectCode?: string) {
  return requestJson<TeachingClassRelation[]>(
    withQuery('/api/teacher/classes', {
      subjectCode
    })
  )
}

export async function fetchTeacherHomeworks(query: HomeworkListQuery = {}) {
  return requestJson<PagedData<HomeworkListItem>>(
    withQuery('/api/teacher/homeworks', {
      keyword: query.keyword,
      classId: query.classId,
      subjectCode: query.subjectCode,
      status: query.status,
      pageNo: query.pageNo ?? 1,
      pageSize: query.pageSize ?? 200
    })
  )
}

export async function createTeacherHomework(form: AssignmentFormInput, publishNow: boolean) {
  return requestJson<{ homeworkId: number | string; status: string }>('/api/teacher/homeworks', {
    method: 'POST',
    body: JSON.stringify(buildHomeworkPayload(form, publishNow))
  })
}

export async function updateTeacherHomework(
  homeworkId: number | string,
  form: AssignmentFormInput,
  publishAfterSave: boolean
) {
  const data = await requestJson<void>(`/api/teacher/homeworks/${homeworkId}`, {
    method: 'PUT',
    body: JSON.stringify(buildHomeworkPayload(form, false))
  })

  if (publishAfterSave) {
    await publishTeacherHomework(homeworkId)
  }

  return data
}

export async function publishTeacherHomework(homeworkId: number | string) {
  return requestJson<void>(`/api/teacher/homeworks/${homeworkId}/publish`, {
    method: 'POST',
    body: JSON.stringify({})
  })
}

export async function revokeTeacherHomework(homeworkId: number | string, reason: string) {
  return requestJson<void>(`/api/teacher/homeworks/${homeworkId}/revoke`, {
    method: 'POST',
    body: JSON.stringify({
      reason
    })
  })
}

export async function fetchTeacherHomeworkDetail(homeworkId: number | string) {
  return requestJson<HomeworkDetail>(`/api/teacher/homeworks/${homeworkId}`)
}

export async function fetchTeacherHomeworkTasks(homeworkId: number | string, query: HomeworkTaskQuery = {}) {
  return requestJson<PagedData<HomeworkTaskListItem>>(
    withQuery(`/api/teacher/homeworks/${homeworkId}/tasks`, {
      classId: query.classId,
      taskStatus: query.taskStatus,
      keyword: query.keyword,
      pageNo: query.pageNo ?? 1,
      pageSize: query.pageSize ?? 200
    })
  )
}

export async function fetchTeacherTaskDetail(taskId: number | string) {
  return requestJson<HomeworkTaskDetail>(`/api/teacher/tasks/${taskId}`)
}

export async function submitTeacherTaskReview(
  taskId: number | string,
  payload: {
    submissionId: number | string
    reviewStatus: 'completed' | 'revision_required'
    score?: number
    scoreLevel?: string
    commentText?: string
  }
) {
  return requestJson<void>(`/api/teacher/tasks/${taskId}/reviews`, {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export async function remindTeacherHomework(
  homeworkId: number | string,
  payload: {
    remindType?: 'pending' | 'overdue'
    classId?: number | string
  } = {}
) {
  return requestJson<void>(`/api/teacher/homeworks/${homeworkId}/remind`, {
    method: 'POST',
    body: JSON.stringify({
      remindType: payload.remindType ?? 'pending',
      classId: payload.classId
    })
  })
}

export async function fetchTeacherHomeworkOverview(query: HomeworkStatsQuery = {}) {
  return requestJson<HomeworkOverviewStats>(
    withQuery('/api/teacher/stats/homework-overview', {
      classId: query.classId,
      subjectCode: query.subjectCode,
      startDate: query.startDate,
      endDate: query.endDate
    })
  )
}
