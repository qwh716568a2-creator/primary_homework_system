import type { AuthSession, RegistrationDraft, UserRole } from '@/types/auth'

const AUTH_STORAGE_KEY = 'primary-homework-auth'
const AUTH_SESSION_STORAGE_KEY = 'primary-homework-auth-session'
const REGISTRATION_DRAFT_KEY = 'primary-homework-registration-drafts'

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
    summary: '发布作业、查看提交、批改反馈，并跟踪班级学习进展。',
    entryLabel: '教师工作台'
  },
  admin: {
    label: '管理员',
    summary: '维护账号、组织与权限配置，查看平台级运行情况。',
    entryLabel: '管理入口'
  },
  student: {
    label: '学生',
    summary: '查看每日作业、提交学习成果，并接收老师反馈。',
    entryLabel: '学生入口'
  },
  parent: {
    label: '家长',
    summary: '接收作业提醒、了解孩子进度，并协助完成家庭任务。',
    entryLabel: '家长入口'
  }
}

function canUseStorage() {
  return typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
}

function parseStoredJson<T>(storage: Storage | undefined, key: string, fallback: T): T {
  if (!storage) {
    return fallback
  }

  try {
    const raw = storage.getItem(key)

    if (!raw) {
      return fallback
    }

    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

function getLocalStorage() {
  return canUseStorage() ? window.localStorage : undefined
}

function getSessionStorage() {
  return canUseStorage() ? window.sessionStorage : undefined
}

export function normalizeAccount(account: string) {
  return account.trim()
}

export function normalizeRole(roleType?: string): UserRole {
  const normalized = `${roleType ?? ''}`.trim().toLowerCase()

  if (normalized === 'teacher' || normalized === 'admin' || normalized === 'student' || normalized === 'parent') {
    return normalized
  }

  return 'teacher'
}

export function buildAuthSession(payload: {
  account: string
  name: string
  school?: string
  schoolId?: number | string | null
  roleType?: string
  token: string
  expiresIn?: number
  permissions?: string[]
}) {
  return {
    authenticated: true,
    account: normalizeAccount(payload.account),
    name: payload.name || '未命名用户',
    school: payload.school?.trim() || '校内统一认证',
    schoolId: payload.schoolId ?? null,
    role: normalizeRole(payload.roleType),
    token: payload.token,
    expiresIn: payload.expiresIn,
    permissions: payload.permissions ?? []
  } satisfies AuthSession
}

export function getRegistrationDrafts() {
  return parseStoredJson<RegistrationDraft[]>(getLocalStorage(), REGISTRATION_DRAFT_KEY, [])
}

export function saveRegistrationDraft(payload: RegistrationDraft) {
  const storage = getLocalStorage()

  if (!storage) {
    return
  }

  storage.setItem(
    REGISTRATION_DRAFT_KEY,
    JSON.stringify([
      ...getRegistrationDrafts(),
      {
        ...payload,
        account: normalizeAccount(payload.account)
      }
    ])
  )
}

export function getAuthSession() {
  const sessionValue = parseStoredJson<AuthSession | null>(getSessionStorage(), AUTH_SESSION_STORAGE_KEY, null)

  if (sessionValue) {
    return sessionValue
  }

  return parseStoredJson<AuthSession | null>(getLocalStorage(), AUTH_STORAGE_KEY, null)
}

export function persistAuthSession(session: AuthSession, remember = true) {
  const localStorage = getLocalStorage()
  const sessionStorage = getSessionStorage()

  if (!localStorage || !sessionStorage) {
    return
  }

  clearAuthSession()

  const targetStorage = remember ? localStorage : sessionStorage
  const targetKey = remember ? AUTH_STORAGE_KEY : AUTH_SESSION_STORAGE_KEY
  targetStorage.setItem(targetKey, JSON.stringify(session))
}

export function clearAuthSession() {
  getLocalStorage()?.removeItem(AUTH_STORAGE_KEY)
  getSessionStorage()?.removeItem(AUTH_SESSION_STORAGE_KEY)
}

export function getDefaultRouteForRole(role: UserRole) {
  return role === 'teacher' ? '/dashboard' : '/access-hub'
}
