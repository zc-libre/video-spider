package com.libre.video.core.request.strategy;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Maps;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.ThreadUtil;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.core.mapstruct.VideoBaAvMapping;
import com.libre.video.core.pojo.parse.Video91DetailParse;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.core.request.VideoRequest;
import com.libre.video.pojo.BaAvVideo;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@VideoRequest(RequestTypeEnum.REQUEST_91)
public class Video91RequestStrategy extends AbstractVideoRequestStrategy<Video91Parse> {

	private String baseUrl;

	private Integer requestType;

	private String urlTemplate;

	private final static String PARAM_PAGE = "page";

	private final List<Video> videoList = Lists.newCopyOnWriteArrayList();

	public Video91RequestStrategy(VideoService videoService, WebClient webClient) {
		super(videoService, webClient);
	}

	@Override
	public void execute(VideoRequestParam requestParam) {
		Mono<String> mono = request(baseUrl);
		String html = mono.block();
		Integer pageSize = parsePageSize(html);
		Optional.ofNullable(pageSize).orElseThrow(() -> new LibreException("解析页码失败"));
		readVideoList(pageSize);
		log.info("video request complete!");
	}

	@Override
	protected void readVideoList(Integer pageSize) {
		for (int i = 1220; i <= pageSize; i++) {
			try {
				Map<String, Object> params = Maps.newHashMap();
				params.put(PARAM_PAGE, i);
				String requestUrl = buildUrl(urlTemplate, params);
				Mono<String> mono = request(requestUrl);
				String html = mono.block();
				List<Video91Parse> video91Parses = parsePage(html);
				if (CollectionUtil.isEmpty(video91Parses)) {
					log.error("parseList is empty");
					continue;
				}
				readAndSave(video91Parses);
			}
			catch (Exception e) {
				log.error("请求失败 ", e);
			}
		}
	}

	@Override
	protected List<Video91Parse> parsePage(String html) {
		if (StringUtil.isBlank(html)) {
			log.error("html is blank");
			return Collections.emptyList();
		}
		List<Video91Parse> video91Parses = DomMapper.readList(html, Video91Parse.class);
		for (Video91Parse video91Parse : video91Parses) {
			parseVideoInfo(html, video91Parse);
		}
		return video91Parses;
	}

	@Override
	protected void readAndSave(List<Video91Parse> parseList) {
		try {
			parseList.forEach(this::readVideo);
		}
		catch (Exception e) {
			log.error("parse video error, {}", e.getMessage());
		}
		List<Video> list = Lists.newArrayList();
		list.addAll(videoList);
		VideoEventPublisher.publishVideoSaveEvent(list);
		videoList.clear();
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
		String author = video91Parse.getAuthor();
		if (StringUtil.isNotBlank(author)) {
			video91Parse.setAuthor(StringUtil.trimWhitespace(author));
		}
	}

	public void readVideo(Video91Parse video91Parse) {
		try {
			String url = video91Parse.getUrl();
			if (StringUtil.isBlank(url)) {
				throw new LibreException("url is blank, url: " +  url);
			}
			Mono<String> mono = request(url);
			String body = mono.block();

			if (StringUtil.isBlank(body)) {
				throw new LibreException("body is blank, body: " +  body);
			}
			String realUrl = JsEncodeUtil.encodeRealVideoUrl(body);
			log.info("realVideoUrl: {}", realUrl);
			if (StringUtil.isBlank(realUrl)) {
				throw new LibreException("realVideoUrl is blank, realVideoUrl: " +  body);
			}
			Video91Mapping mapping = Video91Mapping.INSTANCE;
			Video video = mapping.sourceToTarget(video91Parse);
			Video91DetailParse video91DetailParse = null;
			try {
				video91DetailParse = DomMapper.readValue(body, Video91DetailParse.class);
			}
			catch (Exception e) {
				log.error("read bean error, e: {}", e.getMessage());
			}
			long id = parseVideoId(realUrl);
			video.setVideoId(id);
			video.setVideoWebsite(requestType);
			video.setRealUrl(realUrl);
			Optional.ofNullable(video91DetailParse).ifPresent(dto -> video.setPublishTime(dto.getPublishTime()));
			video.setUpdateTime(LocalDateTime.now());
			videoList.add(video);
			ThreadUtil.sleep(TimeUnit.SECONDS, 3);
		} catch (Exception e) {
			log.error("解析失败,", e);
		}

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

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		super.afterPropertiesSet();
		VideoRequest videoRequest = this.getClass().getAnnotation(VideoRequest.class);
		RequestTypeEnum requestTypeEnum = videoRequest.value();
		Assert.notNull(requestTypeEnum, "requestTypeEnum must not be null");
		baseUrl = requestTypeEnum.getBaseUrl();
		requestType = requestTypeEnum.getType();
		urlTemplate = baseUrl + StringPool.AMPERSAND + PARAM_PAGE + StringPool.EQUALS + "{page}";
	}


}
