package com.libre.video.core.request;

import com.libre.video.core.dto.VideoRequestParam;

public interface VideoRequestStrategy {

	/**
	 * execute spider task
	 * @param requestParam spider website type
	 */
    void execute(VideoRequestParam requestParam);

}
