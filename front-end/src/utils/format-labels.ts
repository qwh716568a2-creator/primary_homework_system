const submitTypeLabelMap: Record<string, string> = {
  text: '\u6587\u5b57',
  image: '\u56fe\u7247',
  file: '\u9644\u4ef6',
  audio: '\u97f3\u9891',
  video: '\u89c6\u9891',
  mixed: '\u56fe\u6587'
}

const fallbackLabel = '\u672a\u914d\u7f6e'

export function formatSubmitType(value?: string | null) {
  if (!value) return fallbackLabel

  return submitTypeLabelMap[value] ?? value
}

export function formatSubmitTypes(values?: Array<string | null | undefined> | string | null) {
  if (!values) return fallbackLabel

  const list = Array.isArray(values)
    ? values
    : values
        .split(/[\/,|]+/)
        .map((item) => item.trim())
        .filter(Boolean)

  const labels = Array.from(new Set(list.filter(Boolean).map((item) => formatSubmitType(`${item}`))))

  return labels.length ? labels.join(' / ') : fallbackLabel
}

export function formatAssetType(value?: string | null) {
  return formatSubmitType(value)
}
