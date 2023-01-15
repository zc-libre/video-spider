package com.libre.video.core.spider.reader;

import com.google.common.collect.Maps;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.spider.DomMapper;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Libre
 * @Date: 2023/1/15 8:54 PM
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_BA_AV, step = VideoStepType.READER)
public class VideoBaAvSpiderReader extends AbstractVideoSpiderReader<VideoBaAvParse> {

	private final String baseUrl;

	private final String urlTemplate;

	protected VideoBaAvSpiderReader(RedisUtils redisUtils) {
		super(redisUtils);
		this.baseUrl = getRequestType().getBaseUrl();
		this.urlTemplate = baseUrl + "/list/300-{page}.html";
	}

	@Override
	protected List<VideoBaAvParse> doParse(Integer page) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("page", page);
		String requestUrl = WebClientUtils.buildUrl(urlTemplate, params);
		Mono<String> res = WebClientUtils.request(requestUrl);
		String html = res.block();
		if (StringUtil.isBlank(html)) {
			throw new LibreException("html is blank, page: " +  page);
		}
		List<VideoBaAvParse> parseList = DomMapper.readList(html, VideoBaAvParse.class);
		return parseList.stream().filter(parse -> StringUtil.isNotBlank(parse.getUrl())).collect(Collectors.toList());
	}


	@Override
	protected String requestIndexPage() {
		String url = baseUrl + "/list/300.html";
		Mono<String> mono = WebClientUtils.request(url);
		return mono.block();
	}

	@Override
	protected List<VideoBaAvParse> readVideoParseList(String html) {
		if (StringUtil.isBlank(html)) {
			log.error("html is blank");
			return Collections.emptyList();
		}
		return DomMapper.readList(html, VideoBaAvParse.class);
	}

	@Override
	protected Integer parsePageSize(String html) {
		Document document = DomMapper.readDocument(html);
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


	@Override
	protected RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_BA_AV;
	}

}
