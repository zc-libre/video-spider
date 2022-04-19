package com.libre.video.core.task;

import com.libre.video.core.dto.RequestParam;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: Libre
 * @Date: 2022/4/20 12:53 AM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoRequestVideo9sTask {

	private final VideoService videoService;

	@Scheduled(cron = "0 0 2 * * ?")
	public void execute() {
		log.info("autoRequestVideo9sTask is start....");
		videoService.request(RequestParam.builder().requestType(2).size(20).build());
		log.info("autoRequestVideo9sTask completed");
	}
}
