package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.pojo.Video;
import com.libre.video.pojo.dto.Video9s;
import com.libre.video.pojo.dto.Video9sParse;
import com.libre.video.pojo.dto.Video9sDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface Video9sMapping extends BaseConvert<Video9sDTO, Video9s> {

    Video9sMapping INSTANCE = Mappers.getMapper(Video9sMapping.class);

    Video9s convertToVideo9s(Video9sParse video9sParse);

    List<Video9s> convertToVideo9sList(Collection<Video> collection);
}
