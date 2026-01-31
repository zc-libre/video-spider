import { useState, useCallback, useEffect } from 'react'
import * as authApi from '@/api/auth'

const TOKEN_KEY = 'auth_token'

export function useAuth() {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [username, setUsername] = useState<string>('')
  const [loading, setLoading] = useState(false)

  const isLoggedIn = !!token

  // 启动时校验 token 有效性
  useEffect(() => {
    if (!token) return
    authApi.getUserInfo(token).then((info) => {
      setUsername(info.name)
    }).catch(() => {
      localStorage.removeItem(TOKEN_KEY)
      setToken(null)
    })
  }, [token])

  const login = useCallback(async (user: string, password: string) => {
    setLoading(true)
    try {
      const result = await authApi.login(user, password)
      localStorage.setItem(TOKEN_KEY, result.token)
      setToken(result.token)
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(async () => {
    if (token) {
      try {
        await authApi.logout(token)
      } catch {
        // 忽略登出接口错误
      }
    }
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUsername('')
  }, [token])

  return { token, username, isLoggedIn, loading, login, logout }
}
