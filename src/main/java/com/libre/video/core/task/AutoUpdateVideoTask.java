package com.libre.video.core.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video;
import com.libre.video.service.ErrorVideoService;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoUpdateVideoTask {


	private final M3u8Download m3u8Download;
	private final VideoService videoService;

	@Scheduled(cron = "0 0 * * * ?")
	public void updateVideo() {
		log.info("updateVideoTask is start....");
		List<Video> list = videoService.list(Wrappers.<Video>lambdaQuery().isNull(Video::getVideoPath));
		for (Video video : list) {
			try {
				m3u8Download.download(video);
			} catch (Exception e) {
				log.error("update error: {}", e.getMessage());
			}
		}
	}
}
