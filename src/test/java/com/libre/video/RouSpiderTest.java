package com.libre.video;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libre.video.core.pojo.parse.VideoRouParse;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 肉视频爬虫测试 - 验证 JSON 解析、数据转换逻辑
 */
public class RouSpiderTest {

	private static final String BASE_URL = "https://rou.video";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Pattern NEXT_DATA_PATTERN = Pattern
		.compile("<script\\s+id=\"__NEXT_DATA__\"[^>]*>([\\s\\S]*?)</script>");

	/**
	 * 测试 __NEXT_DATA__ JSON 提取和解析
	 */
	@Test
	void testNextDataJsonParsing() throws Exception {
		// 模拟 __NEXT_DATA__ 中的 latestVideos JSON
		String sampleJson = """
				{
				  "props": {
				    "pageProps": {
				      "latestVideos": [
				        {
				          "id": "cmluv6vet0000s6xmjx49sqe1",
				          "vid": null,
				          "name": "繁體標題測試",
				          "nameZh": "简体标题测试",
				          "tags": ["自拍流出"],
				          "duration": 491.72,
				          "viewCount": 33,
				          "coverImageUrl": "https://v.rn204.xyz/m/test/cover.jpg",
				          "sources": [
				            {
				              "id": "src001",
				              "videoId": "cmluv6vet0000s6xmjx49sqe1",
				              "resolution": 404,
				              "folder": "cmluv6vet0000s6xmjx49sqe1-404"
				            }
				          ],
				          "createdAt": "2026-02-20T12:26:53.190Z"
				        },
				        {
				          "id": "cmluj7v0u0000s6yda5bhv7sk",
				          "vid": "ABC-123",
				          "name": "第二個視頻",
				          "nameZh": null,
				          "tags": ["国产", "高清"],
				          "duration": 1326.72,
				          "viewCount": 1094,
				          "coverImageUrl": "https://v.rn206.xyz/m/test2/cover.jpg",
				          "sources": [
				            {
				              "id": "src002",
				              "videoId": "cmluj7v0u0000s6yda5bhv7sk",
				              "resolution": 720,
				              "folder": "cmluj7v0u0000s6yda5bhv7sk-720"
				            }
				          ],
				          "createdAt": "2026-02-19T08:00:00.000Z"
				        }
				      ]
				    }
				  }
				}
				""";

		JsonNode root = OBJECT_MAPPER.readTree(sampleJson);
		JsonNode videosNode = root.at("/props/pageProps/latestVideos");

		assertFalse(videosNode.isMissingNode());
		assertTrue(videosNode.isArray());
		assertEquals(2, videosNode.size());

		List<VideoRouParse> videos = OBJECT_MAPPER.convertValue(videosNode, new TypeReference<>() {
		});

		assertEquals(2, videos.size());

		// 验证第一个视频
		VideoRouParse v1 = videos.get(0);
		assertEquals("cmluv6vet0000s6xmjx49sqe1", v1.getId());
		assertNull(v1.getVid());
		assertEquals("繁體標題測試", v1.getName());
		assertEquals("简体标题测试", v1.getNameZh());
		assertEquals(1, v1.getTags().size());
		assertEquals("自拍流出", v1.getTags().get(0));
		assertEquals(491.72, v1.getDuration(), 0.01);
		assertEquals(33, v1.getViewCount());
		assertTrue(v1.getCoverImageUrl().contains("v.rn204.xyz"));
		assertEquals(1, v1.getSources().size());
		assertEquals("cmluv6vet0000s6xmjx49sqe1-404", v1.getSources().get(0).getFolder());
		assertEquals(404, v1.getSources().get(0).getResolution());

		// 验证第二个视频
		VideoRouParse v2 = videos.get(1);
		assertEquals("ABC-123", v2.getVid());
		assertNull(v2.getNameZh());
		assertEquals(720, v2.getSources().get(0).getResolution());
	}

	/**
	 * 测试正则表达式提取 __NEXT_DATA__
	 */
	@Test
	void testNextDataRegexExtraction() {
		String html = """
				<html><head></head><body>
				<div id="__next">content</div>
				<script id="__NEXT_DATA__" type="application/json">{"props":{"pageProps":{"latestVideos":[]}}}</script>
				</body></html>
				""";

		Matcher matcher = NEXT_DATA_PATTERN.matcher(html);
		assertTrue(matcher.find(), "__NEXT_DATA__ 应该被正则匹配到");
		String json = matcher.group(1);
		assertNotNull(json);
		assertTrue(json.contains("latestVideos"));
	}

	/**
	 * 测试正则表达式 - 无 __NEXT_DATA__ 的情况
	 */
	@Test
	void testNextDataRegexNoMatch() {
		String html = "<html><body><script>var data = {};</script></body></html>";

		Matcher matcher = NEXT_DATA_PATTERN.matcher(html);
		assertFalse(matcher.find(), "不应匹配到 __NEXT_DATA__");
	}

