package com.libre.video.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;
import java.util.function.Function;

/**
 * @author: Libre
 * @Date: 2022/5/9 10:21 PM
 */
@Configuration(proxyBeanMethods = false)
public class WebClientConfiguration {

	@Bean
	public ReactorResourceFactory reactorResourceFactory() {
		ConnectionProvider connectionProvider = ConnectionProvider.builder("reactive-pool").maxConnections(1000)
				.pendingAcquireMaxCount(2000).build();
		ReactorResourceFactory reactorResourceFactory = new ReactorResourceFactory();
		reactorResourceFactory.setUseGlobalResources(false);
		reactorResourceFactory.setConnectionProvider(connectionProvider);
		reactorResourceFactory.setLoopResources(LoopResources.create("reactive-loop"));
		return reactorResourceFactory;
	}

	@Bean
	public WebClient webClient(ReactorResourceFactory reactorResourceFactory) {

		Function<HttpClient, HttpClient> mapper = client -> client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 100000)
				.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10))
						.addHandlerLast(new WriteTimeoutHandler(10)))
				.proxyWithSystemProperties().responseTimeout(Duration.ofSeconds(10));

		ClientHttpConnector connector = new ReactorClientHttpConnector(reactorResourceFactory, mapper);

		return WebClient.builder().clientConnector(connector)
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build();
	}

}
