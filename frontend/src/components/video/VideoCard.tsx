import { Play, Eye, User } from 'lucide-react'
import type { Video } from '@/types/video'
import { formatViewCount, getImageUrl } from '@/lib/format'

interface VideoCardProps {
  video: Video
  onClick: (video: Video) => void
  onAuthorClick?: (author: string) => void
}

export function VideoCard({ video, onClick, onAuthorClick }: VideoCardProps) {
  const handleAuthorClick = (e: React.MouseEvent) => {
    e.stopPropagation()
    if (onAuthorClick && video.author) {
      onAuthorClick(video.author)
    }
  }

  return (
    <div
      className="group flex flex-col gap-3 cursor-pointer"
      onClick={() => onClick(video)}
    >
      {/* Thumbnail */}
      <div className="relative aspect-video rounded-xl overflow-hidden bg-slate-200 shadow-sm group-hover:shadow-xl transition-all duration-300 group-hover:-translate-y-1">
        <img
          src={getImageUrl(video.image)}
          alt={video.title}
          className="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-500"
          loading="lazy"
        />
        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors duration-300 flex items-center justify-center">
          <div className="opacity-0 group-hover:opacity-100 w-12 h-12 bg-white/30 backdrop-blur-sm rounded-full flex items-center justify-center transition-all duration-300 scale-50 group-hover:scale-100">
            <Play className="w-6 h-6 text-white fill-white ml-1" />
          </div>
        </div>
        <span className="absolute bottom-2 right-2 px-1.5 py-0.5 bg-black/80 text-white text-xs font-medium rounded">
          {video.duration || '00:00'}
        </span>
      </div>

      {/* Info */}
      <div className="flex flex-col gap-1">
        <h3 className="font-semibold text-slate-900 leading-tight line-clamp-2 group-hover:text-blue-600 transition-colors">
          {video.title}
        </h3>
        <div className="flex items-center gap-3 text-sm text-slate-500">
          <span
            className={`flex items-center gap-1 ${onAuthorClick && video.author ? 'hover:text-blue-600 transition-colors' : ''}`}
            onClick={handleAuthorClick}
          >
            <User className="w-3.5 h-3.5" />
            {video.author || '未知'}
          </span>
          <span className="flex items-center gap-1">
            <Eye className="w-3.5 h-3.5" />
            {formatViewCount(video.lookNum)}
          </span>
        </div>
      </div>
    </div>
  )
}
