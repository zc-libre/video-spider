import { useEffect, useRef } from 'react'
import Hls from 'hls.js'

interface VideoPlayerProps {
  src: string
  poster?: string
  autoPlay?: boolean
}

export function VideoPlayer({ src, poster, autoPlay = true }: VideoPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null)
  const hlsRef = useRef<Hls | null>(null)

  useEffect(() => {
    const video = videoRef.current
    if (!video || !src) return

    // 判断是否为 m3u8 格式
    const isHls = src.includes('.m3u8')
    const videoUrl = src.startsWith('http') ? src : `/file/${src}`

    if (isHls && Hls.isSupported()) {
      // 使用 HLS.js 播放
      const hls = new Hls({
        enableWorker: true,
        lowLatencyMode: true,
      })
      hlsRef.current = hls
      hls.loadSource(videoUrl)
      hls.attachMedia(video)
      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        if (autoPlay) {
          video.play().catch(console.error)
        }
      })
      hls.on(Hls.Events.ERROR, (_event, data) => {
        if (data.fatal) {
          console.error('HLS error:', data)
        }
      })
    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
      // Safari 原生支持 HLS
      video.src = videoUrl
      if (autoPlay) {
        video.play().catch(console.error)
      }
    } else {
      // 普通视频格式
      video.src = videoUrl
      if (autoPlay) {
        video.play().catch(console.error)
      }
    }

    return () => {
      if (hlsRef.current) {
        hlsRef.current.destroy()
        hlsRef.current = null
      }
    }
  }, [src, autoPlay])

  return (
    <video
      ref={videoRef}
      className="w-full h-full bg-black"
      controls
      poster={poster}
      playsInline
    />
  )
}
