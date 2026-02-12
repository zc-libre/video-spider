package com.libre.video.core.spider.reader;

import com.libre.core.toolkit.StringUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.spider.DomMapper;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.pojo.parse.VideoHeiliaoParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 黑料网视频爬虫 Reader
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_HEILIAO, step = VideoStepType.READER)
public class VideoHeiliaoSpiderReader extends AbstractVideoSpiderReader<VideoHeiliaoParse> {

	private final String baseUrl;

	protected VideoHeiliaoSpiderReader(RedisUtils redisUtils) {
		super(redisUtils);
		this.baseUrl = getRequestType().getBaseUrl();
	}

	@Override
	protected List<VideoHeiliaoParse> doParse(Integer page) {
		String requestUrl = buildPageUrl(page);
		log.debug("爬取黑料网第 {} 页, URL: {}", page, requestUrl);

		String html;
		try {
			html = WebClientUtils.requestHtml(requestUrl);
		}
		catch (Exception e) {
			log.error("请求失败, URL: {}", requestUrl, e);
			return Collections.emptyList();
		}

		if (StringUtil.isBlank(html)) {
			log.error("html is blank, page: {}", page);
			return Collections.emptyList();
		}

		return readVideoParseList(html);
	}

	@Override
	protected String requestIndexPage() {
		return WebClientUtils.requestHtml(baseUrl + "/hlcg/");
	}

	@Override
	protected List<VideoHeiliaoParse> readVideoParseList(String html) {
		if (StringUtil.isBlank(html)) {
			log.error("html is blank");
			return Collections.emptyList();
		}
		List<VideoHeiliaoParse> parseList = DomMapper.readList(html, VideoHeiliaoParse.class);
		// 过滤广告条目：只保留 /archives/ 开头的内链
		return parseList.stream()
			.filter(parse -> StringUtil.isNotBlank(parse.getUrl()) && parse.getUrl().startsWith("/archives/"))
			.filter(parse -> StringUtil.isNotBlank(parse.getTitle()))
			.collect(Collectors.toList());
	}

	@Override
	protected Integer parsePageSize(String html) {
		Document document = DomMapper.readDocument(html);
		if (Objects.isNull(document)) {
			return null;
		}
		Elements elements = document.getElementsByClass("pagination");
		if (elements.isEmpty()) {
			return 1;
		}
		Element pagination = elements.get(0);
		Elements pageItems = pagination.getElementsByClass("page-item");
		if (pageItems.isEmpty()) {
			return 1;
		}
		// 最后一个 page-item（排除 ... 省略项）是尾页
		Element lastPage = pageItems.get(pageItems.size() - 1);
		String text = lastPage.text();
		if (StringUtil.isBlank(text) || !text.matches("\\d+")) {
			return 1;
		}
		return Integer.parseInt(text);
	}

	@Override
	protected RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_HEILIAO;
	}

	private String buildPageUrl(int page) {
		if (page <= 1) {
			return baseUrl + "/hlcg/";
		}
		return baseUrl + "/hlcg/page/" + page + "/";
	}

}
