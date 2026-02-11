import { Play, Eye, User, Globe, Calendar, Clock } from 'lucide-react'
import type { Video } from '@/types/video'
import { formatViewCount, formatDate, getImageUrl, getWebsiteLabel } from '@/lib/format'

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
      <div className="relative aspect-video rounded-2xl overflow-hidden bg-[#1C1C1E] transition-all duration-200 ring-1 ring-[rgba(84,84,88,0.65)] group-hover:ring-[#F5F5F7]/20">
        <img
          src={getImageUrl(video.image)}
          alt={video.title}
          className="w-full h-full object-cover"
          loading="lazy"
        />
        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-colors duration-200 flex items-center justify-center">
          <div className="opacity-0 group-hover:opacity-100 w-12 h-12 bg-[#0A84FF]/90 backdrop-blur-sm rounded-full flex items-center justify-center transition-opacity duration-200">
            <Play className="w-6 h-6 text-white fill-white ml-1" />
          </div>
        </div>
        <span className="absolute bottom-2 right-2 px-1.5 py-0.5 bg-black/80 text-white text-xs font-medium rounded-md">
          {video.duration || '00:00'}
        </span>
      </div>

      {/* Info */}
      <div className="flex flex-col gap-1">
        <h3 className="font-semibold text-[#F5F5F7] leading-tight line-clamp-2 group-hover:text-[#0A84FF] transition-colors">
          {video.title}
        </h3>
        <div className="flex items-center gap-3 text-sm text-[#8E8E93]">
          <span
            className={`flex items-center gap-1 ${onAuthorClick && video.author ? 'hover:text-[#0A84FF] transition-colors' : ''}`}
            onClick={handleAuthorClick}
          >
            <User className="w-3.5 h-3.5" />
            {video.author || '未知'}
          </span>
          <span className="flex items-center gap-1">
            <Eye className="w-3.5 h-3.5" />
            {formatViewCount(video.lookNum)}
          </span>
          <span className="flex items-center gap-1">
            <Globe className="w-3.5 h-3.5" />
            {getWebsiteLabel(video.videoWebsite)}
          </span>
        </div>
        <div className="flex items-center gap-3 text-xs text-[#636366]">
          {video.publishTime && (
            <span className="flex items-center gap-1">
              <Calendar className="w-3 h-3" />
              {video.publishTime}
            </span>
          )}
          {video.createTime && (
            <span className="flex items-center gap-1">
              <Clock className="w-3 h-3" />
              {formatDate(video.createTime)}
            </span>
          )}
        </div>
      </div>
    </div>
  )
}
