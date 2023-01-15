package com.libre.video.core.spider.processor;

import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.mapstruct.VideoBaAvMapping;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.core.spider.VideoRequest;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.RegexUtil;
import com.libre.video.toolkit.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author: Libre
 * @Date: 2023/1/15 9:07 PM
 */
@Slf4j
@Component
@VideoRequest(value = RequestTypeEnum.REQUEST_BA_AV, step = VideoStepType.PROCESSOR)
public class VideoBaAvSpiderProcessor extends AbstractVideoProcessor<VideoBaAvParse> {

	private final String baseUrl;

	protected VideoBaAvSpiderProcessor(M3u8Download m3u8Download) {
		super(m3u8Download);
		this.baseUrl = getRequestType().getBaseUrl();
	}

	@Override
	protected Video doProcess(VideoBaAvParse parse) throws Exception {
		String url = parse.getUrl();
		String realRequestUrl = baseUrl + StringPool.SLASH + "embed" + url;
		Mono<String> request = WebClientUtils.request(realRequestUrl);
		String body = request.block();
		String realUrl = RegexUtil.matchM3u8Url(body);
		VideoBaAvMapping mapping = VideoBaAvMapping.INSTANCE;
		BaAvVideo baAvVideo = mapping.sourceToTarget(parse);
		Long id = parseId(url);
		baAvVideo.setVideoId(id);
		baAvVideo.setUrl(baseUrl + url);
		baAvVideo.setRealUrl(realUrl);
		String image = baAvVideo.getImage();
		baAvVideo.setImage(image);
		VideoBaAvMapping videoBaAvMapping = VideoBaAvMapping.INSTANCE;
		return videoBaAvMapping.convertToVide(baAvVideo);
	}

	@Override
	public RequestTypeEnum getRequestType() {
		return RequestTypeEnum.REQUEST_BA_AV;
	}

	private Long parseId(String url) {
		int start = url.indexOf(StringPool.SLASH) + 1;
		int end = url.indexOf(".html");
		String idStr = url.substring(start, end);
		if (StringUtil.isBlank(idStr)) {
			throw new LibreException("id parse error");
		}
		return Long.parseLong(idStr);
	}

}
