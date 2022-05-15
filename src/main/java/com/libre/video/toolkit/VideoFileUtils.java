package com.libre.video.toolkit;

import com.libre.boot.autoconfigure.SpringContext;
import com.libre.video.config.VideoProperties;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author: Libre
 * @Date: 2022/5/15 9:12 AM
 */
@UtilityClass
public class VideoFileUtils {

	private final VideoProperties properties;

	static {
		properties = SpringContext.getBean(VideoProperties.class);
		Assert.notNull(properties, "VideoProperties must not be null");
	}

	public static String getVideoPath(String videoName) {
		return properties.getDownloadPath() + videoName + ".mp4";
	}

	public static boolean videoExist(String path) {
		Path videoPath = Paths.get(path);
		return Files.exists(videoPath);
	}
}
