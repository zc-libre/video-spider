package com.libre.video.core.request;

import com.libre.video.core.request.strategy.VideoRequestStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class VideoRequestProcessor implements ApplicationContextAware, InitializingBean {

	@Override
	@SuppressWarnings("unchecked")
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Map<String, Object> orderStrategyMap = applicationContext.getBeansWithAnnotation(VideoRequest.class);
		orderStrategyMap.forEach((k, v) -> {
			Class<VideoRequestStrategy> requestStrategyClass = (Class<VideoRequestStrategy>) v.getClass();
			int type = requestStrategyClass.getAnnotation(VideoRequest.class).value().getType();
			VideoRequestContext.getVideoRequestStrategyContext().put(type, requestStrategyClass);
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<Integer, Class<VideoRequestStrategy>> context = VideoRequestContext.getVideoRequestStrategyContext();
		log.info("VideoRequestContext init classes: {}", context.values());
	}
}