	/**
	 * 测试 m3u8 URL 构造
	 */
	@Test
	void testM3u8UrlConstruction() {
		String coverImageUrl = "https://v.rn204.xyz/m/GQMFdunXrxC4j5A0RAvjLhDMbVkCTPkL1h7UV-ssvP0/cover.jpg";
		String videoId = "cmluv6vet0000s6xmjx49sqe1";
		String folder = "cmluv6vet0000s6xmjx49sqe1-404";

		String cdnHost = URI.create(coverImageUrl).getHost();
		assertEquals("v.rn204.xyz", cdnHost);

		String m3u8Url = "https://" + cdnHost + "/hls/" + videoId + "/" + folder + "/index.m3u8";
		assertEquals(
				"https://v.rn204.xyz/hls/cmluv6vet0000s6xmjx49sqe1/cmluv6vet0000s6xmjx49sqe1-404/index.m3u8",
				m3u8Url);
	}

	/**
	 * 测试 CDN 域名提取 - 各种 coverImageUrl 格式
	 */
	@Test
	void testCdnHostExtraction() {
		// 正常 imgproxy URL
		assertEquals("v.rn204.xyz",
				URI.create("https://v.rn204.xyz/m/test/cover.jpg").getHost());
		assertEquals("v.rn206.xyz",
				URI.create("https://v.rn206.xyz/m/test/cover.jpg").getHost());
		assertEquals("v.rn207.xyz",
				URI.create("https://v.rn207.xyz/another/path/image.png").getHost());
	}

	/**
	 * 测试 duration 格式化
	 */
	@Test
	void testDurationFormatting() {
		// 8分11秒
		assertEquals("8分11秒", formatDuration(491.72));
		// 22分06秒
		assertEquals("22分06秒", formatDuration(1326.72));
		// 0秒
		assertEquals("0秒", formatDuration(0.0));
		// 59秒
		assertEquals("59秒", formatDuration(59.9));
		// 1分00秒
		assertEquals("1分00秒", formatDuration(60.0));
		// 60分00秒
		assertEquals("60分00秒", formatDuration(3600.0));
	}

	/**
	 * 测试 videoId 生成（hashCode 取绝对值）
	 */
	@Test
	void testVideoIdGeneration() {
		String cuid1 = "cmluv6vet0000s6xmjx49sqe1";
		String cuid2 = "cmluj7v0u0000s6yda5bhv7sk";

		Long id1 = Math.abs((long) cuid1.hashCode());
		Long id2 = Math.abs((long) cuid2.hashCode());

		assertTrue(id1 > 0, "videoId 应为正数");
		assertTrue(id2 > 0, "videoId 应为正数");
		assertNotEquals(id1, id2, "不同 cuid 应生成不同 videoId");
	}

	/**
	 * 测试详情页 URL 构造
	 */
	@Test
	void testDetailUrlConstruction() {
		String cuid = "cmluv6vet0000s6xmjx49sqe1";
		String detailUrl = BASE_URL + "/v/" + cuid;
		assertEquals("https://rou.video/v/cmluv6vet0000s6xmjx49sqe1", detailUrl);
	}

	/**
	 * 测试标签分页 URL 构造
	 */
	@Test
	void testTagPageUrlConstruction() {
		String tag = "國產AV";
		String encodedTag = URLEncoder.encode(tag, StandardCharsets.UTF_8);
		String url = BASE_URL + "/t/" + encodedTag + "?order=createdAt&page=1";
		assertEquals("https://rou.video/t/%E5%9C%8B%E7%94%A2AV?order=createdAt&page=1", url);

		String tag2 = "自拍流出";
		String url2 = BASE_URL + "/t/" + URLEncoder.encode(tag2, StandardCharsets.UTF_8) + "?order=createdAt&page=3";
		assertEquals("https://rou.video/t/%E8%87%AA%E6%8B%8D%E6%B5%81%E5%87%BA?order=createdAt&page=3", url2);
	}

	/**
	 * 测试标签分页 __NEXT_DATA__ 解析（videos 路径）
	 */
	@Test
	void testTagPageVideosParsing() throws Exception {
		String sampleJson = """
				{
				  "props": {
				    "pageProps": {
				      "tag": "國產AV",
				      "tagZh": "国产AV",
				      "order": "createdAt",
				      "pageNum": 1,
				      "totalPage": 533,
				      "totalVideoNum": 13856,
				      "videos": [
				        {
				          "id": "test-video-001",
				          "vid": null,
				          "name": "測試視頻",
				          "nameZh": "测试视频",
				          "description": "some description",
				          "ref": "some-ref",
				          "published": true,
				          "publisher": "admin",
				          "tags": ["國產AV"],
				          "duration": 300.5,
				          "viewCount": 100,
				          "coverImageUrl": "https://v.rn204.xyz/m/test/cover.jpg",
				          "sources": [{"id": "s1", "videoId": "test-video-001", "resolution": 720, "folder": "test-video-001-720"}],
				          "createdAt": "2026-02-20T00:00:00.000Z"
				        }
				      ]
				    }
				  }
				}
				""";

		JsonNode root = OBJECT_MAPPER.readTree(sampleJson);

		// 验证 videos 路径（非 latestVideos）
		JsonNode videosNode = root.at("/props/pageProps/videos");
		assertFalse(videosNode.isMissingNode());
		assertEquals(1, videosNode.size());

		List<VideoRouParse> videos = OBJECT_MAPPER.convertValue(videosNode, new TypeReference<>() {
		});
		assertEquals(1, videos.size());
		assertEquals("test-video-001", videos.get(0).getId());
		assertEquals("测试视频", videos.get(0).getNameZh());

		// 验证 totalPage
		assertEquals(533, root.at("/props/pageProps/totalPage").asInt());

		// 验证未知字段（description, ref, published, publisher）不影响解析
		assertNotNull(videos.get(0).getSources());
	}

