package com.libre.video.core.batch;

import com.google.common.collect.Lists;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Libre
 * @Date: 2023/1/13 12:37 AM
 */
@Component
@RequiredArgsConstructor
public class VideoItemWriter implements ItemWriter<Video> {

	private final VideoService videoService;

	@Override
	public void write(List<? extends Video> videos) throws Exception {
		videoService.saveBatch(Lists.newArrayList(videos));
	}
}
