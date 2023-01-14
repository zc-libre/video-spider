package com.libre.video.core.batch;

import com.google.common.collect.Lists;
import com.libre.core.exception.LibreException;
import com.libre.core.time.DatePattern;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.core.constant.RequestConstant;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.core.mapstruct.Video9sMapping;
import com.libre.video.core.pojo.dto.Video9sDTO;
import com.libre.video.core.pojo.parse.Video9sDetailParse;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.core.request.VideoRequest;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: Libre
 * @Date: 2023/1/14 10:06 PM
 */
@Slf4j
public class Video9SSpiderReader extends AbstractVideoSpiderReader {

	private final String baseUrl = RequestTypeEnum.REQUEST_9S.getBaseUrl();


	public Video9SSpiderReader(VideoService videoService, WebClient webClient) {
		super(videoService, webClient);
	}

	@Override
	protected void doReadPage() {
		if (results == null) {
			results = new CopyOnWriteArrayList<>();
		}
		else {
			results.clear();
		}
		this.parseList();
	}

	@Override
	protected void doJumpToPage(int itemIndex) {
       log.info("234",itemIndex);
	}

	public void parseList() {
		int page = 1372 + this.getPage();
		String requestVideoUrl = baseUrl + StringPool.SLASH + page;
		try {
			List<Video9sParse> video9sParseList = requestParseList(requestVideoUrl);
			this.setPageSize(video9sParseList.size());
			results.addAll(video9sParseList);
		}
		catch (Exception e) {
			log.error("解析视频失败, url:" + requestVideoUrl, e);
		}
	}

	protected List<Video9sParse> requestParseList(String requestVideoUrl) {
		Mono<String> mono = request(requestVideoUrl);
		String videoPageHtml = mono.block();
		Assert.notNull(videoPageHtml, "videoPageHtml is blank");
		List<Video9sParse> parseList = DomMapper.readList(videoPageHtml, Video9sParse.class);
		if (CollectionUtil.isEmpty(parseList)) {
			log.error("parseList is empty");
			throw new LibreException("parseList is empty, url: " + requestVideoUrl);
		}
		return parseList;
	}

	protected Integer requestIndexPage() {
		Mono<String> request = request(baseUrl);
		String html = request.block();
		Integer pageSize = parsePageSize(html);
		Optional.ofNullable(pageSize).orElseThrow(() -> new LibreException("解析页码失败"));
		return pageSize;
	}

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
}
