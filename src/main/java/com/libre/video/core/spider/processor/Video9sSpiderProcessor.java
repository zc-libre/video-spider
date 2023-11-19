package com.libre.video.core.spider.processor;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.base.Throwables;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.core.exception.LibreException;
import com.libre.core.time.DatePattern;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.config.VideoProperties;
import com.libre.video.core.constant.RequestConstant;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.core.mapstruct.Video9sMapping;
import com.libre.video.core.pojo.dto.Video9sDTO;
import com.libre.video.core.pojo.parse.Video9sDetailParse;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author: Libre
 * @Date: 2023/1/14 11:11 PM
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_9S, step = VideoStepType.PROCESSOR)
public class Video9sSpiderProcessor extends AbstractVideoProcessor<Video9sParse> {

	private final WebClient webClient;

	private final VideoProperties properties;

	public Video9sSpiderProcessor(M3u8Download m3u8Download, WebClient webClient, VideoProperties properties) {
		super(m3u8Download);
		this.webClient = webClient;
		this.properties = properties;
	}

	@Override
	public Video doProcess(Video9sParse video9sParse) throws Exception {
		return read(video9sParse);
	}

	private Video read(Video9sParse video9sParse) {
		Video9sMapping mapping = Video9sMapping.INSTANCE;
		Video9sDTO video9SDTO = mapping.convertToVideo9s(video9sParse);
		String url = video9sParse.getUrl();
		if (StringUtil.isBlank(url)) {
			throw new LibreException("url is blank, url: " + url);
		}
		if (StringUtil.isNotBlank(url)) {
			url = RequestConstant.REQUEST_9S_BASE_URL + url;
			video9SDTO.setUrl(url);
		}
		String body = HttpClientUtils.request(url);
		if (StringUtil.isBlank(body)) {
			throw new LibreException("body is blank, url: " + url);
		}

		parseVideoInfo(body, video9SDTO);
		log.debug("解析到一条视频数据: {}", video9SDTO);
		Video91Mapping video91Mapping = Video91Mapping.INSTANCE;
		Video video = video91Mapping.convertToVideo91(video9SDTO);
		String image = video.getImage();
		if (StringUtil.isBlank(image)) {
			return video;
		}
		Mono<Resource> mono = webClient.get()
			.uri("https:" + image)
			.accept(MediaType.APPLICATION_OCTET_STREAM)
			.retrieve()
			.bodyToMono(Resource.class);
		Resource resource = mono.block();
		if (Objects.isNull(resource)) {
			return video;
		}
		try {
			InputStream inputStream = resource.getInputStream();
			VideoService videoService = SpringContext.getBean(VideoService.class);
			Assert.notNull(videoService, "videoService must not be null");
			String imageName = IdWorker.getId() + ".webp";
			String imagePath = videoService.saveVideoImageToOss(inputStream, imageName);
			video.setImage(imagePath);
		}
		catch (Exception e) {
			log.error("图片上传失败", Throwables.getRootCause(e));
			IOUtils.closeQuietly();
		}
		return video;
	}

	private void parseVideoInfo(String html, Video9sDTO video9SDTO) {
		Video9sDetailParse video9SDetailParse = DomMapper.readValue(html, Video9sDetailParse.class);
		String publishTime = video9SDetailParse.getPublishTime();
		if (StringUtil.isNotBlank(publishTime)) {
			publishTime = StringUtil.trimWhitespace(publishTime);
			video9SDTO.setPublishTime(
					LocalDate.parse(publishTime, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
		}
		BeanUtils.copyProperties(video9SDetailParse, video9SDTO);
	}

	@Override
	public RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_9S;
	}

}
