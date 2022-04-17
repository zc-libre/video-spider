package com.libre.video.core;

import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video91;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.video.pojo.dto.Video9s;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class VideoEventPublisher {

    public static void publishVideo91SaveEvent(List<Video91> video91List) {
        Video91SaveEvent video91SaveEvent = new Video91SaveEvent(video91List);
        ApplicationContext applicationContext = SpringContext.getContext();
        applicationContext.publishEvent(video91SaveEvent);
    }

    public static void publishVideo9sSaveEvent(List<Video9s> video9sList) {
        Video9sSaveEvent video9sSaveEvent = new Video9sSaveEvent(video9sList);
        ApplicationContext applicationContext = SpringContext.getContext();
        applicationContext.publishEvent(video9sSaveEvent);
    }

    public static void publishErrorEvent(ErrorVideo errorVideo) {
        ApplicationContext applicationContext = SpringContext.getContext();
        applicationContext.publishEvent(errorVideo);
    }
}
