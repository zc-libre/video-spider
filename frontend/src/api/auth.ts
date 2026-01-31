import axios from 'axios'
import type { ApiResponse } from '@/types/video'

const authClient = axios.create({
  baseURL: '/video/user',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

interface LoginResult {
  token: string
}

interface UserInfo {
  name: string
  roles: string[]
  avatar: string
}

export async function login(username: string, password: string): Promise<LoginResult> {
  const response = await authClient.post<ApiResponse<LoginResult>>('/login', { username, password })
  const data = response.data
  if (data.success) {
    return data.data
  }
  throw new Error(data.msg || '登录失败')
}

export async function getUserInfo(token: string): Promise<UserInfo> {
  const response = await authClient.post<ApiResponse<UserInfo>>('/info', null, {
    headers: { Authorization: token },
  })
  const data = response.data
  if (data.success) {
    return data.data
  }
  throw new Error(data.msg || '获取用户信息失败')
}

export async function logout(token: string): Promise<void> {
  await authClient.post('/logout', null, {
    headers: { Authorization: token },
  })
}
