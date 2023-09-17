package com.libre.video.core.spider.reader;

import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.spider.DomMapper;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.toolkit.HttpClientUtils;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * @author: Libre
 * @Date: 2023/1/14 10:06 PM
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_9S, step = VideoStepType.READER)
public class Video9SSpiderReader extends AbstractVideoSpiderReader<Video9sParse> {

	private final String baseUrl = RequestTypeEnum.REQUEST_9S.getBaseUrl();

	public Video9SSpiderReader(RedisUtils redisUtils) {
		super(redisUtils);
	}



	@Override
	protected String requestIndexPage() {
		return HttpClientUtils.request(baseUrl);
	}

	@Override
	protected List<Video9sParse> doParse(Integer page) {
		String requestVideoUrl = baseUrl + StringPool.SLASH + page;
		String videoPageHtml = HttpClientUtils.request(requestVideoUrl);
		Assert.notNull(videoPageHtml, "videoPageHtml is blank");
		List<Video9sParse> parseList = readVideoParseList(videoPageHtml);
		if (CollectionUtil.isEmpty(parseList)) {
			log.error("parseList is empty");
			throw new LibreException("parseList is empty, url: " + requestVideoUrl);
		}
		return parseList;
	}


	@Override
	protected List<Video9sParse> readVideoParseList(String html) {
       return DomMapper.readList(html, Video9sParse.class);
	}


	@Override
	protected RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_9S;
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
		Element page = allPage.get(allPage.size() - 3);
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
