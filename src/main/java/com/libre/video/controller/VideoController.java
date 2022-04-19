package com.libre.video.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.libre.core.result.R;
import com.libre.video.core.dto.VideoRequestParam;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	@GetMapping("/list")
	public R<Page<Video>> page(PageDTO<Video> page, @RequestParam String title) {
		Page<Video> videoPage = videoService.findByTitlePage(title, page);
		return R.data(videoPage);
	}

	@GetMapping("/download/{id}")
	public R<Boolean> download(@PathVariable Long id) {
		videoService.download(Lists.newArrayList(id));
		return R.data(Boolean.TRUE);
	}

	@GetMapping("/sync")
	public R<Boolean> sync() {
		videoService.dataSyncToElasticsearch();;
		return R.success("数据同步成功");
	}

	@GetMapping("/request")
	public R<Boolean> request() {
		videoService.request(VideoRequestParam.builder().requestType(2).build());
		return R.status(Boolean.TRUE);
	}
}
