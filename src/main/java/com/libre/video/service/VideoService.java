package com.libre.video.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.video.core.event.VideoUploadEvent;
import com.libre.video.pojo.Video;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.dto.VideoQuery;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface VideoService extends IService<Video> {

	/**
	 * 爬取视频
	 * @param param /
	 */
	@Async("videoRequestExecutor")
	void request(VideoRequestParam param);

	/**
	 * 同步数据至elasticsearch
	 */
	void syncToElasticsearch();

    /**
     * 通过视频id下载视频
     * @param ids id集合
     */
    void download(List<Long> ids);

	/**
	 * 下载视频并存储至oss
	 * @param videoUploadEvent video
	 */
	void saveVideoToOss(VideoUploadEvent videoUploadEvent);


	void saveVideoToLocal(VideoUploadEvent videoUploadEvent);
	/**
	 * es分页查询视频
	 * @param page 分页参数
	 * @param videoQuery 查询参数
	 * @return list
	 */
	Page<Video> findByPage(PageDTO<Video> page, VideoQuery videoQuery);


    void watch(Long videoId);

    void shutdown();

}
