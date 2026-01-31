export interface Video {
  id: number
  videoId: number
  url: string
  realUrl: string
  title: string
  image: string
  duration: string
  author: string
  lookNum: number
  collectNum: number
  videoPath: string
  videoWebsite: number
  publishTime: string
  createTime: string
  updateTime: string
}

export interface VideoQuery {
  title?: string
  author?: string
  sort?: string
  sortOrder?: number
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface ApiResponse<T> {
  code: number
  success: boolean
  data: T
  msg: string
}
