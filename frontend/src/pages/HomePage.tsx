import { useState, useEffect } from 'react'
import { Header } from '@/components/layout/Header'
import { VideoGrid } from '@/components/video/VideoGrid'
import { VideoModal } from '@/components/video/VideoModal'
import { Pagination } from '@/components/video/Pagination'
import { Loading } from '@/components/common/Loading'
import { useVideos } from '@/hooks/useVideos'
import { useDebounce } from '@/hooks/useDebounce'
import type { Video } from '@/types/video'
import { ArrowUpDown, X } from 'lucide-react'

const SORT_TABS = [
  { label: '热门', sort: 'lookNum', sortOrder: 0 },
  { label: '最新发布', sort: 'publishTime', sortOrder: 0 },
  { label: '最新收录', sort: 'createTime', sortOrder: 0 },
  { label: '最多收藏', sort: 'collectNum', sortOrder: 0 },
] as const

interface HomePageProps {
  onLogout?: () => void
  username?: string
}

export function HomePage({ onLogout, username }: HomePageProps) {
  const [searchTerm, setSearchTerm] = useState('')
  const debouncedSearch = useDebounce(searchTerm, 500)
  const [activeTab, setActiveTab] = useState<number | null>(null)

  const {
    videos,
    loading,
    error,
    page,
    totalPages,
    totalElements,
    query,
    search,
    updateSort,
    updateAuthor,
    resetQuery,
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

  const handleResetAll = () => {
    setActiveTab(null)
    setSearchTerm('')
    resetQuery()
  }

  const handleTabClick = (index: number) => {
    if (activeTab === index) {
      // 点击已激活的 tab，切换排序方向
      const currentOrder = query.sortOrder === 1 ? 0 : 1
      updateSort(SORT_TABS[index].sort, currentOrder)
    } else {
      setActiveTab(index)
      updateSort(SORT_TABS[index].sort, SORT_TABS[index].sortOrder)
    }
  }

  const handleAuthorClick = (author: string) => {
    updateAuthor(author)
  }

  const clearAuthorFilter = () => {
    updateAuthor('')
  }

  return (
    <div className="min-h-screen bg-white">
      <Header searchValue={searchTerm} onSearchChange={setSearchTerm} onSearch={handleSearch} onLogout={onLogout} username={username} />

      <main className="container mx-auto px-4 py-8">
        {/* 排序标签 */}
        <div className="flex gap-2 overflow-x-auto pb-4 scrollbar-hide">
          <button
            onClick={handleResetAll}
            className={`px-4 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
              activeTab === null && !query.title && !query.author
                ? 'bg-slate-900 text-white'
                : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
            }`}
          >
            全部
          </button>
          {SORT_TABS.map((tab, i) => (
            <button
              key={tab.label}
              onClick={() => handleTabClick(i)}
              className={`flex items-center gap-1 px-4 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
                activeTab === i
                  ? 'bg-slate-900 text-white'
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
              }`}
            >
              {tab.label}
              {activeTab === i && (
                <ArrowUpDown className="w-3.5 h-3.5 opacity-70" />
              )}
            </button>
          ))}
        </div>

        {/* 作者过滤标签 */}
        {query.author && (
          <div className="flex items-center gap-2 pb-4">
            <span className="text-sm text-slate-500">筛选：</span>
            <span className="inline-flex items-center gap-1 px-3 py-1 bg-blue-50 text-blue-700 text-sm font-medium rounded-full">
              作者：{query.author}
              <button
                onClick={clearAuthorFilter}
                className="ml-0.5 p-0.5 hover:bg-blue-100 rounded-full transition-colors"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </span>
          </div>
        )}

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
          <VideoGrid videos={videos} onVideoClick={handleVideoClick} onAuthorClick={handleAuthorClick} />
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
