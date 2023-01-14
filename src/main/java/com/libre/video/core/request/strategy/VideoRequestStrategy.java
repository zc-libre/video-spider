package com.libre.video.core.request.strategy;

import com.libre.video.core.pojo.dto.VideoRequestParam;

import java.util.List;

public interface VideoRequestStrategy<P> {

	/**
	 * execute spider task
	 * @param requestParam spider website type
	 */
    void execute(VideoRequestParam requestParam);



	/**
	 * 解析页码
	 * @param body html
	 * @return 页码
	 */
	  Integer parsePageSize(String body);

	/**
	 * 读取所有视频
	 * @param pageSize 页数
	 */
	  void readVideoList(Integer pageSize);

	/**
	 * 分页解析
	 * @param html /
	 * @return /
	 */
	  List<P> parsePage(String html);


	/**
	 * 读取视频信息并存储
	 * @param parseList /
	 */
	  void readAndSave(List<P> parseList);
}
