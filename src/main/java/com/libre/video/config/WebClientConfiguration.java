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
		ConnectionProvider connectionProvider = ConnectionProvider.builder("reactive-pool")
			.maxConnections(1000)
			.pendingAcquireMaxCount(2000)
			.build();
		ReactorResourceFactory reactorResourceFactory = new ReactorResourceFactory();
		reactorResourceFactory.setUseGlobalResources(false);
		reactorResourceFactory.setConnectionProvider(connectionProvider);
		reactorResourceFactory.setLoopResources(LoopResources.create("reactive-loop"));
		return reactorResourceFactory;
	}


	@Bean
	public WebClient webClient(ReactorResourceFactory reactorResourceFactory) {

		Function<HttpClient, HttpClient> mapper = client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100000)
			.doOnConnected(conn -> conn
				.addHandlerLast(new ReadTimeoutHandler(10))
				.addHandlerLast(new WriteTimeoutHandler(10)))
			.proxyWithSystemProperties()
			.responseTimeout(Duration.ofSeconds(10));

		ClientHttpConnector connector = new ReactorClientHttpConnector(reactorResourceFactory, mapper);

		return WebClient.builder()
			.clientConnector(connector)
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
			.defaultHeaders(httpHeaders -> httpHeaders.addAll(httpHeaders()))
			.build();
	}

	private MultiValueMap<String, String> httpHeaders() {
		Random r = RandomHolder.RANDOM;
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		headers.add(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent());
		headers.add(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9,en;q=0.8");
		headers.add("Connection", "keep-alive");
		headers.add("X-Forwarded-For", r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256));
		return headers;
	}
}
