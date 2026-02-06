package com.libre.video.core.spider.reader;

import com.google.common.collect.Lists;
import com.libre.core.toolkit.StringUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.spider.DomMapper;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.toolkit.WebClientUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BaAv 视频爬虫 Reader - 支持多分类爬取
 *
 * @author: Libre
 * @Date: 2023/1/15 8:54 PM
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_BA_AV, step = VideoStepType.READER)
public class VideoBaAvSpiderReader extends AbstractVideoSpiderReader<VideoBaAvParse> {

	/**
	 * 所有分类配置 (分类ID, 分类名称)
	 */
	private static final List<CategoryConfig> CATEGORIES = Arrays.asList(new CategoryConfig("1", "国产情色"),
			new CategoryConfig("2", "日本无码"), new CategoryConfig("3", "AV明星"), new CategoryConfig("20", "中文字幕"),
			new CategoryConfig("21", "网红主播"), new CategoryConfig("24", "国模私拍"), new CategoryConfig("25", "长腿丝袜"),
			new CategoryConfig("26", "邻家人妻"), new CategoryConfig("27", "韩国伦理"), new CategoryConfig("28", "香港伦理"),
			new CategoryConfig("31", "精品推荐"), new CategoryConfig("35", "AV明星1"));

	private final String baseUrl;

	/**
	 * 每个分类的总页数
	 */
	private final Map<String, Integer> categoryPageSizes = new LinkedHashMap<>();

	/**
	 * 每个分类的起始全局页码 (用于快速定位)
	 */
	private final int[] categoryStartPages = new int[CATEGORIES.size()];

	/**
	 * 所有分类的总页数
	 */
	private int totalPages = 0;

	protected VideoBaAvSpiderReader(RedisUtils redisUtils) {
		super(redisUtils);
		this.baseUrl = getRequestType().getBaseUrl();
	}

	@Override
	protected void doOpen() throws Exception {
		// 初始化所有分类的页数信息
		initCategoryPageSizes();
		// 调用父类逻辑
		super.doOpen();
	}

	/**
	 * 初始化所有分类的页数信息
	 */
	private void initCategoryPageSizes() {
		log.info("开始获取所有分类的页数信息...");
		int cumulativePages = 0;

		for (int i = 0; i < CATEGORIES.size(); i++) {
			CategoryConfig category = CATEGORIES.get(i);
			categoryStartPages[i] = cumulativePages;

			try {
				String html = requestCategoryIndexPage(category.getId());
				Integer pageSize = parseCategoryPageSize(html, category.getId());
				if (pageSize == null || pageSize <= 0) {
					pageSize = 1;
				}
				categoryPageSizes.put(category.getId(), pageSize);
				cumulativePages += pageSize;
				log.info("分类 [{}] {} 共 {} 页", category.getId(), category.getName(), pageSize);
			}
			catch (Exception e) {
				log.error("获取分类 [{}] {} 页数失败: {}", category.getId(), category.getName(), e.getMessage());
				categoryPageSizes.put(category.getId(), 1);
				cumulativePages += 1;
			}
		}

		this.totalPages = cumulativePages;
		log.info("所有分类共 {} 页", totalPages);
	}

	/**
	 * 请求指定分类的首页
	 */
	private String requestCategoryIndexPage(String categoryId) {
		String url = buildCategoryUrl(categoryId, 1);
		return WebClientUtils.requestHtml(url);
	}

	/**
	 * 解析指定分类的总页数
	 */
	private Integer parseCategoryPageSize(String html, String categoryId) {
		Document document = DomMapper.readDocument(html);
		if (Objects.isNull(document)) {
			return null;
		}
		Elements elements = document.getElementsByClass("pages");
		if (elements.isEmpty()) {
			return 1;
		}
		Element ele = elements.get(0);
		Elements pages = ele.getAllElements();
		if (pages.isEmpty()) {
			return 1;
		}
		// 获取最后一个链接 (尾页)
		Element lastPageElement = pages.get(pages.size() - 1);
		String href = lastPageElement.attr("href");
		if (StringUtil.isBlank(href)) {
			return 1;
		}
		// 解析页数: /site/6/1-1415.html -> 1415
		return extractPageNumber(href, categoryId);
	}

