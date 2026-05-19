import type { AdminClassItem, AdminSchoolItem, AdminUserItem, ParentRelationItem, TeacherRelationItem } from '@/types/admin-portal'

export const adminRoleLabelMap: Record<string, string> = {
  admin: '管理员',
  teacher: '教师',
  student: '学生',
  parent: '家长'
}

export const subjectLabelMap: Record<string, string> = {
  chinese: '语文',
  math: '数学',
  english: '英语',
  science: '科学',
  morality: '道德与法治'
}

export const relationTypeLabelMap: Record<string, string> = {
  father: '父亲',
  mother: '母亲',
  guardian: '监护人',
  grandparent: '祖辈'
}

export function normalizeStatus(status?: string) {
  return status === 'disabled' ? 'disabled' : 'enabled'
}

export function getAdminRoleLabel(roleType?: string) {
  return roleType ? adminRoleLabelMap[roleType] || roleType : '未知角色'
}

export function getSubjectLabel(subjectCode?: string) {
  return subjectCode ? subjectLabelMap[subjectCode] || subjectCode : '未设置学科'
}

export function getRelationTypeLabel(relationType?: string) {
  return relationType ? relationTypeLabelMap[relationType] || relationType : '未设置关系'
}

export function getAccountDisplay(user: AdminUserItem) {
  return user.mobile || user.studentNo || user.teacherNo || user.loginName || user.account || '未设置账号'
}

export function getSchoolSummary(schools: AdminSchoolItem[], classes: AdminClassItem[], users: AdminUserItem[]) {
  return schools.map((school) => {
    const schoolId = `${school.schoolId}`
    const schoolClasses = classes.filter((item) => `${item.schoolId}` === schoolId)
    const schoolUsers = users.filter((item) => `${item.schoolId ?? ''}` === schoolId)

    return {
      ...school,
      classCount: school.classCount ?? schoolClasses.length,
      teacherCount: school.teacherCount ?? schoolUsers.filter((item) => item.roleType === 'teacher').length,
      studentCount: school.studentCount ?? schoolUsers.filter((item) => item.roleType === 'student').length
    }
  })
}

export function findUserName(users: AdminUserItem[], userId?: number | string) {
  if (!userId) return '未匹配账号'
  return users.find((item) => `${item.userId}` === `${userId}`)?.userName || `账号 ${userId}`
}

export function findUserMobile(users: AdminUserItem[], userId?: number | string) {
  if (!userId) return ''
  const user = users.find((item) => `${item.userId}` === `${userId}`)
  return user?.mobile || user?.loginName || ''
}

export function findClassName(classes: AdminClassItem[], classId?: number | string) {
  if (!classId) return '未匹配班级'
  return classes.find((item) => `${item.classId}` === `${classId}`)?.className || `班级 ${classId}`
}

export function withResolvedTeacherRelation(
  item: TeacherRelationItem,
  users: AdminUserItem[],
  classes: AdminClassItem[]
) {
  return {
    ...item,
    teacherName: item.teacherName || findUserName(users, item.teacherId),
    className: item.className || findClassName(classes, item.classId),
    subjectName: item.subjectName || getSubjectLabel(item.subjectCode)
  }
}

export function withResolvedParentRelation(item: ParentRelationItem, users: AdminUserItem[], classes: AdminClassItem[]) {
  return {
    ...item,
    parentName: item.parentName || findUserName(users, item.parentUserId),
    parentMobile: item.parentMobile || findUserMobile(users, item.parentUserId),
    studentName: item.studentName || findUserName(users, item.studentId),
    className: item.className || findClassName(classes, item.classId),
    relationTypeLabel: getRelationTypeLabel(item.relationType)
  }
}
