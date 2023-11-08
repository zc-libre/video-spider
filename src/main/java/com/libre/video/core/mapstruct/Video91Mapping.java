package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.pojo.Video;
import com.libre.video.core.pojo.parse.Video91Parse;
import com.libre.video.core.pojo.dto.Video9sDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface Video91Mapping extends BaseConvert<Video91Parse, Video> {

	Video91Mapping INSTANCE = Mappers.getMapper(Video91Mapping.class);

	List<Video> convertToVideo91List(Collection<Video9sDTO> collection);

	Video convertToVideo91(Video9sDTO video9SDTO);

}
