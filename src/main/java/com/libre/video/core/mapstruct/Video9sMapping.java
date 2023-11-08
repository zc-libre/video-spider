package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.core.pojo.parse.Video9sDetailParse;
import com.libre.video.pojo.Video;
import com.libre.video.core.pojo.dto.Video9sDTO;
import com.libre.video.core.pojo.parse.Video9sParse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface Video9sMapping extends BaseConvert<Video9sDetailParse, Video9sDTO> {

	Video9sMapping INSTANCE = Mappers.getMapper(Video9sMapping.class);

	Video9sDTO convertToVideo9s(Video9sParse video9sParse);

	List<Video9sDTO> convertToVideo9sList(Collection<Video> collection);

}
