import { useEffect, useState } from 'react'
import { X, ThumbsUp, Share2, Eye } from 'lucide-react'
import { VideoPlayer } from './VideoPlayer'
import { videoApi } from '@/api/video'
import type { Video } from '@/types/video'
import { formatViewCount, getImageUrl } from '@/lib/format'

interface VideoModalProps {
  video: Video | null
  isOpen: boolean
  onClose: () => void
}

export function VideoModal({ video, isOpen, onClose }: VideoModalProps) {
  const [videoUrl, setVideoUrl] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (isOpen && video) {
      setLoading(true)
      setError(null)
      setVideoUrl(null)

      videoApi
        .getWatchUrl(video.id)
        .then((url) => setVideoUrl(url))
        .catch((err) => setError(err.message || '获取播放地址失败'))
        .finally(() => setLoading(false))
    }
  }, [isOpen, video])

  // 按 ESC 关闭
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

  if (!isOpen || !video) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/80 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative bg-white w-full max-w-5xl rounded-2xl overflow-hidden shadow-2xl flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-300">
        {/* Video Player */}
        <div className="relative aspect-video bg-black w-full">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 z-10 p-2 bg-black/50 hover:bg-black/70 text-white rounded-full transition-colors"
          >
            <X className="w-5 h-5" />
          </button>

          {loading && (
            <div className="w-full h-full flex items-center justify-center">
              <div className="w-8 h-8 border-4 border-white/30 border-t-white rounded-full animate-spin" />
            </div>
          )}

          {error && (
            <div className="w-full h-full flex items-center justify-center text-red-400">
              {error}
            </div>
          )}

          {videoUrl && !loading && (
            <VideoPlayer src={videoUrl} poster={getImageUrl(video.image)} />
          )}
        </div>

        {/* Details */}
        <div className="p-6 overflow-y-auto">
          <h2 className="text-2xl font-bold text-slate-900 mb-2">
            {video.title}
          </h2>

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-100 pb-6 mb-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-bold">
                {(video.author || '未知')[0]}
              </div>
              <div>
                <h4 className="font-semibold text-slate-900">
                  {video.author || '未知'}
                </h4>
                <p className="text-sm text-slate-500 flex items-center gap-1">
                  <Eye className="w-3.5 h-3.5" />
                  {formatViewCount(video.lookNum)} 观看
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button className="flex items-center gap-2 px-4 py-2 bg-slate-100 rounded-full hover:bg-slate-200 transition-colors text-sm font-medium">
                <ThumbsUp className="w-4 h-4" />
                {formatViewCount(video.collectNum)}
              </button>
              <button className="flex items-center gap-2 px-4 py-2 bg-slate-100 rounded-full hover:bg-slate-200 transition-colors text-sm font-medium">
                <Share2 className="w-4 h-4" />
                分享
              </button>
            </div>
          </div>

          <div className="bg-slate-50 p-4 rounded-xl text-sm text-slate-600">
            <p>发布时间：{video.publishTime || '未知'}</p>
          </div>
        </div>
      </div>
    </div>
  )
}
