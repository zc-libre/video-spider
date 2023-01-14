package com.libre.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.libre.video.pojo.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.mapping.ResultSetType;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {


	@Options(fetchSize = 2000, resultSetType = ResultSetType.FORWARD_ONLY)
	List<Video> findAll();

}
