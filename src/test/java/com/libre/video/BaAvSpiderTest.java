package com.libre.video;

import com.libre.spider.DomMapper;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaAv 爬虫测试 - 验证 URL 路径和 CSS 选择器是否正确
 */
public class BaAvSpiderTest {

	private static final String BASE_URL = "https://www.tasexy.com";

	/**
	 * 测试分类列表页 URL 和数据解析
	 */
	@Test
	void testCategoryListPage() throws IOException {
		// 测试国产情色分类首页
		String categoryUrl = BASE_URL + "/site/6/1.html";
		System.out.println("测试分类列表页: " + categoryUrl);

		Document doc = Jsoup.connect(categoryUrl)
				.userAgent("Mozilla/5.0")
				.timeout(30000)
				.get();

		// 验证页面标题
		String title = doc.title();
		System.out.println("页面标题: " + title);
		assertNotNull(title);
		assertTrue(title.contains("国产情色") || title.contains("Tasexy"));

		// 验证列表容器存在
		Elements listBox = doc.select(".list_box > ul");
		System.out.println("找到视频条目数: " + listBox.size());
		assertTrue(listBox.size() > 0, "应该能找到视频列表");

		// 验证 CSS 选择器
		for (int i = 0; i < Math.min(3, listBox.size()); i++) {
			Element ul = listBox.get(i);

			// 验证链接
			Element link = ul.selectFirst("a[href]");
			if (link != null) {
				String href = link.attr("href");
				System.out.println("视频链接: " + href);
				assertTrue(href.startsWith("/v/") && href.endsWith(".html"),
						"链接格式应为 /v/{id}.html");
			}

			// 验证标题
			Element titleEl = ul.selectFirst("a > .title");
			if (titleEl != null) {
				System.out.println("视频标题: " + titleEl.text());
				assertFalse(titleEl.text().isEmpty(), "标题不应为空");
			}

			// 验证时长
			Element durationEl = ul.selectFirst("a > li.image > span.note");
			if (durationEl != null) {
				System.out.println("时长: " + durationEl.text());
			}

			// 验证封面图
			Element imgEl = ul.selectFirst("a > li.image > .lazy");
			if (imgEl != null) {
				String imgUrl = imgEl.attr("img");
				System.out.println("封面图: " + imgUrl);
				assertFalse(imgUrl.isEmpty(), "封面图URL不应为空");
			}

			System.out.println("---");
		}
	}

	/**
	 * 测试使用 DomMapper 解析列表页
	 */
	@Test
	void testDomMapperParse() throws IOException {
		String categoryUrl = BASE_URL + "/site/6/1.html";
		System.out.println("测试 DomMapper 解析: " + categoryUrl);

		Document doc = Jsoup.connect(categoryUrl)
				.userAgent("Mozilla/5.0")
				.timeout(30000)
				.get();

		String html = doc.html();
		List<VideoBaAvParse> parseList = DomMapper.readList(html, VideoBaAvParse.class);

		System.out.println("解析到视频数量: " + parseList.size());
		assertTrue(parseList.size() > 0, "应该能解析到视频数据");

		// 验证前 3 条数据
		for (int i = 0; i < Math.min(3, parseList.size()); i++) {
			VideoBaAvParse parse = parseList.get(i);
			System.out.println("=== 视频 " + (i + 1) + " ===");
			System.out.println("标题: " + parse.getTitle());
			System.out.println("链接: " + parse.getUrl());
			System.out.println("封面: " + parse.getImage());
			System.out.println("时长: " + parse.getDuration());
			System.out.println("观看数: " + parse.getLookNum());
			System.out.println("发布时间: " + parse.getPublishTime());

			// 验证必填字段
			if (parse.getUrl() != null && !parse.getUrl().isEmpty()) {
				assertTrue(parse.getUrl().startsWith("/v/"), "URL 应以 /v/ 开头");
			}
		}
	}

	/**
	 * 测试分页解析
	 */
	@Test
	void testPaginationParse() throws IOException {
		String categoryUrl = BASE_URL + "/site/6/1.html";
		System.out.println("测试分页解析: " + categoryUrl);

		Document doc = Jsoup.connect(categoryUrl)
				.userAgent("Mozilla/5.0")
				.timeout(30000)
				.get();

		// 验证分页容器
		Elements pages = doc.getElementsByClass("pages");
		assertFalse(pages.isEmpty(), "应该能找到分页元素");

		Element pagesEl = pages.first();
		Elements pageLinks = pagesEl.select("a");
		System.out.println("分页链接数: " + pageLinks.size());

		// 获取最后一个链接 (尾页)
		Element lastPage = pageLinks.last();
		String lastPageHref = lastPage.attr("href");
		System.out.println("尾页链接: " + lastPageHref);

		// 解析总页数
		Integer totalPages = extractPageNumber(lastPageHref, "1");
		System.out.println("解析到总页数: " + totalPages);
		assertNotNull(totalPages);
		assertTrue(totalPages > 100, "国产情色分类应该有超过100页");
	}

