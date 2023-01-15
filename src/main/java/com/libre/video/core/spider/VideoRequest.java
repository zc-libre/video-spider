package com.libre.video.core.spider;

import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface VideoRequest {

	/**
	 * 策略类型
	 */
	RequestTypeEnum value();

	VideoStepType step() default VideoStepType.READER;

}
