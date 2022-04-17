package com.libre.video.core.task;

import com.libre.video.service.ErrorVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoRequestErrorVideoTask {

	private final ErrorVideoService errorVideoService;

	public void executeErrorVideo() {

	}
}
