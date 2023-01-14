package com.libre.video.core.batch;

import com.libre.video.pojo.Video;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * @author: Libre
 * @Date: 2023/1/13 11:04 PM
 */
public class VideoParseItemWriter implements ItemWriter<Video> {

	@Override
	public void write(List<? extends Video> items) throws Exception {

	}
}