	/**
	 * 测试视频详情页和 embed URL
	 */
	@Test
	void testVideoDetailAndEmbed() throws IOException {
		// 先获取一个视频链接
		String categoryUrl = BASE_URL + "/site/6/1.html";
		Document listDoc = Jsoup.connect(categoryUrl)
				.userAgent("Mozilla/5.0")
				.timeout(30000)
				.get();

		Element firstLink = listDoc.selectFirst(".list_box > ul a[href^='/v/']");
		assertNotNull(firstLink, "应该能找到视频链接");

		String videoPath = firstLink.attr("href");
		System.out.println("视频详情页路径: " + videoPath);

		// 提取视频 ID
		Long videoId = parseVideoId(videoPath);
		System.out.println("解析到视频 ID: " + videoId);
		assertNotNull(videoId);

		// 构建 embed URL
		String embedUrl = BASE_URL + "/embed/" + videoId + ".html";
		System.out.println("Embed URL: " + embedUrl);

		// 访问 embed 页面验证
		Document embedDoc = Jsoup.connect(embedUrl)
				.userAgent("Mozilla/5.0")
				.timeout(30000)
				.get();

		String embedHtml = embedDoc.html();
		System.out.println("Embed 页面长度: " + embedHtml.length());

		// 检查是否包含 m3u8 或视频源
		boolean hasVideoSource = embedHtml.contains("m3u8") ||
				embedHtml.contains("source") ||
				embedHtml.contains("video");
		System.out.println("包含视频源: " + hasVideoSource);
	}

	/**
	 * 测试多个分类的 URL 可访问性
	 */
	@Test
	void testMultipleCategoriesAccessibility() {
		String[][] categories = {
				{"1", "国产情色"},
				{"2", "日本无码"},
				{"3", "AV明星"},
				{"20", "中文字幕"},
				{"21", "网红主播"}
		};

		System.out.println("测试多分类 URL 可访问性:");
		for (String[] cat : categories) {
			String categoryId = cat[0];
			String categoryName = cat[1];
			String url = BASE_URL + "/site/6/" + categoryId + ".html";

			try {
				Document doc = Jsoup.connect(url)
						.userAgent("Mozilla/5.0")
						.timeout(30000)
						.get();

				Elements listBox = doc.select(".list_box > ul");
				int videoCount = listBox.size();

				// 获取总页数
				Elements pages = doc.getElementsByClass("pages");
				Integer totalPages = 1;
				if (!pages.isEmpty()) {
					Element lastPage = pages.first().select("a").last();
					if (lastPage != null) {
						totalPages = extractPageNumber(lastPage.attr("href"), categoryId);
					}
				}

				System.out.printf("✅ [%s] %s - 当前页 %d 条, 共 %d 页%n",
						categoryId, categoryName, videoCount, totalPages);

			} catch (Exception e) {
				System.out.printf("❌ [%s] %s - 访问失败: %s%n",
						categoryId, categoryName, e.getMessage());
			}
		}
	}

	/**
	 * 测试分类分页 URL 格式
	 */
	@Test
	void testCategoryPaginationUrl() throws IOException {
		// 测试第 2 页
		String page2Url = BASE_URL + "/site/6/1-2.html";
		System.out.println("测试分页 URL: " + page2Url);

		Document doc = Jsoup.connect(page2Url)
				.userAgent("Mozilla/5.0")
				.timeout(30000)
				.get();

		Elements listBox = doc.select(".list_box > ul");
		System.out.println("第 2 页视频数量: " + listBox.size());
		assertTrue(listBox.size() > 0, "第 2 页应该有视频数据");

		// 验证分页状态
		Elements pages = doc.getElementsByClass("pages");
		Element activePage = pages.first().selectFirst("a.active");
		if (activePage != null) {
			System.out.println("当前页码: " + activePage.text());
			assertEquals("2", activePage.text(), "当前应该是第 2 页");
		}
	}

	// ========== 辅助方法 ==========

	private Integer extractPageNumber(String href, String categoryId) {
		try {
			String pattern = "/" + categoryId + "-";
			int start = href.indexOf(pattern);
			if (start != -1) {
				start += pattern.length();
				int end = href.lastIndexOf(".html");
				if (end > start) {
					return Integer.parseInt(href.substring(start, end));
				}
			}
			if (href.contains("/" + categoryId + ".html")) {
				return 1;
			}
		} catch (NumberFormatException e) {
			System.err.println("解析页数失败: " + href);
		}
		return 1;
	}

	private Long parseVideoId(String url) {
		// URL 格式: /v/827573.html
		int start = url.lastIndexOf("/") + 1;
		int end = url.indexOf(".html");
		if (end > start) {
			return Long.parseLong(url.substring(start, end));
		}
		return null;
	}
}
