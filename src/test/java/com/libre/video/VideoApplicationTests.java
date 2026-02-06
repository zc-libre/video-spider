package com.libre.video;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.libre.spider.DomMapper;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.download.VideoEncoder;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.VideoFileUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.data.elasticsearch.client.erhlc.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@SpringBootTest
class VideoApplicationTests {

	@Autowired
	VideoEncoder download;

	@Autowired
	M3u8Download m3u8Download;

	// @Autowired
	// VideoEsRepository videoEsRepository;

	@Autowired
	VideoService videoService;

	@Autowired
	ElasticsearchOperations elasticsearchRestTemplate;

	@Autowired
	WebClient webClient;

	@Test
	void request() {
		String block = webClient.get().uri("https://www.baav.xyz/list/355-6.html").retrieve().bodyToMono(String.class)
				.block();
		List<VideoBaAvParse> videoBaAvParses = DomMapper.readList(block, VideoBaAvParse.class);

	}

	@Test
	void test1() throws FileNotFoundException {
		File file = new File("/Users/libre/Desktop/index.m3u8");
		FileInputStream inputStream = new FileInputStream(file);
		Video video = new Video();
		video.setId(IdWorker.getId());
		video.setVideoWebsite(RequestTypeEnum.REQUEST_9S.getType());
		video.setRealUrl("https://v.bigcloud.cyou/hls/685304/index.m3u8");
		m3u8Download.downloadVideoToLocal(inputStream, video);
	}

	@Test
	void remove() {
		Collection<File> files = FileUtils.listFiles(new File("/Users/libre/video/1538566051627876354"), null, false);
		boolean b = VideoFileUtils.mergeFiles(files.stream().map(File::getAbsolutePath).toArray(String[]::new),
				"/Users/libre/video");

	}

	@Test
	void copy() {
	}

	// @Test
	// void contextLoads() throws IOException {
	// //
	// download.encodeAndWrite("https://cdn2.jiuse.cloud/hls/627157/index.m3u8?t=1650123558&m=jgg3XW16UHq0mw0Tjcib0g",
	// "123");
	// }

	@Test
	void createIndex() {
		IndexCoordinates indexCoordinates = IndexCoordinates.of("video");
		elasticsearchRestTemplate.indexOps(indexCoordinates).getSettings().put("max_result_window", 100000);
	}

}
