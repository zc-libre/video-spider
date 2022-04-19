package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.pojo.Video;
import com.libre.video.core.dto.Video91Parse;
import com.libre.video.core.dto.Video9s;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface Video91Mapping extends BaseConvert<Video91Parse, Video> {

    Video91Mapping INSTANCE = Mappers.getMapper(Video91Mapping.class);

    List<Video> convertToVideo91List(Collection<Video9s> collection);
}
