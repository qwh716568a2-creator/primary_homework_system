import { clearAuthSession, getAuthSession } from '@/utils/auth-session-clean'

interface ApiResponse<T> {
  code: number
  message?: string
  data: T
}

type RequestOptions = RequestInit & {
  auth?: boolean
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') ?? ''

function buildUrl(path: string) {
  return API_BASE_URL ? `${API_BASE_URL}${path}` : path
}

function buildHeaders(headers?: HeadersInit, auth = true) {
  const nextHeaders = new Headers(headers)

  if (!nextHeaders.has('Content-Type')) {
    nextHeaders.set('Content-Type', 'application/json')
  }

  if (auth) {
    const session = getAuthSession()

    if (session?.token) {
      nextHeaders.set('Authorization', `Bearer ${session.token}`)
    }
  }

  return nextHeaders
}

function isWrappedResponse<T>(payload: unknown): payload is ApiResponse<T> {
  return Boolean(payload && typeof payload === 'object' && 'code' in payload && 'data' in payload)
}

export async function requestJson<T>(path: string, options: RequestOptions = {}) {
  const { auth = true, headers, ...rest } = options

  let response: Response

  try {
    response = await fetch(buildUrl(path), {
      ...rest,
      headers: buildHeaders(headers, auth)
    })
  } catch {
    throw new Error('无法连接后端服务，请检查接口服务是否已启动。')
  }

  let payload: unknown = null

  try {
    payload = await response.json()
  } catch {
    if (!response.ok) {
      throw new Error(`接口请求失败：${response.status}`)
    }
  }

  if (response.status === 401) {
    clearAuthSession()
  }

  if (!response.ok) {
    throw new Error(
      isWrappedResponse<T>(payload) && payload.message ? payload.message : `接口请求失败：${response.status}`
    )
  }

  if (!payload) {
    throw new Error('接口未返回可用数据。')
  }

  if (isWrappedResponse<T>(payload)) {
    if (payload.code !== 0) {
      throw new Error(payload.message || '接口请求未通过。')
    }

    return payload.data
  }

  return payload as T
}
