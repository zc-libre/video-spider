package com.libre.video.core.request.strategy;

import com.libre.video.core.pojo.dto.VideoRequestParam;

public interface VideoRequestStrategy {

	/**
	 * execute spider task
	 * @param requestParam spider website type
	 */
    void execute(VideoRequestParam requestParam);

}
