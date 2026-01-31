import apiClient from './client'
import type { Video, PageResponse, VideoQuery } from '@/types/video'
import qs from 'qs'

export const videoApi = {
  /**
   * 分页查询视频列表
   */
  getList: async (
    current: number,
    size: number,
    query?: VideoQuery
  ): Promise<PageResponse<Video>> => {
    const params = {
      current,
      size,
      ...query,
    }
    return apiClient.post('/video/list', qs.stringify(params))
  },

  /**
   * 获取视频播放地址
   */
  getWatchUrl: async (videoId: number): Promise<string> => {
    return apiClient.get(`/video/watch/${videoId}`)
  },
}
