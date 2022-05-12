package com.libre.video.core.request.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.core.mapstruct.VideoBaAvMapping;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.core.request.VideoRequest;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.RegexUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * @author: Libre
 * @Date: 2022/5/12 12:00 AM
 */
@Slf4j
@Component
@VideoRequest(RequestTypeEnum.REQUEST_BA_AV)
public class VideoBaAvRequestStrategy extends AbstractVideoRequestStrategy {

	private final WebClient webClient;

	private String baseUrl;

	private String urlTemplate;

	private final List<BaAvVideo> videoList = Lists.newCopyOnWriteArrayList();


	public VideoBaAvRequestStrategy(VideoService videoService, WebClient webClient) {
		super(videoService);
		this.webClient = webClient;
	}

	@Override
	public void execute(VideoRequestParam requestParam) {
		String url = baseUrl + "/300.html";
		Mono<String> mono = request(url);
		String body = mono.block();
		if (StringUtil.isBlank(body)) {
			return;
		}
		Integer pageSize = parsePageSize(body);
		Optional.ofNullable(pageSize).orElseThrow(() -> new LibreException("解析页码失败"));
		Map<String, Object> params = Maps.newHashMap();
		for (int i = 2; i <= pageSize; i++) {
			params.put("page", i);
			String requestUrl = buildUrl(params);
			Mono<String> res = request(requestUrl);
			try {
				String html = res.block();
				readVideoListAsync(html);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}


	@Override
	public List<Video> readVideoList(String html) {
		return null;
	}

	public void readVideoListAsync(String html) {
		if (StringUtil.isBlank(html)) {
			log.error("html is blank");
			return;
		}
		List<VideoBaAvParse> parseList = DomMapper.readList(html, VideoBaAvParse.class);
		if (CollectionUtil.isEmpty(parseList)) {
			log.error("parseList is empty");
			return;
		}
		parseList.forEach(videoBaAvParse -> {
			try {
				this.readVideo(videoBaAvParse);
			} catch (Exception e) {
				log.error("parse video error, {}", e.getMessage());
			}
		});
		List<BaAvVideo> list = Lists.newArrayList();
		list.addAll(videoList);
		VideoEventPublisher.publishBaAvVideoSaveEvent(list);
		videoList.clear();
	}

	private void readVideo(VideoBaAvParse parse) {
		String url = parse.getUrl();
		String realRequestUrl = baseUrl + StringPool.SLASH + "embed" + url;
		Mono<String> request = request(realRequestUrl);
		request.subscribe(body -> {
			String realUrl = RegexUtil.matchM3u8Url(body);
			VideoBaAvMapping mapping = VideoBaAvMapping.INSTANCE;
			BaAvVideo video = mapping.sourceToTarget(parse);
			Long id = parseId(url);
			video.setId(id);
			video.setUrl(baseUrl + url);
			video.setRealUrl(realUrl);
			videoList.add(video);
		});
	}


	private Integer parsePageSize(String body) {
		Document document = DomMapper.readDocument(body);
		if (Objects.isNull(document)) {
			return null;
		}
		Elements elements = document.getElementsByClass("pages");
		if (elements.isEmpty()) {
			return null;
		}
		Element ele = elements.get(0);
		Elements pages = ele.getAllElements();
		Element element = pages.get(pages.size() - 1);
		String href = element.attr("href");
		if (StringUtil.isBlank(href)) {
			return null;
		}
		int start = href.indexOf("300") + 4;
		int end = href.lastIndexOf(".html");
		String pageSizeStr = href.substring(start, end);
		if (StringUtil.isNotBlank(pageSizeStr)) {
			return Integer.parseInt(pageSizeStr);
		}
		return null;
	}


	private Long parseId(String url) {
		int start = url.indexOf(StringPool.SLASH) + 1;
		int end = url.indexOf(".html");
		String idStr = url.substring(start, end);
		if (StringUtil.isBlank(idStr)) {
			throw new LibreException("id parse error");
		}
		return Long.parseLong(idStr);
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		VideoRequest videoRequest = this.getClass().getAnnotation(VideoRequest.class);
		RequestTypeEnum requestTypeEnum = videoRequest.value();
		Assert.notNull(requestTypeEnum, "requestTypeEnum must not be null");
		baseUrl = requestTypeEnum.getBaseUrl();
		urlTemplate = baseUrl + "/300-{page}.html";
	}


	private Mono<String> request(String url) {
		log.info("start request url: {}", url);
		return webClient.get()
			.uri(url)
			.retrieve()
			.bodyToMono(String.class)
			.doOnError(e -> log.error("request error, url: {},message: {}", url, e.getMessage()))
			.retry(3);
	}

	private String buildUrl(Map<String, Object> params) {
		DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
		return uriBuilderFactory.uriString(urlTemplate).build(params).toString();
	}

}
