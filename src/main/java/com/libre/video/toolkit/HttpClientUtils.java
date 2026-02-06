package com.libre.video.toolkit;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import net.dreamlu.mica.http.LogLevel;
import net.dreamlu.mica.http.ResponseSpec;
import okhttp3.Cookie;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author: Libre
 * @Date: 2023/5/14 12:58 AM
 */
@Slf4j
public class HttpClientUtils {

	public static String get(String url) {
		HttpRequest.setGlobalLog(LogLevel.BASIC);

		return HttpRequest.get(url)
			.addHeader(WebClientUtils.getHeaders())
			.readTimeout(Duration.ofSeconds(30))
			.connectTimeout(Duration.ofSeconds(30))
			.writeTimeout(Duration.ofSeconds(30))
			.disableSslValidation()
			// .proxy("127.0.0.1", 7897)
			.execute()
			.onFailed((response, e) -> log.error("", e))
			.asString();
	}

	public static void asyncRequest(String url, Consumer<ResponseSpec> consumer) {
		HttpRequest.setGlobalLog(LogLevel.BASIC);
		HttpRequest.get(url)
			.addHeader(WebClientUtils.headers.toSingleValueMap())
			.readTimeout(Duration.ofSeconds(30))
			.connectTimeout(Duration.ofSeconds(30))
			.writeTimeout(Duration.ofSeconds(30))
			.async()
			.onResponse(consumer);
	}

	public static String get(String url, Map<String, String> headers) {
		HttpRequest.setGlobalLog(LogLevel.BODY);

		return HttpRequest.get(url)
			.addHeader(headers)
			.readTimeout(Duration.ofSeconds(30))
			.connectTimeout(Duration.ofSeconds(30))
			.writeTimeout(Duration.ofSeconds(30))
			.disableSslValidation()
			.execute()
			.onFailed((response, e) -> log.error("error:", e))
			.asString();
	}

	public static String post(String url, Map<String, String> headers, String body) {
		HttpRequest.setGlobalLog(LogLevel.BODY);
		return HttpRequest.post(url)
			.addHeader(headers)
			.readTimeout(Duration.ofSeconds(30))
			.connectTimeout(Duration.ofSeconds(30))
			.writeTimeout(Duration.ofSeconds(30))
			// .addCookie(Cookie.)
			.bodyString(body)
			.execute()
			.onFailed((response, e) -> log.error("error:", e))
			.asString();
	}

}
