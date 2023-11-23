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

	private String downloadPath;

	private String imagePath = "image";

	public String getImagePath()  {
		String image = downloadPath + imagePath;
		try {
			Path imageDir = Paths.get(image);
			if (StringUtil.isNotBlank(image) && !Files.exists(imageDir)) {
				Files.createDirectory(imageDir);
			}
		}
		catch (IOException e) {
		    log.error("create image dir error", e);
		}
		return image;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!Files.exists(Paths.get(downloadPath))) {
			Files.createDirectory(Paths.get(downloadPath));
		}
	}

}
