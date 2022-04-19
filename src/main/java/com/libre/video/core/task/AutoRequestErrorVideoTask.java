package com.libre.video.core.task;

import com.libre.video.pojo.ErrorVideo;
import com.libre.video.service.ErrorVideoService;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AutoRequestErrorVideoTask {

	private final ErrorVideoService errorVideoService;
	private final VideoService videoService;

	public void executeErrorVideo() {
		List<ErrorVideo> list = errorVideoService.list();

	}
}
