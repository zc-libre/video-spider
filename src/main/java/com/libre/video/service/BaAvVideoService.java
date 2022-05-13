package com.libre.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;

/**
 * @author: Libre
 * @Date: 2022/5/12 2:40 AM
 */
public interface BaAvVideoService extends IService<BaAvVideo> {

	Cursor<BaAvVideo> cursorList(int limit);

}
