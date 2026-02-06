package com.libre.video;

import com.libre.spider.DomMapper;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * BaAv 爬虫独立测试 - 可直接通过 main 方法运行
 */
public class BaAvSpiderTestMain {

    private static final String BASE_URL = "https://www.tasexy.com";
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       BaAv 爬虫功能测试");
        System.out.println("========================================\n");

        try {
            testCategoryListPage();
            testDomMapperParse();
            testPaginationParse();
            testVideoEmbed();
            testMultipleCategories();
        } catch (Exception e) {
            System.err.println("测试异常: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================");
        System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
        System.out.println("========================================");
    }

    private static void testCategoryListPage() throws Exception {
        System.out.println("【测试1】分类列表页 URL 和选择器");
        String url = BASE_URL + "/site/6/1.html";
        System.out.println("  请求: " + url);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        // 验证列表容器
        Elements listBox = doc.select(".list_box > ul");
        if (listBox.size() > 0) {
            System.out.println("  ✅ 找到视频列表容器, 共 " + listBox.size() + " 个条目");
            passed++;
        } else {
            System.out.println("  ❌ 未找到视频列表容器");
            failed++;
            return;
        }

        // 验证第一个有内容的条目的选择器 (跳过可能的广告容器)
        Element first = null;
        for (Element ul : listBox) {
            if (ul.selectFirst("a[href]") != null) {
                first = ul;
                break;
            }
        }

        if (first == null) {
            System.out.println("  ❌ 未找到有效的视频条目");
            failed++;
            return;
        }

        Element link = first.selectFirst("a[href]");
        if (link != null && link.attr("href").startsWith("/v/")) {
            System.out.println("  ✅ 链接选择器正确: " + link.attr("href"));
            passed++;
        } else {
            System.out.println("  ❌ 链接选择器失败");
            failed++;
        }

        Element title = first.selectFirst("a > .title");
        if (title != null && !title.text().isEmpty()) {
            System.out.println("  ✅ 标题选择器正确: " + title.text().substring(0, Math.min(30, title.text().length())) + "...");
            passed++;
        } else {
            System.out.println("  ❌ 标题选择器失败");
            failed++;
        }

        Element duration = first.selectFirst("a > li.image > span.note");
        if (duration != null) {
            System.out.println("  ✅ 时长选择器正确: " + duration.text());
            passed++;
        } else {
            System.out.println("  ❌ 时长选择器失败");
            failed++;
        }

        Element img = first.selectFirst("a > li.image > .lazy");
        if (img != null && !img.attr("img").isEmpty()) {
            System.out.println("  ✅ 封面图选择器正确: " + img.attr("img"));
            passed++;
        } else {
            System.out.println("  ❌ 封面图选择器失败");
            failed++;
        }
    }

    private static void testDomMapperParse() throws Exception {
        System.out.println("\n【测试2】DomMapper 解析");
        String url = BASE_URL + "/site/6/1.html";

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        List<VideoBaAvParse> parseList = DomMapper.readList(doc.html(), VideoBaAvParse.class);

        if (parseList != null && parseList.size() > 0) {
            System.out.println("  ✅ DomMapper 解析成功, 共 " + parseList.size() + " 条数据");
            passed++;

            VideoBaAvParse first = parseList.stream()
                    .filter(p -> p.getUrl() != null && !p.getUrl().isEmpty())
                    .findFirst()
                    .orElse(null);

            if (first != null) {
                System.out.println("  --- 第一条数据 ---");
                System.out.println("    标题: " + first.getTitle());
                System.out.println("    链接: " + first.getUrl());
                System.out.println("    时长: " + first.getDuration());
                System.out.println("    封面: " + first.getImage());
                System.out.println("    观看: " + first.getLookNum());
                System.out.println("    时间: " + first.getPublishTime());
            }
        } else {
            System.out.println("  ❌ DomMapper 解析失败");
            failed++;
        }
    }

