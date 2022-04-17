package com.libre.video.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.video.core.VideoDownload;
import com.libre.video.mapper.Video91Mapper;
import com.libre.video.pojo.Video91;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<Video91Mapper, Video91> implements VideoService {

    private final VideoDownload videoDownload;
  //  private final VideoEsMapper videoEsMapper;

    @Override
    public void download(List<Long> ids) {
        List<Video91> video91List = this.listByIds(ids);
        if (CollectionUtil.isEmpty(video91List)) {
            return;
        }
        for (Video91 video91 : video91List) {
            try {
                videoDownload.encodeAndWrite(video91.getRealUrl(), video91.getTitle());
            } catch (IOException e) {
                log.error("download error: id: {}, title: {}", video91.getId(), video91.getTitle());
            }
        }
    }

    @Override
    public void download() {
        List<Video91> list = this.list(Wrappers.<Video91>lambdaQuery().orderByAsc(Video91::getId));
        for (Video91 video91 : list) {
            try {
                videoDownload.encodeAndWrite(video91.getRealUrl(), video91.getTitle());
            } catch (IOException e) {
                log.error("download error: id: {}, title: {}", video91.getId(), video91.getTitle());
            }

        }

    }

    @Override
    public void createIndex() {
      /*  LambdaEsIndexWrapper<Video91EsRepository> indexWrapper = new LambdaEsIndexWrapper<>();
        indexWrapper.indexName(Video91EsRepository.class.getSimpleName().toLowerCase());
        indexWrapper.mapping(Video91EsRepository::getTitle, FieldType.TEXT)
                .mapping(Video91EsRepository::getId, FieldType.LONG)
                .mapping(Video91EsRepository::getUrl, FieldType.KEYWORD)
                .mapping(Video91EsRepository::getRealUrl, FieldType.KEYWORD)
                .mapping(Video91EsRepository::getAuthor, FieldType.KEYWORD)
                .mapping(Video91EsRepository::getImage, FieldType.KEYWORD)
                .mapping(Video91EsRepository::getCollectNum, FieldType.INTEGER)
                .mapping(Video91EsRepository::getLookNum, FieldType.INTEGER)
                .mapping(Video91EsRepository::getAuthor, FieldType.KEYWORD);
        videoEsMapper.createIndex(indexWrapper);*/
    }


}
