package com.libre.video.controller;

import com.google.common.collect.Lists;
import com.libre.core.result.R;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	@GetMapping("/list/{title}/{page}/{size}")
	public R<Page<Video>> page(@PathVariable String title,
								   @PathVariable Integer page,
								   @PathVariable Integer size) {
		Page<Video> videoPage = videoService.findByTitlePage(title, page, size);
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
}
