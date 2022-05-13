package com.libre.video.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.libre.core.exception.LibreException;
import com.libre.core.result.R;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.mapstruct.VideoBaAvMapping;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.mapper.UserMapper;
import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.User;
import com.libre.video.pojo.Video;
import com.libre.video.pojo.dto.VideoQuery;
import com.libre.video.service.BaAvVideoService;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.RegexUtil;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.UserAgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import org.apache.ibatis.cursor.Cursor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;
	private final VideoDownload videoDownload;
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

	@PostMapping("/download")
	public R<Boolean> downloadByUrl(String videoUrl) {
		videoDownload.encodeAndWrite(videoUrl, IdWorker.get32UUID());
		return R.data(Boolean.TRUE);
	}

	@PostMapping
	public R<String> watch(String url) {
		String html = HttpRequest.get(url)
			.setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.setHeader(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent())
			.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5")
			.execute().asString();
		String videoUrl = RegexUtil.matchM3u8Url(html);
		if (StringUtil.isBlank(videoUrl)) {
			throw new LibreException("url 获取失败");
		}
		videoDownload.encodeAndWrite(videoUrl, System.currentTimeMillis() + "");
		return R.data(videoUrl);
	}

	@Transactional
	@GetMapping("/sync")
	public R<Boolean> sync() {
		videoService.dataAsyncToElasticsearch();
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
