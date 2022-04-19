package com.libre.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.video.pojo.Video;
import com.libre.video.core.dto.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface VideoService extends IService<Video> {

	void request(RequestParam param);
    /**
     * 下载视频
     * @param ids id集合
     */
    void download(List<Long> ids);

	void dataSyncToElasticsearch();

	List<Video> findByTitle(String title);

	Page<Video> findByTitlePage(String title, Integer page, Integer size);
}
