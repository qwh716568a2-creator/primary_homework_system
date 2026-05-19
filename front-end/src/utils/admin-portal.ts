import type {
  AdminClassItem,
  AdminSchoolItem,
  AdminUserItem,
  ParentRelationItem,
  TeacherRelationItem
} from '@/types/admin-portal'

export const adminRoleLabelMap: Record<string, string> = {
  admin: '管理员',
  teacher: '教师',
  student: '学生',
  parent: '家长'
}

export function getAdminRoleLabel(roleType?: string) {
  return adminRoleLabelMap[`${roleType ?? ''}`] || roleType || '未知角色'
}

export function getAccountDisplay(user: AdminUserItem) {
  return user.account || user.loginName || user.mobile || user.studentNo || '--'
}

export function normalizeStatus(status?: string) {
  const normalized = `${status ?? 'enabled'}`.toLowerCase()
  return normalized === 'disabled' || normalized === 'inactive' ? 'disabled' : 'enabled'
}

export function getSchoolSummary(
  schools: AdminSchoolItem[],
  classes: AdminClassItem[],
  users: AdminUserItem[]
) {
  return schools.map((school) => {
    const schoolClasses = classes.filter((item) => `${item.schoolId}` === `${school.schoolId}`)
    const schoolUsers = users.filter((item) => `${item.schoolId ?? ''}` === `${school.schoolId}`)

    return {
      ...school,
      classCount: school.classCount ?? schoolClasses.length,
      teacherCount:
        school.teacherCount ?? schoolUsers.filter((item) => item.roleType === 'teacher').length,
      studentCount:
        school.studentCount ?? schoolUsers.filter((item) => item.roleType === 'student').length
    }
  })
}

export function withResolvedTeacherRelation(
  relation: TeacherRelationItem,
  users: AdminUserItem[],
  classes: AdminClassItem[]
) {
  const teacher = users.find((item) => `${item.userId}` === `${relation.teacherId}`)
  const schoolClass = classes.find((item) => `${item.classId}` === `${relation.classId}`)

  return {
    ...relation,
    teacherName: relation.teacherName || teacher?.userName || '--',
    className: relation.className || schoolClass?.className || '--'
  }
}

export function withResolvedParentRelation(
  relation: ParentRelationItem,
  users: AdminUserItem[],
  classes: AdminClassItem[]
) {
  const parent = users.find((item) => `${item.userId}` === `${relation.parentUserId}`)
  const student = users.find((item) => `${item.userId}` === `${relation.studentId}`)
  const schoolClass = classes.find((item) => `${item.classId}` === `${relation.classId ?? ''}`)

  return {
    ...relation,
    parentName: relation.parentName || parent?.userName || '--',
    studentName: relation.studentName || student?.userName || '--',
    className: relation.className || student?.className || schoolClass?.className || '--'
  }
}
