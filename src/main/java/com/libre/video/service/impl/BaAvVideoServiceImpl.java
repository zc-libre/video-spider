package com.libre.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.libre.video.mapper.BaAvVideoMapper;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.service.BaAvVideoService;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: Libre
 * @Date: 2022/5/12 2:41 AM
 */
@Service
public class BaAvVideoServiceImpl extends ServiceImpl<BaAvVideoMapper, BaAvVideo> implements BaAvVideoService {

	@Override
	@Transactional
	public Cursor<BaAvVideo> cursorList(int limit) {
		return baseMapper.cursorList(limit);
	}
}
