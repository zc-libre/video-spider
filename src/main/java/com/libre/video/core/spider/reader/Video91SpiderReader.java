package com.libre.video.core.spider.reader;

import com.google.common.collect.Maps;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.spider.DomMapper;
import com.libre.toolkit.core.StringPool;
import com.libre.toolkit.core.ThreadUtil;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.pojo.parse.Video91Parse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: Libre
 * @Date: 2023/1/15 6:52 PM
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_91, step = VideoStepType.READER)
public class Video91SpiderReader extends AbstractVideoSpiderReader<Video91Parse> {

	private final String baseUrl;

	private final String urlTemplate;

	protected Video91SpiderReader(RedisUtils redisUtils) {
		super(redisUtils);
		this.baseUrl = getRequestType().getBaseUrl();
		this.urlTemplate = baseUrl + StringPool.AMPERSAND + "page" + StringPool.EQUALS + "{page}";
	}

	@Override
	protected List<Video91Parse> doParse(Integer page) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("page", page);
		String requestUrl = WebClientUtils.buildUrl(urlTemplate, params);

		String html = WebClientUtils.requestHtml(requestUrl);
		ThreadUtil.sleep(TimeUnit.SECONDS, 3L);

		return readVideoParseList(html);
	}

	@Override
	protected List<Video91Parse> readVideoParseList(String html) {
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
	protected String requestIndexPage() {
		return WebClientUtils.requestHtml(baseUrl);
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
			video91Parse.setLookNum(Integer.parseInt(lookText.strip()));
		}
		TextNode collectNode = (TextNode) nodes.get(16);
		String collectText = collectNode.text();
		if (StringUtil.isNotBlank(collectText)) {
			video91Parse.setCollectNum(Integer.parseInt(collectText.strip()));
		}
		String author = video91Parse.getAuthor();
		if (StringUtil.isNotBlank(author)) {
			video91Parse.setAuthor(author.strip());
		}
	}

	@Override
	protected Integer parsePageSize(String html) {
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
	protected RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_91;
	}

}
