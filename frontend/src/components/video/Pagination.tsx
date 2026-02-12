import { ChevronLeft, ChevronRight } from 'lucide-react'

interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
  pageSize: number
  pageSizeOptions: readonly number[]
  onPageSizeChange: (size: number) => void
}

export function Pagination({ currentPage, totalPages, onPageChange, pageSize, pageSizeOptions, onPageSizeChange }: PaginationProps) {
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
      <div className="flex items-center gap-1 mr-4">
        {pageSizeOptions.map(size => (
          <button
            key={size}
            onClick={() => onPageSizeChange(size)}
            className={`px-2.5 py-1 rounded-lg text-xs font-medium transition-colors ${
              size === pageSize
                ? 'bg-[#0A84FF] text-white'
                : 'text-[#8E8E93] hover:bg-[#1C1C1E] hover:text-[#F5F5F7]'
            }`}
          >
            {size}条
          </button>
        ))}
      </div>

      <button
        onClick={() => onPageChange(Math.max(0, currentPage - 1))}
        disabled={currentPage === 0}
        className="p-2 rounded-xl text-[#8E8E93] hover:bg-[#1C1C1E] hover:text-[#F5F5F7] disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
      >
        <ChevronLeft className="w-5 h-5" />
      </button>

      {getPageNumbers().map((page, index) => {
        if (page === '...') {
          return (
            <span key={`ellipsis-${index}`} className="px-2 text-[#636366]">
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
              w-10 h-10 rounded-xl font-medium transition-colors duration-200
              ${isActive
                ? 'bg-[#0A84FF] text-white'
                : 'text-[#8E8E93] hover:bg-[#1C1C1E] hover:text-[#F5F5F7]'}
            `}
          >
            {pageNum + 1}
          </button>
        )
      })}

      <button
        onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
        disabled={currentPage === totalPages - 1}
        className="p-2 rounded-xl text-[#8E8E93] hover:bg-[#1C1C1E] hover:text-[#F5F5F7] disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
      >
        <ChevronRight className="w-5 h-5" />
      </button>
    </div>
  )
}
