package com.libre.video.core.download;

import com.libre.core.exception.LibreException;
import com.libre.video.config.VideoProperties;
import com.libre.core.toolkit.StringPool;
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

import java.io.IOException;
import java.time.Duration;


@Slf4j
@RequiredArgsConstructor
public class VideoDownload {
    private final VideoProperties properties;
    private final static String MP4_FORMAT = "mp4";

    @Async("downloadExecutor")
    public void encodeAndWrite(String url, String filename)  {
		try {
			FFmpeg ffmpeg = new FFmpeg(properties.getFfmpegPath() + "ffmpeg");
			FFprobe ffprobe = new FFprobe(properties.getFfmpegPath() + "ffprobe");
			//时长 s
			FFmpegProbeResult in = ffprobe.probe(url);
			//封面信息保存
			FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
			//整体下载
			String path = getDownPath(filename);

			FFmpegOutputBuilder fFmpegOutputBuilder = getFfmpegOutputBuilder(url, path);
			FFmpegBuilder builder;
			FFmpegFormat info = in.getFormat();
			double duration = info.duration;
			if (duration > Duration.ofMinutes(4).getSeconds()) {
				//大于4分钟截取前10秒
				builder = fFmpegOutputBuilder.addExtraArgs("-ss", "00:00:10").done();
			} else {
				builder = fFmpegOutputBuilder.done();
			}
			FFmpegJob fFmpegJob = executor.createJob(builder, new VideoProgressListener(filename, duration));
			fFmpegJob.run();
		} catch (IOException e) {
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
