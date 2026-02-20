package com.libre.video.core.spider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 肉视频 ev 字段解密器
 * <p>
 * 从详情页 __NEXT_DATA__ 的 ev 字段解密出带签名的 m3u8 URL。 供爬虫 Processor 和 watch 回源逻辑共享使用。
 */
@Slf4j
@Component
public class RouM3u8Resolver {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Pattern NEXT_DATA_PATTERN = Pattern
		.compile("<script\\s+id=\"__NEXT_DATA__\"[^>]*>([\\s\\S]*?)</script>");

	/**
	 * 请求详情页并解密出带签名的 m3u8 URL
	 */
	public String resolveM3u8Url(String detailPageUrl) {
		String html = WebClientUtils.requestHtml(detailPageUrl);
		if (StringUtil.isBlank(html)) {
			throw new LibreException("detail page is blank, url: " + detailPageUrl);
		}
		return resolveM3u8UrlFromHtml(html, detailPageUrl);
	}

	/**
	 * 从详情页 HTML 中解密出 m3u8 URL
	 */
	public String resolveM3u8UrlFromHtml(String html, String detailPageUrl) {
		Matcher matcher = NEXT_DATA_PATTERN.matcher(html);
		if (!matcher.find()) {
			throw new LibreException("__NEXT_DATA__ not found, url: " + detailPageUrl);
		}

		try {
			JsonNode root = OBJECT_MAPPER.readTree(matcher.group(1));
			JsonNode evNode = root.at("/props/pageProps/ev");
			if (evNode.isMissingNode()) {
				throw new LibreException("ev node not found, url: " + detailPageUrl);
			}

			String d = evNode.get("d").asText();
			int k = evNode.get("k").asInt();

			String decrypted = decryptEv(d, k);
			JsonNode videoInfo = OBJECT_MAPPER.readTree(decrypted);
			String videoUrl = videoInfo.get("videoUrl").asText();

			if (StringUtil.isBlank(videoUrl)) {
				throw new LibreException("videoUrl is blank after ev decryption, url: " + detailPageUrl);
			}
			return videoUrl;
		}
		catch (LibreException e) {
			throw e;
		}
		catch (Exception e) {
			throw new LibreException("ev decryption failed, url: " + detailPageUrl, e);
		}
	}

	/**
	 * 解密 ev 字段：Base64 解码 → 每个字符 charCode 减去 k → JSON 字符串
	 */
	private String decryptEv(String base64Data, int key) {
		byte[] decoded = Base64.getDecoder().decode(base64Data);
		StringBuilder sb = new StringBuilder(decoded.length);
		for (byte b : decoded) {
			sb.append((char) ((b & 0xFF) - key));
		}
		return sb.toString();
	}

}
