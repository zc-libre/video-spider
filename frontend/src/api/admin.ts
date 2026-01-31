import apiClient from './client'

export const adminApi = {
  /** 触发爬虫 */
  triggerSpider: (type: number): Promise<string> =>
    apiClient.get(`/video/spider/${type}`),

  /** 同步数据到 ES */
  syncToEs: (): Promise<string> =>
    apiClient.get('/video/sync'),

  /** 停止爬虫 */
  shutdownSpider: (): Promise<string> =>
    apiClient.get('/video/shutdown'),
}
