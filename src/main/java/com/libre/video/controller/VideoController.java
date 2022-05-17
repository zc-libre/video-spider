package com.libre.video.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.core.exception.LibreException;
import com.libre.core.result.R;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.config.VideoProperties;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.Video;
import com.libre.video.pojo.dto.VideoQuery;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.RegexUtil;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.UserAgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;
	private final VideoDownload videoDownload;
	private final WebClient webClient;

	@PostMapping("/list")
	public R<Page<Video>> page(PageDTO<Video> page, VideoQuery videoQuery) {
		Page<Video> videoPage = videoService.findByPage(page, videoQuery);
		return R.data(videoPage);
	}

	private final M3u8Download m3u8Download;
	@GetMapping("/test")
	public void test() throws IOException {
		Video video = videoService.getById(1525773772160831491L);
		m3u8Download.download(video);
	}



	@GetMapping("/download/{id}")
	public R<Boolean> download(@PathVariable Long id) {
		videoService.download(Lists.newArrayList(id));

		return R.data(Boolean.TRUE);
	}

	@PostMapping("/download")
	public R<Boolean> downloadByUrl(String videoUrl) {
		Video video = new Video();
		video.setUrl(videoUrl);
		video.setTitle(IdWorker.get32UUID());
		videoDownload.encodeAndWrite(video);
		return R.data(Boolean.TRUE);
	}

	@PostMapping
	public R<String> watch(String url) throws IOException {
		String html = webClient.get()
			.uri(url)
			.retrieve()
			.bodyToMono(String.class)
			.block();
		String videoUrl = RegexUtil.matchM3u8Url(html);
		if (StringUtil.isBlank(videoUrl)) {
			throw new LibreException("url 获取失败");
		}
		Video video = new Video();
		video.setUrl(videoUrl);
		video.setTitle(IdWorker.get32UUID());
		videoDownload.encodeAndWrite(video);
		return R.data(videoUrl);
	}

	@Transactional
	@GetMapping("/sync")
	public R<Boolean> sync() {
		videoService.syncToElasticsearch();
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

}
