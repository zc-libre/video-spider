package com.libre.video.core;

import com.libre.core.toolkit.CollectionUtil;
import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video91;
import com.libre.video.pojo.dto.Video9s;
import com.libre.video.service.ErrorVideoService;
import com.libre.video.service.Video9sService;
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
    private final Video9sService video9sService;
    private final ErrorVideoService errorVideoService;

    @Async("taskScheduler")
    @EventListener(Video91SaveEvent.class)
    public void onSaveEvent(Video91SaveEvent video91SaveEvent) {
        List<Video91> video91List = video91SaveEvent.getVideo91List();
        if (CollectionUtil.isEmpty(video91List)) {
            return;
        }
        log.info("start save videos.....");
        videoService.saveOrUpdateBatch(video91List);
    }

    @Async("taskScheduler")
    @EventListener(Video9sSaveEvent.class)
    public void onVideo9sSaveEvent(Video9sSaveEvent video9sSaveEvent) {
        List<Video9s> videoList = video9sSaveEvent.getVideoList();
        if (CollectionUtil.isEmpty(videoList)) {
            return;
        }
        log.info("start save videos.....");
        video9sService.saveOrUpdateBatch(videoList);
    }

    @Async("taskScheduler")
    @EventListener(ErrorVideo.class)
    public void onErrorEvent(ErrorVideo errorVideo) {
        log.info("start save error video, errorType: {}", errorVideo.getType());
        errorVideoService.save(errorVideo);
    }
}
