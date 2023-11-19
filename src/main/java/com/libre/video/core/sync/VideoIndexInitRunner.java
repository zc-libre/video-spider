package com.libre.video.core.sync;

import com.google.common.collect.Maps;
import com.libre.video.pojo.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author: Libre
 * @Date: 2023/9/16 7:59 PM
 */
@Component
@RequiredArgsConstructor
public class VideoIndexInitRunner implements ApplicationRunner {

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public void run(ApplicationArguments args) throws Exception {
//		IndexOperations indexOperations = elasticsearchOperations.indexOps(Video.class);
//		indexOperations.delete();
//		HashMap<String, Object> map = Maps.newHashMap();
//		map.put("max_result_window", 1000000);
//		indexOperations.create(map);
	}

}
