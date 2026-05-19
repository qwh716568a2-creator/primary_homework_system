import { requestJson } from '@/api/teacher-http-clean'
import type {
  AdminClassItem,
  AdminDashboardOverview,
  AdminSchoolItem,
  AdminUserItem,
  AdminUserPayload,
  AdminUserQuery,
  ParentRelationItem,
  TeacherRelationItem
} from '@/types/admin-portal'

interface PagedData<T> {
  list: T[]
  total: number
  pageNo?: number
  pageSize?: number
}

function normalizeList<T>(payload: T[] | PagedData<T> | null | undefined) {
  if (Array.isArray(payload)) {
    return payload
  }

  if (payload && Array.isArray(payload.list)) {
    return payload.list
  }

  return []
}

function normalizePaged<T>(payload: T[] | PagedData<T> | null | undefined, pageSize = 200): PagedData<T> {
  if (Array.isArray(payload)) {
    return {
      list: payload,
      total: payload.length,
      pageNo: 1,
      pageSize
    }
  }

  if (payload && Array.isArray(payload.list)) {
    return {
      list: payload.list,
      total: payload.total ?? payload.list.length,
      pageNo: payload.pageNo ?? 1,
      pageSize: payload.pageSize ?? pageSize
    }
  }

  return {
    list: [],
    total: 0,
    pageNo: 1,
    pageSize
  }
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

export async function fetchAdminDashboardOverview() {
  return requestJson<AdminDashboardOverview>('/api/admin/dashboard/overview')
}

export async function fetchAdminSchools() {
  const data = await requestJson<AdminSchoolItem[] | PagedData<AdminSchoolItem>>('/api/admin/schools')
  return normalizeList(data)
}

export async function fetchAdminClasses(query: {
  schoolId?: number | string
  gradeId?: number | string
  keyword?: string
} = {}) {
  const data = await requestJson<AdminClassItem[] | PagedData<AdminClassItem>>(
    withQuery('/api/admin/classes', {
      schoolId: query.schoolId,
      gradeId: query.gradeId,
      keyword: query.keyword
    })
  )

  return normalizeList(data)
}

export async function fetchAdminUsers(query: AdminUserQuery = {}) {
  const data = await requestJson<PagedData<AdminUserItem> | AdminUserItem[]>(
    withQuery('/api/admin/users', {
      keyword: query.keyword,
      roleType: query.roleType,
      schoolId: query.schoolId,
      status: query.status,
      pageNo: query.pageNo ?? 1,
      pageSize: query.pageSize ?? 200
    })
  )

  return normalizePaged(data, query.pageSize ?? 200)
}

export async function createAdminUser(payload: AdminUserPayload) {
  return requestJson<void>('/api/admin/users', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export async function updateAdminUser(userId: number | string, payload: Partial<AdminUserPayload>) {
  return requestJson<void>(`/api/admin/users/${userId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  })
}

export async function fetchTeacherRelations() {
  const data = await requestJson<TeacherRelationItem[] | PagedData<TeacherRelationItem>>(
    '/api/admin/teacher-class-subject-rels'
  )
  return normalizeList(data)
}

export async function createTeacherRelation(payload: {
  teacherId: number | string
  classId: number | string
  subjectCode: string
  isHeadTeacher?: boolean
}) {
  return requestJson<void>('/api/admin/teacher-class-subject-rels', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

export async function fetchParentRelations() {
  const data = await requestJson<ParentRelationItem[] | PagedData<ParentRelationItem>>(
    '/api/admin/parent-student-rels'
  )
  return normalizeList(data)
}

export async function createParentRelation(payload: {
  parentUserId: number | string
  studentId: number | string
  relationType: string
  isPrimary?: boolean
}) {
  return requestJson<void>('/api/admin/parent-student-rels', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}
