package com.libre.video.core.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobInstance;
import org.springframework.stereotype.Component;

/**
 * @author: Libre
 * @Date: 2023/1/13 10:00 PM
 */
@Slf4j
@Component
public class EsSyncJobListener implements JobExecutionListener {

	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("beforeJob execute ......");
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		log.info("job execute complete");
	}

}
