package com.libre.video.toolkit;

import com.libre.boot.autoconfigure.SpringContext;
import lombok.experimental.UtilityClass;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@UtilityClass
public class ThreadPoolUtil {

	public static ThreadPoolTaskExecutor videoRequestExecutor() {
		return SpringContext.getBean("videoRequestExecutor");
	}

	public static ThreadPoolTaskExecutor downloadExecutor() {
		return SpringContext.getBean("downloadExecutor");
	}

}
