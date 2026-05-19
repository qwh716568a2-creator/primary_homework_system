import type { AuthSession, RegistrationDraft, UserRole } from '@/types/auth'

const AUTH_STORAGE_KEY = 'primary-homework-auth'
const AUTH_SESSION_STORAGE_KEY = 'primary-homework-auth-session'
const REGISTRATION_DRAFT_KEY = 'primary-homework-registration-drafts'
const USE_MOCK_AUTH = import.meta.env.VITE_USE_MOCK_AUTH === 'true'

export const roleMeta: Record<
  UserRole,
  {
    label: string
    summary: string
    entryLabel: string
  }
> = {
  teacher: {
    label: '\u6559\u5e08',
    summary: '\u53d1\u5e03\u4f5c\u4e1a\u3001\u67e5\u770b\u63d0\u4ea4\u3001\u6279\u6539\u53cd\u9988\uff0c\u5e76\u8ddf\u8e2a\u73ed\u7ea7\u5b66\u4e60\u8fdb\u5ea6\u3002',
    entryLabel: '\u6559\u5e08\u5de5\u4f5c\u53f0'
  },
  admin: {
    label: '\u7ba1\u7406\u5458',
    summary: '\u7ef4\u62a4\u8d26\u53f7\u3001\u7ec4\u7ec7\u4e0e\u6743\u9650\u914d\u7f6e\uff0c\u67e5\u770b\u5e73\u53f0\u6574\u4f53\u8fd0\u884c\u60c5\u51b5\u3002',
    entryLabel: '\u7ba1\u7406\u63a7\u5236\u53f0'
  },
  student: {
    label: '\u5b66\u751f',
    summary: '\u67e5\u770b\u4f5c\u4e1a\u3001\u63d0\u4ea4\u5b66\u4e60\u6210\u679c\uff0c\u5e76\u63a5\u6536\u8001\u5e08\u53cd\u9988\u3002',
    entryLabel: '\u5b66\u751f\u5165\u53e3'
  },
  parent: {
    label: '\u5bb6\u957f',
    summary: '\u63a5\u6536\u4f5c\u4e1a\u63d0\u9192\u3001\u4e86\u89e3\u5b69\u5b50\u8fdb\u5ea6\uff0c\u5e76\u534f\u52a9\u5b8c\u6210\u5bb6\u5ead\u4efb\u52a1\u3002',
    entryLabel: '\u5bb6\u957f\u5165\u53e3'
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
    name: payload.name || '\u672a\u547d\u540d\u7528\u6237',
    school: payload.school?.trim() || '\u6821\u5185\u7edf\u4e00\u8ba4\u8bc1',
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

function isMockSession(session: AuthSession) {
  const token = `${session.token ?? ''}`
  const permissions = session.permissions ?? []
  const usesLegacyMockPrefix = token.startsWith('mock-jwt-token-') || token.startsWith('local-mock-token-')
  const looksLikeOldFrontendMock =
    token.startsWith('mock-token-') &&
    permissions.length === 0 &&
    ['\u793a\u4f8b\u5c0f\u5b66', '\u5e73\u53f0\u7ba1\u7406\u7aef', '\u6821\u5185\u7edf\u4e00\u8ba4\u8bc1'].includes(`${session.school ?? ''}`)

  return usesLegacyMockPrefix || looksLikeOldFrontendMock
}

export function getAuthSession() {
  const sessionValue = parseStoredJson<AuthSession | null>(getSessionStorage(), AUTH_SESSION_STORAGE_KEY, null)

  if (sessionValue) {
    if (!USE_MOCK_AUTH && isMockSession(sessionValue)) {
      clearAuthSession()
      return null
    }

    return sessionValue
  }

  const persistedSession = parseStoredJson<AuthSession | null>(getLocalStorage(), AUTH_STORAGE_KEY, null)

  if (!USE_MOCK_AUTH && persistedSession && isMockSession(persistedSession)) {
    clearAuthSession()
    return null
  }

  return persistedSession
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
  if (role === 'teacher') {
    return '/dashboard'
  }

  if (role === 'admin') {
    return '/admin/dashboard'
  }

  if (role === 'student') {
    return '/student/home'
  }

  if (role === 'parent') {
    return '/parent/home'
  }

  return '/login'
}