    private static void testPaginationParse() throws Exception {
        System.out.println("\n【测试3】分页解析");
        String url = BASE_URL + "/site/6/1.html";

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        Elements pages = doc.getElementsByClass("pages");
        if (!pages.isEmpty()) {
            Element lastPage = pages.first().select("a").last();
            String href = lastPage.attr("href");
            Integer totalPages = extractPageNumber(href, "1");

            if (totalPages != null && totalPages > 100) {
                System.out.println("  ✅ 分页解析成功, 尾页: " + href + ", 总页数: " + totalPages);
                passed++;
            } else {
                System.out.println("  ❌ 分页解析异常, 页数: " + totalPages);
                failed++;
            }
        } else {
            System.out.println("  ❌ 未找到分页元素");
            failed++;
        }
    }

    private static void testVideoEmbed() throws Exception {
        System.out.println("\n【测试4】视频 embed URL 和 m3u8 获取");

        // 获取一个视频 ID
        String listUrl = BASE_URL + "/site/6/1.html";
        Document listDoc = Jsoup.connect(listUrl)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        Element firstLink = listDoc.selectFirst(".list_box > ul a[href^='/v/']");
        if (firstLink == null) {
            System.out.println("  ❌ 未找到视频链接");
            failed++;
            return;
        }

        String videoPath = firstLink.attr("href");
        Long videoId = parseVideoId(videoPath);
        System.out.println("  视频路径: " + videoPath + ", ID: " + videoId);

        // 构建 embed URL
        String embedUrl = BASE_URL + "/embed/" + videoId + ".html";
        System.out.println("  Embed URL: " + embedUrl);

        Document embedDoc = Jsoup.connect(embedUrl)
                .userAgent("Mozilla/5.0")
                .timeout(30000)
                .get();

        String html = embedDoc.html();
        if (html.contains("m3u8")) {
            // 提取 m3u8 地址
            int start = html.indexOf("https://");
            if (start > 0) {
                int end = html.indexOf(".m3u8", start);
                if (end > start) {
                    String m3u8 = html.substring(start, end + 5);
                    System.out.println("  ✅ 获取 m3u8 成功: " + m3u8);
                    passed++;
                    return;
                }
            }
        }

        if (html.contains("source") || html.contains("video")) {
            System.out.println("  ✅ 找到视频源 (非 m3u8 格式)");
            passed++;
        } else {
            System.out.println("  ❌ 未找到视频源");
            failed++;
        }
    }

    private static void testMultipleCategories() throws Exception {
        System.out.println("\n【测试5】多分类 URL 可访问性");

        String[][] categories = {
                {"1", "国产情色"},
                {"2", "日本无码"},
                {"20", "中文字幕"},
                {"21", "网红主播"},
                {"22", "成人动漫"}
        };

        int successCount = 0;
        for (String[] cat : categories) {
            String id = cat[0];
            String name = cat[1];
            String url = BASE_URL + "/site/6/" + id + ".html";

            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(30000)
                        .get();

                Elements listBox = doc.select(".list_box > ul");
                Elements pages = doc.getElementsByClass("pages");
                Integer totalPages = 1;
                if (!pages.isEmpty()) {
                    Element lastPage = pages.first().select("a").last();
                    totalPages = extractPageNumber(lastPage.attr("href"), id);
                }

                System.out.println("  ✅ [" + id + "] " + name + " - " + listBox.size() + " 条/页, 共 " + totalPages + " 页");
                successCount++;
            } catch (Exception e) {
                System.out.println("  ❌ [" + id + "] " + name + " - 访问失败: " + e.getMessage());
            }
        }

        if (successCount == categories.length) {
            System.out.println("  ✅ 所有分类访问成功");
            passed++;
        } else {
            System.out.println("  ❌ 部分分类访问失败");
            failed++;
        }
    }

    private static Integer extractPageNumber(String href, String categoryId) {
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
        } catch (NumberFormatException e) {
            // ignore
        }
        return 1;
    }

    private static Long parseVideoId(String url) {
        int start = url.lastIndexOf("/") + 1;
        int end = url.indexOf(".html");
        if (end > start) {
            return Long.parseLong(url.substring(start, end));
        }
        return null;
    }
}
