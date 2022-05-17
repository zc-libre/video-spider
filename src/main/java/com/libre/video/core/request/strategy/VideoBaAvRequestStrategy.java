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
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;


/**
 * @author: Libre
 * @Date: 2022/5/12 12:00 AM
 */
@Slf4j
@Component
@VideoRequest(RequestTypeEnum.REQUEST_BA_AV)
public class VideoBaAvRequestStrategy extends AbstractVideoRequestStrategy<VideoBaAvParse>  {

	private String baseUrl;
	private String urlTemplate;
	private Integer requestType;
	private final List<BaAvVideo> videoList = Lists.newCopyOnWriteArrayList();

	public VideoBaAvRequestStrategy(VideoService videoService, WebClient webClient) {
		super(videoService, webClient);

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
		readVideoList(pageSize);
		log.info("request complete!");
	}


	protected void readVideoList(Integer pageSize) {
		Map<String, Object> params = Maps.newHashMap();
		for (int x = 1; x <= pageSize; x++) {
			params.put("page", x);
			String requestUrl = buildUrl(urlTemplate, params);
			Mono<String> res = request(requestUrl);
			String html = res.block();
			List<VideoBaAvParse> videoBaAvParses = parsePage(html);
			if (CollectionUtil.isEmpty(videoBaAvParses)) {
				log.error("parseList is empty");
				continue;
			}
			readAndSave(videoBaAvParses);
		}

	}

	protected List<VideoBaAvParse> parsePage(String html) {
		if (StringUtil.isBlank(html)) {
			log.error("html is blank");
			return Collections.emptyList();
		}
		return DomMapper.readList(html, VideoBaAvParse.class);
	}

	public void readAndSave(List<VideoBaAvParse> videoBaAvParses) {
		try {
			videoBaAvParses.forEach(this::readVideo);
		} catch (Exception e) {
			log.error("parse video error, {}", e.getMessage());
		}
		List<BaAvVideo> list = Lists.newArrayList();
		list.addAll(videoList);
		VideoBaAvMapping mapping = VideoBaAvMapping.INSTANCE;
		List<Video> videos = mapping.convertToVideList(list);
		videos.forEach(video -> video.setVideoWebsite(requestType));
		VideoEventPublisher.publishVideoSaveEvent(videos);
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
			video.setVideoId(id);
			video.setUrl(baseUrl + url);
			video.setRealUrl(realUrl);
			videoList.add(video);
		});
	}

	@Override
	public Integer parsePageSize(String body) {
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
		requestType = requestTypeEnum.getType();
	}

}
