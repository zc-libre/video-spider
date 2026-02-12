package com.libre.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@Configuration(proxyBeanMethods = false)
public class VideoThreadPoolConfiguration {

	@Bean
	public VirtualThreadTaskExecutor videoRequestExecutor() {
		return new VirtualThreadTaskExecutor("video-request-task-");
	}

	@Bean
	public VirtualThreadTaskExecutor downloadExecutor() {
		return new VirtualThreadTaskExecutor("download-task-");
	}

}
