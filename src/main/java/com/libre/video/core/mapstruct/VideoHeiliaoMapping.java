package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.core.pojo.parse.VideoHeiliaoParse;
import com.libre.video.pojo.Video;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VideoHeiliaoMapping extends BaseConvert<VideoHeiliaoParse, Video> {

	VideoHeiliaoMapping INSTANCE = Mappers.getMapper(VideoHeiliaoMapping.class);

}
