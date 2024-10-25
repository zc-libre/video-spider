package com.libre.video.toolkit;

import com.libre.boot.autoconfigure.SpringContext;
import com.libre.core.random.RandomHolder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Random;

/**
 * @author: Libre
 * @Date: 2023/1/15 6:39 PM
 */
@Slf4j
@UtilityClass
public class WebClientUtils {

	private static WebClient webClient;

	public static final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

	static {
		initialize();
	}

	private final Random r = RandomHolder.RANDOM;

	public static Map<String, String> getHeaders() {
		Map<String, String> headersMap = headers.toSingleValueMap();
		headersMap.put("X-Forwarded-For",
				r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256));
		return headersMap;

	}

	public static String requestHtml(String url) {
		return HttpRequest.get(url)
			.addHeader(headers.toSingleValueMap())
			.proxy("127.0.0.1", 7897)
			.addHeader("X-Forwarded-For",
					r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256))
			.execute()
			.asString();
	}

	public static Mono<String> request(String url) {
		log.debug("start request url: {}", url);
		return webClient.get()
			.uri(url)
			.headers(httpHeaders -> httpHeaders.addAll(headers))
			.header("X-Forwarded-For",
					r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256))
			.retrieve()
			.bodyToMono(String.class)
			.doOnError(e -> log.error("request error, url: {},message: {}", url, e.getMessage()))
			.retry(3);
	}

	public static String buildUrl(String urlTemplate, Map<String, Object> params) {
		DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
		return uriBuilderFactory.uriString(urlTemplate).build(params).toString();
	}

	public static void initialize() {
		webClient = SpringContext.getBean(WebClient.class);
		Assert.notNull(webClient, "webClient must not be null");
		headers.add(HttpHeaders.ACCEPT,
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		headers.add(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent());
		headers.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5");
		headers.add("Connection", "keep-alive");
	}

}
