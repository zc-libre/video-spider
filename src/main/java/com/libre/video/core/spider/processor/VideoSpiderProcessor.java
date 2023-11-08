package com.libre.video.core.spider.processor;

import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.pojo.parse.VideoParse;
import com.libre.video.pojo.Video;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;

/**
 * @author: Libre
 * @Date: 2023/1/15 7:25 PM
 */
@StepScope
public interface VideoSpiderProcessor<P extends VideoParse> extends ItemProcessor<VideoParse, Video> {

	RequestTypeEnum getRequestType();

}
