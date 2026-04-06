import type { AuthSession, RegisteredUser, UserRole } from '@/types/auth'

const AUTH_STORAGE_KEY = 'primary-homework-auth'
const USER_STORAGE_KEY = 'primary-homework-users'

export const demoUsers: RegisteredUser[] = [
  {
    account: 'teacher_lin',
    password: '123456',
    name: '林嘉',
    school: '晨曦实验小学',
    role: 'teacher'
  },
  {
    account: 'admin_root',
    password: '123456',
    name: '系统管理员',
    school: '晨曦实验小学',
    role: 'admin'
  },
  {
    account: 'student_chen',
    password: '123456',
    name: '陈沐阳',
    school: '晨曦实验小学',
    role: 'student'
  },
  {
    account: 'parent_wang',
    password: '123456',
    name: '王可欣家长',
    school: '晨曦实验小学',
    role: 'parent'
  }
]

export const roleMeta: Record<
  UserRole,
  {
    label: string
    summary: string
    entryLabel: string
  }
> = {
  teacher: {
    label: '教师',
    summary: '负责发布作业、查看学生提交、批改作业与查看统计数据。',
    entryLabel: '教师工作台'
  },
  admin: {
    label: '管理员',
    summary: '负责账号管理、关系配置、组织维护和平台级数据查看。',
    entryLabel: '管理入口'
  },
  student: {
    label: '学生',
    summary: '负责查看作业、提交内容、查看老师反馈并按要求订正。',
    entryLabel: '学生入口'
  },
  parent: {
    label: '家长',
    summary: '负责接收提醒、查看孩子状态，并在低年级场景协助提交。',
    entryLabel: '家长入口'
  }
}

function canUseStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function normalizeAccount(account: string) {
  return account.trim().toLowerCase()
}

function parseStoredJson<T>(key: string, fallback: T): T {
  if (!canUseStorage()) {
    return fallback
  }

  try {
    const raw = window.localStorage.getItem(key)

    if (!raw) {
      return fallback
    }

    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

export function getRegisteredUsers() {
  return parseStoredJson<RegisteredUser[]>(USER_STORAGE_KEY, [])
}

export function getAllUsers() {
  return [...demoUsers, ...getRegisteredUsers()]
}

export function registerUser(payload: RegisteredUser) {
  const account = normalizeAccount(payload.account)
  const allUsers = getAllUsers()

  if (allUsers.some((item) => normalizeAccount(item.account) === account)) {
    return {
      ok: false,
      message: '该账号已存在，请更换账号后再注册。'
    }
  }

  const nextUsers = [...getRegisteredUsers(), { ...payload, account }]

  if (canUseStorage()) {
    window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(nextUsers))
  }

  return {
    ok: true
  }
}

export function authenticateUser(account: string, password: string) {
  const matchedUser = getAllUsers().find(
    (item) => normalizeAccount(item.account) === normalizeAccount(account) && item.password === password
  )

  if (!matchedUser) {
    return null
  }

  return {
    authenticated: true,
    account: normalizeAccount(matchedUser.account),
    name: matchedUser.name,
    school: matchedUser.school,
    role: matchedUser.role
  } satisfies AuthSession
}

export function getAuthSession() {
  return parseStoredJson<AuthSession | null>(AUTH_STORAGE_KEY, null)
}

export function persistAuthSession(session: AuthSession) {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export function clearAuthSession() {
  if (!canUseStorage()) {
    return
  }

  window.localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function getDefaultRouteForRole(role: UserRole) {
  return role === 'teacher' ? '/dashboard' : '/access-hub'
}
