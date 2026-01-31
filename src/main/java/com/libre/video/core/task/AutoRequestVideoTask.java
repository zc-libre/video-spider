package com.libre.video.core.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Libre
 * @Date: 2022/4/20 12:53 AM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoRequestVideoTask {

	private final VideoService videoService;

	@Scheduled(cron = "0 0 3 * * ?")
	public void execute() {
		log.info("autoRequestVideo9sTask is start....");
		videoService.spider(2);
	}

}
