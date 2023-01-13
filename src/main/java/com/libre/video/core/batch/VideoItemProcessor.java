package com.libre.video.core.batch;

import com.libre.video.pojo.Video;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * @author: Libre
 * @Date: 2023/1/13 12:35 AM
 */
@Component
public class VideoItemProcessor implements ItemProcessor<Video, Video> {

	@Override
	public Video process(Video video) throws Exception {
		return video;
	}
}
