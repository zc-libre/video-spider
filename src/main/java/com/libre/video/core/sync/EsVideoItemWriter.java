package com.libre.video.core.sync;

import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.pojo.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.suggest.Completion;
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
	public void write(Chunk<? extends Video> chunk) throws Exception {
		log.debug("start to save videos to ES ....");
		for (Video video : chunk.getItems()) {
			if (video.getTitle() != null) {
				video.setTitleSuggest(new Completion(new String[] { video.getTitle() }));
			}
		}
		videoEsRepository.saveAll(chunk.getItems());
	}

}
