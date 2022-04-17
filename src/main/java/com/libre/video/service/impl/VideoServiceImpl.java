package com.libre.video.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.request.VideoRequestContext;
import com.libre.video.core.request.VideoRequestStrategy;
import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.mapper.VideoMapper;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

	private final VideoRequestContext videoRequestContext;
    private final VideoDownload videoDownload;
	private final VideoEsRepository videoEsRepository;

	@Override
	public void request(Integer requestType) {
		RequestTypeEnum requestTypeEnum = RequestTypeEnum.find(requestType);
		Assert.notNull(requestTypeEnum, "request type must not be null");

		log.info("start request type: {}, baseUrl: {}", requestTypeEnum.name(), requestTypeEnum.getBaseUrl());

		VideoRequestStrategy requestStrategy = videoRequestContext.getRequestStrategy(requestTypeEnum.getType());
		requestStrategy.execute(requestTypeEnum);
	}

	@Override
    public void download(List<Long> ids) {
        List<Video> videoList = this.listByIds(ids);
        if (CollectionUtil.isEmpty(videoList)) {
            return;
        }
        for (Video video : videoList) {
            try {
                videoDownload.encodeAndWrite(video.getRealUrl(), video.getTitle());
            } catch (IOException e) {
                log.error("download error: id: {}, title: {}", video.getId(), video.getTitle());
            }
        }
    }

    @Override
    public void download() {
        List<Video> list = this.list(Wrappers.<Video>lambdaQuery().orderByAsc(Video::getId));
        for (Video video : list) {
            try {
                videoDownload.encodeAndWrite(video.getRealUrl(), video.getTitle());
            } catch (IOException e) {
                log.error("download error: id: {}, title: {}", video.getId(), video.getTitle());
            }

        }
    }

	@Override
	public List<Video> findByTitle(String title) {
		return videoEsRepository.findVideosByTitle(title);
	}
}
