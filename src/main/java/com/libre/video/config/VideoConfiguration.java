package com.libre.video.config;

import com.libre.boot.autoconfigure.SpringContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(VideoProperties.class)
@RequiredArgsConstructor
public class VideoConfiguration implements WebMvcConfigurer {

	private final VideoProperties videoProperties;
    @Bean
    public SpringContext springContext() {
        return new SpringContext();
    }


	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String downloadPath = videoProperties.getDownloadPath();
		registry.addResourceHandler(downloadPath + "**")
			.addResourceLocations("file:" + downloadPath);

	}

}
