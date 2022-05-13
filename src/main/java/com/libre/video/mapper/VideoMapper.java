package com.libre.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.libre.video.pojo.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {

	@Select("SELECT id, image, duration, title, author, look_num, url, real_url, collect_num, publish_time, create_time, update_time FROM video LIMIT #{limit}")
    Cursor<Video> cursorList(int limit);
}
