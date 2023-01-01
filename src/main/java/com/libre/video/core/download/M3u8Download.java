package com.libre.video.core.download;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.Exceptions;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.config.VideoProperties;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.event.VideoUploadEvent;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.VideoFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: Libre
 * @Date: 2022/5/16 12:02 AM
 */
@Slf4j
@Component
public class M3u8Download {

	private final static String MU38_SUFFIX = ".m3u8";

	private final static String TS_SUFFIX = "ts";

	private final static String MP4_SUFFIX = ".mp4";

	private final WebClient webClient;

	private final VideoEncode videoEncode;

	private final VideoProperties properties;

	private VideoService videoService;

	public M3u8Download(WebClient webClient, VideoEncode videoEncode, VideoProperties properties) {
		this.webClient = webClient;
		this.videoEncode = videoEncode;
		this.properties = properties;
	}

	public InputStream downloadAsStream(Video video) {
		try {
			String url = video.getRealUrl();
			Mono<Resource> mono = webClient.get().uri(url).accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
					.bodyToMono(Resource.class);
			Resource resource = mono.block();
			Assert.notNull(resource, "resource must not be null");
			return resource.getInputStream();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void download(Video video) {
		String url = video.getRealUrl();
		Mono<Resource> mono = webClient.get().uri(url).accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
				.bodyToMono(Resource.class);
		Resource resource = mono.block();

		String fileName = video.getId() + MU38_SUFFIX;
		String m3u8File = video.getId() + StringPool.SLASH + fileName;

		video.setVideoPath(fileName);

		if (ObjectUtils.isEmpty(videoService)) {
			ApplicationContext context = SpringContext.getContext();
			videoService = context.getBean(VideoService.class);
		}
		videoService.saveVideoToLocal(new VideoUploadEvent(true, video, resource));
	}

	@Async
	public void downloadM3u8FileToLocal(InputStream inputStream, Video video, boolean isMerge) {
		String tempDir = createDirectory(video.getId());
		String m3u8FileName = tempDir + File.separator + "index.m3u8";
		Path m3u8FilePath = Paths.get(m3u8FileName);
		List<String> lines = copyM3u8File(inputStream, m3u8FilePath);
		if (CollectionUtil.isEmpty(lines)) {
			log.error("lines is empty");
			return;
		}

		List<String> tsLines = lines.stream().filter(line -> line.endsWith(TS_SUFFIX) || line.contains(".ts"))
				.collect(Collectors.toList());
		String realUrl = video.getRealUrl();
		String baseUrl = realUrl.substring(0, realUrl.lastIndexOf(StringPool.SLASH));
		List<String> tsFiles = Lists.newArrayList();
		if (RequestTypeEnum.REQUEST_BA_AV.getType() == video.getVideoWebsite()
				|| RequestTypeEnum.REQUEST_91.getType() == video.getVideoWebsite()) {
			downloadTsFilesAsync(baseUrl, tempDir, tsLines);

		}
		else if (RequestTypeEnum.REQUEST_9S.getType() == video.getVideoWebsite()) {
			 tsFiles = downloadTsFiles(baseUrl, tempDir, tsLines);
		}
		if (isMerge) {
			mergeTsFiles(video, tempDir, tsFiles);
		}

		try {
			Files.delete(m3u8FilePath);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> downloadTsFiles(String baseUrl, String tempDir, List<String> lines) {
		int i = 0;
		List<String> tsFiles = Lists.newArrayList();
		for (String ts : lines) {
			i++;
			try {
				Mono<Resource> mono = webClient.get().uri(baseUrl + StringPool.SLASH + ts)
						.accept(MediaType.APPLICATION_OCTET_STREAM).retrieve().bodyToMono(Resource.class).retry(5)
						.doOnError(e -> log.error("请求异常"));
				Resource resource = mono.block();
				String tsPath = tempDir + StringPool.SLASH + i + ".ts";
				log.info("正在下载： {}", tsPath);
				if (ObjectUtils.isEmpty(resource)) {
					continue;
				}
				copyTsFile(tsPath, resource);
				tsFiles.add(tsPath);
			}
			catch (Exception e) {
				log.error("文件下载失败，", e);
			}

		}
		return tsFiles;
	}

	public void downloadTsFilesAsync(String baseUrl, String tempDir, List<String> lines) {
		Flux.fromIterable(lines).sort().flatMap(ts -> {
			String url;
			String tsName;
			if (StringUtil.startsWithIgnoreCase(ts, "https")) {
				url = ts;
				tsName = ts;
			}
			else {
				String tsStr = ts.substring(ts.lastIndexOf(StringPool.SLASH) + 1);
				if (ts.length() > tsStr.length()) {
					url = baseUrl + tsStr;

				}
				else {
					url = baseUrl + StringPool.SLASH + ts;
				}
				tsName = tsStr;
			}
			return webClient.get().uri(url).accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
					.bodyToMono(Resource.class).retry(5).doOnError(e -> log.error("请求异常"))
					.publishOn(Schedulers.boundedElastic()).map(resource -> {
						String tsPath = tempDir + File.separator + tsName;
						log.info("正在下载： {}", tsPath);
						copyTsFile(tsPath, resource);
						return resource;
					});
		}).collectList().doOnError(e -> log.error("请求异常")).doOnNext(list -> log.info("所有请求完成")).block();
	}

	private List<String> copyM3u8File(InputStream inputStream, Path m3u8FilePath) {
		List<String> lines = Lists.newArrayList();
		if (Files.exists(m3u8FilePath)) {
			return lines;
		}
		try {
			Files.copy(inputStream, m3u8FilePath);
			lines.addAll(Files.readAllLines(m3u8FilePath));
		}
		catch (IOException e) {
			log.error("copy error: {}", Exceptions.getStackTraceAsString(e));
			throw new LibreException(e);
		}
		finally {
			Closeables.closeQuietly(inputStream);
		}
		log.info("video copy success, path: {}", m3u8FilePath);
		return lines;
	}

	private void copyTsFile(String tsPath, Resource resource) {
		Path path = Paths.get(tsPath);
		if (Files.exists(path)) {
			return;
		}
		try (InputStream in = resource.getInputStream()) {
			Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private String createDirectory(Long videoId) {
		String downloadPath = properties.getDownloadPath();
		String tempDir = downloadPath + videoId;
		Path dirPath = Paths.get(tempDir);
		if (!Files.exists(dirPath)) {
			try {
				Files.createDirectory(dirPath);
			}
			catch (IOException e) {
				log.error("createDirectory error: {}", Exceptions.getStackTraceAsString(e));
				throw new LibreException(e);
			}
		}
		return tempDir;
	}

	private void mergeTsFiles(Video video, String downloadPath, List<String> tsSet) {
		String[] paths = tsSet.stream().sorted().toArray(String[]::new);
		String videoPath = downloadPath + File.separator + video.getId() + MP4_SUFFIX;
		VideoFileUtils.mergeFiles(paths, videoPath);
		log.info("合并完成");
		video.setVideoPath(videoPath);
		video.setTitle(String.valueOf(video.getId()));
		videoEncode.encodeAndWrite(video);
		FileUtils.deleteQuietly(new File(videoPath));
	}

}
