package com.libre.video.core.request;

import com.libre.video.core.dto.RequestParam;

public interface VideoRequestStrategy {

	/**
	 * execute spider task
	 * @param requestParam spider website type
	 */
    void execute(RequestParam requestParam);

}