	/**
	 * 测试 Jackson 对未知字段的容忍（@JsonIgnoreProperties）
	 */
	@Test
	void testUnknownFieldsTolerance() throws Exception {
		String json = """
				{
				  "id": "test-id",
				  "name": "Test",
				  "unknownField1": "value",
				  "unknownField2": 42,
				  "anotherUnknown": {"nested": true}
				}
				""";

		VideoRouParse parse = OBJECT_MAPPER.readValue(json, VideoRouParse.class);
		assertEquals("test-id", parse.getId());
		assertEquals("Test", parse.getName());
		// 不应抛出异常
	}

	/**
	 * 测试 ev 字段解密算法
	 */
	@Test
	void testEvDecryption() throws Exception {
		// 使用真实的 ev 数据（来自 cmluj7v0u0000s6yda5bhv7sk 详情页）
		String d = "ijGFeHN0fmSBezFJMXeDg3+CST4+hT2BfUE/RD2HiIk+d3uCPnJ8e4R5RoU/hD8/Pz+CRYhzcERxd4VGgno+eH1zdIc9fEKER06FTEU1dId/TEBGRkBFQkRFPz81cISDd0yCQVZwhmhFUX5zdHFwWz9uZH9IfWBTSHR3c2d/ZkJCQHZCf3VXQ0J2hHVDMTsxg3eEfHFlY2NkgXsxSTF3g4N/gkk+PoU9gX1BP0Y9h4iJPnd7gj5yfHuEeUaFP4Q/Pz8/gkWIc3BEcXeFRoJ6PoN3hHxxgj6Dd4R8cX1weHs9hYODToVMRTV0h39MQEZGQEVCREU/PzVwhIN3TIJBVnCGaEVRfnN0cXBbP25kf0h9YFNIdHdzZ39mQkJAdkJ/dVdDQnaEdUMxjA==";
		int k = 15;

		byte[] decoded = Base64.getDecoder().decode(d);
		StringBuilder sb = new StringBuilder(decoded.length);
		for (byte b : decoded) {
			sb.append((char) ((b & 0xFF) - k));
		}
		String decrypted = sb.toString();

		// 验证解密结果是有效 JSON
		JsonNode videoInfo = OBJECT_MAPPER.readTree(decrypted);
		assertNotNull(videoInfo.get("videoUrl"), "解密结果应包含 videoUrl");

		String videoUrl = videoInfo.get("videoUrl").asText();
		assertTrue(videoUrl.contains(".m3u8"), "videoUrl 应包含 .m3u8");
		assertTrue(videoUrl.contains("auth="), "videoUrl 应包含 auth 签名");
		assertTrue(videoUrl.startsWith("https://"), "videoUrl 应以 https:// 开头");

		// 验证 thumbVTTUrl 也存在
		assertNotNull(videoInfo.get("thumbVTTUrl"), "解密结果应包含 thumbVTTUrl");
	}

	/**
	 * 测试 ev 解密 - 不同 k 值
	 */
	@Test
	void testEvDecryptionWithDifferentKey() throws Exception {
		// 手工构造: 原始 JSON → 每个字符 + key → Base64 编码
		String original = "{\"videoUrl\":\"https://example.com/test.m3u8?auth=abc123\"}";
		int key = 7;

		// 加密
		StringBuilder encrypted = new StringBuilder();
		for (char c : original.toCharArray()) {
			encrypted.append((char) (c + key));
		}
		String base64Encoded = Base64.getEncoder().encodeToString(encrypted.toString().getBytes("ISO-8859-1"));

		// 解密
		byte[] decoded = Base64.getDecoder().decode(base64Encoded);
		StringBuilder sb = new StringBuilder(decoded.length);
		for (byte b : decoded) {
			sb.append((char) ((b & 0xFF) - key));
		}
		String decrypted = sb.toString();

		assertEquals(original, decrypted);

		JsonNode videoInfo = OBJECT_MAPPER.readTree(decrypted);
		assertEquals("https://example.com/test.m3u8?auth=abc123", videoInfo.get("videoUrl").asText());
	}

	// === 辅助方法 ===

	private String formatDuration(Double seconds) {
		int totalSeconds = seconds.intValue();
		int minutes = totalSeconds / 60;
		int secs = totalSeconds % 60;
		if (minutes > 0) {
			return minutes + "分" + String.format("%02d", secs) + "秒";
		}
		return secs + "秒";
	}

}
