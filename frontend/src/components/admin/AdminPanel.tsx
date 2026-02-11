import { useState, useEffect, useCallback } from 'react'
import { X, Bug, RefreshCw, StopCircle, Loader2 } from 'lucide-react'
import { adminApi } from '@/api/admin'

interface AdminPanelProps {
  isOpen: boolean
  onClose: () => void
}

const SPIDER_SOURCES = [
  { type: 1, label: '91porn', color: 'bg-[#0A84FF]' },
  { type: 2, label: '九色', color: 'bg-[#FF9F0A]' },
  { type: 3, label: 'baav', color: 'bg-[#5E5CE6]' },
] as const

type ActionKey = `spider-${number}` | 'sync' | 'shutdown'

export function AdminPanel({ isOpen, onClose }: AdminPanelProps) {
  const [loadingMap, setLoadingMap] = useState<Record<string, boolean>>({})
  const [resultMap, setResultMap] = useState<Record<string, { ok: boolean; msg: string }>>({})

  const setLoading = (key: ActionKey, val: boolean) =>
    setLoadingMap((prev) => ({ ...prev, [key]: val }))

  const setResult = (key: ActionKey, ok: boolean, msg: string) =>
    setResultMap((prev) => ({ ...prev, [key]: { ok, msg } }))

  const exec = useCallback(async (key: ActionKey, fn: () => Promise<string>) => {
    setLoading(key, true)
    setResultMap((prev) => {
      const next = { ...prev }
      delete next[key]
      return next
    })
    try {
      const msg = await fn()
      setResult(key, true, msg || '操作成功')
    } catch (e: unknown) {
      setResult(key, false, e instanceof Error ? e.message : '操作失败')
    } finally {
      setLoading(key, false)
    }
  }, [])

  // ESC 关闭
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    if (isOpen) {
      document.addEventListener('keydown', handleEsc)
      document.body.style.overflow = 'hidden'
    }
    return () => {
      document.removeEventListener('keydown', handleEsc)
      document.body.style.overflow = ''
    }
  }, [isOpen, onClose])

  if (!isOpen) return null

  const renderResult = (key: ActionKey) => {
    const r = resultMap[key]
    if (!r) return null
    return (
      <p className={`mt-2 text-xs ${r.ok ? 'text-[#30D158]' : 'text-[#FF453A]'}`}>
        {r.msg}
      </p>
    )
  }

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center sm:p-4">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/80 backdrop-blur-sm" onClick={onClose} />

      {/* Panel - iOS Settings Style */}
      <div className="relative bg-[#1C1C1E] w-full sm:max-w-lg sm:rounded-2xl rounded-t-2xl shadow-2xl animate-in fade-in slide-in-from-bottom-4 duration-300">
        {/* 顶部拖拽指示器 (移动端) */}
        <div className="sm:hidden flex justify-center pt-2 pb-1">
          <div className="w-9 h-1 bg-[#3A3A3C] rounded-full" />
        </div>

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-[rgba(84,84,88,0.65)]">
          <h2 className="text-lg font-bold text-[#F5F5F7]">管理面板</h2>
          <button
            onClick={onClose}
            className="p-1.5 text-[#8E8E93] hover:text-[#F5F5F7] hover:bg-[#2C2C2E] rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* 爬虫操作区 - iOS 分组列表风格 */}
          <section>
            <h3 className="text-xs font-semibold text-[#8E8E93] uppercase tracking-wide mb-3 px-1">
              爬虫触发
            </h3>
            <div className="bg-[#2C2C2E] rounded-xl overflow-hidden">
              {SPIDER_SOURCES.map(({ type, label, color }, index) => {
                const key: ActionKey = `spider-${type}`
                const isLoading = !!loadingMap[key]
                return (
                  <div key={type}>
                    {index > 0 && <div className="mx-4 border-t border-[rgba(84,84,88,0.65)]" />}
                    <button
                      disabled={isLoading}
                      onClick={() => exec(key, () => adminApi.triggerSpider(type))}
                      className="w-full flex items-center gap-3 px-4 py-3.5 hover:bg-[#3A3A3C]/50 transition-colors disabled:opacity-60"
                    >
                      <div className={`w-8 h-8 ${color} rounded-lg flex items-center justify-center`}>
                        {isLoading ? (
                          <Loader2 className="w-4 h-4 text-white animate-spin" />
                        ) : (
                          <Bug className="w-4 h-4 text-white" />
                        )}
                      </div>
                      <span className="text-sm font-medium text-[#F5F5F7]">{label}</span>
                    </button>
                    {renderResult(key)}
                  </div>
                )
              })}
            </div>
          </section>

          {/* 系统操作区 - iOS 分组列表风格 */}
          <section>
            <h3 className="text-xs font-semibold text-[#8E8E93] uppercase tracking-wide mb-3 px-1">
              系统操作
            </h3>
            <div className="bg-[#2C2C2E] rounded-xl overflow-hidden">
              {/* 同步到 ES */}
              <button
                disabled={!!loadingMap['sync']}
                onClick={() => exec('sync', adminApi.syncToEs)}
                className="w-full flex items-center gap-3 px-4 py-3.5 hover:bg-[#3A3A3C]/50 transition-colors disabled:opacity-60"
              >
                <div className="w-8 h-8 bg-[#30D158] rounded-lg flex items-center justify-center">
                  {loadingMap['sync'] ? (
                    <Loader2 className="w-4 h-4 text-white animate-spin" />
                  ) : (
                    <RefreshCw className="w-4 h-4 text-white" />
                  )}
                </div>
                <span className="text-sm font-medium text-[#F5F5F7]">同步到 ES</span>
              </button>
              {renderResult('sync')}

              <div className="mx-4 border-t border-[rgba(84,84,88,0.65)]" />

              {/* 停止爬虫 */}
              <button
                disabled={!!loadingMap['shutdown']}
                onClick={() => exec('shutdown', adminApi.shutdownSpider)}
                className="w-full flex items-center gap-3 px-4 py-3.5 hover:bg-[#3A3A3C]/50 transition-colors disabled:opacity-60"
              >
                <div className="w-8 h-8 bg-[#FF453A] rounded-lg flex items-center justify-center">
                  {loadingMap['shutdown'] ? (
                    <Loader2 className="w-4 h-4 text-white animate-spin" />
                  ) : (
                    <StopCircle className="w-4 h-4 text-white" />
                  )}
                </div>
                <span className="text-sm font-medium text-[#FF453A]">停止爬虫</span>
              </button>
              {renderResult('shutdown')}
            </div>
          </section>
        </div>
      </div>
    </div>
  )
}
