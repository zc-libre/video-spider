package com.libre.video.core.request.strategy;

import com.google.common.collect.Maps;
import com.libre.core.random.RandomHolder;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.core.event.VideoEventPublisher;
import com.libre.video.pojo.ErrorVideo;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.UserAgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
public abstract class AbstractVideoRequestStrategy<P> implements VideoRequestStrategy, InitializingBean {

	protected final VideoService videoService;
	protected final WebClient webClient;
	protected  MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
	private final Random r = RandomHolder.RANDOM;

	public AbstractVideoRequestStrategy(VideoService videoService, WebClient webClient) {
		this.videoService = videoService;
		this.webClient = webClient;
	}



	/**
	 * 解析页码
	 * @param body html
	 * @return 页码
	 */
	protected abstract Integer parsePageSize(String body);

	/**
	 * 读取所有视频
	 * @param pageSize 页数
	 */
	protected abstract void readVideoList(Integer pageSize);

	/**
	 * 分页解析
	 * @param html /
	 * @return /
	 */
	protected abstract List<P> parsePage(String html);

	/**
	 * 读取视频信息并存储
	 * @param parseList /
	 */
	protected abstract void readAndSave(List<P> parseList);


	protected static void publishErrorVideo(String url, String html, ErrorRequestType type) {
		ErrorVideo errorVideo = new ErrorVideo();
		errorVideo.setUrl(url);
		errorVideo.setType(type.getCode());
		errorVideo.setHtml(html);
		VideoEventPublisher.publishErrorEvent(errorVideo);
	}

	protected Mono<String> request(String url) {
		log.info("start request url: {}", url);
		return webClient.get()
			.uri(url)
			.headers(httpHeaders -> httpHeaders.addAll(headers))
			.header("X-Forwarded-For", r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256))
			.retrieve()
			.bodyToMono(String.class)
			.doOnError(e -> log.error("request error, url: {},message: {}", url, e.getMessage()))
			.retry(3);
	}

	protected String buildUrl(String urlTemplate, Map<String, Object> params) {
		DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
		return uriBuilderFactory.uriString(urlTemplate).build(params).toString();
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		headers.add(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent());
		headers.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5");
		headers.add("Connection", "keep-alive");
	}



//	protected InetSocketAddress getProxyAddress() {
//		String proxy = getProxy();
//		int index = proxy.indexOf(StringPool.COLON);
//		String ip = proxy.substring(0, index);
//		int port = Integer.parseInt(proxy.substring(index + 1));
//		return new InetSocketAddress(ip, port);
//	}

	//	protected String getProxy() {
//		Map<String, Object> proxyMap = HttpRequest.get("http://localhost:5010/get").execute().asMap(Object.class);
//		if (CollectionUtil.isNotEmpty(proxyMap)) {
//			String proxy = (String) proxyMap.get("proxy");
//			if (StringUtil.isNotBlank(proxy)) {
//				return proxy;
//			}
//		}
//		return getProxy();
//	}



}
