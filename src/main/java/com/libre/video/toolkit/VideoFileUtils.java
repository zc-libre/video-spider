package com.libre.video.toolkit;

import com.libre.boot.autoconfigure.SpringContext;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.Exceptions;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.config.VideoProperties;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.TextUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author: Libre
 * @Date: 2022/5/15 9:12 AM
 */
@Slf4j
@UtilityClass
public class VideoFileUtils {

	private final VideoProperties properties;

	static {
		properties = SpringContext.getBean(VideoProperties.class);
		Assert.notNull(properties, "VideoProperties must not be null");
	}

	public static String getVideoTempPath(String path) {
		return properties.getDownloadPath() + path;
	}

	public static String getVideoM3u8TempPath(Long videoId) {
		return properties.getDownloadPath() + videoId + "index.m3u8";
	}

	public static String getVideoName(String videoName) {
		return videoName + ".mp4";
	}

	public static boolean videoExist(String path) {
		Path videoPath = Paths.get(path);
		return Files.exists(videoPath);
	}

	public static void deleteTempVideo(String path) {
		Path videoPath = Paths.get(path);
		try {
			Files.delete(videoPath);
		}
		catch (IOException e) {
			throw new LibreException(String.format("文件删除失败, %s", e.getMessage()));
		}
	}

	public static String decode(String videoUrl) {
		String url;
		try {
			url = URLDecoder.decode(videoUrl, StandardCharsets.UTF_8.name());
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
		return url;
	}

	public static boolean mergeFiles(String[] filePaths, String resultPath) {
		if (ObjectUtils.isEmpty(filePaths) || StringUtil.isBlank(resultPath)) {
			return false;
		}
		if (filePaths.length == 1) {
			return new File(filePaths[0]).renameTo(new File(resultPath));
		}

		File[] files = new File[filePaths.length];
		for (int i = 0; i < filePaths.length; i++) {
			files[i] = new File(filePaths[i]);
			if (StringUtil.isBlank(filePaths[i]) || !files[i].exists() || !files[i].isFile()) {
				return false;
			}
		}

		File resultFile = new File(resultPath);

		try (FileOutputStream outputStream = new FileOutputStream(resultFile, true)) {
			FileChannel resultFileChannel = outputStream.getChannel();
			for (String filePath : filePaths) {
				try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
					FileChannel channel = fileInputStream.getChannel();
					resultFileChannel.transferFrom(channel, resultFileChannel.size(), channel.size());
					channel.close();
				}
				catch (IOException e) {
					throw new LibreException(e);
				}
			}
			resultFileChannel.close();
		}
		catch (IOException e) {
			log.error(e.getMessage());
			return false;
		}

		for (String filePath : filePaths) {
			try {
				Files.delete(Paths.get(filePath));
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return true;
	}

}
