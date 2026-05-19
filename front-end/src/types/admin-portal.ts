export interface AdminDashboardOverview {
  publishCountToday: number
  submissionRate: number
  overdueRate: number
  activeTeacherCount: number
  activeStudentCount: number
}

export interface AdminSchoolItem {
  schoolId: number | string
  schoolName: string
  schoolCode?: string
  status?: string
  gradeCount?: number
  classCount?: number
  teacherCount?: number
  studentCount?: number
}

export interface AdminClassItem {
  classId: number | string
  schoolId: number | string
  schoolName?: string
  gradeId?: number | string
  gradeName?: string
  className: string
  classCode?: string
  homeroomTeacherId?: number | string
  homeroomTeacherName?: string
  studentCount?: number
  status?: string
}

export interface AdminUserItem {
  userId: number | string
  userName: string
  roleType: string
  schoolId?: number | string | null
  schoolName?: string
  status?: string
  loginName?: string
  mobile?: string
  teacherNo?: string
  studentNo?: string
  classId?: number | string
  className?: string
  gradeId?: number | string
  gradeName?: string
  account?: string
}

export interface AdminUserQuery {
  keyword?: string
  roleType?: string
  schoolId?: number | string
  status?: string
  pageNo?: number
  pageSize?: number
}

export interface AdminUserPayload {
  userName: string
  roleType: 'teacher' | 'student' | 'parent' | 'admin'
  schoolId?: number | string | null
  password?: string
  loginName?: string
  status?: string
  profile?: {
    teacherNo?: string
    mobile?: string
    studentNo?: string
    gradeId?: number | string
    classId?: number | string
  }
}

export interface TeacherRelationItem {
  id?: number | string
  teacherId: number | string
  teacherName?: string
  classId: number | string
  className?: string
  subjectCode: string
  subjectName?: string
  isHeadTeacher?: boolean
  status?: string
}

export interface ParentRelationItem {
  id?: number | string
  parentUserId: number | string
  parentName?: string
  parentMobile?: string
  studentId: number | string
  studentName?: string
  classId?: number | string
  className?: string
  relationType: string
  isPrimary?: boolean
  status?: string
}
