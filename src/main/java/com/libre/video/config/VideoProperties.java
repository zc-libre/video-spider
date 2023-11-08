package com.libre.video.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Files;
import java.nio.file.Paths;

@Data
@ConfigurationProperties(prefix = "video")
public class VideoProperties implements InitializingBean {

	private String ffmpegPath;

	private String mp4boxPath;

	private String downloadPath;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!Files.exists(Paths.get(downloadPath))) {
			Files.createDirectory(Paths.get(downloadPath));
		}
	}

}
