import { clearAuthSession, getAuthSession } from '@/utils/auth-session-clean'

interface WrappedResponse<T> {
  code: number
  message: string
  data: T
  requestId?: string
  timestamp?: string
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

function isWrappedResponse<T>(payload: unknown): payload is WrappedResponse<T> {
  if (!payload || typeof payload !== 'object') {
    return false
  }

  return 'code' in payload && 'message' in payload && 'data' in payload
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
    throw new Error(
      '\u65e0\u6cd5\u8fde\u63a5\u540e\u7aef\u670d\u52a1\uff0c\u8bf7\u68c0\u67e5\u63a5\u53e3\u670d\u52a1\u662f\u5426\u5df2\u7ecf\u542f\u52a8\u3002'
    )
  }

  let payload: unknown = null

  try {
    payload = await response.json()
  } catch {
    if (!response.ok) {
      throw new Error(`\u63a5\u53e3\u8bf7\u6c42\u5931\u8d25\uff1a${response.status}`)
    }
  }

  if (response.status === 401) {
    clearAuthSession()
  }

  if (!response.ok) {
    if (isWrappedResponse<T>(payload)) {
      throw new Error(payload.message || `\u63a5\u53e3\u8bf7\u6c42\u5931\u8d25\uff1a${response.status}`)
    }

    throw new Error(`\u63a5\u53e3\u8bf7\u6c42\u5931\u8d25\uff1a${response.status}`)
  }

  if (!payload) {
    return undefined as T
  }

  if (isWrappedResponse<T>(payload)) {
    if (payload.code !== 0) {
      throw new Error(payload.message || '\u63a5\u53e3\u8bf7\u6c42\u672a\u901a\u8fc7\u3002')
    }

    return payload.data
  }

  return payload as T
}
