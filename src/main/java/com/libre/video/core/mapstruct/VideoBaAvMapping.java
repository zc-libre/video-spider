package com.libre.video.core.mapstruct;

import com.libre.core.mapstruct.BaseConvert;
import com.libre.video.core.pojo.dto.Video9s;
import com.libre.video.core.pojo.parse.Video91Parse;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface VideoBaAvMapping extends BaseConvert<VideoBaAvParse, BaAvVideo> {

    VideoBaAvMapping INSTANCE = Mappers.getMapper(VideoBaAvMapping.class);

	@Override
	BaAvVideo sourceToTarget(VideoBaAvParse videoBaAvParse);

	List<Video> convertToVideList(Collection<BaAvVideo> baAvVideos);

}
