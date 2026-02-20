package com.libre.video.core.spider.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libre.core.toolkit.StringUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.pojo.parse.VideoRouParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.toolkit.WebClientUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 肉视频爬虫 Reader - 支持多分类爬取
 * <p>
 * rou.video 按标签分类分页，URL 格式: /t/{tag}?order=createdAt&page={n}。
 * 启动时获取各分类总页数，将全局页码映射到具体的 (tag, pageInTag)。
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_ROU, step = VideoStepType.READER)
public class VideoRouSpiderReader extends AbstractVideoSpiderReader<VideoRouParse> {

	private static final String BASE_URL = RequestTypeEnum.REQUEST_ROU.getBaseUrl();

	private static final Pattern NEXT_DATA_PATTERN = Pattern
		.compile("<script\\s+id=\"__NEXT_DATA__\"[^>]*>([\\s\\S]*?)</script>");

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** 首页展示的主分类标签 */
	private static final List<TagConfig> TAGS = List.of(
//		new TagConfig("國產AV", "国产AV"),
		new TagConfig("自拍流出", "自拍流出"),
		new TagConfig("探花", "探花")
//		new TagConfig("OnlyFans", "OnlyFans")
		//new TagConfig("日本", "日本")
	);

	/** 每个分类的总页数 */
	private final Map<String, Integer> tagPageSizes = new LinkedHashMap<>();

	/** 每个分类的起始全局页码 */
	private final int[] tagStartPages = new int[TAGS.size()];

	/** 所有分类的总页数 */
	private int totalPages = 0;

	protected VideoRouSpiderReader(RedisUtils redisUtils) {
		super(redisUtils);
	}

	@Override
	protected void doOpen() throws Exception {
		initTagPageSizes();
		super.doOpen();
	}

	/**
	 * 初始化所有分类的页数信息
	 */
	private void initTagPageSizes() {
		log.info("开始获取肉视频各分类页数信息...");
		int cumulativePages = 0;

		for (int i = 0; i < TAGS.size(); i++) {
			TagConfig tag = TAGS.get(i);
			tagStartPages[i] = cumulativePages;

			try {
				String html = WebClientUtils.requestHtml(buildTagPageUrl(tag.getId(), 1));
				Integer pageSize = parseTotalPage(html);
				if (pageSize == null || pageSize <= 0) {
					pageSize = 1;
				}
				tagPageSizes.put(tag.getId(), pageSize);
				cumulativePages += pageSize;
				log.info("分类 [{}] {} 共 {} 页", tag.getId(), tag.getName(), pageSize);
			}
			catch (Exception e) {
				log.error("获取分类 [{}] {} 页数失败: {}", tag.getId(), tag.getName(), e.getMessage());
				tagPageSizes.put(tag.getId(), 1);
				cumulativePages += 1;
			}
		}

		this.totalPages = cumulativePages;
		log.info("肉视频所有分类共 {} 页", totalPages);
	}

	/**
	 * 根据全局页码定位到具体的分类和分类内页码
	 */
	private TagPage locateTagPage(int globalPage) {
		for (int i = TAGS.size() - 1; i >= 0; i--) {
			if (globalPage > tagStartPages[i]) {
				String tagId = TAGS.get(i).getId();
				int pageInTag = globalPage - tagStartPages[i];
				return new TagPage(tagId, pageInTag);
			}
		}
		return new TagPage(TAGS.get(0).getId(), 1);
	}

	@Override
	protected List<VideoRouParse> doParse(Integer page) {
		TagPage tagPage = locateTagPage(page);
		String requestUrl = buildTagPageUrl(tagPage.getTagId(), tagPage.getPage());
		log.debug("爬取肉视频 [{}] 第 {} 页, URL: {}", tagPage.getTagId(), tagPage.getPage(), requestUrl);

		String html;
		try {
			html = WebClientUtils.requestHtml(requestUrl);
		}
		catch (Exception e) {
			log.error("请求失败, tag: {}, page: {}", tagPage.getTagId(), tagPage.getPage(), e);
			return Collections.emptyList();
		}

		if (StringUtil.isBlank(html)) {
			log.warn("html is blank, tag: {}, page: {}", tagPage.getTagId(), tagPage.getPage());
			return Collections.emptyList();
		}

		return parseTagPageVideos(html);
	}

	@Override
	protected String requestIndexPage() {
		String url = buildTagPageUrl(TAGS.get(0).getId(), 1);
		return WebClientUtils.requestHtml(url);
	}

	@Override
	protected List<VideoRouParse> readVideoParseList(String html) {
		return parseTagPageVideos(html);
	}

	@Override
	protected Integer parsePageSize(String html) {
		return this.totalPages > 0 ? this.totalPages : parseTotalPage(html);
	}

	@Override
	protected RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_ROU;
	}

	/**
	 * 解析标签分页的视频列表，JSON 路径: props.pageProps.videos
	 */
	private List<VideoRouParse> parseTagPageVideos(String html) {
		String json = extractNextData(html);
		if (json == null) {
			log.error("__NEXT_DATA__ not found in html");
			return Collections.emptyList();
		}

		try {
			JsonNode root = OBJECT_MAPPER.readTree(json);
			JsonNode videosNode = root.at("/props/pageProps/videos");
			if (videosNode.isMissingNode() || !videosNode.isArray()) {
				log.warn("videos node not found or not array");
				return Collections.emptyList();
			}
			return OBJECT_MAPPER.convertValue(videosNode, new TypeReference<>() {
			});
		}
		catch (Exception e) {
			log.error("解析 __NEXT_DATA__ JSON 失败", e);
			return Collections.emptyList();
		}
	}

	/**
	 * 从 __NEXT_DATA__ 中解析 totalPage
	 */
	private Integer parseTotalPage(String html) {
		if (StringUtil.isBlank(html)) {
			return 1;
		}
		String json = extractNextData(html);
		if (json == null) {
			return 1;
		}
		try {
			JsonNode root = OBJECT_MAPPER.readTree(json);
			JsonNode totalPageNode = root.at("/props/pageProps/totalPage");
			if (!totalPageNode.isMissingNode()) {
				return totalPageNode.asInt();
			}
		}
		catch (Exception e) {
			log.error("解析 totalPage 失败", e);
		}
		return 1;
	}

	private String buildTagPageUrl(String tag, int page) {
		String encodedTag = URLEncoder.encode(tag, StandardCharsets.UTF_8);
		return BASE_URL + "/t/" + encodedTag + "?order=createdAt&page=" + page;
	}

	private String extractNextData(String html) {
		Matcher matcher = NEXT_DATA_PATTERN.matcher(html);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	/**
	 * 分类标签配置
	 */
	@Getter
	private static class TagConfig {

		private final String id;

		private final String name;

		TagConfig(String id, String name) {
			this.id = id;
			this.name = name;
		}

	}

	/**
	 * 分类页码定位结果
	 */
	@Getter
	private static class TagPage {

		private final String tagId;

		private final int page;

		TagPage(String tagId, int page) {
			this.tagId = tagId;
			this.page = page;
		}

	}

}
