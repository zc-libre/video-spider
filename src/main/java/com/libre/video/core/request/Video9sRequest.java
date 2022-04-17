package com.libre.video.core.request;

import com.google.common.collect.Lists;
import com.libre.core.exception.LibreException;
import com.libre.core.random.RandomHolder;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.core.VideoEventPublisher;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.pojo.*;
import com.libre.video.pojo.dto.Video9s;
import com.libre.video.pojo.dto.Video9sDTO;
import com.libre.video.pojo.dto.Video9sParse;
import com.libre.video.service.Video9sService;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.UserAgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Video9sRequest {

    private final static String REQUEST_BASE_URL = "https://xn--sjqr38j.com/video/category/latest";
    private final Video9sService video9sService;

    public void execute() {
        ThreadPoolTaskExecutor executor = ThreadPoolUtil.requestExecutor();
        String url = REQUEST_BASE_URL;
        String html = requestAsHtml(REQUEST_BASE_URL);
        Integer pageSize = parsePageSize(html);

        if (StringUtil.isBlank(html)) {
            publishErrorVideo(url,null, ErrorRequestType.REQUEST_CATEGORY);
            return;
        }
        if (pageSize == null) {
            return;
        }
        readVideosAndSave(html, url);
        for (int i = pageSize; i >= 2; i--) {
            url = REQUEST_BASE_URL + StringPool.SLASH + i;
            String doc = requestAsHtml(url);
            if (StringUtil.isBlank(doc)) {
                publishErrorVideo(url, null, ErrorRequestType.REQUEST_PAGE);
                continue;
            }
            String requestVideoUrl = url;
            executor.execute(() -> readVideosAndSave(doc, requestVideoUrl));
        }

        executor.shutdown();
        log.info("video request complete!");
    }

    private void publishErrorVideo(String url, String html, ErrorRequestType type) {
        ErrorVideo errorVideo = new ErrorVideo();
        errorVideo.setUrl(url);
        errorVideo.setType(type.getCode());
        errorVideo.setHtml(html);
        VideoEventPublisher.publishErrorEvent(errorVideo);
    }

    public void readVideosAndSave(String html, String requestVideoUrl) {
        try {
            List<Video9s> videos = readVideoList(html);
            VideoEventPublisher.publishVideo9sSaveEvent(videos);
        } catch (Exception e) {
            publishErrorVideo(requestVideoUrl, html, ErrorRequestType.PARSE);
        }
    }

    public List<Video9s> readVideoList(String html) {
        List<Video9sParse> parseList = DomMapper.readList(html, Video9sParse.class);
        if (CollectionUtil.isEmpty(parseList)) {
            throw new LibreException("html parse error");
        }
        List<Video9s> videos = Lists.newArrayList();
        Map<String, Video9sParse> parseMap = parseList.stream()
                .filter(video91Parse -> StringUtil.isNotBlank(video91Parse.getUrl()))
                .collect(Collectors.toMap(Video9sParse::getUrl, Video9sParse -> Video9sParse, (v1, v2) -> v1));

      // List<Video9s> list = video9sService.list(Wrappers.<Video9s>lambdaQuery().in(Video9s::getUrl, parseMap.keySet()));
//        Map<String, Video9s> videoMap = list.stream().collect(Collectors.toMap(Video9s::getUrl, url -> url, (v1, v2) -> v1));
//        for (Map.Entry<String, Video9s> entry : videoMap.entrySet()) {
//            parseMap.remove(entry.getKey());
//        }
        for (Video9sParse video9sParse : parseMap.values()) {
            Video9s video9s = readVideo(video9sParse);
            videos.add(video9s);
        }
        return videos;
    }

    private void parseVideoInfo(String html, Video9s video9s) {
        Video9sDTO video9sDTO = DomMapper.readValue(html, Video9sDTO.class);
        BeanUtils.copyProperties(video9sDTO, video9s);
    }

    public Video9s readVideo(Video9sParse video9sParse) {
        Video9s video9s = new Video9s();
        BeanUtils.copyProperties(video9sParse, video9s);
        String url = video9sParse.getUrl();
        if (StringUtil.isBlank(url)) {
            return null;
        }
        if (StringUtil.isNotBlank(url)) {
            url =  RequestConstant.REQUEST_9S_BASE_URL + url;
            video9s.setUrl(url);
        }

        String body = requestAsHtml(url);
        if (StringUtil.isBlank(body)) {
            return null;
        }
        parseVideoInfo(body, video9s);
        return video9s;
    }

    private Integer parsePageSize(String html) {
        Document document = Parser.parse(html, "");
        Elements elements = document.getElementsByClass("pagination");
        if (elements.isEmpty()) {
            return null;
        }
        Element pagination = elements.get(0);
        if (Objects.isNull(pagination)) {
            return null;
        }
        Elements allPage = pagination.getAllElements();
        Element page = allPage.get(8);
        if (Objects.isNull(page)) {
            return null;
        }
        String text = page.ownText();
        if (StringUtil.isBlank(text)) {
            return null;
        }
        return Integer.parseInt(text);
    }

    private  String requestAsHtml(String url) {
        log.info("start request url: {}", url);
        try {
            return httpRequest(url)
                    .executeAsyncAndJoin()
                    .asString();
        } catch (Exception e) {
            log.error("request error");
        }
        return null;
    }

    private HttpRequest httpRequest(String url) {
        Random r = RandomHolder.RANDOM;
        return HttpRequest.get(url)
                .setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .setHeader(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent())
                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5")
                .setHeader("Connection", "keep-alive")
                .setHeader("X-Forwarded-For",r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256))
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5));
    }
}
