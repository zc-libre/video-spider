import axios from 'axios'
import type { ApiResponse } from '@/types/video'

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
  },
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
    const message = error.response?.data?.msg || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export default apiClient
