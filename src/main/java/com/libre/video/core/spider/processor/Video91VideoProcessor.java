package com.libre.video.core.spider.processor;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringUtil;
import com.libre.core.toolkit.ThreadUtil;
import com.libre.spider.DomMapper;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.core.pojo.parse.Video91DetailParse;
import com.libre.video.core.pojo.parse.Video91Parse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.JsEncodeUtil;
import com.libre.video.toolkit.RegexUtil;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author: Libre
 * @Date: 2023/1/15 8:44 PM
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_91, step = VideoStepType.PROCESSOR)
public class Video91VideoProcessor extends AbstractVideoProcessor<Video91Parse> {

	public Video91VideoProcessor(M3u8Download m3u8Download) {
		super(m3u8Download);
	}

	@Override
	protected Video doProcess(Video91Parse parse) throws Exception {
		return readVideo(parse);
	}

	public Video readVideo(Video91Parse video91Parse) {
		Video video = null;
		try {
			String url = video91Parse.getUrl();
			if (StringUtil.isBlank(url)) {
				throw new LibreException("url is blank, url: " + url);
			}
			String body = WebClientUtils.requestHtml(url);
			if (StringUtil.isBlank(body)) {
				throw new LibreException("body is blank, body: " + body);
			}
			String realUrl = JsEncodeUtil.encodeRealVideoUrl(body);
			log.info("realVideoUrl: {}", realUrl);
			if (StringUtil.isBlank(realUrl)) {
				throw new LibreException("realVideoUrl is blank, realVideoUrl: " + body);
			}
			Video91Mapping mapping = Video91Mapping.INSTANCE;
			video = mapping.sourceToTarget(video91Parse);
			Video91DetailParse video91DetailParse = null;
			try {
				video91DetailParse = DomMapper.readValue(body, Video91DetailParse.class);
			}
			catch (Exception e) {
				log.error("read bean error, e: {}", e.getMessage());
			}
			long id = parseVideoId(realUrl);
			video.setVideoId(id);
			video.setVideoWebsite(getRequestType().getType());
			video.setRealUrl(realUrl);
			Video finalVideo = video;
			Optional.ofNullable(video91DetailParse).ifPresent(dto -> finalVideo.setPublishTime(dto.getPublishTime()));
			video.setUpdateTime(LocalDateTime.now());
			ThreadUtil.sleep(TimeUnit.SECONDS, 3);
		}
		catch (Exception e) {
			log.error("解析失败,", e);
		}
		return video;
	}

	private static long parseVideoId(String realUrl) {
		String regexValue = null;
		if (realUrl.contains("mp4")) {
			regexValue = RegexUtil.getRegexValue("(?<=/mp43/).*(?=.mp4)", 0, realUrl);
		}
		else if (realUrl.contains("m3u8")) {
			regexValue = RegexUtil.getRegexValue("(?<=/m3u8/(\\d+)/).*(?=.m3u8)", 0, realUrl);
		}
		regexValue = Optional.ofNullable(regexValue).orElse(IdWorker.getIdStr());
		return Long.parseLong(regexValue);
	}

	@Override
	public RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_91;
	}

}
