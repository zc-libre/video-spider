package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.core.pojo.parse.VideoRouParse;
import com.libre.video.pojo.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VideoRouMapping extends BaseConvert<VideoRouParse, Video> {

	VideoRouMapping INSTANCE = Mappers.getMapper(VideoRouMapping.class);

	@Override
	@Mapping(target = "title", source = "nameZh")
	@Mapping(target = "lookNum", source = "viewCount")
	@Mapping(target = "image", source = "coverImageUrl")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "videoId", ignore = true)
	@Mapping(target = "url", ignore = true)
	@Mapping(target = "realUrl", ignore = true)
	@Mapping(target = "duration", ignore = true)
	Video sourceToTarget(VideoRouParse source);

}
