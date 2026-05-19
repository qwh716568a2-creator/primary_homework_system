import { buildAuthSession } from '@/utils/auth-session-clean'
import type { AuthSession, UserRole } from '@/types/auth'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface LoginUserInfo {
  userId?: number | string
  userName?: string
  roleType?: string
  schoolId?: number | string | null
  schoolName?: string
}

interface LoginResponseData {
  token: string
  expiresIn?: number
  userInfo?: LoginUserInfo
  permissions?: string[]
}

interface CurrentUserData {
  userId?: number | string
  userName?: string
  roleType?: string
  schoolId?: number | string | null
  schoolName?: string
  permissions?: string[]
}

interface RegisterResponseData {
  userId: number | string
  account: string
  userName: string
  roleType: string
  schoolId?: number | string | null
  schoolName?: string
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? ''
const USE_MOCK_AUTH = import.meta.env.VITE_USE_MOCK_AUTH === 'true'

function buildUrl(path: string) {
  return API_BASE_URL ? `${API_BASE_URL}${path}` : path
}

function normalizeLoginType(loginType?: string): UserRole {
  if (loginType === 'admin' || loginType === 'student' || loginType === 'parent' || loginType === 'teacher') {
    return loginType
  }

  return 'teacher'
}

function createMockSession(account: string, loginType?: string): AuthSession {
  const role = normalizeLoginType(loginType)
  const defaultNameMap: Record<UserRole, string> = {
    teacher: '\u6d4b\u8bd5\u6559\u5e08',
    admin: '\u6d4b\u8bd5\u7ba1\u7406\u5458',
    student: '\u6d4b\u8bd5\u5b66\u751f',
    parent: '\u6d4b\u8bd5\u5bb6\u957f'
  }

  return buildAuthSession({
    account,
    name: defaultNameMap[role],
    school: role === 'admin' ? '\u5e73\u53f0\u7ba1\u7406\u7aef' : '\u793a\u4f8b\u5c0f\u5b66',
    schoolId: role === 'admin' ? null : 1,
    roleType: role,
    token: `local-mock-token-${role}-${Date.now()}`,
    permissions: []
  })
}

async function requestJson<T>(path: string, init: RequestInit) {
  let response: Response

  try {
    response = await fetch(buildUrl(path), init)
  } catch {
    throw new Error(
      '\u65e0\u6cd5\u8fde\u63a5\u767b\u5f55\u670d\u52a1\uff0c\u8bf7\u68c0\u67e5\u540e\u7aef\u662f\u5426\u542f\u52a8\u6216\u63a5\u53e3\u5730\u5740\u662f\u5426\u6b63\u786e\u3002'
    )
  }

  let payload: ApiResponse<T> | null = null

  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    if (!response.ok) {
      throw new Error(`\u8bf7\u6c42\u5931\u8d25\uff1a${response.status}`)
    }
  }

  if (!response.ok) {
    throw new Error(payload?.message || `\u8bf7\u6c42\u5931\u8d25\uff1a${response.status}`)
  }

  if (!payload) {
    throw new Error('\u670d\u52a1\u8fd4\u56de\u4e3a\u7a7a\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5\u3002')
  }

  if (payload.code !== 0) {
    throw new Error(payload.message || '\u8bf7\u6c42\u672a\u901a\u8fc7\uff0c\u8bf7\u68c0\u67e5\u8f93\u5165\u4fe1\u606f\u3002')
  }

  return payload.data
}

export async function fetchCurrentUser(token: string) {
  return requestJson<CurrentUserData>('/api/auth/me', {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
}

export async function loginByPassword(payload: {
  account: string
  password: string
  loginType: UserRole
  schoolId?: number | string | null
}): Promise<AuthSession> {
  const loginType = normalizeLoginType(payload.loginType)
  const normalizedSchoolId =
    payload.schoolId === '' || payload.schoolId === null || payload.schoolId === undefined
      ? null
      : Number(payload.schoolId)

  if (USE_MOCK_AUTH) {
    return createMockSession(payload.account.trim(), loginType)
  }

  const data = await requestJson<LoginResponseData>('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      account: payload.account.trim(),
      password: payload.password,
      loginType,
      schoolId: normalizedSchoolId
    })
  })

  const currentUser = data.token
    ? await fetchCurrentUser(data.token).catch(() => null)
    : null

  const userInfo = currentUser ?? data.userInfo ?? {}

  return buildAuthSession({
    account: payload.account,
    name: userInfo.userName || payload.account.trim(),
    school:
      userInfo.schoolName ||
      ('schoolName' in (data.userInfo ?? {}) ? data.userInfo?.schoolName : undefined) ||
      '\u6821\u5185\u7edf\u4e00\u8ba4\u8bc1',
    schoolId: userInfo.schoolId ?? data.userInfo?.schoolId ?? null,
    roleType: userInfo.roleType ?? data.userInfo?.roleType,
    token: data.token,
    expiresIn: data.expiresIn,
    permissions: currentUser?.permissions ?? data.permissions ?? []
  })
}

export function inferLoginType(account: string) {
  const normalizedAccount = account.trim()

  if (/^1\d{10}$/.test(normalizedAccount)) {
    return 'teacher'
  }

  if (/[A-Za-z]/.test(normalizedAccount) && !/^\d+$/.test(normalizedAccount)) {
    return 'admin'
  }

  return null
}

export async function registerAccount(payload: {
  name: string
  school: string
  role: 'student' | 'parent'
  account: string
  password: string
  confirmPassword: string
}) {
  if (USE_MOCK_AUTH) {
    return Promise.resolve({
      userId: Date.now(),
      account: payload.account.trim(),
      userName: payload.name.trim(),
      roleType: payload.role,
      schoolId: 1,
      schoolName: payload.school.trim() || '\u793a\u4f8b\u5c0f\u5b66'
    } satisfies RegisterResponseData)
  }

  return requestJson<RegisterResponseData>('/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: payload.name.trim(),
      school: payload.school.trim(),
      role: payload.role,
      account: payload.account.trim(),
      password: payload.password,
      confirmPassword: payload.confirmPassword
    })
  })
}
