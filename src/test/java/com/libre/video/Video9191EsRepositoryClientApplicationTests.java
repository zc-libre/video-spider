package com.libre.video;

import com.google.common.collect.Lists;
import com.libre.video.core.VideoDownload;
import com.libre.video.core.request.Video9sRequest;
import com.libre.video.mapper.Video91EsRepository;
import com.libre.video.mapper.Video9sEsRepository;
import com.libre.video.pojo.Video91;
import com.libre.video.pojo.dto.Video9s;
import com.libre.video.service.Video9sService;
import com.libre.video.service.VideoService;
import com.libre.video.service.mapstruct.Video9sMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class Video9191EsRepositoryClientApplicationTests {
    @Autowired
    VideoDownload download;
    @Autowired
    Video9sRequest request;
    @Autowired
    Video91EsRepository video91EsRepository;

    @Autowired
    Video9sEsRepository video9sEsRepository;
    @Autowired
    VideoService videoService;
    @Autowired
    Video9sService video9sService;
    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void contextLoads() throws IOException {
        //
        download.encodeAndWrite("https://cdn2.jiuse.cloud/hls/627157/index.m3u8?t=1650123558&m=jgg3XW16UHq0mw0Tjcib0g", "123");
    }

    @Test
    void createIndex() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Video9s.class);
        indexOperations.delete();
    }

    @Test
    void findAll() {
        Iterable<Video91> all = video91EsRepository.findAll();
        videoService.saveBatch(Lists.newArrayList(all));
    }
    @Test
    void search() {
        List<Video9s> list = video9sService.list();
        elasticsearchRestTemplate.save(list);
    }

    @Test
    void request() {
        request.execute();
    }

    @Test
    void update() {
        List<Video91> list = videoService.list();
        Video9sMapping mapping = Video9sMapping.INSTANCE;
        List<Video9s> video9s = mapping.convertToVideo9sList(list);
        video9sService.saveOrUpdateBatch(video9s);
    }
}
