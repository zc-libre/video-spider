package com.libre.video.core.download;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class VideoProgressListener implements ProgressListener {

    private final String fileName;
	private final Double duration;
	private final double duration_ns;
	public VideoProgressListener(String fileName, Double duration) {
		this.fileName = fileName;
		this.duration = duration;
		this.duration_ns = duration * Duration.ofSeconds(1).toNanos();
	}

	@Override
    public void progress(Progress progress) {
		double percentage = progress.out_time_ns / duration_ns;
		// Print out interesting information about the progress
		log.info(String.format(
			"[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx",
			percentage * 100,
			progress.status,
			progress.frame,
			FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
			progress.fps.doubleValue(),
			progress.speed
		));
    }
}
