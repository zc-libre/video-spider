package com.libre.video;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.request.Video9SRequestStrategy;
import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.pojo.Video;
import com.libre.video.pojo.dto.Video9s;
import com.libre.video.service.VideoService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsAction;
import org.elasticsearch.rest.action.admin.indices.RestUpdateSettingsAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class VideoApplicationTests {
    @Autowired
    VideoDownload download;
    @Autowired
	Video9SRequestStrategy request;
    @Autowired
	VideoEsRepository videoEsRepository;

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

    @Test
    void findAll() {
        Iterable<Video> all = videoEsRepository.findAll();
        videoService.saveBatch(Lists.newArrayList(all));
    }
    @Test
    void search() {
		List<Video> all = videoEsRepository.findAllByTitleLike("黑丝袜");
		System.out.println(all.size());
		all.forEach(System.out::println);
	}

    @Test
    void request() {
		videoService.request(2);
    }

    @Test
    void update() {
		List<Video> list = videoService.list();
		elasticsearchRestTemplate.save(list);
	}
}
