package com.libre.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.libre.video.mapper.ErrorVideoMapper;
import com.libre.video.pojo.ErrorVideo;
import com.libre.video.service.ErrorVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorVideoServiceImpl extends ServiceImpl<ErrorVideoMapper, ErrorVideo> implements ErrorVideoService {

}
