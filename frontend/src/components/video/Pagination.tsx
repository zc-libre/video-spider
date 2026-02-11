import { ChevronLeft, ChevronRight } from 'lucide-react'

interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null

  // 生成页码数组
  const getPageNumbers = () => {
    const pages: (number | string)[] = []
    const showPages = 5

    if (totalPages <= showPages + 2) {
      // 总页数较少，显示全部
      for (let i = 0; i < totalPages; i++) {
        pages.push(i)
      }
    } else {
      // 总页数较多，显示省略
      pages.push(0)

      let start = Math.max(1, currentPage - 1)
      let end = Math.min(totalPages - 2, currentPage + 1)

      if (currentPage < 3) {
        end = 3
      }
      if (currentPage > totalPages - 4) {
        start = totalPages - 4
      }

      if (start > 1) {
        pages.push('...')
      }

      for (let i = start; i <= end; i++) {
        pages.push(i)
      }

      if (end < totalPages - 2) {
        pages.push('...')
      }

      pages.push(totalPages - 1)
    }

    return pages
  }

  return (
    <div className="flex items-center justify-center space-x-2 py-8">
      <button
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
        disabled={currentPage === 0}
        className="p-2 rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
      >
        <ChevronLeft className="w-5 h-5" />
      </button>

      {getPageNumbers().map((page, index) => {
        if (page === '...') {
          return (
            <span key={`ellipsis-${index}`} className="px-2 text-slate-500">
              ...
            </span>
          )
        }

        const pageNum = page as number
        const isActive = pageNum === currentPage

        return (
          <button
            key={pageNum}
            onClick={() => onPageChange(pageNum)}
            className={`
              w-10 h-10 rounded-lg font-medium transition-all duration-200
              ${isActive
                ? 'bg-rose-600 text-white shadow-lg shadow-rose-600/30 scale-105'
                : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'}
            `}
          >
            {pageNum + 1}
          </button>
        )
      })}

      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
        disabled={currentPage === totalPages - 1}
        className="p-2 rounded-lg text-slate-400 hover:bg-white/5 hover:text-slate-200 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
      >
        <ChevronRight className="w-5 h-5" />
      </button>
    </div>
  )
}
