package com.libre.video.core.spider.reader;

import com.libre.core.toolkit.CollectionUtil;
import com.libre.redis.cache.RedisUtils;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.pojo.parse.VideoParse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.database.AbstractPagingItemReader;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: Libre
 * @Date: 2023/1/14 10:02 PM
 */
@Slf4j
@StepScope
public abstract class AbstractVideoSpiderReader<P extends VideoParse> extends AbstractPagingItemReader<P> {

	protected final static String PAGE_CACHE_KEY = "libre:video:page:";

	protected Integer maxPageSize;

	protected final RedisUtils redisUtils;

	protected RequestTypeEnum requestType;



	protected AbstractVideoSpiderReader(RedisUtils redisUtils) {
		this.redisUtils = redisUtils;
	}

	@Override
	protected void doReadPage() {
		if (results == null) {
			results = new CopyOnWriteArrayList<>();
		}
		else {
			results.clear();
		}

		this.doParseVideo();
	}

	@Override
	protected void doOpen() throws Exception {
		super.doOpen();
		String indexPageHtml = requestIndexPage();
		Integer totalPageSize = parsePageSize(indexPageHtml);
		this.maxPageSize = totalPageSize;
		List<P> video9sParses = readVideoParseList(indexPageHtml);
		if (CollectionUtil.isNotEmpty(video9sParses)) {
			this.setMaxItemCount(totalPageSize * video9sParses.size());
		}

		Integer videoCurrentPage = redisUtils.get(PAGE_CACHE_KEY + requestType.name());
		int page = this.getPage();
		if (Objects.nonNull(videoCurrentPage) && videoCurrentPage > page) {
			this.setPageSize(video9sParses.size());
			this.setCurrentItemCount(videoCurrentPage * video9sParses.size());
		}
	}


	@Override
	public void update(@NotNull ExecutionContext executionContext) throws ItemStreamException {
		super.update(executionContext);
		int currentPage = this.getPage();
		log.info("{} parse video is executing, currentPage is: {}, totalPage is: {}", requestType.name(), currentPage,
				maxPageSize);
		redisUtils.set(PAGE_CACHE_KEY + requestType.name(), this.getPage());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.requestType = getRequestType();
	}

	protected void doParseVideo() {
		int page = this.getPage();
		List<P> video9sParses = doParse(page);
		doProcessVideos(video9sParses);
	}

	private void doProcessVideos(List<P> videoParseList) {
		this.setPageSize(videoParseList.size());
		results.addAll(videoParseList);
	}

	abstract protected List<P> doParse(Integer page);

	abstract protected String requestIndexPage();

	abstract protected List<P> readVideoParseList(String indexPageHtml);

	abstract protected Integer parsePageSize(String indexPageHtml);

	abstract protected RequestTypeEnum getRequestType();

}
