import axios from 'axios'
import type { ApiResponse } from '@/types/video'

const TOKEN_KEY = 'auth_token'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
})

// 请求拦截器：携带 token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

// 响应拦截器：统一处理 R<T> 格式
apiClient.interceptors.response.use(
  (response) => {
    const data: ApiResponse<unknown> = response.data
    if (data.success) {
      return data.data as never
    }
    return Promise.reject(new Error(data.msg || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY)
      window.location.reload()
    }
    const message = error.response?.data?.msg || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export default apiClient
