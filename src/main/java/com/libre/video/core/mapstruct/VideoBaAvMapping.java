package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VideoBaAvMapping extends BaseConvert<VideoBaAvParse, BaAvVideo> {

    VideoBaAvMapping INSTANCE = Mappers.getMapper(VideoBaAvMapping.class);

	@Override
	BaAvVideo sourceToTarget(VideoBaAvParse videoBaAvParse);

	@Mapping(source = "time", target = "publishTime")
	Video convertToVide(BaAvVideo baAvVideos);

	@Mapping(source = "time", target = "publishTime")
	List<Video> convertToVideList(List<BaAvVideo> baAvVideoList);
}
