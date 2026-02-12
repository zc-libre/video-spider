package com.libre.video.toolkit;

import com.libre.boot.autoconfigure.SpringContext;
import lombok.experimental.UtilityClass;
import org.springframework.core.task.AsyncTaskExecutor;

@UtilityClass
public class ThreadPoolUtil {

	public static AsyncTaskExecutor videoRequestExecutor() {
		return SpringContext.getBean("videoRequestExecutor");
	}

	public static AsyncTaskExecutor downloadExecutor() {
		return SpringContext.getBean("downloadExecutor");
	}

}
