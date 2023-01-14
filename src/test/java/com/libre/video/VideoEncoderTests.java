package com.libre.video;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.libre.core.toolkit.StringPool;
import com.libre.video.config.VideoProperties;
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.MimeType;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: Libre
 * @Date: 2022/5/15 10:31 PM
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VideoEncoderTests {

	@Autowired
	WebClient webClient;
	@Autowired
	VideoProperties videoProperties;

	@Test
	void download() throws IOException {


	}

	private void compine(Set<String> lines) {
		List<File> files = Lists.newArrayList();

//		for (String s : tsSet) {
//			File tsFile = new File(videoProperties.getDownloadPath() + s);
//			if (tsFile.exists()) {
//				String name = tsFile.getName();
//				System.out.println(name);
//			}
//		}
	}
}
