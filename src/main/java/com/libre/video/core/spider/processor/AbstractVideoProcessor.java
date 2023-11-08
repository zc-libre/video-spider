package com.libre.video.core.spider.processor;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.pojo.parse.VideoParse;
import com.libre.video.pojo.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

/**
 * @author: Libre
 * @Date: 2023/1/14 11:11 PM
 */
@Slf4j
public abstract class AbstractVideoProcessor<P extends VideoParse> implements VideoSpiderProcessor<VideoParse> {

	protected final M3u8Download m3u8Download;

	protected AbstractVideoProcessor(M3u8Download m3u8Download) {
		this.m3u8Download = m3u8Download;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Video process(@NonNull VideoParse item) throws Exception {
		Video video = this.doProcess((P) item);
		video.setId(IdWorker.getId());
		video.setVideoWebsite(getRequestType().getType());
		m3u8Download.downloadAndReadM3u8File(video);
		return video;
	}

	abstract protected Video doProcess(P parse) throws Exception;

}
