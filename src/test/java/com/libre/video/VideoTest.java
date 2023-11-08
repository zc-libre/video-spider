package com.libre.video;

import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringPool;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.VideoFileUtils;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class VideoTest {

	@Test
	void test1() throws IOException {
		Stream<Path> list = Files.list(Paths.get("/Users/libre/video/1609239160856510466"));

		List<String> fileList = list.map(p -> p.toAbsolutePath().toString()).sorted((o1, o2) -> {
			try {
				String id1 = o1.substring(o1.lastIndexOf('/') + 1, o1.lastIndexOf("."));
				String id2 = o2.substring(o1.lastIndexOf('/') + 1, o2.lastIndexOf("."));
				if (StringUtils.isNotBlank(id1) && StringUtils.isNotBlank(id2)
						&& Integer.parseInt(id1) - Integer.parseInt(id2) < 0) {
					return -1;
				}
				else {
					return 1;
				}
			}
			catch (Exception e) {
				// throw new RuntimeException(e);
			}
			return 0;
		}).collect(Collectors.toList());

		boolean b = VideoFileUtils.mergeFiles(fileList.toArray(new String[0]), "/Users/libre/video/2");
		Video video = new Video();
		video.setVideoPath("/Users/libre/video/2");
		video.setTitle("2");
		encodeAndWrite(video);
	}

	public static void encodeAndWrite(Video video) {
		try {
			FFmpeg ffmpeg = new FFmpeg("/usr/local/Cellar/ffmpeg/5.0.1/bin/" + "ffmpeg");
			FFprobe ffprobe = new FFprobe("/usr/local/Cellar/ffmpeg/5.0.1/bin/" + "ffprobe");
			// 时长 s
			String videoPath = video.getVideoPath();
			FFmpegProbeResult in = ffprobe.probe(videoPath);
			// 封面信息保存
			FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
			// 整体下载
			String path = getDownPath(video.getTitle());

			FFmpegOutputBuilder fFmpegOutputBuilder = getFfmpegOutputBuilder(videoPath, path);
			FFmpegBuilder builder = fFmpegOutputBuilder.done();
			FFmpegFormat info = in.getFormat();
			FFmpegJob fFmpegJob = executor.createJob(builder);
			fFmpegJob.run();
		}
		catch (Exception e) {
			throw new LibreException(e.getMessage());
		}
	}

	private static String getDownPath(String filename) {
		return "/Users/libre/video/" + filename + StringPool.DOT + "mp4";
	}

	private static FFmpegOutputBuilder getFfmpegOutputBuilder(String url, String path) {
		return new FFmpegBuilder().overrideOutputFiles(true).setInput(url).addOutput(path).setFormat("mp4")
				.addExtraArgs("-allowed_extensions", "ALL", "-c", "copy", "-threads",
						String.valueOf(Runtime.getRuntime().availableProcessors() * 2), "-preset", "ultrafast");
		// .addExtraArgs("-c:v", "libx264", "-crf", "22", "-threads", "4");

	}

}
