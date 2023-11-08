package com.libre.video.mapper;

import com.libre.video.pojo.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author: Libre
 * @Date: 2022/4/19 9:30 PM
 */
public interface VideoEsRepository extends ElasticsearchRepository<Video, Long> {

	Page<Video> findVideosByTitleLike(String title, Pageable pageable);

	Video findByRealUrl(String url);

}
