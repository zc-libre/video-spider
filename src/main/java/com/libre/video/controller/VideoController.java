package com.libre.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.libre.core.result.R;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.config.VideoProperties;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.download.VideoEncode;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.Video;
import com.libre.video.pojo.VideoParam;
import com.libre.video.pojo.dto.VideoQuery;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	private final VideoEncode videoEncode;

	private final M3u8Download m3u8Download;

	private final VideoProperties videoProperties;

	@PostMapping("/list")
	public R<Page<Video>> page(PageDTO<Video> page, VideoQuery videoQuery) {
		Page<Video> videoPage = videoService.findByPage(page, videoQuery);
		return R.data(videoPage);
	}

	@GetMapping("/watch/{videoId}")
	public R<Boolean> watch(@PathVariable Long videoId) {
		videoService.watch(videoId);
		return R.status(true);
	}

	@GetMapping("/download/{id}")
	public R<Boolean> download(@PathVariable Long id) {
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
		videoEncode.encodeAndWrite(video);
		return R.data(Boolean.TRUE);
	}

	@Transactional
	@GetMapping("/sync")
	public R<Boolean> sync() {
		videoService.syncToElasticsearch();
		return R.success("数据同步成功");
	}

	@GetMapping("/request")
	public R<Boolean> request(@RequestParam Integer requestType, Integer size) {
		videoService.request(VideoRequestParam.builder().requestType(requestType).size(size).build());
		return R.status(Boolean.TRUE);
	}

	@GetMapping("/shutdown")
	public R<Boolean> shutdown() {
		videoService.shutdown();
		return R.status(true);
	}

	@GetMapping("read")
	public R<Boolean> read() throws Exception {
		String downloadPath = videoProperties.getDownloadPath();
		Collection<File> files = FileUtils.listFiles(new File(downloadPath), new String[] { "m3u8" }, false);
		Map<Long, File> map = Maps.newTreeMap();
		for (File file : files) {
			try {
				String name = file.getName();
				String id = name.substring(0, name.indexOf("."));
				map.put(Long.parseLong(id), file);
			}
			catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		Set<Long> ids = map.keySet();

		ThreadPoolTaskExecutor executor = ThreadPoolUtil.videoRequestExecutor();
		LambdaQueryWrapper<Video> queryWrapper = Wrappers.<Video>lambdaQuery().eq(Video::getVideoWebsite,
				RequestTypeEnum.REQUEST_91.getType());
		queryWrapper.in(Video::getId, ids);
		List<Video> videos = videoService.list(queryWrapper);

		for (Video video : videos) {
			if (StringUtil.isBlank(video.getM3u8Content()) && map.containsKey(video.getId())) {
				executor.execute(() -> {
					try {
						m3u8Download.downloadAndReadM3u8File(video);
					}
					catch (Exception e) {
						log.error(e.getMessage());
					}
				});
			}
		}
		List<List<Video>> allList = Lists.newArrayList();
		List<Video> list = Lists.newArrayList();
		for (Video video : videos) {
			list.add(video);
			if (list.size() == 500) {
				allList.add(Lists.newArrayList(list));
				list.clear();
			}
		}

		for (List<Video> videoList : allList) {
			executor.execute(() -> videoService.updateBatchById(videoList));
		}
		return R.status(true);
	}

}