	/**
	 * 从 URL 中提取页数 URL 格式: /site/6/{categoryId}-{page}.html
	 */
	private Integer extractPageNumber(String href, String categoryId) {
		try {
			// 尝试匹配 /{categoryId}-{page}.html 格式
			String pattern = "/" + categoryId + "-";
			int start = href.indexOf(pattern);
			if (start != -1) {
				start += pattern.length();
				int end = href.lastIndexOf(".html");
				if (end > start) {
					String pageStr = href.substring(start, end);
					return Integer.parseInt(pageStr);
				}
			}
			// 如果是首页格式 /{categoryId}.html，返回 1
			if (href.contains("/" + categoryId + ".html")) {
				return 1;
			}
		}
		catch (NumberFormatException e) {
			log.warn("解析页数失败: {}", href);
		}
		return 1;
	}

	/**
	 * 构建分类页面 URL 首页: /site/6/{categoryId}.html 分页: /site/6/{categoryId}-{page}.html
	 */
	private String buildCategoryUrl(String categoryId, int page) {
		if (page == 1) {
			return baseUrl + "/site/6/" + categoryId + ".html";
		}
		return baseUrl + "/site/6/" + categoryId + "-" + page + ".html";
	}

	/**
	 * 根据全局页码定位到具体的分类和分类内页码
	 */
	private CategoryPage locateCategoryPage(int globalPage) {
		for (int i = CATEGORIES.size() - 1; i >= 0; i--) {
			if (globalPage > categoryStartPages[i]) {
				String categoryId = CATEGORIES.get(i).getId();
				int pageInCategory = globalPage - categoryStartPages[i];
				return new CategoryPage(categoryId, pageInCategory);
			}
		}
		// 默认返回第一个分类的第一页
		return new CategoryPage(CATEGORIES.get(0).getId(), 1);
	}

	@Override
	protected List<VideoBaAvParse> doParse(Integer page) {
		// 根据全局页码定位到具体分类和页码
		CategoryPage categoryPage = locateCategoryPage(page);
		String requestUrl = buildCategoryUrl(categoryPage.getCategoryId(), categoryPage.getPage());

		log.debug("爬取分类 [{}] 第 {} 页, URL: {}", categoryPage.getCategoryId(), categoryPage.getPage(), requestUrl);

		String html;
		try {
			html = WebClientUtils.requestHtml(requestUrl);
		}
		catch (Exception e) {
			log.error("请求失败, URL: {}", requestUrl, e);
			return Lists.newArrayList();
		}

		if (StringUtil.isBlank(html)) {
			log.error("html is blank, page: {}, url: {}", page, requestUrl);
			return Lists.newArrayList();
		}

		List<VideoBaAvParse> parseList = DomMapper.readList(html, VideoBaAvParse.class);
		return parseList.stream().filter(parse -> StringUtil.isNotBlank(parse.getUrl())).collect(Collectors.toList());
	}

	@Override
	protected String requestIndexPage() {
		// 返回第一个分类的首页
		return requestCategoryIndexPage(CATEGORIES.get(0).getId());
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
		// 返回所有分类的总页数
		return this.totalPages > 0 ? this.totalPages : parseCategoryPageSize(html, CATEGORIES.get(0).getId());
	}

	@Override
	protected RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_BA_AV;
	}

	/**
	 * 分类配置
	 */
	@Getter
	private static class CategoryConfig {

		private final String id;

		private final String name;

		CategoryConfig(String id, String name) {
			this.id = id;
			this.name = name;
		}

	}

	/**
	 * 分类页码定位结果
	 */
	@Getter
	private static class CategoryPage {

		private final String categoryId;

		private final int page;

		CategoryPage(String categoryId, int page) {
			this.categoryId = categoryId;
			this.page = page;
		}

	}

}
