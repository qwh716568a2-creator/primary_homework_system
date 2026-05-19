export type AttachmentLike = {
  name?: string
  type?: string
  url?: string
  assetName?: string
  assetType?: string
  assetUrl?: string
  fileUrl?: string
  previewUrl?: string
  path?: string
  objectKey?: string
}

const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL ?? ''}`.replace(/\/$/, '')

function withApiBase(path: string) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return API_BASE_URL ? `${API_BASE_URL}${normalizedPath}` : normalizedPath
}

export function resolveAttachmentUrl(file: AttachmentLike) {
  const rawValue =
    file.assetUrl ||
    file.url ||
    file.fileUrl ||
    file.previewUrl ||
    file.path ||
    file.objectKey ||
    ''
  const raw = `${rawValue}`.trim()

  if (!raw) {
    return ''
  }

  if (/^(https?:|data:|blob:)/i.test(raw)) {
    return raw
  }

  if (raw.startsWith('//')) {
    return `https:${raw}`
  }

  if (/^[\w.-]+\.aliyuncs\.com\//i.test(raw)) {
    return `https://${raw}`
  }

  if (
    raw.startsWith('/api/') ||
    raw.startsWith('/files/') ||
    raw.startsWith('/uploads/') ||
    raw.startsWith('/primary-homework/')
  ) {
    return withApiBase(raw)
  }

  return raw
}

export function getAttachmentDisplayName(file: AttachmentLike, fallback = '未命名附件') {
  const name = `${file.assetName || file.name || ''}`.trim()

  if (name) {
    return name
  }

  const url = resolveAttachmentUrl(file).split('?')[0]
  return url.split('/').filter(Boolean).pop() || fallback
}

export function isImageAttachmentLike(file: AttachmentLike) {
  const type = `${file.assetType ?? file.type ?? ''}`.toLowerCase()
  const name = `${file.assetName ?? file.name ?? ''}`.toLowerCase()
  const url = resolveAttachmentUrl(file).toLowerCase()

  return (
    type.includes('image') ||
    /\.(png|jpe?g|gif|webp|bmp|svg)(\?.*)?$/.test(name) ||
    /\.(png|jpe?g|gif|webp|bmp|svg)(\?.*)?$/.test(url)
  )
}

export function isPdfAttachmentLike(file: AttachmentLike) {
  const type = `${file.assetType ?? file.type ?? ''}`.toLowerCase()
  const name = `${file.assetName ?? file.name ?? ''}`.toLowerCase()
  const url = resolveAttachmentUrl(file).toLowerCase()

  return type.includes('pdf') || /\.pdf(\?.*)?$/.test(name) || /\.pdf(\?.*)?$/.test(url)
}
