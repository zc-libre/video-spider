package com.libre.video.core.request;

import com.google.common.collect.Maps;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.video.core.request.strategy.VideoRequestStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VideoRequestContext  {

	private static final Map<Integer, Class<VideoRequestStrategy>> VideoRequestStrategyContext = Maps.newHashMap();

	public VideoRequestStrategy getRequestStrategy(Integer type) {
		Class<VideoRequestStrategy> strategyClass = VideoRequestStrategyContext.get(type);
		if (strategyClass == null) {
			throw new IllegalArgumentException("context haven't this request type: [" + type + "], please delimit it" );
		}
		return SpringContext.getBean(strategyClass);
	}

	public static Map<Integer, Class<VideoRequestStrategy>> getVideoRequestStrategyContext() {
		return VideoRequestStrategyContext;
	}

	public static Integer getRequestType(Class<VideoRequestStrategy> clazz) {
		for (Map.Entry<Integer, Class<VideoRequestStrategy>> entry : VideoRequestStrategyContext.entrySet()) {
			Class<VideoRequestStrategy> value = entry.getValue();
			if (value.equals(clazz)) {
				return entry.getKey();
			}
		}
		return null;
	}


}
