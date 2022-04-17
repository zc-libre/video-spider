package com.libre.video.config;

import com.libre.video.core.download.VideoDownload;
import com.libre.boot.autoconfigure.SpringContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(VideoProperties.class)
public class VideoConfiguration {

    @Bean
    public SpringContext springContext() {
        return new SpringContext();
    }

    @Bean
    public VideoDownload videoDownload(VideoProperties properties) {
        return new VideoDownload(properties);
    }
}
