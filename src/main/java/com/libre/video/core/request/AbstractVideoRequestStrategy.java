package com.libre.video.core.request;

import com.libre.core.random.RandomHolder;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.UserAgentContext;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public abstract class AbstractVideoRequestStrategy implements VideoRequestStrategy {

	@Autowired
	protected VideoService videoService;

	public abstract void execute(RequestTypeEnum requestTypeEnum);

	public abstract List<Video> readVideoList(String html);

	protected void readVideosAndSave(String html, String url) {
		try {
			List<Video> videos = readVideoList(html);
			VideoEventPublisher.publishVideoSaveEvent(videos);
		} catch (Exception e) {
			publishErrorVideo(url, html, ErrorRequestType.PARSE);
		}
	}

	protected static void publishErrorVideo(String url, ErrorRequestType type) {
		publishErrorVideo(url, null, type);
	}

	protected static void publishErrorVideo(String url, String html, ErrorRequestType type) {
		ErrorVideo errorVideo = new ErrorVideo();
		errorVideo.setUrl(url);
		errorVideo.setType(type.getCode());
		errorVideo.setHtml(html);
		VideoEventPublisher.publishErrorEvent(errorVideo);
	}

	protected static String requestAsHtml(String url) {
		log.info("start request url: {}", url);
		try {
			return httpRequest(url)
				.executeAsyncAndJoin()
				.asString();
		} catch (Exception e) {
			log.error("request error");
		}
		return null;
	}

	protected static HttpRequest httpRequest(String url) {
		Random r = RandomHolder.RANDOM;
//        String proxy = getProxy();
//        int index = proxy.indexOf(StringPool.COLON);
//        String ip = proxy.substring(0, index);
//        int port = Integer.parseInt(proxy.substring(index + 1));

		return HttpRequest.get(url)
			.setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
			.setHeader(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent())
			.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5")
			.setHeader("Connection", "keep-alive")
			.setHeader("X-Forwarded-For", r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256))
			//     .proxy(ip, port)
			.connectTimeout(Duration.ofSeconds(5))
			.readTimeout(Duration.ofSeconds(5));
	}

	protected static String getProxy() {
		Map<String, Object> proxyMap = HttpRequest.get("http://localhost:5010/get").execute().asMap(Object.class);
		if (CollectionUtil.isNotEmpty(proxyMap)) {
			String proxy = (String) proxyMap.get("proxy");
			if (StringUtil.isNotBlank(proxy)) {
				return proxy;
			}
		}
		return getProxy();
	}

}
