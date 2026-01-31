import { useState, useEffect, useCallback } from 'react'
import { videoApi } from '@/api/video'
import type { Video, VideoQuery } from '@/types/video'

interface UseVideosOptions {
  initialPage?: number
  pageSize?: number
}

export function useVideos(options: UseVideosOptions = {}) {
  const { initialPage = 0, pageSize = 24 } = options

  const [videos, setVideos] = useState<Video[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const [page, setPage] = useState(initialPage)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [query, setQuery] = useState<VideoQuery>({})

  const fetchVideos = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await videoApi.getList(page, pageSize, query)
      setVideos(response.content)
      setTotalPages(response.totalPages)
      setTotalElements(response.totalElements)
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch'))
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, query])

  useEffect(() => {
    fetchVideos()
  }, [fetchVideos])

  const search = useCallback((title: string) => {
    setQuery({ title: title || undefined })
    setPage(0)
  }, [])

  const goToPage = useCallback((newPage: number) => {
    setPage(newPage)
  }, [])

  return {
    videos,
    loading,
    error,
    page,
    totalPages,
    totalElements,
    search,
    goToPage,
    refetch: fetchVideos,
  }
}
