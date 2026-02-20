package com.libre.video.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.libre.core.result.R;
import com.libre.video.config.VideoProperties;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.download.VideoEncoder;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.Video;
import com.libre.video.pojo.VideoParam;
import com.libre.video.pojo.dto.VideoQuery;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	private final VideoEncoder videoEncoder;

	private final M3u8Download m3u8Download;

	private final WebClient webClient;

	@GetMapping("/spider/{type}")
	public R<Boolean> spider(@PathVariable("type") Integer type,
			@RequestParam(value = "maxPages", required = false) Integer maxPages) {
		videoService.spider(type, maxPages);
		return R.status(true);
	}

	@PostMapping("/list")
	public R<Page<Video>> page(PageDTO<Video> page, VideoQuery videoQuery) {
		Page<Video> videoPage = videoService.findByPage(page, videoQuery);
		return R.data(videoPage);
	}

	@GetMapping("/suggest")
	public R<List<String>> suggest(@RequestParam("q") String q,
			@RequestParam(value = "size", defaultValue = "10") int size) {
		return R.data(videoService.suggest(q, size));
	}

	@GetMapping("/watch/{videoId}")
	public R<String> watch(@PathVariable("videoId") Long videoId) throws IOException {
		String url = videoService.watch(videoId);
		return R.data(url);
	}

	@GetMapping("/download/{id}")
	public R<Boolean> download(@PathVariable("id") Long id) {
		videoService.download(Lists.newArrayList(id));
		return R.data(Boolean.TRUE);
	}

	@PostMapping("encode")
	public R encode(@RequestBody VideoParam param) {
		Video video = new Video();
		video.setId(IdWorker.getId());
		video.setRealUrl(param.getRealUrl());
		video.setVideoWebsite(RequestTypeEnum.REQUEST_9S.getType());
		InputStream inputStream = m3u8Download.downloadAsStream(video);
		m3u8Download.downloadVideoToLocal(inputStream, video);
		return R.status(true);
	}

	@PostMapping("/download")
	public R<Boolean> downloadByUrl(String videoUrl) {
		Video video = new Video();
		video.setUrl(videoUrl);
		video.setTitle(IdWorker.get32UUID());
		videoEncoder.encodeAndWrite(video);
		return R.data(Boolean.TRUE);
	}

	/**
	 * 代理 m3u8 分片（.ts / crypt.key）请求，解决第三方 CDN 不返回 CORS 头的问题。
	 * url 参数为 Base64-URL 编码的原始 CDN 地址。
	 */
	@GetMapping("/ts-proxy")
	public ResponseEntity<byte[]> tsProxy(@RequestParam("url") String encodedUrl) {
		String url = new String(Base64.getUrlDecoder().decode(encodedUrl), StandardCharsets.UTF_8);
		byte[] data = webClient.get()
			.uri(url)
			.retrieve()
			.bodyToMono(byte[].class)
			.block();
		if (data == null) {
			return ResponseEntity.notFound().build();
		}
		HttpHeaders headers = new HttpHeaders();
		if (url.contains(".ts")) {
			headers.setContentType(MediaType.valueOf("video/mp2t"));
		}
		else {
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		}
		return ResponseEntity.ok().headers(headers).body(data);
	}

	@GetMapping("/sync")
	public R<Boolean> sync() {
		videoService.syncToElasticsearch();
		return R.success("数据同步成功");
	}

	@GetMapping("/shutdown")
	public R<Boolean> shutdown() {
		videoService.shutdown();
		return R.status(true);
	}

}
