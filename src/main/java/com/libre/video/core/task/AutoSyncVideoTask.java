package com.libre.video.core.task;

import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;

/**
 * @author: Libre
 * @Date: 2023/4/2 2:20 AM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoSyncVideoTask {

	private final VideoService videoService;

	//@Scheduled(cron = "* */5 * * * ?")
	public void execute() {
		log.info("sync task is start ......");
		videoService.syncToElasticsearch();
	}

}
