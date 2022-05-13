package com.libre.video.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.libre.boot.autoconfigure.SpringContext;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringUtil;
import com.libre.video.core.download.VideoDownload;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.mapstruct.VideoBaAvMapping;
import com.libre.video.core.request.strategy.Video9SRequestStrategy;
import com.libre.video.core.request.VideoRequestContext;
import com.libre.video.core.request.strategy.VideoRequestStrategy;
import com.libre.video.mapper.VideoEsRepository;
import com.libre.video.mapper.VideoMapper;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.pojo.Video;
import com.libre.video.core.pojo.dto.VideoRequestParam;
import com.libre.video.pojo.dto.VideoQuery;
import com.libre.video.service.BaAvVideoService;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.PageUtil;
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
	private final VideoRequestContext videoRequestContext;
	private final VideoDownload videoDownload;
	private final VideoEsRepository videoEsRepository;

	private final BaAvVideoService baAvVideoService;
	private final ElasticsearchOperations elasticsearchOperations;

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
	public void download(List<Long> ids) {
		List<Video> videoList = baseMapper.selectBatchIds(ids);
		if (CollectionUtil.isEmpty(videoList)) {
			return;
		}
		for (Video video : videoList) {
			videoDownload.encodeAndWrite(video.getRealUrl(), video.getTitle());
		}
	}

	@Override
	public void requestAndDownload(String url) {

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
			Sort.Order createTime = Order.by("createTime").with(Sort.Direction.DESC);
			orders.add(createTime);
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
	public void dataAsyncToElasticsearch() {
		VideoBaAvMapping mapping = VideoBaAvMapping.INSTANCE;
		CompletableFuture<List<Video>> future = CompletableFuture.supplyAsync(() -> {
			List<Video> list = this.list();
			List<BaAvVideo> baAvVideoList = baAvVideoService.list();
			List<Video> videoList = mapping.convertToVideList(baAvVideoList);
			list.addAll(videoList);
			return list;
		});
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



	@Override
	public List<Video> findByTitle(String title) {
		return null;
	}

	@Override
	public Page<Video> findByTitlePage(String title, PageDTO<Video> page) {
		List<Sort.Order> orders = PageUtil.getOrders(page);
		PageRequest pageRequest = PageRequest.of((int) page.getCurrent(), (int) page.getSize(), Sort.by(orders));
		return videoEsRepository.findVideosByTitleLike(title, pageRequest);
	}
}
