package com.libre.video.core.download;

import com.libre.video.pojo.Video;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class VideoProgressListener implements ProgressListener {

	private double durationNs;

	private Video video;

	// public VideoProgressListener(Video video, Double duration, WebSocketServer
	// webSocketServer) {
	// this.durationNs = duration * Duration.ofSeconds(1).toNanos();
	// // this.webSocketServer = webSocketServer;
	// this.video = video;
	// }

	@Override
	public void progress(Progress progress) {
		double percentage = progress.out_time_ns / durationNs;
		// Print out interesting information about the progress
		log.info(String.format("[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx", percentage * 100,
				progress.status, progress.frame, FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
				progress.fps.doubleValue(), progress.speed));

		// notifyDownloadProgress(progress, percentage);

	}

	// private void notifyDownloadProgress(Progress progress, double percentage) {
	// if (Objects.nonNull(video)) {
	// VideoDownloadMessage message = new VideoDownloadMessage();
	// message.setVideoId(video.getId());
	// message.setPercentage(String.format("%.2f", percentage * 100));
	// message.setEnd(progress.isEnd());
	// message.setType(1);
	// message.setVideo(video);
	// try {
	// webSocketServer.sendInfo(message, SystemConstants.WEBSOCKET_ENDPOINT);
	// } catch (IOException e) {
	// log.error(e.getMessage());
	// }
	// }

}
