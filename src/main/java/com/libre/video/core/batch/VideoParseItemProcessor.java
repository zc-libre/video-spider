package com.libre.video.core.batch;

import com.libre.video.pojo.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

/**
 * @author: Libre
 * @Date: 2023/1/13 12:35 AM
 */
@Slf4j
public class VideoParseItemProcessor implements ItemProcessor<Video, Video> {

	@Override
	public Video process(Video video) throws Exception {
		log.info("读取到一条数据： {}, {}", video.getId(), video.getTitle());
		return video;
	}
}
