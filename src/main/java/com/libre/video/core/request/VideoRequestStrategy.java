package com.libre.video.core.request;

import com.libre.video.core.enums.RequestTypeEnum;

public interface VideoRequestStrategy {

	/**
	 * execute spider task
	 * @param requestTypeEnum spider website type
	 */
    void execute(RequestTypeEnum requestTypeEnum);
}
