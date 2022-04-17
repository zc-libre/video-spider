package com.libre.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.video.pojo.Video91;

import java.util.List;

public interface VideoService extends IService<Video91> {

    /**
     * 下载视频
     * @param ids id集合
     */
    void download(List<Long> ids);

    /**
     * 下载视频
     */
    void download();

    /**
     * 创建索引
     */
    void createIndex();
}
