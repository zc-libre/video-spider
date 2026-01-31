import { useState, useEffect } from 'react'
import { Header } from '@/components/layout/Header'
import { VideoGrid } from '@/components/video/VideoGrid'
import { VideoModal } from '@/components/video/VideoModal'
import { Pagination } from '@/components/video/Pagination'
import { Loading } from '@/components/common/Loading'
import { useVideos } from '@/hooks/useVideos'
import { useDebounce } from '@/hooks/useDebounce'
import type { Video } from '@/types/video'

export function HomePage() {
  const [searchTerm, setSearchTerm] = useState('')
  const debouncedSearch = useDebounce(searchTerm, 500)

  const {
    videos,
    loading,
    error,
    page,
    totalPages,
    totalElements,
    search,
    goToPage,
  } = useVideos({ pageSize: 24 })

  const [selectedVideo, setSelectedVideo] = useState<Video | null>(null)
  const [modalOpen, setModalOpen] = useState(false)

  // 搜索词变化时触发搜索
  useEffect(() => {
    search(debouncedSearch)
  }, [debouncedSearch, search])

  const handleVideoClick = (video: Video) => {
    setSelectedVideo(video)
    setModalOpen(true)
  }

  const handleSearch = (query: string) => {
    setSearchTerm(query)
  }

  return (
    <div className="min-h-screen bg-white">
      <Header onSearch={handleSearch} />

      <main className="container mx-auto px-4 py-8">
        {/* 分类标签 */}
        <div className="flex gap-2 overflow-x-auto pb-6 scrollbar-hide mb-2">
          {['全部', '热门', '最新', '推荐'].map((tag, i) => (
            <button
              key={tag}
              className={`px-4 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
                i === 0
                  ? 'bg-slate-900 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {tag}
            </button>
          ))}
        </div>

        {/* 统计信息 */}
        {!loading && !error && (
          <div className="text-sm text-slate-500 mb-4">
            共 {totalElements} 个视频
          </div>
        )}

        {/* 视频列表 */}
        {loading ? (
          <Loading />
        ) : error ? (
          <div className="text-center py-20 text-red-500">{error.message}</div>
        ) : (
          <VideoGrid videos={videos} onVideoClick={handleVideoClick} />
        )}

        {/* 分页 */}
        {!loading && !error && totalPages > 1 && (
          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={goToPage}
          />
        )}
      </main>

      {/* 视频播放模态框 */}
      <VideoModal
        video={selectedVideo}
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
      />
    </div>
  )
}
