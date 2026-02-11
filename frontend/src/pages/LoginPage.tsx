import { useState } from 'react'
import { Video } from 'lucide-react'

interface LoginPageProps {
  onLogin: (username: string, password: string) => Promise<void>
  loading: boolean
}

export function LoginPage({ onLogin, loading }: LoginPageProps) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await onLogin(username, password)
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败')
    }
  }

  return (
    <div className="min-h-screen bg-black flex items-center justify-center px-4 relative overflow-hidden">
      {/* 背景装饰 - 微妙蓝色渐变 */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,rgba(10,132,255,0.08),transparent_50%)]" />

      <div className="w-full max-w-sm relative z-10">
        {/* Logo */}
        <div className="flex items-center justify-center gap-2 mb-8">
          <div className="p-2 bg-[#0A84FF] rounded-xl">
            <Video className="w-6 h-6 text-white" />
          </div>
          <span className="text-2xl font-bold text-[#F5F5F7]">
            VideoHub
          </span>
        </div>

        {/* Form - iOS Grouped Style */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <h2 className="text-lg font-semibold text-[#F5F5F7] text-center mb-6">登录</h2>

          {error && (
            <div className="text-sm text-[#FF453A] bg-[#FF453A]/10 rounded-xl px-4 py-2.5">{error}</div>
          )}

          <div className="bg-[#1C1C1E] rounded-xl overflow-hidden">
            <div className="px-4 py-3 flex items-center border-b border-[rgba(84,84,88,0.65)]">
              <label className="text-sm text-[#8E8E93] w-16 shrink-0">用户名</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="flex-1 bg-transparent text-sm text-[#F5F5F7] placeholder-[#636366] focus:outline-none"
                placeholder="请输入用户名"
                required
                autoFocus
              />
            </div>
            <div className="px-4 py-3 flex items-center">
              <label className="text-sm text-[#8E8E93] w-16 shrink-0">密码</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="flex-1 bg-transparent text-sm text-[#F5F5F7] placeholder-[#636366] focus:outline-none"
                placeholder="请输入密码"
                required
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 bg-[#0A84FF] text-white text-sm font-semibold rounded-xl hover:bg-[#0A84FF]/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? '登录中...' : '登录'}
          </button>
        </form>
      </div>
    </div>
  )
}
