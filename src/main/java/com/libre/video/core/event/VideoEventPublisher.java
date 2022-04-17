package com.libre.video.core.event;

import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video;
import com.libre.boot.autoconfigure.SpringContext;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class VideoEventPublisher {

    public static void publishVideoSaveEvent(List<Video> videoList) {
        VideoSaveEvent videoSaveEvent = new VideoSaveEvent(videoList);
        ApplicationContext applicationContext = SpringContext.getContext();
        applicationContext.publishEvent(videoSaveEvent);
    }

    public static void publishErrorEvent(ErrorVideo errorVideo) {
        ApplicationContext applicationContext = SpringContext.getContext();
        applicationContext.publishEvent(errorVideo);
    }
}
