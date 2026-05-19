const SESSION_KEY = 'primary-homework-mobile-session'
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

interface UploadResult {
  fileUrl: string
  fileName?: string
  fileSize?: number
  contentType?: string
}

interface UploadResponse {
  code?: number
  message?: string
  data?: UploadResult
}

function getSessionToken() {
  const session = uni.getStorageSync(SESSION_KEY) as { token?: string } | undefined
  return session?.token ?? ''
}

export function uploadMobileFile(filePath: string, bizType: string) {
  return new Promise<UploadResult>((resolve, reject) => {
    const token = getSessionToken()

    uni.uploadFile({
      url: `${API_BASE_URL}/api/files/upload`,
      filePath,
      name: 'file',
      formData: {
        bizType
      },
      header: token ? { Authorization: `Bearer ${token}` } : {},
      success(response) {
        try {
          const payload = JSON.parse(response.data) as UploadResponse

          if (typeof payload?.code === 'number' && payload.code !== 0) {
            reject(new Error(payload.message || '上传失败'))
            return
          }

          if (payload?.data?.fileUrl) {
            resolve(payload.data)
            return
          }

          reject(new Error('上传接口未返回文件地址'))
        } catch (error) {
          reject(error instanceof Error ? error : new Error('上传返回解析失败'))
        }
      },
      fail(error) {
        reject(error)
      }
    })
  })
}
