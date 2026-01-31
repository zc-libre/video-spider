package com.libre.video.config;

import com.libre.core.toolkit.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Data
@ConfigurationProperties(prefix = "video")
public class VideoProperties implements InitializingBean {

	private String ffmpegPath;

	private String mp4boxPath;

	private String downloadPath = "~/app/video/";

	private String imagePath;

	private String pron9sDomain = "";

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!Files.exists(Paths.get(downloadPath))) {
			Files.createDirectories(Paths.get(downloadPath));
		}

		// 在配置注入后设置 imagePath
		if (StringUtil.isBlank(imagePath)) {
			imagePath = downloadPath + "image";
		}

		if (!Files.exists(Paths.get(imagePath))) {
			Files.createDirectories(Paths.get(imagePath));
		}
	}

}
