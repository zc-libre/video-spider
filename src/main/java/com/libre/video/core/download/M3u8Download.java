package com.libre.video.core.download;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringPool;
import com.libre.video.config.VideoProperties;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.core.event.VideoUploadEvent;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.VideoFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: Libre
 * @Date: 2022/5/16 12:02 AM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class M3u8Download {

	private final static String MU38_SUFFIX = ".m3u8";

	private final static String TS_SUFFIX = "ts";

	private final WebClient webClient;

	private final VideoEncode videoEncode;

	private final VideoProperties properties;

	public void download(Video video) {
		String url = video.getRealUrl();

		Mono<Resource> mono = webClient.get().uri(url).accept(MediaType.APPLICATION_OCTET_STREAM).retrieve()
				.bodyToMono(Resource.class);
		Resource resource = mono.block();

		String fileName = video.getId() + MU38_SUFFIX;
		String m3u8File = video.getId() + StringPool.SLASH + fileName;

		video.setVideoPath(m3u8File);
		VideoEventPublisher.publishVideoUploadEvent(new VideoUploadEvent(true, video, resource));
	}

	public void downloadM3u8FileToLocal(InputStream inputStream, Video video) {
		String tempDir = createDirectory(video.getId());
		String m3u8FileName = tempDir + File.separator + "index.m3u8";
		Path m3u8FilePath = Paths.get(m3u8FileName);
		if (Files.exists(m3u8FilePath)) {
			return;
		}
		List<String> lines = copyM3u8File(inputStream, m3u8FilePath);
		ThreadPoolTaskExecutor executor = ThreadPoolUtil.downloadExecutor();
	     executor.execute(() -> {
			 List<String> tsLines = lines.stream().filter(line -> line.endsWith(TS_SUFFIX)).collect(Collectors.toList());
			 String realUrl = video.getRealUrl();
			 String baseUrl = realUrl.substring(0, realUrl.lastIndexOf(StringPool.SLASH));
			 if (RequestTypeEnum.REQUEST_BA_AV.getType() == video.getVideoWebsite()) {
				 downloadTsFilesAsync(baseUrl, tempDir, tsLines);
			 }
			 else if (RequestTypeEnum.REQUEST_9S.getType() == video.getVideoWebsite()) {
				 downloadTsFiles(baseUrl, tempDir, tsLines);
			 }
		 });
	}

	public void downloadTsFiles(String baseUrl, String tempDir, List<String> lines) {
		for (String ts : lines) {
			Mono<Resource> mono = webClient.get().uri(baseUrl + StringPool.SLASH + ts)
					.accept(MediaType.APPLICATION_OCTET_STREAM).retrieve().bodyToMono(Resource.class).retry(5)
					.doOnError(e -> log.error("请求异常"));
			Resource resource = mono.block();
			String tsPath = tempDir + StringPool.SLASH + ts;
			log.info("正在下载： {}", tsPath);
			if (ObjectUtils.isEmpty(resource)) {
				continue;
			}
			copyTsFile(tsPath, resource);
		}
	}

	public void downloadTsFilesAsync(String baseUrl, String tempDir, List<String> lines) {
		Flux.fromIterable(lines).flatMap(ts -> {
			Mono<Resource> mono = webClient.get().uri(baseUrl + StringPool.SLASH + ts).accept(MediaType.APPLICATION_OCTET_STREAM)
				.retrieve().bodyToMono(Resource.class).retry(5).doOnError(e -> log.error("请求异常"))
				.publishOn(Schedulers.boundedElastic()).map(resource -> {
					String tsPath = tempDir + File.separator + ts;
					log.info("正在下载： {}", tsPath);
					copyTsFile(tsPath, resource);
					return resource;
				});
			return mono;
		}).collectList().doOnError(e -> log.error("请求异常")).doOnNext(list -> log.info("所有请求完成")).block();

	}

	private List<String> copyM3u8File(InputStream inputStream, Path m3u8FilePath) {
		List<String> lines = Lists.newArrayList();
		try {
			Files.copy(inputStream, m3u8FilePath);
			lines.addAll(Files.readAllLines(m3u8FilePath));
		}
		catch (IOException e) {
			throw new LibreException(e);
		}
		finally {
			Closeables.closeQuietly(inputStream);
		}
		return lines;
	}

	private void copyTsFile(String tsPath, Resource resource) {
		try (InputStream in = resource.getInputStream()) {
			Path path = Paths.get(tsPath);
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
				throw new LibreException(e);
			}
		}
		return tempDir;
	}

	private void mergeTsFiles(Video video, String downloadPath, Set<String> tsSet) {
		List<String> fileList = tsSet.stream().sorted().collect(Collectors.toList());
		String[] paths = new String[fileList.size()];
		String videoPath = downloadPath + video.getId() + MU38_SUFFIX;
		VideoFileUtils.mergeFiles(paths, videoPath);
		log.info("合并完成");
		video.setVideoPath(videoPath);
		videoEncode.encodeAndWrite(video);
	}

}
