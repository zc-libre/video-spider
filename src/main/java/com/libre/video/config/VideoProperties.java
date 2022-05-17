package com.libre.video.config;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


@Data
@ConfigurationProperties(prefix = "video")
public class VideoProperties {

    private String ffmpegPath;

    private String mp4boxPath;

    private String downloadPath;

}
