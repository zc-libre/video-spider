package com.libre.video.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "video")
public class VideoProperties {

    private String ffmpegPath;

    private String mp4boxPath;

    private String downloadPath;
}
