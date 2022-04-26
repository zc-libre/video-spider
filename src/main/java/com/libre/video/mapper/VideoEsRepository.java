package com.libre.video.mapper;

import com.libre.elasticsearch.core.EsRepository;
import com.libre.video.pojo.Video;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


/**
 * @author: Libre
 * @Date: 2022/4/19 9:30 PM
 */
public interface VideoEsRepository extends EsRepository<Video, Long> {

	Page<Video> findVideosByTitleLike(String title, Pageable pageable);

}
