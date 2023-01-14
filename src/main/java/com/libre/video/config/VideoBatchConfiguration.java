package com.libre.video.config;

import com.libre.video.core.batch.Video9SSpiderReader;
import com.libre.video.core.batch.Video9sSpiderProcessor;
import com.libre.video.core.batch.VideoSpiderWriter;
import com.libre.video.core.batch.sync.EsSyncJobListener;
import com.libre.video.core.batch.sync.EsVideoItemWriter;
import com.libre.video.core.download.M3u8Download;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.pojo.Video;
import com.libre.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

import javax.batch.api.chunk.ItemReader;

/**
 * @author: Libre
 * @Date: 2023/1/13 7:42 PM
 */
@EnableBatchProcessing
@Configuration
@RequiredArgsConstructor
public class VideoBatchConfiguration {

	public final JobBuilderFactory jobBuilderFactory;

	public final StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job esSyncJob(@Qualifier("esStep") Step esStep, EsSyncJobListener esSyncJobListener) {
		return jobBuilderFactory.get("esSyncJob").incrementer(new RunIdIncrementer()).listener(esSyncJobListener)
				.flow(esStep).end().build();
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
		return stepBuilderFactory.get("esStep").<Video, Video>chunk(1000).reader(itemReader).writer(esVideoWriter)
				.taskExecutor(taskExecutor).build();
	}

	@Bean
	@StepScope
	public Video9SSpiderReader video9SSpiderReader(VideoService videoService, WebClient webClient) {
		return new Video9SSpiderReader(videoService, webClient);
	}

	@Bean
	@StepScope
	public Video9sSpiderProcessor video9sSpiderProcessor(VideoService videoService, WebClient webClient,
			M3u8Download m3u8Download) {
		return new Video9sSpiderProcessor(videoService, webClient, m3u8Download);
	}

	@Bean
	@StepScope
	public VideoSpiderWriter videoSpiderWriter(VideoService videoService) {
		return new VideoSpiderWriter(videoService);
	}

	@Bean
	public Step videoSpiderStep(Video9SSpiderReader itemReader,
								Video9sSpiderProcessor video9sSpiderProcessor,
			                    VideoSpiderWriter videoSpiderWriter,
								@Qualifier("videoRequestExecutor") TaskExecutor taskExecutor) {

		return stepBuilderFactory.get("videoSpiderStep")
			.<Video9sParse, Video>chunk(100)
			.reader(itemReader).faultTolerant().skip(Exception.class)
			.processor(video9sSpiderProcessor).faultTolerant().skip(Exception.class)
			.writer(videoSpiderWriter).faultTolerant().skip(Exception.class)
			.taskExecutor(taskExecutor).build();
	}

	@Bean
	public Job videoSpiderJob(@Qualifier("videoSpiderStep") Step videoSpiderStep) {
		return jobBuilderFactory.get("videoSpiderJob")
			.incrementer(new RunIdIncrementer())
			.flow(videoSpiderStep)
			.end()
			.build();
	}

}
