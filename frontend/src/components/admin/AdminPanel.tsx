import { useState, useEffect, useCallback } from 'react'
import { X, Bug, RefreshCw, StopCircle, Loader2 } from 'lucide-react'
import { adminApi } from '@/api/admin'

interface AdminPanelProps {
  isOpen: boolean
  onClose: () => void
}

const SPIDER_SOURCES = [
  { type: 1, label: '91porn', color: 'from-pink-500 to-rose-600' },
  { type: 2, label: '九色', color: 'from-amber-500 to-orange-600' },
  { type: 3, label: 'baav', color: 'from-cyan-500 to-blue-600' },
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
      <p className={`mt-2 text-xs ${r.ok ? 'text-green-400' : 'text-red-400'}`}>
        {r.msg}
      </p>
    )
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/80 backdrop-blur-sm" onClick={onClose} />

      {/* Panel */}
      <div className="relative bg-[#12121e]/95 backdrop-blur-2xl border border-white/10 w-full max-w-lg rounded-2xl shadow-2xl animate-in fade-in zoom-in-95 duration-300">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/[0.08]">
          <h2 className="text-lg font-bold text-slate-100">管理面板</h2>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-200 hover:bg-white/10 rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* 爬虫操作区 */}
          <section>
            <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">
              爬虫触发
            </h3>
            <div className="grid grid-cols-3 gap-3">
              {SPIDER_SOURCES.map(({ type, label, color }) => {
                const key: ActionKey = `spider-${type}`
                const isLoading = !!loadingMap[key]
                return (
                  <div key={type} className="flex flex-col">
                    <button
                      disabled={isLoading}
                      onClick={() => exec(key, () => adminApi.triggerSpider(type))}
                      className={`flex flex-col items-center gap-2 p-4 rounded-xl bg-gradient-to-br ${color} text-white font-medium text-sm hover:opacity-90 active:scale-95 transition-all disabled:opacity-60 shadow-lg`}
                    >
                      {isLoading ? (
                        <Loader2 className="w-6 h-6 animate-spin" />
                      ) : (
                        <Bug className="w-6 h-6" />
                      )}
                      {label}
                    </button>
                    {renderResult(key)}
                  </div>
                )
              })}
            </div>
          </section>

          {/* 系统操作区 */}
          <section>
            <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wide mb-3">
              系统操作
            </h3>
            <div className="grid grid-cols-2 gap-3">
              {/* 同步到 ES */}
              <div className="flex flex-col">
                <button
                  disabled={!!loadingMap['sync']}
                  onClick={() => exec('sync', adminApi.syncToEs)}
                  className="flex items-center justify-center gap-2 p-3 rounded-xl bg-white/5 border border-white/[0.08] text-slate-300 font-medium text-sm hover:bg-white/10 active:scale-95 transition-all disabled:opacity-60"
                >
                  {loadingMap['sync'] ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                  ) : (
                    <RefreshCw className="w-5 h-5" />
                  )}
                  同步到 ES
                </button>
                {renderResult('sync')}
              </div>

              {/* 停止爬虫 */}
              <div className="flex flex-col">
                <button
                  disabled={!!loadingMap['shutdown']}
                  onClick={() => exec('shutdown', adminApi.shutdownSpider)}
                  className="flex items-center justify-center gap-2 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 font-medium text-sm hover:bg-red-500/20 active:scale-95 transition-all disabled:opacity-60"
                >
                  {loadingMap['shutdown'] ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                  ) : (
                    <StopCircle className="w-5 h-5" />
                  )}
                  停止爬虫
                </button>
                {renderResult('shutdown')}
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  )
}
