package com.libre.video;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libre.video.core.pojo.parse.VideoRouParse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 肉视频爬虫独立测试 - 直接通过 main 方法运行，请求真实网站
 */
public class RouSpiderTestMain {

	private static final String BASE_URL = "https://rou.video";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Pattern NEXT_DATA_PATTERN = Pattern
		.compile("<script\\s+id=\"__NEXT_DATA__\"[^>]*>([\\s\\S]*?)</script>");

	private static int passed = 0;

	private static int failed = 0;

	public static void main(String[] args) {
		System.out.println("========================================");
		System.out.println("       肉视频 (rou.video) 爬虫功能测试");
		System.out.println("========================================\n");

		try {
			testPageRequest();
			testJsonParsing();
			testEvDecryptAndM3u8Access();
			testMultiplePages();
		}
		catch (Exception e) {
			System.err.println("测试异常: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("\n========================================");
		System.out.println("测试结果: " + passed + " 通过, " + failed + " 失败");
		System.out.println("========================================");
	}

	private static void testPageRequest() throws Exception {
		System.out.println("【测试1】首页请求和 __NEXT_DATA__ 提取");
		String url = BASE_URL + "/home?page=1";
		System.out.println("  请求: " + url);

		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(30000).get();
		String html = doc.html();

		Matcher matcher = NEXT_DATA_PATTERN.matcher(html);
		if (matcher.find()) {
			String json = matcher.group(1);
			System.out.println("  找到 __NEXT_DATA__, JSON 长度: " + json.length());
			passed++;
		}
		else {
			System.out.println("  未找到 __NEXT_DATA__");
			failed++;
		}
	}

	private static void testJsonParsing() throws Exception {
		System.out.println("\n【测试2】latestVideos JSON 解析");
		String url = BASE_URL + "/home?page=1";

		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(30000).get();
		String html = doc.html();

		Matcher matcher = NEXT_DATA_PATTERN.matcher(html);
		if (!matcher.find()) {
			System.out.println("  未找到 __NEXT_DATA__");
			failed++;
			return;
		}

		String json = matcher.group(1);
		JsonNode root = OBJECT_MAPPER.readTree(json);
		JsonNode videosNode = root.at("/props/pageProps/latestVideos");

		if (videosNode.isMissingNode() || !videosNode.isArray()) {
			System.out.println("  latestVideos 节点不存在或不是数组");
			failed++;
			return;
		}

		List<VideoRouParse> videos = OBJECT_MAPPER.convertValue(videosNode, new TypeReference<>() {
		});
		System.out.println("  解析到视频数量: " + videos.size());

		if (videos.isEmpty()) {
			System.out.println("  视频列表为空");
			failed++;
			return;
		}

		passed++;

		// 打印前 3 个视频的详细信息
		for (int i = 0; i < Math.min(3, videos.size()); i++) {
			VideoRouParse v = videos.get(i);
			System.out.println("  --- 视频 " + (i + 1) + " ---");
			System.out.println("    id: " + v.getId());
			System.out.println("    vid: " + v.getVid());
			System.out.println("    nameZh: " + v.getNameZh());
			System.out.println("    name: " + v.getName());
			System.out.println("    duration: " + v.getDuration() + "s → " + formatDuration(v.getDuration()));
			System.out.println("    viewCount: " + v.getViewCount());
			System.out.println("    tags: " + v.getTags());
			System.out.println("    coverImageUrl: " + (v.getCoverImageUrl() != null
					? v.getCoverImageUrl().substring(0, Math.min(80, v.getCoverImageUrl().length())) + "..."
					: "null"));

			if (v.getSources() != null && !v.getSources().isEmpty()) {
				VideoRouParse.VideoSource src = v.getSources().get(0);
				System.out.println("    source.videoId: " + src.getVideoId());
				System.out.println("    source.folder: " + src.getFolder());
				System.out.println("    source.resolution: " + src.getResolution());

				// 构造 m3u8 URL
				String cdnHost = URI.create(v.getCoverImageUrl()).getHost();
				String m3u8 = "https://" + cdnHost + "/hls/" + src.getVideoId() + "/" + src.getFolder()
						+ "/index.m3u8";
				System.out.println("    m3u8 URL: " + m3u8);
			}

			// videoId 生成
			Long videoId = Math.abs((long) v.getId().hashCode());
			System.out.println("    videoId (hashCode): " + videoId);
			System.out.println("    detailUrl: " + BASE_URL + "/v/" + v.getId());
		}

		// 验证关键字段完整性
		long validCount = videos.stream()
			.filter(v -> v.getId() != null && v.getName() != null && v.getSources() != null
					&& !v.getSources().isEmpty())
			.count();
		System.out.println("\n  有效视频数: " + validCount + " / " + videos.size());
		if (validCount == videos.size()) {
			System.out.println("  所有视频数据完整");
			passed++;
		}
		else {
			System.out.println("  部分视频数据不完整");
			failed++;
		}
	}

	private static void testEvDecryptAndM3u8Access() throws Exception {
		System.out.println("\n【测试3】详情页 ev 解密和 m3u8 URL 验证");

		// 从首页拿第一个视频 ID
		Document listDoc = Jsoup.connect(BASE_URL + "/home?page=1").userAgent("Mozilla/5.0").timeout(30000).get();
		Matcher listMatcher = NEXT_DATA_PATTERN.matcher(listDoc.html());
		if (!listMatcher.find()) {
			System.out.println("  跳过：无法获取列表数据");
			return;
		}

		JsonNode listRoot = OBJECT_MAPPER.readTree(listMatcher.group(1));
		JsonNode videosNode = listRoot.at("/props/pageProps/latestVideos");
		List<VideoRouParse> videos = OBJECT_MAPPER.convertValue(videosNode, new TypeReference<>() {
		});
		if (videos.isEmpty()) {
			System.out.println("  跳过：无视频数据");
			return;
		}

		String videoId = videos.get(0).getId();
		String detailUrl = BASE_URL + "/v/" + videoId;
		System.out.println("  详情页: " + detailUrl);

		// 请求详情页
		Document detailDoc = Jsoup.connect(detailUrl).userAgent("Mozilla/5.0").timeout(30000).get();
		Matcher detailMatcher = NEXT_DATA_PATTERN.matcher(detailDoc.html());
		if (!detailMatcher.find()) {
			System.out.println("  详情页 __NEXT_DATA__ 未找到");
			failed++;
			return;
		}

		JsonNode detailRoot = OBJECT_MAPPER.readTree(detailMatcher.group(1));
		JsonNode evNode = detailRoot.at("/props/pageProps/ev");
		if (evNode.isMissingNode()) {
			System.out.println("  ev 节点不存在");
			failed++;
			return;
		}

		String d = evNode.get("d").asText();
		int k = evNode.get("k").asInt();
		System.out.println("  ev.d 长度: " + d.length() + ", ev.k: " + k);

		// 解密
		byte[] decoded = Base64.getDecoder().decode(d);
		StringBuilder sb = new StringBuilder(decoded.length);
		for (byte b : decoded) {
			sb.append((char) ((b & 0xFF) - k));
		}
		String decrypted = sb.toString();
		JsonNode videoInfo = OBJECT_MAPPER.readTree(decrypted);
		String m3u8Url = videoInfo.get("videoUrl").asText();
		System.out.println("  解密 videoUrl: " + m3u8Url);

		if (m3u8Url.contains("auth=") && m3u8Url.contains(".m3u8")) {
			System.out.println("  ev 解密成功，URL 包含 auth 签名");
			passed++;
		}
		else {
			System.out.println("  ev 解密结果异常");
			failed++;
		}

		// 验证 m3u8 URL 可访问
		HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(m3u8Url))
			.header("User-Agent", "Mozilla/5.0")
			.GET()
			.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		int statusCode = response.statusCode();
		System.out.println("  HTTP 状态码: " + statusCode);

		if (statusCode == 200) {
			System.out.println("  m3u8 URL 访问成功 (200)");
			String body = response.body();
			if (body.contains("#EXTM3U")) {
				System.out.println("  m3u8 内容有效 (包含 #EXTM3U)");
				passed++;
			}
			else {
				System.out.println("  m3u8 内容异常");
				failed++;
			}
		}
		else {
			System.out.println("  m3u8 URL 返回 " + statusCode);
			failed++;
		}
	}

	private static void testMultiplePages() throws Exception {
		System.out.println("\n【测试4】多页数据一致性");

		int[] pages = { 1, 2 };
		for (int page : pages) {
			String url = BASE_URL + "/home?page=" + page;
			try {
				Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(30000).get();
				Matcher matcher = NEXT_DATA_PATTERN.matcher(doc.html());
				if (!matcher.find()) {
					System.out.println("  第 " + page + " 页: 未找到 __NEXT_DATA__");
					failed++;
					continue;
				}

				JsonNode root = OBJECT_MAPPER.readTree(matcher.group(1));
				JsonNode videosNode = root.at("/props/pageProps/latestVideos");
				int count = videosNode.isArray() ? videosNode.size() : 0;
				System.out.println("  第 " + page + " 页: " + count + " 个视频");

				if (count > 0) {
					passed++;
				}
				else {
					System.out.println("  第 " + page + " 页为空");
					failed++;
				}
			}
			catch (Exception e) {
				System.out.println("  第 " + page + " 页请求失败: " + e.getMessage());
				failed++;
			}
		}
	}

	private static String formatDuration(Double seconds) {
		if (seconds == null) {
			return "N/A";
		}
		int totalSeconds = seconds.intValue();
		int minutes = totalSeconds / 60;
		int secs = totalSeconds % 60;
		if (minutes > 0) {
			return minutes + "分" + String.format("%02d", secs) + "秒";
		}
		return secs + "秒";
	}

}
