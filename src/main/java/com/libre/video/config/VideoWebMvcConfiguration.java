package com.libre.video.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * @author: Libre
 * @Date: 2023/1/14 12:54 AM
 */
@EnableWebMvc
@Configuration
@RequiredArgsConstructor
public class VideoWebMvcConfiguration implements WebMvcConfigurer {

	private final VideoProperties videoProperties;

	private final AuthInterceptor authInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor).addPathPatterns("/api/**", "/video/**")
				.excludePathPatterns("/video/user/login", "/file/**", "/api/video/ts-proxy");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String path = videoProperties.getDownloadPath();
		String pathUtl = "file:" + path.replace("\\", "/");
		registry.addResourceHandler("/file/**").addResourceLocations(pathUtl).setCachePeriod(0);
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOriginPatterns("*").allowedMethods("PUT", "DELETE", "GET", "POST", "OPTIONS")
				.allowedHeaders("*").exposedHeaders("access-control-allow-headers", "access-control-allow-methods",
						"access-control-allow-origin", "access-control-max-age", "X-Frame-Options")
				.maxAge(3600);
	}

}
