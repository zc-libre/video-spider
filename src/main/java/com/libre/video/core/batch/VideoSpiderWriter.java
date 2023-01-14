package com.libre.video.core.batch;

import com.google.common.collect.Lists;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collection;
import java.util.List;

/**
 * @author: Libre
 * @Date: 2023/1/14 11:29 PM
 */
@RequiredArgsConstructor
public class VideoSpiderWriter implements ItemWriter<Video> {

	private final VideoService videoService;

	@Override
	public void write(@NonNull List<? extends Video> videos) throws Exception {
		VideoEventPublisher.publishVideoSaveEvent(Lists.newArrayList(videos));
	}

}
