package com.libre.video.core.batch;

import com.libre.video.core.pojo.parse.VideoParse;
import com.libre.video.pojo.Video;
import org.springframework.batch.item.ItemProcessor;

/**
 * @author: Libre
 * @Date: 2023/1/14 11:08 PM
 */
public interface VideoSpiderProcessor<I extends VideoParse> extends ItemProcessor<I, Video> {


}
