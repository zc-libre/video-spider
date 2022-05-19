package com.libre.video.core.event;

import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video;
import com.libre.boot.autoconfigure.SpringContext;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.util.List;

public class VideoEventPublisher {
	private static final ApplicationContext applicationContext;

	static {
		 applicationContext = SpringContext.getContext();
		Assert.notNull(applicationContext, "applicationContext must not null");
	}

    public static void publishVideoSaveEvent(List<Video> videoList) {
        VideoSaveEvent videoSaveEvent = new VideoSaveEvent(videoList);
        applicationContext.publishEvent(videoSaveEvent);
    }

    public static void publishErrorEvent(ErrorVideo errorVideo) {
        applicationContext.publishEvent(errorVideo);
    }

	public static void publishVideoUploadEvent(VideoUploadEvent downloadEvent) {
		applicationContext.publishEvent(downloadEvent);
	}
}
