package com.libre.video.core.download;

import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.config.VideoProperties;
import com.libre.core.toolkit.StringPool;
import com.libre.video.constant.SystemConstants;
import com.libre.video.core.websocker.VideoDownloadMessage;
import com.libre.video.core.websocker.WebSocketServer;
import com.libre.video.mapper.VideoEsRepository;
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
import java.time.Duration;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Component
public class VideoDownload {
    private final VideoProperties properties;
	private final VideoEsRepository videoEsRepository;
	private final WebSocketServer webSocketServer;
    private final static String MP4_FORMAT = "mp4";

	@Async("downloadExecutor")
	public void encodeAndWrite(Long videoId) {
		try {
			Optional<Video> optional = videoEsRepository.findById(videoId);
			optional.ifPresent(this::encodeAndWrite);
		} catch (Exception e) {
			VideoDownloadMessage message = new VideoDownloadMessage();
			message.setVideoId(videoId);
			message.setMessage(e.getMessage());
			message.setType(2);
			try {
				webSocketServer.sendInfo(message, SystemConstants.WEBSOCKET_ENDPOINT);
			} catch (IOException ex) {
				log.error(ex.getMessage());
			}
		}
	}

    public void encodeAndWrite(Video video)  {
		try {
			String ffmpegPath;
			if (StringUtil.isBlank(properties.getFfmpegPath())) {
				ffmpegPath = "ffmpeg";
			} else {
				 ffmpegPath = properties.getFfmpegPath() + "ffmpeg";
			}
			FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
			FFprobe ffprobe = new FFprobe(properties.getFfmpegPath() + "ffprobe");
			//时长 s
			FFmpegProbeResult in = ffprobe.probe(video.getRealUrl());
			//封面信息保存
			FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
			//整体下载
			String path = getDownPath(video.getTitle());

			FFmpegOutputBuilder fFmpegOutputBuilder = getFfmpegOutputBuilder(video.getRealUrl(), path);
			FFmpegBuilder builder;
			FFmpegFormat info = in.getFormat();
			double duration = info.duration;
			if (duration > Duration.ofMinutes(4).getSeconds()) {
				//大于4分钟截取前10秒
				builder = fFmpegOutputBuilder.addExtraArgs("-ss", "00:00:10").done();
			} else {
				builder = fFmpegOutputBuilder.done();
			}
			FFmpegJob fFmpegJob = executor.createJob(builder, new VideoProgressListener(video, duration, webSocketServer));
			fFmpegJob.run();
		} catch (Exception e) {
			throw new LibreException(e.getMessage());
		}
	}

    private String getDownPath(String filename) {
        return properties.getDownloadPath() + filename + StringPool.DOT + MP4_FORMAT;
    }

    private FFmpegOutputBuilder getFfmpegOutputBuilder(String url, String path) {
        return new FFmpegBuilder()
                .overrideOutputFiles(true)
                .setInput(url)
                .addOutput(path)
                .setFormat(MP4_FORMAT)
                .addExtraArgs("-c:v", "libx264", "-crf", "22", "-threads", "4");
    }
}
