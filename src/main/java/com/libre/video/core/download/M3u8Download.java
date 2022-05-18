package com.libre.video.core.download;

import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringPool;
import com.libre.video.config.VideoProperties;
import com.libre.video.core.event.VideoDownloadEvent;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.VideoFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
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
	private final VideoEncode videoEncode;

	@Async("downloadExecutor")
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
		String fileName = url.substring(url.lastIndexOf(StringPool.SLASH) + 1);
		String m3u8File = tempDir + File.separator + fileName;
		Path outputPath = Paths.get(m3u8File);
		try (InputStream inputStream = resource.getInputStream()) {
			Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
			lines = Files.readAllLines(outputPath);
		} catch (IOException e) {
			throw new LibreException(e);
		}

		downloadTsFiles(url, tempDir, lines);

		video.setVideoPath(m3u8File);
		VideoEventPublisher.publishVideoDownloadEvent(new VideoDownloadEvent(true, video));

		//extracted(video, downloadPath, tsSet);
	}

	private void downloadTsFiles(String url, String tempDir, List<String> lines) {
		String baseUrl = url.substring(0, url.lastIndexOf(StringPool.SLASH));
		Set<String> tsSet = lines.stream().filter(line -> line.contains("ts")).collect(Collectors.toSet());
		String[] urls = tsSet.toArray(new String[0]);
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
					String tsPath = tempDir + File.separator + ts;
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

	private void extracted(Video video, String downloadPath, Set<String> tsSet) {
		List<String> fileList = tsSet.stream().sorted().collect(Collectors.toList());
		String[] paths = new String[fileList.size()];
		String videoPath = downloadPath + video.getId() + MU38_SUFFIX;
		VideoFileUtils.mergeFiles(paths, videoPath);
		log.info("合并完成");
		video.setVideoPath(videoPath);
		videoEncode.encodeAndWrite(video);
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
