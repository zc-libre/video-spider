package com.libre.video.core.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author: Libre
 * @Date: 2023/1/14 11:44 PM
 */
@RequiredArgsConstructor
public class VideoSpiderWriterFactoryBean implements FactoryBean<VideoSpiderWriter> {

	private final String requestType;

	@Override
	public VideoSpiderWriter getObject() throws Exception {

		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
