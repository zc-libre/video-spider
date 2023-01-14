package com.libre.video.core.batch;

import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.core.pojo.parse.VideoParse;
import com.libre.video.core.request.strategy.VideoRequestStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * @author: Libre
 * @Date: 2023/1/13 11:03 PM
 */
@RequiredArgsConstructor
public class VideoParseItemReader implements ItemReader<VideoParse> {

	private final VideoRequestStrategy<?> videoRequestStrategy;

	@Override
	public Video9sParse read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		return null;
	}

}
