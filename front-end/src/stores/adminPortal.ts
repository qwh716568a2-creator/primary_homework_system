import { computed, reactive, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  createAdminUser,
  createParentRelation,
  createTeacherRelation,
  fetchAdminClasses,
  fetchAdminDashboardOverview,
  fetchAdminSchools,
  fetchAdminUsers,
  fetchParentRelations,
  fetchTeacherRelations,
  updateAdminUser
} from '@/api/admin'
import { clearAuthSession, getAuthSession, persistAuthSession } from '@/utils/auth-session-clean'
import type { AuthSession } from '@/types/auth'
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

const emptyOverview: AdminDashboardOverview = {
  publishCountToday: 0,
  submissionRate: 0,
  overdueRate: 0,
  activeTeacherCount: 0,
  activeStudentCount: 0
}

function optionLabel(name: string, metaItems: Array<string | number | null | undefined>) {
  const meta = metaItems.filter((item) => item !== undefined && item !== null && `${item}`.trim()).join(' / ')
  return meta ? `${name}（${meta}）` : name
}

export const useAdminPortalStore = defineStore('admin-portal', () => {
  const authUser = ref<AuthSession | null>(getAuthSession())
  const overview = ref<AdminDashboardOverview>({ ...emptyOverview })
  const schools = ref<AdminSchoolItem[]>([])
  const classes = ref<AdminClassItem[]>([])
  const users = ref<AdminUserItem[]>([])
  const teacherRelations = ref<TeacherRelationItem[]>([])
  const parentRelations = ref<ParentRelationItem[]>([])

  const loading = reactive({
    overview: false,
    schools: false,
    classes: false,
    users: false,
    teacherRelations: false,
    parentRelations: false,
    action: false
  })

  const schoolOptions = computed(() =>
    schools.value.map((item) => ({
      label: item.schoolName,
      value: item.schoolId
    }))
  )

  const classOptions = computed(() =>
    classes.value.map((item) => ({
      label: item.className,
      value: item.classId
    }))
  )

  const teacherOptions = computed(() =>
    users.value
      .filter((item) => item.roleType === 'teacher')
      .map((item) => ({
        label: optionLabel(item.userName, [item.mobile, item.teacherNo, item.loginName]),
        value: item.userId
      }))
  )

  const parentOptions = computed(() =>
    users.value
      .filter((item) => item.roleType === 'parent')
      .map((item) => ({
        label: optionLabel(item.userName, [item.mobile, item.loginName]),
        value: item.userId
      }))
  )

  const studentOptions = computed(() =>
    users.value
      .filter((item) => item.roleType === 'student')
      .map((item) => ({
        label: optionLabel(item.userName, [item.studentNo, item.loginName]),
        value: item.userId
      }))
  )

  function setSession(session: AuthSession | null) {
    authUser.value = session
    if (session) {
      persistAuthSession(session)
    } else {
      clearAuthSession()
    }
  }

  async function loadDashboardOverview() {
    loading.overview = true
    try {
      overview.value = await fetchAdminDashboardOverview()
    } finally {
      loading.overview = false
    }
  }

  async function loadSchools() {
    loading.schools = true
    try {
      schools.value = await fetchAdminSchools()
    } finally {
      loading.schools = false
    }
  }

  async function loadClasses(query?: { schoolId?: number | string; gradeId?: number | string; keyword?: string }) {
    loading.classes = true
    try {
      classes.value = await fetchAdminClasses(query)
    } finally {
      loading.classes = false
    }
  }

  async function loadUsers(query?: AdminUserQuery) {
    loading.users = true
    try {
      const result = await fetchAdminUsers(query)
      users.value = result.list
      return result
    } finally {
      loading.users = false
    }
  }

  async function saveUser(payload: Partial<AdminUserPayload>, userId?: number | string) {
    loading.action = true
    try {
      const saved = userId ? await updateAdminUser(userId, payload) : await createAdminUser(payload as AdminUserPayload)
      await loadUsers({ pageNo: 1, pageSize: 300 })
      return saved
    } finally {
      loading.action = false
    }
  }

  async function loadTeacherRelations() {
    loading.teacherRelations = true
    try {
      teacherRelations.value = await fetchTeacherRelations()
    } finally {
      loading.teacherRelations = false
    }
  }

  async function loadParentRelations() {
    loading.parentRelations = true
    try {
      parentRelations.value = await fetchParentRelations()
    } finally {
      loading.parentRelations = false
    }
  }

  async function saveTeacherRelation(payload: {
    teacherId: number | string
    classId: number | string
    subjectCode: string
    isHeadTeacher?: boolean
  }) {
    loading.action = true
    try {
      const saved = await createTeacherRelation(payload)
      await loadTeacherRelations()
      return saved
    } finally {
      loading.action = false
    }
  }

  async function saveParentRelation(payload: {
    parentUserId: number | string
    studentId: number | string
    relationType: string
    isPrimary?: boolean
  }) {
    loading.action = true
    try {
      const saved = await createParentRelation(payload)
      await loadParentRelations()
      return saved
    } finally {
      loading.action = false
    }
  }

  return {
    authUser,
    overview,
    schools,
    classes,
    users,
    teacherRelations,
    parentRelations,
    loading,
    schoolOptions,
    classOptions,
    teacherOptions,
    parentOptions,
    studentOptions,
    setSession,
    setAuthenticatedUser: (session: AuthSession | null, _options?: { remember?: boolean }) => setSession(session),
    loadOverview: loadDashboardOverview,
    loadDashboardOverview,
    loadSchools,
    loadClasses,
    loadUsers,
    saveUser,
    loadTeacherRelations,
    loadParentRelations,
    saveTeacherRelation,
    saveParentRelation
  }
})
