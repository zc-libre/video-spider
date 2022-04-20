package com.libre.video.core.listener;

import com.libre.boot.exception.LibreErrorEvent;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.Exceptions;
import com.libre.video.core.event.VideoSaveEvent;
import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video;
import com.libre.video.service.ErrorVideoService;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoEventListener {

    private final VideoService videoService;
    private final ErrorVideoService errorVideoService;

    @Async("taskScheduler")
    @EventListener(VideoSaveEvent.class)
    public void onSaveEvent(VideoSaveEvent videoSaveEvent) {
        List<Video> videoList = videoSaveEvent.getVideoList();
        if (CollectionUtil.isEmpty(videoList)) {
            return;
        }
        log.info("start save videos.....");
		try {
			videoService.saveOrUpdateBatch(videoList);
		} catch (Exception e) {
			log.error("保存数据失败: {}", e.getMessage());
		}
	}

    @Async("taskScheduler")
    @EventListener(ErrorVideo.class)
    public void onErrorEvent(ErrorVideo errorVideo) {
        log.info("start save error video, errorType: {}", errorVideo.getType());
        errorVideoService.save(errorVideo);
    }

	@EventListener(LibreErrorEvent.class)
	public void exceptionsEvent(LibreErrorEvent event) {
		log.error("发生异常： {}", event.toString());
	}
}
