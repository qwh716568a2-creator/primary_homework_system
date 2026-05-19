import { buildAuthSession } from '@/utils/auth-session'
import type { AuthSession, UserRole } from '@/types/auth'

interface ApiResponse<T> {
  code: number
  message?: string
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
    teacher: '测试教师',
    admin: '测试管理员',
    student: '测试学生',
    parent: '测试家长'
  }

  return buildAuthSession({
    account,
    name: defaultNameMap[role],
    school: role === 'admin' ? '平台管理端' : '示例小学',
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
    throw new Error('无法连接登录服务，请检查后端是否启动或接口地址是否正确。')
  }

  let payload: ApiResponse<T> | null = null

  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    if (!response.ok) {
      throw new Error(`请求失败：${response.status}`)
    }
  }

  if (!response.ok) {
    throw new Error(payload?.message || `请求失败：${response.status}`)
  }

  if (!payload) {
    throw new Error('服务返回为空，请稍后重试。')
  }

  if (payload.code !== 0) {
    throw new Error(payload.message || '请求未通过，请检查输入信息。')
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

  const currentUser = data.token ? await fetchCurrentUser(data.token).catch(() => null) : null
  const userInfo = currentUser ?? data.userInfo ?? {}

  return buildAuthSession({
    account: payload.account,
    name: userInfo.userName || payload.account.trim(),
    school:
      userInfo.schoolName ||
      ('schoolName' in (data.userInfo ?? {}) ? data.userInfo?.schoolName : undefined) ||
      '校内统一认证',
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
      schoolName: payload.school.trim() || '示例小学'
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
