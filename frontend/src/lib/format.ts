/**
 * 格式化观看次数
 */
export function formatViewCount(count: number | null | undefined): string {
  if (count === null || count === undefined) return '0'
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}千`
  }
  return String(count)
}

/**
 * 格式化日期
 */
export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}

const VIDEO_WEBSITE_MAP: Record<number, string> = {
  1: '91',
  2: '九色',
  3: 'BaAV',
  4: '黑料网',
}

/**
 * 获取视频网站名称
 */
export function getWebsiteLabel(type: number | null | undefined): string {
  if (type === null || type === undefined) return '未知'
  return VIDEO_WEBSITE_MAP[type] || '未知'
}

/**
 * 获取图片完整 URL
 * - 完整 URL（http/https）→ 直接返回
 * - 协议相对 URL（//xxx.com/...）→ 加上 https:
 * - 本地文件名 → 拼接为 /file/image/xxx
 */
export function getImageUrl(image: string | null | undefined): string {
  if (!image) return ''
  // 完整 URL
  if (image.startsWith('http://') || image.startsWith('https://')) {
    return image
  }
  // 协议相对 URL (//xxx.com/path)
  if (image.startsWith('//')) {
    return `https:${image}`
  }
  // 本地文件名
  return `/file/image/${image}`
}
