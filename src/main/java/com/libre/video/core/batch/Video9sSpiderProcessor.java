package com.libre.video.core.batch;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.libre.core.exception.LibreException;
import com.libre.core.time.DatePattern;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.core.constant.RequestConstant;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.core.mapstruct.Video9sMapping;
import com.libre.video.core.pojo.dto.Video9sDTO;
import com.libre.video.core.pojo.parse.Video9sDetailParse;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author: Libre
 * @Date: 2023/1/14 11:11 PM
 */
@Slf4j
public class Video9sSpiderProcessor extends AbstractVideoProcessor<Video9sParse> {

	private final M3u8Download m3u8Download;

	public Video9sSpiderProcessor(VideoService videoService, WebClient webClient, M3u8Download m3u8Download) {
		super(videoService, webClient);
		this.m3u8Download = m3u8Download;
	}

	@Override
	public Video process(Video9sParse video9sParse) throws Exception {
		Video video = this.read(video9sParse);
		video.setId(IdWorker.getId());
		video.setVideoWebsite(RequestTypeEnum.REQUEST_9S.getType());
		m3u8Download.downloadAndReadM3u8File(video);
		return video;
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
		Mono<String> request = request(url);
		String body = request.block();
		if (StringUtil.isBlank(body)) {
			throw new LibreException("body is blank, url: " + url);
		}

		parseVideoInfo(body, video9SDTO);
		log.debug("解析到一条视频数据: {}", video9SDTO);
		Video91Mapping video91Mapping = Video91Mapping.INSTANCE;
		return video91Mapping.convertToVideo91(video9SDTO);
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
}
