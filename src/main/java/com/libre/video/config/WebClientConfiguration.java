package com.libre.video.config;

import com.libre.core.random.RandomHolder;
import com.libre.video.toolkit.UserAgentContext;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.awt.*;
import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

/**
 * @author: Libre
 * @Date: 2022/5/9 10:21 PM
 */
@Configuration(proxyBeanMethods = false)
public class WebClientConfiguration {

	@Bean
	public ReactorResourceFactory reactorResourceFactory() {
		ReactorResourceFactory reactorResourceFactory = new ReactorResourceFactory();
		reactorResourceFactory.setUseGlobalResources(false);
		reactorResourceFactory.setConnectionProvider(ConnectionProvider.create("reactive-pool", 1000));
		reactorResourceFactory.setLoopResources(LoopResources.create("reactive-loop",1000, true ));
		return reactorResourceFactory;
	}


	@Bean
	public WebClient webClient(ReactorResourceFactory reactorResourceFactory) {
		Function<HttpClient, HttpClient> mapper = client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.doOnConnected(conn -> conn
					.addHandlerLast(new ReadTimeoutHandler(100))
					.addHandlerLast(new WriteTimeoutHandler(100)))
				.responseTimeout(Duration.ofSeconds(100));

		ClientHttpConnector connector =
			new ReactorClientHttpConnector(reactorResourceFactory, mapper);

		return WebClient.builder()
			.clientConnector(connector)
			.defaultHeaders(httpHeaders -> httpHeaders.addAll(httpHeaders()))
			.build();
	}

	private MultiValueMap<String, String> httpHeaders() {
		Random r = RandomHolder.RANDOM;
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		headers.add(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent());
		headers.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5");
		headers.add("Connection", "keep-alive");
		headers.add("X-Forwarded-For", r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256));
		return headers;
	}
}
