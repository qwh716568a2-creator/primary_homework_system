import type { LoginPayload, MobileSession, UserRole } from '@/types/mobile'
import { request } from '@/services/request'
import { createPreviewState } from '@/data/preview-mobile'

interface LoginApiResult {
  token?: string
  accessToken?: string
  expiresIn?: number
  role?: UserRole
  roleType?: UserRole
  userName?: string
  realName?: string
  schoolId?: string | number
  schoolName?: string
  userId?: string | number
  permissions?: string[]
  userInfo?: {
    userId?: string | number
    userName?: string
    realName?: string
    roleType?: UserRole
    schoolId?: string | number
    schoolName?: string
    permissions?: string[]
  }
}

interface CurrentUserResult {
  userId?: string | number
  userName?: string
  roleType?: UserRole
  schoolId?: string | number
  schoolName?: string
  permissions?: string[]
}

const USE_LOCAL_MOCK = import.meta.env.VITE_USE_MOBILE_MOCK === 'true'

function buildLocalSession(payload: LoginPayload): MobileSession {
  const previewState = createPreviewState()
  const isParent = payload.role === 'parent'

  return {
    role: payload.role,
    userId: isParent ? previewState.parentProfile.id : previewState.studentProfile.id,
    userName: isParent ? previewState.parentProfile.name : previewState.studentProfile.name,
    schoolId: '1',
    schoolName: isParent ? previewState.parentProfile.school : previewState.studentProfile.school,
    token: `mobile-local-${payload.role}-${Date.now()}`,
    permissions: isParent
      ? ['parent.student.list', 'parent.homework.list']
      : ['student.homework.list', 'student.homework.submit']
  }
}

function normalizeRole(payload: LoginPayload, result: LoginApiResult): UserRole {
  return result.role ?? result.roleType ?? result.userInfo?.roleType ?? payload.role
}

function normalizeSession(payload: LoginPayload, result: LoginApiResult): MobileSession {
  const role = normalizeRole(payload, result)
  const userInfo = result.userInfo ?? {}

  return {
    role,
    userId: String(result.userId ?? userInfo.userId ?? ''),
    userName: result.userName ?? result.realName ?? userInfo.userName ?? userInfo.realName ?? '用户',
    schoolId: String(result.schoolId ?? userInfo.schoolId ?? payload.schoolId ?? ''),
    schoolName: result.schoolName ?? userInfo.schoolName ?? '',
    token: result.token ?? result.accessToken ?? '',
    permissions: result.permissions ?? userInfo.permissions ?? []
  }
}

export async function loginByPassword(payload: LoginPayload): Promise<MobileSession> {
  if (USE_LOCAL_MOCK) {
    return buildLocalSession(payload)
  }

  const result = await request<LoginApiResult, Record<string, string | null>>({
    url: '/api/auth/login',
    method: 'POST',
    data: {
      loginType: payload.role,
      account: payload.account,
      password: payload.password,
      schoolId: payload.schoolId ?? null
    }
  })

  return normalizeSession(payload, result)
}

export function fetchCurrentUser() {
  return request<CurrentUserResult>({
    url: '/api/auth/me',
    method: 'GET'
  })
}
