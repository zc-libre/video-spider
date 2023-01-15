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
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 * @author: Libre
 * @Date: 2023/1/13 7:42 PM
 */

@Configuration
@RequiredArgsConstructor
public class VideoBatchConfiguration {

	public final JobBuilderFactory jobBuilderFactory;

	public final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job esSyncJob(@Qualifier("esStep") Step esStep, EsSyncJobListener esSyncJobListener) {
		return jobBuilderFactory.get("esSyncJob")
			.incrementer(new RunIdIncrementer())
			.listener(esSyncJobListener)
			.flow(esStep)
			.end()
			.build();
	}

	@Bean
	@StepScope
	public MyBatisCursorItemReader<Video> myBatisCursorItemReader(SqlSessionFactory sqlSessionFactory) {
		MyBatisCursorItemReader<Video> itemReader = new MyBatisCursorItemReader<>();
		itemReader.setQueryId("com.libre.video.mapper.VideoMapper.findAll");
		itemReader.setSqlSessionFactory(sqlSessionFactory);
		return itemReader;
	}

	@Bean
	@StepScope
	public MyBatisPagingItemReader<Video> myBatisPagingItemReader(SqlSessionFactory sqlSessionFactory) {
		MyBatisPagingItemReader<Video> itemReader = new MyBatisPagingItemReader<>();
		itemReader.setQueryId("com.libre.video.mapper.VideoMapper.findByBatchPage");
		itemReader.setSqlSessionFactory(sqlSessionFactory);
		itemReader.setPageSize(2000);
		return itemReader;
	}

	@Bean
	@StepScope
	public EsVideoItemWriter esVideoWriter(VideoEsRepository repository) {
		return new EsVideoItemWriter(repository);
	}

	@Bean
	public Step esStep(EsVideoItemWriter esVideoWriter, MyBatisPagingItemReader<Video> itemReader,
			@Qualifier("videoRequestExecutor") TaskExecutor taskExecutor) {

		return stepBuilderFactory.get("esStep")
			.<Video, Video>chunk(1000)
			.reader(itemReader)
			.writer(esVideoWriter)
			.taskExecutor(taskExecutor)
			.build();
	}


//	@Bean
//	@StepScope
//	public Video9SSpiderReader video9SSpiderReader(RedisUtils redisUtils) {
//		return new Video9SSpiderReader(redisUtils);
//	}
//
//	@Bean
//	@StepScope
//	public Video9sSpiderProcessor video9sSpiderProcessor() {
//		return new Video9sSpiderProcessor();
//	}
//
//	@Bean
//	@StepScope
//	public VideoSpiderWriter videoSpiderWriter() {
//		return new VideoSpiderWriter();
//	}
//
//	@Bean
//	public Step videoSpiderStep(Video9SSpiderReader itemReader,
//								Video9sSpiderProcessor video9sSpiderProcessor,
//			                    VideoSpiderWriter videoSpiderWriter,
//								@Qualifier("videoRequestExecutor") TaskExecutor taskExecutor) {
//		SkipPolicy skipPolicy = new AlwaysSkipItemSkipPolicy();
//
//		return stepBuilderFactory.get("videoSpiderStep")
//			.<VideoParse, Video>chunk(100)
//			.reader(itemReader).faultTolerant().skip(Exception.class).skipPolicy(skipPolicy)
//			.processor(video9sSpiderProcessor).faultTolerant().skip(Exception.class).skipPolicy(skipPolicy)
//			.writer(videoSpiderWriter).faultTolerant().skip(Exception.class).skipPolicy(skipPolicy)
//			.taskExecutor(taskExecutor).build();
//	}
//
//	@Bean
//	public Job videoSpiderJob(@Qualifier("videoSpiderStep") Step videoSpiderStep) {
//		return jobBuilderFactory.get("videoSpiderJob")
//			.incrementer(new RunIdIncrementer())
//			.flow(videoSpiderStep)
//			.end()
//			.build();
//	}

}
