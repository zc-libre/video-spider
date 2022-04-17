package com.libre.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.pojo.Video;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface VideoService extends IService<Video> {

	@Async("videoRequestExecutor")
	void request(Integer requestType);
    /**
     * 下载视频
     * @param ids id集合
     */
    void download(List<Long> ids);

    /**
     * 下载视频
     */
    void download();

	List<Video> findByTitle(String title);
}
