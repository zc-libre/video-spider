package com.libre.video.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.video.pojo.Video;
import com.libre.video.core.dto.VideoRequestParam;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface VideoService extends IService<Video> {

	@Async("videoRequestExecutor")
	void request(VideoRequestParam param);
    /**
     * 下载视频
     * @param ids id集合
     */
    void download(List<Long> ids);

	void dataSyncToElasticsearch();

	void requestAndDownload(String url, Long id);

	Page<Video> findByPage(PageDTO<Video> page);

	List<Video> findByTitle(String title);

	Page<Video> findByTitlePage(String title, PageDTO<Video> page);
}
