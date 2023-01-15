package com.libre.video.core.batch;

import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.spider.DomMapper;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * @author: Libre
 * @Date: 2023/1/14 10:06 PM
 */
@Slf4j
public class Video9SSpiderReader extends AbstractVideoSpiderReader {

	private final String baseUrl = RequestTypeEnum.REQUEST_9S.getBaseUrl();

	private final RedisUtils redisUtils;

	private Integer maxPageSize;

	public Video9SSpiderReader(RedisUtils redisUtils) {
		this.redisUtils = redisUtils;
	}

	@Override
	protected void doParseVideo() {
		int page = this.getPage();
		List<Video9sParse> video9sParses = doParse(page);
		doProcessVideos(video9sParses);
	}

	@Override
	protected void doOpen() throws Exception {
		super.doOpen();
		String indexPageHtml = requestIndexPage();
		Integer totalPageSize = parsePageSize(indexPageHtml);
		this.maxPageSize = totalPageSize;
		List<Video9sParse> video9sParses = readVideo9sParseList(indexPageHtml);
		if (CollectionUtil.isNotEmpty(video9sParses)) {
			this.setMaxItemCount(totalPageSize * video9sParses.size());
		}

		Integer videoCurrentPage = redisUtils.get(PAGE_CACHE_KEY);
		int page = this.getPage();
		if (Objects.nonNull(videoCurrentPage) && videoCurrentPage > page) {
			this.setPageSize(video9sParses.size());
			this.setCurrentItemCount(videoCurrentPage * video9sParses.size());
		}
	}

	@Override
	protected void doJumpToPage(int itemIndex) {
		log.error("doJumpToPage, currentItemIndex is {}, currentPage is: {}", itemIndex, getPage());

	}

	@Override
	public void update(ExecutionContext executionContext) throws ItemStreamException {
		super.update(executionContext);
		int currentPage = this.getPage();
		log.info("parse video is executing, currentPage is: {}, totalPage is: {}", currentPage, maxPageSize);
		redisUtils.set(PAGE_CACHE_KEY, this.getPage());
	}

	private void doProcessVideos(List<Video9sParse> video9sParseList) {
		this.setPageSize(video9sParseList.size());
		results.addAll(video9sParseList);
	}

	protected String requestIndexPage() {
		Mono<String> request = WebClientUtils.request(baseUrl);
		return request.block();
	}

	private List<Video9sParse> doParse(int page) {
		String requestVideoUrl = baseUrl + StringPool.SLASH + page;
		return requestParseList(requestVideoUrl);
	}

	protected List<Video9sParse> requestParseList(String requestVideoUrl) {
		Mono<String> mono = WebClientUtils.request(requestVideoUrl);
		String videoPageHtml = mono.block();
		Assert.notNull(videoPageHtml, "videoPageHtml is blank");
		List<Video9sParse> parseList = readVideo9sParseList(videoPageHtml);
		if (CollectionUtil.isEmpty(parseList)) {
			log.error("parseList is empty");
			throw new LibreException("parseList is empty, url: " + requestVideoUrl);
		}
		return parseList;
	}

	private static List<Video9sParse> readVideo9sParseList(String videoPageHtml) {
		return DomMapper.readList(videoPageHtml, Video9sParse.class);
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
