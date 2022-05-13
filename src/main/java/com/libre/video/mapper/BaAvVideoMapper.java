package com.libre.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.libre.video.pojo.BaAvVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;

/**
 * @author: Libre
 * @Date: 2022/5/12 2:41 AM
 */
@Mapper
public interface BaAvVideoMapper extends BaseMapper<BaAvVideo> {

	@Select("SELECT id, image, duration, title, author, look_num, url, real_url, collect_num, publish_time, create_time, update_time FROM ba_av_video LIMIT #{limit}")
	Cursor<BaAvVideo> cursorList(@Param("limit") int limit);
}
