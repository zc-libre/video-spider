package com.libre.video.config;

import com.libre.video.core.sync.EsSyncJobListener;
import com.libre.video.core.sync.EsVideoItemWriter;
import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.pojo.Video;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author: Libre
 * @Date: 2023/1/13 7:42 PM
 */

@Configuration
@RequiredArgsConstructor
public class VideoBatchConfiguration extends DefaultBatchConfiguration {

	private final PlatformTransactionManager platformTransactionManager;

	@Bean
	public Job esSyncJob(@Qualifier("esStep") Step esStep, EsSyncJobListener esSyncJobListener,
			JobRepository jobRepository) {
		return new JobBuilder("esSyncJob", jobRepository).incrementer(new RunIdIncrementer())
				.listener(esSyncJobListener).flow(esStep).end().build();
	}

	@Bean
	@StepScope
	public MyBatisCursorItemReader<Video> myBatisCursorItemReader(SqlSessionFactory sqlSessionFactory) {
		MyBatisCursorItemReader<Video> itemReader = new MyBatisCursorItemReader<>();
		itemReader.setQueryId("com.libre.video.mapper.VideoMapper.findAll");
		itemReader.setSqlSessionFactory(sqlSessionFactory);
		itemReader.setSaveState(false);
		return itemReader;
	}

	@Bean
	@StepScope
	public MyBatisPagingItemReader<Video> myBatisPagingItemReader(SqlSessionFactory sqlSessionFactory) {
		MyBatisPagingItemReader<Video> itemReader = new MyBatisPagingItemReader<>();
		itemReader.setQueryId("com.libre.video.mapper.VideoMapper.findByBatchPage");
		itemReader.setSqlSessionFactory(sqlSessionFactory);
		itemReader.setPageSize(2000);
		itemReader.setSaveState(false);
		return itemReader;
	}

	@Bean
	@StepScope
	public EsVideoItemWriter esVideoWriter(VideoEsRepository repository) {
		return new EsVideoItemWriter(repository);
	}

	@Bean
	public Step esStep(EsVideoItemWriter esVideoWriter, MyBatisPagingItemReader<Video> itemReader,
			@Qualifier("videoRequestExecutor") TaskExecutor taskExecutor, JobRepository jobRepository) {

		return new StepBuilder("esStep", jobRepository).<Video, Video>chunk(200, platformTransactionManager)
				.reader(itemReader).writer(esVideoWriter)
				.faultTolerant().skipPolicy(new AlwaysSkipItemSkipPolicy())
				.taskExecutor(taskExecutor).build();
	}

}
