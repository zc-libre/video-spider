package com.libre.video.core.download;

import com.google.common.collect.Lists;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.Exceptions;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.config.VideoProperties;
import com.libre.video.constant.SystemConstants;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.HttpClientUtils;
import com.libre.video.toolkit.VideoFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: Libre
 * @Date: 2022/5/16 12:02 AM
 */
@Slf4j
@Component
public class M3u8Download {

	private final WebClient webClient;

	private final VideoEncoder videoEncoder;

	private final VideoProperties properties;

	public M3u8Download(WebClient webClient, VideoEncoder videoEncoder, VideoProperties properties) {
		this.webClient = webClient;
		this.videoEncoder = videoEncoder;
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
			throw Exceptions.unchecked(e);
		}
	}

	public void downloadAndReadM3u8File(Video video) throws Exception {
		String url = video.getRealUrl();
		String content = downloadM3u8Content(url, video);
		if (StringUtil.isBlank(content)) {
			log.error("m3u8文件读取失败, 文件内容为空, url: {}", url);
			return;
		}
		// 如果是 master playlist，解析出实际的 media playlist URL 并重新下载
		if (content.contains("#EXT-X-STREAM-INF")) {
			String mediaUrl = resolveMediaPlaylistUrl(url, content);
			if (StringUtil.isBlank(mediaUrl)) {
				log.error("master playlist 解析 media playlist 路径失败, url: {}", url);
				return;
			}
			log.debug("检测到 master playlist, 解析 media playlist: {}", mediaUrl);
			video.setRealUrl(mediaUrl);
			content = downloadM3u8Content(mediaUrl, video);
			if (StringUtil.isBlank(content)) {
				log.error("media playlist 下载失败, url: {}", mediaUrl);
				return;
			}
		}
		// 将 m3u8 内容中的相对 .ts 路径改写为绝对 URL
		content = rewriteTsUrls(video.getRealUrl(), content);
		String fileName = video.getId() + SystemConstants.MU38_SUFFIX;
		video.setVideoPath(fileName);
		video.setM3u8Content(content);
		String videoTempDir = getVideoTempDir(video.getId());
		FileUtils.deleteDirectory(new File(videoTempDir));
	}

	private String downloadM3u8Content(String url, Video video) throws IOException {
		Mono<Resource> mono = webClient.get().uri(url)
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.retrieve()
				.bodyToMono(Resource.class);
		Resource resource = mono.block();
		if (Objects.isNull(resource)) {
			log.error("m3u8文件下载失败, url: {}", url);
			return null;
		}
		return readM3u8File(resource.getInputStream(), video);
	}

	/**
	 * 从 master playlist 内容中解析出 media playlist 的完整 URL。
	 * master playlist 格式示例：
	 * <pre>
	 * #EXTM3U
	 * #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2000000,RESOLUTION=1280x720
	 * /20260202/OmaJOVNH/2000kb/hls/index.m3u8
	 * </pre>
	 */
	private String resolveMediaPlaylistUrl(String masterUrl, String content) {
		String[] lines = content.split("\\R");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("#EXT-X-STREAM-INF") && i + 1 < lines.length) {
				String mediaPath = lines[i + 1].trim();
				if (StringUtil.isBlank(mediaPath) || mediaPath.startsWith("#")) {
					continue;
				}
				// 已经是完整 URL
				if (mediaPath.startsWith("http")) {
					return mediaPath;
				}
				// 相对路径，拼接 origin
				URI uri = URI.create(masterUrl);
				String origin = uri.getScheme() + "://" + uri.getAuthority();
				return origin + (mediaPath.startsWith("/") ? mediaPath : "/" + mediaPath);
			}
		}
		return null;
	}

	/**
	 * 将 m3u8 内容中的相对 .ts 路径改写为绝对 URL。
	 * <p>处理三种 .ts 路径格式：</p>
	 * <ul>
	 *   <li>已经是完整 URL（http 开头）→ 不处理</li>
	 *   <li>服务器绝对路径（/ 开头，如 /20251210/xxx.ts）→ 拼接 origin</li>
	 *   <li>相对文件名（如 00.ts）→ 拼接 baseUrl</li>
	 * </ul>
	 */
	private String rewriteTsUrls(String m3u8Url, String content) {
		URI uri = URI.create(m3u8Url);
		String origin = uri.getScheme() + "://" + uri.getAuthority();
		String baseUrl = m3u8Url.substring(0, m3u8Url.lastIndexOf('/'));

		String[] lines = content.split("\\R");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			String trimmed = line.trim();
			if (!trimmed.startsWith("#") && trimmed.contains(".ts")) {
				if (trimmed.startsWith("http")) {
					// 已经是完整 URL，不处理
				}
				else if (trimmed.startsWith("/")) {
					trimmed = origin + trimmed;
				}
				else {
					trimmed = baseUrl + "/" + trimmed;
				}
				sb.append(trimmed);
			}
			else if (trimmed.startsWith("#EXT-X-KEY") && trimmed.contains("URI=\"")) {
				sb.append(rewriteKeyUri(trimmed, origin, baseUrl));
			}
			else {
				sb.append(line);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * 将 #EXT-X-KEY 行中的相对 URI 改写为绝对 URL。
	 */
	private String rewriteKeyUri(String line, String origin, String baseUrl) {
		int uriStart = line.indexOf("URI=\"") + 5;
		int uriEnd = line.indexOf("\"", uriStart);
		String keyUri = line.substring(uriStart, uriEnd);
		if (keyUri.startsWith("http")) {
			return line;
		}
		String absoluteUri;
		if (keyUri.startsWith("/")) {
			absoluteUri = origin + keyUri;
		}
		else {
			absoluteUri = baseUrl + "/" + keyUri;
		}
		return line.substring(0, uriStart) + absoluteUri + line.substring(uriEnd);
	}

	public String readM3u8File(InputStream inputStream, Video video) {
		Path m3u8FilePath = buildM3u8FilePath(video);
		return copyM3u8FileToString(inputStream, m3u8FilePath);
	}

	private Path buildM3u8FilePath(Video video) {
		String tempDir = createDirectory(video.getId());
		String m3u8FileName = tempDir + File.separator + "index.m3u8";
		return Paths.get(m3u8FileName);
	}

	@Async
	public void downloadVideoToLocal(InputStream inputStream, Video video) {
		String tempDir = createDirectory(video.getId());
		String m3u8FileName = tempDir + File.separator + "index.m3u8";
		Path m3u8FilePath = Paths.get(m3u8FileName);
		List<String> lines = copyM3u8File(inputStream, m3u8FilePath);
		downloadTsFilesToLocal(video, true, tempDir, m3u8FilePath, lines);
	}

	public void downloadTsFilesToLocal(Video video, boolean isMerge, String tempDir, Path m3u8FilePath,
			List<String> lines) {
		if (CollectionUtil.isEmpty(lines)) {
			log.error("lines is empty");
			return;
		}

		List<String> tsLines = lines.stream()
				.filter(line -> line.endsWith(SystemConstants.TS_SUFFIX) || line.contains(".ts"))
				.collect(Collectors.toList());
		String realUrl = video.getRealUrl();
		String baseUrl = realUrl.substring(0, realUrl.lastIndexOf(StringPool.SLASH));
		List<String> tsFiles = Lists.newArrayList();
		if (RequestTypeEnum.REQUEST_BA_AV.getType() == video.getVideoWebsite()
				|| RequestTypeEnum.REQUEST_91.getType() == video.getVideoWebsite()) {
			downloadTsFilesAsync(baseUrl, tempDir, tsLines);

		}
		else {
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
		if (CollectionUtils.isEmpty(lines)) {
			throw new LibreException("lines is empty");
		}
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

	private String copyM3u8FileToString(InputStream inputStream, Path m3u8FilePath) {
		String content;
		try {
			Files.copy(inputStream, m3u8FilePath, StandardCopyOption.REPLACE_EXISTING);
			content = Files.readString(m3u8FilePath);
		}
		catch (IOException e) {
			log.error("文件读取失败: ", e);
			throw new LibreException(e);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
		log.debug("video copy success, path: {}", m3u8FilePath);
		return content;
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
			log.error("文件读取失败: ", e);
			throw new LibreException(e);
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
		log.info("video copy success, path: {}", m3u8FilePath);
		return lines;
	}

	public void copyTsFile(String tsPath, Resource resource) {
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
		String tempDir = getVideoTempDir(videoId);
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

	public String getVideoTempDir(Long videoId) {
		String downloadPath = properties.getDownloadPath();
		if (downloadPath.endsWith(File.separator)) {
			return downloadPath + videoId;
		}
		else {
			return downloadPath + File.separator + videoId;
		}

	}

	private void mergeTsFiles(Video video, String downloadPath, List<String> tsSet) {
		String[] paths = tsSet.stream().sorted().toArray(String[]::new);
		String videoPath = downloadPath + File.separator + video.getId() + SystemConstants.MP4_SUFFIX;
		VideoFileUtils.mergeFiles(paths, videoPath);
		log.info("合并完成");
		video.setVideoPath(videoPath);
		video.setTitle(String.valueOf(video.getId()));
		videoEncoder.encodeAndWrite(video);
		FileUtils.deleteQuietly(new File(videoPath));
	}

}
