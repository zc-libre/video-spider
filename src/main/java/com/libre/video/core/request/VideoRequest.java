package com.libre.video.core.request;

import com.libre.video.core.enums.RequestTypeEnum;

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
}
