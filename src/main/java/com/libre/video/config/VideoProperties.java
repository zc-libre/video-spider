package com.libre.video.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "video")
public class VideoProperties {

    private String ffmpegPath = "/usr/local/Cellar/ffmpeg/5.0.1/bin/";

    private String mp4boxPath = "/Applications/GPAC.app/Contents/MacOS/MP4Box/";

    private String downloadPath = "/Users/libre/video/";

    private Integer maxSaveLimit = 5;
}
