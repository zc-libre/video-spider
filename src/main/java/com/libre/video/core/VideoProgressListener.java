package com.libre.video.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

@Slf4j
@RequiredArgsConstructor
public class VideoProgressListener implements ProgressListener {

    private final String fileName;

    @Override
    public void progress(Progress progress) {
        log.info("{}大小为： {} kb", fileName, progress.total_size / 1024);
        if (Progress.Status.CONTINUE.equals(progress.status)) {
            log.info("{}正在下载, 速度： {}", fileName, progress.speed);
        }
    }
}