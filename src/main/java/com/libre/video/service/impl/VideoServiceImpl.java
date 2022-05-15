package com.libre.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.libre.core.exception.LibreException;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringUtil;
import com.libre.oss.support.OssTemplate;
import com.libre.video.constant.SystemConstants;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.mapstruct.VideoBaAvMapping;
import com.libre.video.core.request.VideoRequestContext;
import com.libre.video.core.request.strategy.VideoRequestStrategy;
import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.mapper.VideoMapper;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.dto.VideoQuery;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.VideoFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
	private final VideoRequestContext videoRequestContext;
	private final VideoDownload videoDownload;
	private final VideoEsRepository videoEsRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	private final OssTemplate ossTemplate;

	@Override
	public void request(VideoRequestParam param) {
		RequestTypeEnum requestTypeEnum = RequestTypeEnum.find(param.getRequestType());
		Assert.notNull(requestTypeEnum, "request type must not be null");
		log.info("start request type: {}, baseUrl: {}", requestTypeEnum.name(), requestTypeEnum.getBaseUrl());
		param.setRequestTypeEnum(requestTypeEnum);
		VideoRequestStrategy requestStrategy = videoRequestContext.getRequestStrategy(param.getRequestType());
		requestStrategy.execute(param);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void download(List<Long> ids) {
		for (Long id : ids) {
			videoDownload.encodeAndWrite(id);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveVideoToOss(Video video) {
		String videoPath = VideoFileUtils.getVideoPath(video.getTitle());
		if (!VideoFileUtils.videoExist(videoPath)) {
            throw new LibreException(String.format("视频不存在, videoId: %s, videoTitle: %s", video.getId(), video.getTitle()));
		}
		saveToOss(video, videoPath);
		String videoUrl = ossTemplate.getObjectURL(SystemConstants.VIDEO_BUCKET_NAME, video.getTitle());
		video.setVideoPath(videoUrl);
		this.updateById(video);
		videoEsRepository.save(video);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Page<Video> findByPage(PageDTO<Video> page, VideoQuery videoQuery) {
		List<Sort.Order> orders = getOrders(page);
		PageRequest pageRequest = PageRequest.of((int) page.getCurrent(), (int) page.getSize());
		pageRequest.withSort(Sort.by(orders));
		NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withPageable(pageRequest);

		String title = videoQuery.getTitle();
		if (StringUtil.isNotBlank(title)) {
			nativeSearchQueryBuilder
				.withQuery(QueryBuilders.matchQuery("title", title));
		}

		NativeSearchQuery query = nativeSearchQueryBuilder.build();
		SearchHits<Video> hits = elasticsearchOperations.search(query, Video.class);
		SearchPage<Video> searchPage = SearchHitSupport.searchPageFor(hits, query.getPageable());

		return (Page<Video>) SearchHitSupport.unwrapSearchHits(searchPage);
	}

	private List<Sort.Order> getOrders(PageDTO<Video> page) {
		List<Sort.Order> orders = Lists.newArrayList();
		List<OrderItem> orderItems = page.getOrders();
		if (CollectionUtil.isEmpty(orderItems)) {
			Sort.Order createTime = Order.by("publishTime").with(Sort.Direction.DESC);
			Sort.Order lookNum = Order.by("lookNum").with(Sort.Direction.DESC);
			orders.add(createTime);
			orders.add(lookNum);
		}

		for (OrderItem orderItem : orderItems) {
			String column = orderItem.getColumn();
			boolean asc = orderItem.isAsc();
			Sort.Order order = Order.by(column);
			if (!asc) {
				order.with(Sort.Direction.DESC);
			}
			orders.add(order);
		}
		return orders;
	}

	@Override
	public void syncToElasticsearch() {
		CompletableFuture<List<Video>> future = CompletableFuture.supplyAsync(this::list);
		future.thenAcceptAsync((videoList) -> {
			int batchSize = 1000;
			log.info("数据同步开始, 共{}条数据：", videoList.size());
			List<Video> videos = Lists.newArrayList();
			for (int i = 0; i < videoList.size(); i++) {
				if (i != 0 && i % batchSize != 0) {
					videos.add(videoList.get(i));
				} else {
					videoEsRepository.saveAll(videos);
					log.info("{}条数据同步成功", i);
					videos.clear();
				}
			}
			videoEsRepository.saveAll(videos);
			log.info("数据同步完成, 共{}条数据", videoList.size());
		}, ThreadPoolUtil.requestExecutor());
	}

	private void saveToOss(Video video, String videoPath) {

		try (InputStream inputStream = Files.newInputStream(Paths.get(videoPath))){
			ossTemplate.putObject(SystemConstants.VIDEO_BUCKET_NAME, video.getTitle(), inputStream);
		} catch (IOException e) {
			throw new LibreException("文件下载失败: " + e.getMessage());
		}
	}
}
