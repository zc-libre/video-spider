package com.libre.video.core.spider.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.mapstruct.VideoHeiliaoMapping;
import com.libre.video.core.pojo.parse.VideoHeiliaoParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 黑料网视频爬虫 Processor
 * <p>
 * 从详情页 HTML 中提取 DPlayer 的 config JSON 属性，解析出 M3U8 视频地址。
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_HEILIAO, step = VideoStepType.PROCESSOR)
public class VideoHeiliaoSpiderProcessor extends AbstractVideoProcessor<VideoHeiliaoParse> {

	private static final String BASE_URL = RequestTypeEnum.REQUEST_HEILIAO.getBaseUrl();

	/**
	 * 匹配 DPlayer config 属性中的 JSON 内容。
	 * HTML 格式: config='{"live":false,...,"video":{"url":"..."}}'
	 */
	private static final Pattern CONFIG_PATTERN = Pattern.compile(
			"config='(\\{.*?\"video\":\\{.*?\\}.*?\\})'", Pattern.DOTALL);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	protected VideoHeiliaoSpiderProcessor(M3u8Download m3u8Download) {
		super(m3u8Download);
	}

	@Override
	protected Video doProcess(VideoHeiliaoParse parse) throws Exception {
		String detailPath = parse.getUrl();
		if (StringUtil.isBlank(detailPath)) {
			throw new LibreException("url is blank");
		}

		String detailUrl = BASE_URL + detailPath;
		String html = WebClientUtils.requestHtml(detailUrl);
		if (StringUtil.isBlank(html)) {
			throw new LibreException("detail page is blank, url: " + detailUrl);
		}

		// 从 HTML 中提取并解析 DPlayer config JSON
		JsonNode videoNode = parseVideoConfig(html);
		if (videoNode == null) {
			throw new LibreException("dplayer config not found, url: " + detailUrl);
		}

		String m3u8Url = getNodeText(videoNode, "/url");
		if (StringUtil.isBlank(m3u8Url)) {
			throw new LibreException("m3u8 url not found, url: " + detailUrl);
		}

		String imageUrl = getNodeText(videoNode, "/pic");
		Long videoId = parseVideoId(detailPath);

		VideoHeiliaoMapping mapping = VideoHeiliaoMapping.INSTANCE;
		Video video = mapping.sourceToTarget(parse);
		video.setVideoId(videoId);
		video.setUrl(detailUrl);
		video.setRealUrl(m3u8Url);
		if (StringUtil.isNotBlank(imageUrl)) {
			video.setImage(imageUrl);
		}
		if (StringUtil.isNotBlank(video.getTitle())) {
			video.setTitle(video.getTitle().strip());
		}

		return video;
	}

	@Override
	public RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_HEILIAO;
	}

	/**
	 * 从 HTML 中提取 DPlayer config JSON 并返回 video 节点
	 */
	private JsonNode parseVideoConfig(String html) {
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

	private String getNodeText(JsonNode parent, String path) {
		JsonNode node = parent.at(path);
		return (node != null && !node.isMissingNode()) ? node.asText() : null;
	}

	/**
	 * 从详情页路径中提取视频 ID。路径格式: /archives/91289/
	 */
	private Long parseVideoId(String path) {
		try {
			String idStr = path.replaceAll("[^0-9]", "");
			return Long.parseLong(idStr);
		}
		catch (NumberFormatException e) {
			throw new LibreException("parse video id error, path: " + path);
		}
	}

}
