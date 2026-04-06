import { buildAuthSession } from '@/utils/auth-session'
import type { AuthSession } from '@/types/auth'

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
const DEFAULT_CLIENT_TYPE = import.meta.env.VITE_AUTH_CLIENT_TYPE ?? 'teacher-web'

function buildUrl(path: string) {
  return API_BASE_URL ? `${API_BASE_URL}${path}` : path
}

export function inferLoginClientType(account: string) {
  const normalizedAccount = account.trim()

  if (/^1\d{10}$/.test(normalizedAccount)) {
    return 'teacher-web'
  }

  if (/[A-Za-z]/.test(normalizedAccount) && !/^\d+$/.test(normalizedAccount)) {
    return 'admin-web'
  }

  if (!normalizedAccount) {
    return DEFAULT_CLIENT_TYPE
  }

  return 'student-web'
}

async function requestJson<T>(path: string, init: RequestInit) {
  let response: Response

  try {
    response = await fetch(buildUrl(path), init)
  } catch (error) {
    throw new Error('无法连接登录服务，请检查后端是否启动或接口地址是否正确。')
  }

  let payload: ApiResponse<T> | null = null

  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    if (!response.ok) {
      throw new Error(`服务返回 ${response.status}，且未提供可解析的错误信息。`)
    }
  }

  if (!response.ok) {
    throw new Error(payload?.message || `请求失败（${response.status}）`)
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
  clientType?: string
}): Promise<AuthSession> {
  const clientType = payload.clientType ?? inferLoginClientType(payload.account)

  const data = await requestJson<LoginResponseData>('/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      account: payload.account.trim(),
      password: payload.password,
      clientType
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
      '校内统一认证',
    schoolId: userInfo.schoolId ?? data.userInfo?.schoolId ?? null,
    roleType: userInfo.roleType ?? data.userInfo?.roleType,
    token: data.token,
    expiresIn: data.expiresIn,
    permissions: currentUser?.permissions ?? data.permissions ?? []
  })
}

export async function registerAccount(payload: {
  name: string
  school: string
  role: 'student' | 'parent'
  account: string
  password: string
  confirmPassword: string
}) {
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
