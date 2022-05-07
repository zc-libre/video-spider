package com.libre.video.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.libre.core.result.R;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.dto.VideoRequestParam;
import com.libre.video.pojo.Video;
import com.libre.video.pojo.dto.VideoQuery;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	private final ElasticsearchRestTemplate elasticsearchRestTemplate;

	@PostMapping("/list")
	public R<Page<Video>> page(PageDTO<Video> page, VideoQuery videoQuery) {
		Page<Video> videoPage = videoService.findByPage(page, videoQuery);
		return R.data(videoPage);
	}


	@GetMapping("/download/{id}")
	public R<Boolean> download(@PathVariable Long id) {
		videoService.download(Lists.newArrayList(id));
		return R.data(Boolean.TRUE);
	}

	@PostMapping
	public R<Boolean> watch(String url) {
		videoService.requestAndDownload(url);
		return R.data(Boolean.TRUE);
	}

	@GetMapping("/sync")
	public R<Boolean> sync() {
		videoService.dataSyncToElasticsearch();;
		return R.success("数据同步成功");
	}

	@GetMapping("/request/{requestType}")
	public R<Boolean> request(@PathVariable Integer requestType, Integer size) {
		videoService.request(VideoRequestParam
			.builder()
			.requestType(requestType)
			.size(size)
			.build());
		return R.status(Boolean.TRUE);
	}

	@GetMapping("/query")
	public void query() {
		CriteriaQuery query = new CriteriaQuery(new Criteria("title").matches("黑丝"));
		query.setPageable(PageRequest.of(1, 100, Sort.Direction.DESC, "lookNum"));

		String preference = query.getPreference();
		System.out.println(preference);
		SearchHits<Video> search = elasticsearchRestTemplate.search(query, Video.class);


		List<SearchHit<Video>> searchHits = search.getSearchHits();
		List<Video> videoList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
	}
}
