package com.libre.video.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.video.core.event.VideoUploadEvent;
import com.libre.video.pojo.Video;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.dto.VideoQuery;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface VideoService extends IService<Video> {

	/**
	 * 同步数据至elasticsearch
	 */
	void syncToElasticsearch();

	/**
	 * 爬取视频（全量）
	 * @param type 请求类型
	 */
	@Async("videoRequestExecutor")
	void spider(Integer type);

	/**
	 * 爬取视频
	 * @param type 请求类型
	 * @param maxPages 最大爬取页数，null 表示全量爬取
	 */
	@Async("videoRequestExecutor")
	void spider(Integer type, Integer maxPages);

	/**
	 * 通过视频id下载视频
	 * @param ids id集合
	 */
	void download(List<Long> ids);

	void saveVideoToLocal(VideoUploadEvent videoUploadEvent);

	String saveVideoImageToOss(InputStream inputStream, String fileName);

	/**
	 * es分页查询视频
	 * @param page 分页参数
	 * @param videoQuery 查询参数
	 * @return list
	 */
	Page<Video> findByPage(PageDTO<Video> page, VideoQuery videoQuery);

	String watch(Long videoId) throws IOException;

	void shutdown();

}
