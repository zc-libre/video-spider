package com.libre.video.core.spider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 黑料网 DPlayer config 解析器
 * <p>
 * 从详情页 HTML 中提取 DPlayer config JSON，解析出 m3u8 视频地址。
 * 供爬虫 Processor 和 watch 回源逻辑共享使用。
 */
@Slf4j
@Component
public class HeiliaoM3u8Resolver {

	/**
	 * 匹配 DPlayer config 属性中的 JSON 内容。
	 * HTML 格式: config='{"live":false,...,"video":{"url":"..."}}'
	 */
	private static final Pattern CONFIG_PATTERN = Pattern.compile(
			"config='(\\{.*?\"video\":\\{.*?\\}.*?\\})'", Pattern.DOTALL);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * 请求详情页并解析出带新鲜 auth_key 的 m3u8 URL
	 */
	public String resolveM3u8Url(String detailPageUrl) {
		String html = WebClientUtils.requestHtml(detailPageUrl);
		if (StringUtil.isBlank(html)) {
			throw new LibreException("detail page is blank, url: " + detailPageUrl);
		}

		JsonNode videoNode = parseVideoConfig(html);
		if (videoNode == null) {
			throw new LibreException("dplayer config not found, url: " + detailPageUrl);
		}

		String m3u8Url = getNodeText(videoNode, "/url");
		if (StringUtil.isBlank(m3u8Url)) {
			throw new LibreException("m3u8 url not found, url: " + detailPageUrl);
		}
		return m3u8Url;
	}

	/**
	 * 从 HTML 中提取 DPlayer config JSON 并返回 video 节点
	 */
	public JsonNode parseVideoConfig(String html) {
		Matcher matcher = CONFIG_PATTERN.matcher(html);
		if (!matcher.find()) {
			return null;
		}
		try {
			JsonNode root = OBJECT_MAPPER.readTree(matcher.group(1));
			JsonNode videoNode = root.get("video");
			return (videoNode != null && !videoNode.isMissingNode()) ? videoNode : null;
		}
		catch (Exception e) {
			log.error("解析 DPlayer config JSON 失败", e);
			return null;
		}
	}

	public String getNodeText(JsonNode parent, String path) {
		JsonNode node = parent.at(path);
		return (node != null && !node.isMissingNode()) ? node.asText() : null;
	}

}
