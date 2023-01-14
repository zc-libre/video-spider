package com.libre.video.core.batch;

import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.pojo.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Libre
 * @Date: 2023/1/13 12:37 AM
 */
@Slf4j
@RequiredArgsConstructor
public class EsVideoItemWriter implements ItemWriter<Video> {

	private final VideoEsRepository videoEsRepository;

	@Override
	public void write(List<? extends Video> videos) throws Exception {
		log.info("start to save videos to ES ....");
		videoEsRepository.saveAll(videos);
	}
}
