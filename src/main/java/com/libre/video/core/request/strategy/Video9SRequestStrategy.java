package com.libre.video.core.request.strategy;

import com.google.common.collect.Lists;
import com.libre.core.exception.LibreException;
import com.libre.core.time.DatePattern;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.core.constant.RequestConstant;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.core.request.VideoRequest;
import com.libre.video.pojo.*;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.core.pojo.dto.Video9sDTO;
import com.libre.video.core.pojo.parse.Video9sDetailParse;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.core.mapstruct.Video9sMapping;
import com.libre.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@VideoRequest(RequestTypeEnum.REQUEST_9S)
public class Video9SRequestStrategy extends AbstractVideoRequestStrategy<Video9sParse> {

	private String baseUrl;
	private Integer requestType;
	private final List<Video> videoList = Lists.newCopyOnWriteArrayList();

	public Video9SRequestStrategy(VideoService videoService, WebClient webClient) {
		super(videoService, webClient);
	}


	@Override
    public void execute(VideoRequestParam requestParam) {
		Mono<String> request = request(baseUrl);
		String html = request.block();
		Integer pageSize = parsePageSize(html);
		Optional.ofNullable(pageSize).orElseThrow(() -> new LibreException("解析页码失败"));
		pageSize = Optional.ofNullable(requestParam.getSize()).orElse(pageSize);
		readVideoList(pageSize);
    }

	@Override
    public void readVideoList(Integer pageSize) {
		for (int i = 1220; i <= pageSize; i++) {
			String requestVideoUrl = baseUrl + StringPool.SLASH + i;
			Mono<String> mono = request(requestVideoUrl);
			String body = mono.block();
			List<Video9sParse> parseList = parsePage(body);
			if (CollectionUtil.isEmpty(parseList)) {
				log.error("parseList is empty");
				continue;
			}
			readAndSave(parseList);
		}
		log.info("video request complete!");
    }

	@Override
	public void readAndSave(List<Video9sParse> parseList) {
		parseList.forEach(video9sParse -> {
			try {
				read(video9sParse);
			} catch (Exception e) {
				log.error("视频数据解析错误： {}", e.getMessage());
			}
		});

		List<Video> list = Lists.newArrayList();
		list.addAll(videoList);
		list.forEach(video -> video.setVideoWebsite(requestType));
		VideoEventPublisher.publishVideoSaveEvent(list);
		videoList.clear();
	}


	@Override
	public List<Video9sParse> parsePage(String html) {
		return DomMapper.readList(html, Video9sParse.class);
	}


	@Override
	public Integer parsePageSize(String html) {
		Document document = Parser.parse(html, "");
		Elements elements = document.getElementsByClass("pagination");
		if (elements.isEmpty()) {
			return null;
		}
		Element pagination = elements.get(0);
		if (Objects.isNull(pagination)) {
			return null;
		}
		Elements allPage = pagination.getAllElements();
		Element page = allPage.get(8);
		if (Objects.isNull(page)) {
			return null;
		}
		String text = page.ownText();
		if (StringUtil.isBlank(text)) {
			return null;
		}
		return Integer.parseInt(text);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		VideoRequest videoRequest = this.getClass().getAnnotation(VideoRequest.class);
		RequestTypeEnum requestTypeEnum = videoRequest.value();
		Assert.notNull(requestTypeEnum, "requestTypeEnum must not be null");
		baseUrl = requestTypeEnum.getBaseUrl();
		requestType = requestTypeEnum.getType();

	}

	private void read(Video9sParse video9sParse) {
		Video9sMapping mapping = Video9sMapping.INSTANCE;
		Video9sDTO video9SDTO = mapping.convertToVideo9s(video9sParse);
        String url = video9sParse.getUrl();
        if (StringUtil.isBlank(url)) {
            return;
        }
        if (StringUtil.isNotBlank(url)) {
            url =  RequestConstant.REQUEST_9S_BASE_URL + url;
            video9SDTO.setUrl(url);
        }
		Mono<String> request = request(url);
		String body = request.block();
		if (StringUtil.isBlank(body)) {
			return;
		}
		parseVideoInfo(body, video9SDTO);
		log.debug("解析到一条视频数据: {}", video9SDTO);
		Video91Mapping video91Mapping = Video91Mapping.INSTANCE;
		Video video = video91Mapping.convertToVideo91(video9SDTO);
		videoList.add(video);

    }
	private void parseVideoInfo(String html, Video9sDTO video9SDTO) {
		Video9sDetailParse video9SDetailParse = DomMapper.readValue(html, Video9sDetailParse.class);
		String publishTime = video9SDetailParse.getPublishTime();
		if (StringUtil.isNotBlank(publishTime)) {
			publishTime = StringUtil.trimWhitespace(publishTime);
			video9SDTO.setPublishTime(LocalDate.parse(publishTime, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
		}
		BeanUtils.copyProperties(video9SDetailParse, video9SDTO);
	}


}
