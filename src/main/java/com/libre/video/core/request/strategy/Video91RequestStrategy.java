package com.libre.video.core.request.strategy;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.ThreadUtil;
import com.libre.video.core.pojo.dto.Video91DTO;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.request.VideoRequest;
import com.libre.video.pojo.Video;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.core.pojo.parse.Video91Parse;
import com.libre.video.service.VideoService;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.toolkit.JsEncodeUtil;
import com.google.common.collect.Lists;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.toolkit.RegexUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@VideoRequest(RequestTypeEnum.REQUEST_91)
public class Video91RequestStrategy extends AbstractVideoRequestStrategy {

	private final static String PARAM_CATEGORY = "category";
	private final static String PARAM_PAGE = "page";

	public Video91RequestStrategy(VideoService videoService,WebClient webClient) {
		super(videoService, webClient);
	}

	@Override
	public void execute(VideoRequestParam requestParam) {
		//   Video91Type[] types = Video91Type.values();
		RequestTypeEnum requestTypeEnum = requestParam.getRequestTypeEnum();
		HttpUrl httpUrl = HttpUrl.get(requestTypeEnum.getBaseUrl());
		HttpUrl.Builder urlBuilder = getUrlBuilder(httpUrl);
		String url = urlBuilder.build().toString();
		String html = requestAsHtml(url);

		Integer pageSize = Optional.ofNullable(requestParam.getSize()).orElseGet(() -> parsePageSize(html));

		if (pageSize == null) {
			log.error("parse page error");
			return;
		}

		log.info("parse pageSize is: {}", pageSize);
		readVideosAndSave(html, url);

		for (int i = 2; i <= pageSize; i++) {
			urlBuilder.removeAllQueryParameters(PARAM_PAGE);
			urlBuilder.addQueryParameter(PARAM_PAGE, String.valueOf(i));
			String requestUrl = urlBuilder.build().toString();
			String doc = requestAsHtml(requestUrl);
			if (StringUtil.isBlank(doc)) {
				publishErrorVideo(urlBuilder.build().toString(), ErrorRequestType.REQUEST_PAGE);
				continue;
			}
			readVideosAndSave(doc, url);
			ThreadUtil.sleep(TimeUnit.SECONDS, 3);
		}

		log.info("video request complete!");
	}


	@Override
	public List<Video> readVideoList(String html) {
		List<Video91Parse> introductionList = DomMapper.readList(html, Video91Parse.class);
		if (CollectionUtil.isEmpty(introductionList)) {
			throw new LibreException("html parse error");
		}
		List<Video> videos = Lists.newArrayList();
		Map<String, Video91Parse> videoIntroductionMap = introductionList.stream()
			.filter(video91Parse -> StringUtil.isNotBlank(video91Parse.getUrl()))
			.collect(Collectors.toMap(Video91Parse::getUrl, video91Parse -> video91Parse, (v1, v2) -> v1));

		for (Video91Parse video91Parse : videoIntroductionMap.values()) {
			parseVideoInfo(html, video91Parse);
			Video video = readVideo(video91Parse);
			if (Objects.isNull(video)) {
				publishErrorVideo(video91Parse.getUrl(), html, ErrorRequestType.PARSE);
			}
			videos.add(video);
		}
		return videos;
	}

	private HttpUrl.Builder getUrlBuilder(HttpUrl httpUrl) {
		return httpUrl.newBuilder()
			.addQueryParameter("viewtype", "basic")
			.addQueryParameter(PARAM_PAGE, "1");
	}

	private void parseVideoInfo(String html, Video91Parse video91Parse) {
		Document document = Parser.parse(html, "");
		Elements elements = document.getElementsByClass("well");
		if (elements.isEmpty() || elements.size() < 2) {
			return;
		}
		Element element = elements.get(1);
		Elements wells = element.getElementsByClass("well");
		Element well = wells.get(0);
		if (Objects.isNull(well)) {
			return;
		}
		List<Node> nodes = well.childNodes();
		if (CollectionUtil.isEmpty(nodes) || nodes.size() < 15) {
			return;
		}
		TextNode authorNode = (TextNode) nodes.get(10);
		video91Parse.setAuthor(authorNode.text());
		TextNode lookNode = (TextNode) nodes.get(14);
		String lookText = lookNode.text();
		if (StringUtil.isNotBlank(lookText)) {
			video91Parse.setLookNum(Integer.parseInt(StringUtil.trimWhitespace(lookText)));
		}
		TextNode collectNode = (TextNode) nodes.get(16);
		String collectText = collectNode.text();
		if (StringUtil.isNotBlank(collectText)) {
			video91Parse.setCollectNum(Integer.parseInt(StringUtil.trimWhitespace(collectText)));
		}
	}

	public Video readVideo(Video91Parse video91Parse) {
		String url = video91Parse.getUrl();
		if (StringUtil.isBlank(url)) {
			return null;
		}
		String body = requestAsHtml(url);
		if (StringUtil.isBlank(body)) {
			return null;
		}
		String realUrl = JsEncodeUtil.encodeRealVideoUrl(body);
		log.info("realVideoUrl: {}", realUrl);
		if (StringUtil.isBlank(realUrl)) {
			return null;
		}
		Video91Mapping mapping = Video91Mapping.INSTANCE;
		Video video = mapping.sourceToTarget(video91Parse);
		Video91DTO video91DTO = null;
		try {
			video91DTO = DomMapper.readValue(body, Video91DTO.class);
		} catch (Exception e) {
			log.error("read bean error, e: {}", e.getMessage());
		}
		long id = parseVideoId(realUrl);
		video.setId(id);
		video.setRealUrl(realUrl);
		Optional.ofNullable(video91DTO).ifPresent(dto -> video.setPublishTime(dto.getPublishTime()));
		video.setUpdateTime(LocalDateTime.now());
		return video;
	}

	private static long parseVideoId(String realUrl) {
		String regexValue = null;
		if (realUrl.contains("mp4")) {
			regexValue = RegexUtil.getRegexValue("(?<=/mp43/).*(?=.mp4)", 0, realUrl);
		} else if (realUrl.contains("m3u8")) {
			regexValue = RegexUtil.getRegexValue("(?<=/m3u8/(\\d+)/).*(?=.m3u8)", 0, realUrl);
		}
		regexValue = Optional.ofNullable(regexValue).orElse(IdWorker.getIdStr());
		return Long.parseLong(regexValue);
	}


	public Integer parsePageSize(String html) {
		Document document = DomMapper.readDocument(html);
		Elements elements = document.getElementsByClass("pagingnav");
		if (elements.isEmpty()) {
			return null;
		}
		Element pageNav = elements.get(0);
		if (Objects.isNull(pageNav)) {
			return null;
		}
		Elements allPage = pageNav.getAllElements();
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

}
