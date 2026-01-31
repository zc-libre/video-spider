package com.libre.video.core.download;

import com.libre.core.exception.LibreException;
import com.libre.video.config.VideoProperties;
import com.libre.core.toolkit.StringPool;
import com.libre.video.constant.SystemConstants;
import com.libre.video.core.websocker.VideoDownloadMessage;
import com.libre.video.mapper.VideoMapper;
import com.libre.video.pojo.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class VideoEncoder {

	private final VideoProperties properties;

	private final VideoMapper videoMapper;

	private final static String MP4_FORMAT = "mp4";

	@Async("downloadExecutor")
	public void encodeAndWrite(Long videoId) {
		try {
			Video video = Optional.ofNullable(videoMapper.selectById(videoId))
					.orElseThrow(() -> new LibreException(String.format("视频不存在,videoId: %d", videoId)));
			this.encodeAndWrite(video);
		}
		catch (Exception e) {
			VideoDownloadMessage message = new VideoDownloadMessage();
			message.setVideoId(videoId);
			message.setMessage(e.getMessage());
			// message.setType(2);
			// try {
			// webSocketServer.sendInfo(message, SystemConstants.WEBSOCKET_ENDPOINT);
			// }
			// catch (IOException ex) {
			// log.error(ex.getMessage());
			// }
		}
	}

	public void encodeAndWrite(Video video) {
		try {
			FFmpeg ffmpeg = new FFmpeg(properties.getFfmpegPath() + "ffmpeg");
			FFprobe ffprobe = new FFprobe(properties.getFfmpegPath() + "ffprobe");
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
			throw new LibreException(e);
		}
	}

	private String getDownPath(String filename) {
		return properties.getDownloadPath() + filename + StringPool.DOT + MP4_FORMAT;
	}

	private FFmpegOutputBuilder getFfmpegOutputBuilder(String url, String path) {
		return new FFmpegBuilder().overrideOutputFiles(true).setInput(url).addOutput(path).setFormat(MP4_FORMAT)
				.addExtraArgs("-allowed_extensions", "ALL", "-c", "copy", "-threads",
						String.valueOf(Runtime.getRuntime().availableProcessors() * 2), "-preset", "ultrafast");
		// .addExtraArgs("-c:v", "libx264", "-crf", "22", "-threads", "4");

	}

}
