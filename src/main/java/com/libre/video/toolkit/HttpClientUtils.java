package com.libre.video.toolkit;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import net.dreamlu.mica.http.LogLevel;
import net.dreamlu.mica.http.ResponseSpec;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author: Libre
 * @Date: 2023/5/14 12:58 AM
 */
@Slf4j
public class HttpClientUtils {

	public static String request(String url) {
		HttpRequest.setGlobalLog(LogLevel.BASIC);
		return HttpRequest.get(url).addHeader(WebClientUtils.headers.toSingleValueMap())
				.readTimeout(Duration.ofSeconds(30)).connectTimeout(Duration.ofSeconds(30))
				.writeTimeout(Duration.ofSeconds(30)).execute().onFailed((response, e) -> log.error("", e)).asString();
	}

	public static void asyncRequest(String url, Consumer<ResponseSpec> consumer) {
		HttpRequest.setGlobalLog(LogLevel.BASIC);
		HttpRequest.get(url).addHeader(WebClientUtils.headers.toSingleValueMap()).readTimeout(Duration.ofSeconds(30))
				.connectTimeout(Duration.ofSeconds(30)).writeTimeout(Duration.ofSeconds(30)).async()
				.onResponse(consumer);
	}

}
