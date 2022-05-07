package com.libre.video.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * web配置
 *
 * @author L.cm
 */
@EnableCaching
@Configuration(proxyBeanMethods = false)
public class MicaFastWebConfig implements WebMvcConfigurer {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new PageArgumentResolver());

	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("*")
			.allowedMethods("*")
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600L);
	}

}
