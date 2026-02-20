package com.libre.video.core.spider.processor;

import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.mapstruct.VideoRouMapping;
import com.libre.video.core.pojo.parse.VideoRouParse;
import com.libre.video.core.spider.RouM3u8Resolver;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.pojo.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 肉视频爬虫 Processor
 * <p>
 * 请求详情页，从 __NEXT_DATA__ 的 ev 字段解密出带签名的 m3u8 URL。
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_ROU, step = VideoStepType.PROCESSOR)
public class VideoRouSpiderProcessor extends AbstractVideoProcessor<VideoRouParse> {

	private static final String BASE_URL = RequestTypeEnum.REQUEST_ROU.getBaseUrl();

	private final RouM3u8Resolver rouM3u8Resolver;

	protected VideoRouSpiderProcessor(M3u8Download m3u8Download, RouM3u8Resolver rouM3u8Resolver) {
		super(m3u8Download);
		this.rouM3u8Resolver = rouM3u8Resolver;
	}

	@Override
	protected Video doProcess(VideoRouParse parse) throws Exception {
		if (StringUtil.isBlank(parse.getId())) {
			throw new LibreException("video id is blank");
		}

		String detailUrl = BASE_URL + "/v/" + parse.getId();

		// 请求详情页，从 ev 字段解密出 m3u8 URL
		String m3u8Url = rouM3u8Resolver.resolveM3u8Url(detailUrl);

		Long videoId = Math.abs((long) parse.getId().hashCode());

		VideoRouMapping mapping = VideoRouMapping.INSTANCE;
		Video video = mapping.sourceToTarget(parse);
		video.setVideoId(videoId);
		video.setUrl(detailUrl);
		video.setRealUrl(m3u8Url);

		// nameZh 为空时 fallback 到 name
		if (StringUtil.isBlank(video.getTitle()) && StringUtil.isNotBlank(parse.getName())) {
			video.setTitle(parse.getName());
		}

		// duration 秒数转为 "X分XX秒" 格式
		if (parse.getDuration() != null) {
			video.setDuration(formatDuration(parse.getDuration()));
		}

		if (StringUtil.isNotBlank(video.getTitle())) {
			video.setTitle(video.getTitle().strip());
		}

		return video;
	}

	@Override
	public RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_ROU;
	}

	/**
	 * 秒数转为 "X分XX秒" 格式
	 */
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
