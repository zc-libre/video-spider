package com.libre.video;

import com.google.common.collect.Lists;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.request.Video9SRequestStrategy;
import com.libre.video.mapper.es.VideoEsMapper;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class VideoApplicationTests {
    @Autowired
    VideoDownload download;
    @Autowired
	Video9SRequestStrategy request;
  //  @Autowired
//	VideoEsRepository videoEsRepository;

    @Autowired
	VideoService videoService;

	@Autowired
	VideoEsMapper videoEsMapper;

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void contextLoads() throws IOException {
        //
        download.encodeAndWrite("https://cdn2.jiuse.cloud/hls/627157/index.m3u8?t=1650123558&m=jgg3XW16UHq0mw0Tjcib0g", "123");
    }

    @Test
    void createIndex() {
		IndexCoordinates indexCoordinates = IndexCoordinates.of("video");

		elasticsearchRestTemplate.indexOps(indexCoordinates).getSettings().put("max_result_window", 100000);
	}


    @Test
    void request() {
		videoService.request(2);
    }

    @Test
    void update() {
		System.out.println(videoEsMapper.existsIndex("video"));
	}
}
