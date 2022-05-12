package com.libre.video;

import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.request.strategy.Video9SRequestStrategy;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.io.IOException;

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

}
