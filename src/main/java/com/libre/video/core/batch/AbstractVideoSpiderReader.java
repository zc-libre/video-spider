package com.libre.video.core.batch;

import com.libre.core.random.RandomHolder;
import com.libre.video.core.pojo.parse.Video9sParse;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.UserAgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: Libre
 * @Date: 2023/1/14 10:02 PM
 */
@Slf4j
public abstract class AbstractVideoSpiderReader extends AbstractPagingItemReader<Video9sParse> {

	protected final static String PAGE_CACHE_KEY = "libre:video:page:";

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

	abstract protected void doParseVideo();

}
