package com.libre.video.mapper;

import com.libre.video.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author: Libre
 * @Date: 2022/5/14 3:36 AM
 */
@Mapper
public interface UserMapper {

	@Select("SELECT * FROM video_user WHERE username = #{username}")
	User findByUsername(String username);
}
