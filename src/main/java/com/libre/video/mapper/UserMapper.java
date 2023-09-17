package com.libre.video.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.libre.video.pojo.User;
import com.libre.video.pojo.Video;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

/**
 * @author: Libre
 * @Date: 2022/5/14 3:36 AM
 */
@Mapper
public interface UserMapper {

	@Select("SELECT * FROM video_user WHERE username = #{username}")
	User findByUsername(String username);

}
