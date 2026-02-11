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
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center sm:p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/80 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal - iOS Sheet Style */}
      <div className="relative bg-[#1C1C1E] w-full sm:max-w-5xl sm:rounded-2xl rounded-t-2xl overflow-hidden shadow-2xl flex flex-col max-h-[90vh] animate-in fade-in slide-in-from-bottom-4 duration-300">
        {/* 顶部拖拽指示器 (移动端) */}
        <div className="sm:hidden flex justify-center pt-2 pb-1">
          <div className="w-9 h-1 bg-[#3A3A3C] rounded-full" />
        </div>

        {/* Video Player */}
        <div className="relative aspect-video bg-black w-full">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 z-10 p-2 bg-[#2C2C2E]/80 hover:bg-[#3A3A3C] text-white rounded-full transition-colors backdrop-blur-sm"
          >
            <X className="w-5 h-5" />
          </button>

          {loading && (
            <div className="w-full h-full flex items-center justify-center">
              <div className="w-8 h-8 border-4 border-[#0A84FF]/30 border-t-[#0A84FF] rounded-full animate-spin" />
            </div>
          )}

          {error && (
            <div className="w-full h-full flex items-center justify-center text-[#FF453A]">
              {error}
            </div>
          )}

          {videoUrl && !loading && (
            <VideoPlayer src={videoUrl} poster={getImageUrl(video.image)} />
          )}
        </div>

        {/* Details */}
        <div className="p-6 overflow-y-auto">
          <h2 className="text-2xl font-bold text-[#F5F5F7] mb-2">
            {video.title}
          </h2>

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[rgba(84,84,88,0.65)] pb-6 mb-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-[#0A84FF] flex items-center justify-center text-white font-bold">
                {(video.author || '未知')[0]}
              </div>
              <div>
                <h4 className="font-semibold text-[#F5F5F7]">
                  {video.author || '未知'}
                </h4>
                <p className="text-sm text-[#8E8E93] flex items-center gap-1">
                  <Eye className="w-3.5 h-3.5" />
                  {formatViewCount(video.lookNum)} 观看
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <button className="flex items-center gap-2 px-4 py-2 bg-[#2C2C2E] rounded-full hover:bg-[#3A3A3C] transition-colors text-sm font-medium text-[#F5F5F7]">
                <ThumbsUp className="w-4 h-4" />
                {formatViewCount(video.collectNum)}
              </button>
              <button className="flex items-center gap-2 px-4 py-2 bg-[#2C2C2E] rounded-full hover:bg-[#3A3A3C] transition-colors text-sm font-medium text-[#F5F5F7]">
                <Share2 className="w-4 h-4" />
                分享
              </button>
            </div>
          </div>

          <div className="bg-[#2C2C2E] p-4 rounded-xl text-sm text-[#8E8E93]">
            <p>发布时间：{video.publishTime || '未知'}</p>
          </div>
        </div>
      </div>
    </div>
  )
}
