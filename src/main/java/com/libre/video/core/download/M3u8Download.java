package com.libre.video.core.download;

import com.google.common.collect.Maps;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringPool;
import com.libre.video.config.VideoProperties;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.VideoFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
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
	private final WebClient webClient;
	private final VideoProperties videoProperties;
	private final VideoDownload videoDownload;


	public void download(Video video) {
		String url = video.getRealUrl();
		String downloadPath = videoProperties.getDownloadPath();

		String tempDir = downloadPath + video.getId();
		Path tempDirPath = Paths.get(tempDir);
		if (!Files.exists(tempDirPath)) {
			try {
				Files.createDirectory(tempDirPath);
			} catch (IOException e) {
				throw new LibreException(e);
			}
		}


		Mono<Resource> mono = webClient.get()
			.uri(url)
			.accept(MediaType.APPLICATION_OCTET_STREAM)
			.retrieve()
			.bodyToMono(Resource.class);
		Resource resource = mono.block();
		Optional.ofNullable(resource).orElseThrow(() -> new LibreException("resource is empty"));

		List<String> lines;
		try (InputStream inputStream = resource.getInputStream()) {
			String fileName = url.substring(url.lastIndexOf(StringPool.SLASH) + 1);
			String filePath = tempDir + File.separator + fileName;
			Path outputPath = Paths.get(filePath);
			Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
			lines = Files.readAllLines(outputPath);
		} catch (IOException e) {
			throw new LibreException(e);
		}

		String baseUrl = url.substring(0, url.lastIndexOf(StringPool.SLASH));
		Set<String> tsSet = lines.stream().filter(line -> line.contains("ts")).collect(Collectors.toSet());

		String[] urls = tsSet.toArray(new String[0]);
		downloadTsFiles(tempDir, baseUrl, urls);

		List<String> fileList = tsSet.stream().sorted().collect(Collectors.toList());
		String[] paths = new String[fileList.size()];
		for (int i = 0; i < fileList.size(); i++) {
			paths[i] = tempDir + File.separator + fileList.get(i);
			log.info(paths[i]);
		}
		String videoPath = downloadPath + video.getId() + MU38_SUFFIX;
		VideoFileUtils.mergeFiles(paths, videoPath);
		log.info("合并完成");
		video.setVideoPath(videoPath);
		videoDownload.encodeAndWrite(video);
	}

	private void downloadTsFiles(String fullDir, String baseUrl, String[] urls) {
		Flux.just(urls)
			.flatMap(ts -> webClient.get()
				.uri(baseUrl + File.separator + ts)
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.retrieve()
				.bodyToFlux(Resource.class)
				.retry(5)
				.doOnError(e -> log.error("请求异常"))
				.publishOn(Schedulers.boundedElastic())
				.map(res -> {
					String tsPath = fullDir + File.separator + ts;
					log.info("正在下载： {}", tsPath);
					try (InputStream in = res.getInputStream()) {
						Path path = Paths.get(tsPath);
						Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						log.error(e.getMessage());
					}
					return res;
				})
			)
			.collectList()
			.doOnNext(list -> log.info("Received all messages"))
			.block();

		log.info("所有请求完成, 开始合并ts");
	}
}

//	for (String ts : tsSet) {
//		Mono<Resource> resourceMono = webClient.get()
//	.uri(baseUrl + "/" + ts)
//	.accept(MediaType.APPLICATION_OCTET_STREAM)
//	.retrieve()
//	.bodyToMono(Resource.class)
//	.doOnError(e -> log.error("请求异常"))
//	.retry(5);
//
//	resourceMono.subscribe(res -> {
//	String tsPath = fullDir + "/" + ts;
//	log.info("正在下载： {}", tsPath);
//	try (InputStream in = res.getInputStream()) {
//	Path path = Paths.get(tsPath);
//	Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
//	} catch (IOException e) {
//	log.error(e.getMessage());
//	}
//	});
//	all = Mono.when(resourceMono);
//	}
