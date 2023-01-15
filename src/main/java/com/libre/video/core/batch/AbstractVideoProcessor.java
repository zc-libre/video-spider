package com.libre.video.core.batch;

import com.libre.core.random.RandomHolder;
import com.libre.video.core.pojo.parse.VideoParse;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.UserAgentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Random;

/**
 * @author: Libre
 * @Date: 2023/1/14 11:11 PM
 */
@Slf4j
public abstract class AbstractVideoProcessor<I extends VideoParse> implements VideoSpiderProcessor<I> {


}
