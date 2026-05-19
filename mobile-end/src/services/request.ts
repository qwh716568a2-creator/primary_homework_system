type RequestPayload = object | string | ArrayBuffer

export interface RequestConfig<T = RequestPayload> {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: T
  header?: Record<string, string>
}

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

const SESSION_KEY = 'primary-homework-mobile-session'
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://127.0.0.1:8080'

function getSessionToken() {
  const session = uni.getStorageSync(SESSION_KEY) as { token?: string } | undefined
  return session?.token ?? ''
}

export function request<T, P extends RequestPayload = RequestPayload>(
  config: RequestConfig<P>
): Promise<T> {
  return new Promise((resolve, reject) => {
    const token = getSessionToken()

    uni.request({
      url: `${API_BASE_URL}${config.url}`,
      method: config.method ?? 'GET',
      data: config.data as RequestPayload | undefined,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(config.header ?? {})
      },
      success(response) {
        const payload = response.data as ApiResponse<T>

        if (typeof payload?.code === 'number') {
          if (payload.code === 0) {
            resolve(payload.data)
            return
          }

          reject(new Error(payload.message || '请求失败'))
          return
        }

        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(response.data as T)
          return
        }

        reject(new Error('服务暂时不可用，请稍后再试'))
      },
      fail(error) {
        const errorMessage =
          typeof error === 'object' && error !== null && 'errMsg' in error
            ? String(error.errMsg)
            : '网络请求失败'
        reject(new Error(errorMessage))
      }
    })
  })
}
