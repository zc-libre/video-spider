import type { Video } from '@/types/video'
import { VideoCard } from './VideoCard'

interface VideoGridProps {
  videos: Video[]
  onVideoClick: (video: Video) => void
  onAuthorClick?: (author: string) => void
}

export function VideoGrid({ videos, onVideoClick, onAuthorClick }: VideoGridProps) {
  if (videos.length === 0) {
    return (
      <div className="text-center py-20 text-slate-500">
        暂无视频数据
      </div>
    )
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-x-6 gap-y-10">
      {videos.map((video) => (
        <VideoCard key={video.id} video={video} onClick={onVideoClick} onAuthorClick={onAuthorClick} />
      ))}
    </div>
  )
}
