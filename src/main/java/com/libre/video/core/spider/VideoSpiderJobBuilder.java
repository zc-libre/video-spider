package com.libre.video.core.spider;

import com.google.common.collect.Maps;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.VideoStepType;
import com.libre.video.core.pojo.parse.VideoParse;
import com.libre.video.core.spider.processor.VideoSpiderProcessor;
import com.libre.video.core.spider.reader.AbstractVideoSpiderReader;
import com.libre.video.core.spider.writer.VideoSpiderWriter;
import com.libre.video.pojo.Video;
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Libre
 * @Date: 2023/1/15 6:55 PM
 */
@Component
@RequiredArgsConstructor
public class VideoSpiderJobBuilder implements SmartInitializingSingleton {

	private final JobRepository jobRepository;

	private final PlatformTransactionManager platformTransactionManager;

	private final VideoSpiderWriter writer;

	private final SkipPolicy skipPolicy = new AlwaysSkipItemSkipPolicy();

	private final Map<Integer, AbstractVideoSpiderReader<?>> readerContext = Maps.newHashMap();

	private final Map<Integer, VideoSpiderProcessor<?>> processorContext = Maps.newHashMap();

	public Job videoSpiderJob(Integer requestType) {
		RequestTypeEnum requestTypeEnum = RequestTypeEnum.find(requestType);
		Assert.notNull(requestTypeEnum, "requestTypeEnum must not be null");

		AbstractVideoSpiderReader<?> reader = readerContext.get(requestType);
		VideoSpiderProcessor<?> processor = processorContext.get(requestType);
		Step step = videoSpiderStep(requestTypeEnum.name(), reader, processor, writer);
		return new JobBuilder("videoSpiderJob", jobRepository).incrementer(new RunIdIncrementer()).flow(step).end()
				.build();
	}

	private Step videoSpiderStep(String stepName, AbstractVideoSpiderReader<?> reader,
			VideoSpiderProcessor<?> processor, VideoSpiderWriter writer) {

		ThreadPoolTaskExecutor executor = ThreadPoolUtil.videoRequestExecutor();
		return new StepBuilder(stepName, jobRepository).<VideoParse, Video>chunk(1, platformTransactionManager)
				.reader(reader).faultTolerant().skip(Exception.class).skipPolicy(skipPolicy).processor(processor)
				.faultTolerant().skip(Exception.class).skipPolicy(skipPolicy).writer(writer).faultTolerant()
				.skip(Exception.class).skipPolicy(skipPolicy).taskExecutor(executor).build();
	}

	@Override
	public void afterSingletonsInstantiated() {
		ApplicationContext applicationContext = SpringContext.getContext();
		Assert.notNull(applicationContext, "applicationContext must not be null");

		Map<String, Object> stepMap = applicationContext.getBeansWithAnnotation(VideoRequest.class);
		Collection<Object> values = stepMap.values();
		for (Object step : values) {
			Class<?> clazz = step.getClass();
			VideoRequest videoRequest = clazz.getAnnotation(VideoRequest.class);
			VideoStepType videoStepType = videoRequest.step();
			RequestTypeEnum requestType = videoRequest.value();
			if (VideoStepType.READER.equals(videoStepType)) {
				readerContext.put(requestType.getType(), (AbstractVideoSpiderReader<?>) step);
			}
			else if (VideoStepType.PROCESSOR.equals(videoStepType)) {
				processorContext.put(requestType.getType(), (VideoSpiderProcessor<?>) step);
			}
		}

	}

}
