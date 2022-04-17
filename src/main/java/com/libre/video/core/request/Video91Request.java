package com.libre.video.core.request;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.libre.core.exception.LibreException;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.core.enums.VideoType;
import com.libre.video.core.VideoEventPublisher;
import com.libre.video.pojo.ErrorVideo;
import com.libre.video.pojo.Video91;
import com.libre.video.pojo.dto.Video91Parse;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.JsEncodeUtil;
import com.google.common.collect.Lists;
import com.libre.core.random.RandomHolder;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.toolkit.ThreadPoolUtil;
import com.libre.video.toolkit.UserAgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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
public class Video91Request {

    private final static String REQUEST_BASE_URL = "http://www.91porn.com/v.php";
    private final static String PARAM_CATEGORY = "category";
    private final static String PARAM_PAGE = "page";

    private final VideoService videoService;

    public void execute() {
        VideoType[] types = VideoType.values();
        ArrayUtils.reverse(types);
        HttpUrl httpUrl = HttpUrl.get(REQUEST_BASE_URL);
        HttpUrl.Builder urlBuilder = getUrlBuilder(httpUrl);
        ThreadPoolTaskExecutor executor = ThreadPoolUtil.requestExecutor();
        for (VideoType videoType : types) {
            urlBuilder.addQueryParameter(PARAM_CATEGORY, videoType.getName());
            String url = urlBuilder.build().toString();
            String html = requestAsHtml(url);
            urlBuilder.removeAllQueryParameters(PARAM_CATEGORY);
            if (StringUtil.isBlank(html)) {
                publishErrorVideo(urlBuilder.build().toString(),null, ErrorRequestType.REQUEST_CATEGORY);
                continue;
            }
            Integer pageSize = parsePageSize(html);
            if (pageSize == null) {
                continue;
            }
            readVideosAndSave(html, url);

            for (int i = pageSize; i >= 2; i--) {
                urlBuilder.removeAllQueryParameters(PARAM_PAGE);
                urlBuilder.addQueryParameter(PARAM_PAGE, String.valueOf(i));
                String requestUrl = urlBuilder.build().toString();
                String doc = requestAsHtml(requestUrl);
                if (StringUtil.isBlank(doc)) {
                    publishErrorVideo(urlBuilder.build().toString(), null, ErrorRequestType.REQUEST_PAGE);
                    continue;
                }
                executor.execute(() -> readVideosAndSave(doc, url));
            }
        }
        executor.shutdown();
        log.info("video request complete!");
    }

    private HttpUrl.Builder getUrlBuilder(HttpUrl httpUrl) {
        return httpUrl.newBuilder()
                .addQueryParameter("viewtype", "basic")
                .addQueryParameter(PARAM_PAGE, "1");
    }


    private void publishErrorVideo(String url, String html, ErrorRequestType type) {
        ErrorVideo errorVideo = new ErrorVideo();
        errorVideo.setUrl(url);
        errorVideo.setType(type.getCode());
        errorVideo.setHtml(html);
        VideoEventPublisher.publishErrorEvent(errorVideo);
    }

    public void readVideosAndSave(String html, String url) {
        try {
            List<Video91> video91s = readVideoList(html);
            VideoEventPublisher.publishVideo91SaveEvent(video91s);
        } catch (Exception e) {
            publishErrorVideo(url, html, ErrorRequestType.PARSE);
        }
    }


    public List<Video91> readVideoList(String html) {
        List<Video91Parse> introductionList = DomMapper.readList(html, Video91Parse.class);
        if (CollectionUtil.isEmpty(introductionList)) {
             throw new LibreException("html parse error");
        }
        List<Video91> video91s = Lists.newArrayList();
        Map<String, Video91Parse> videoIntroductionMap = introductionList.stream()
                .filter(video91Parse -> StringUtil.isNotBlank(video91Parse.getUrl()))
                .collect(Collectors.toMap(Video91Parse::getUrl, video91Parse -> video91Parse, (v1, v2) -> v1));

        List<Video91> list = videoService.list(Wrappers.<Video91>lambdaQuery().in(Video91::getUrl, videoIntroductionMap.keySet()));
        Map<String, Video91> videoMap = list.stream().collect(Collectors.toMap(Video91::getUrl, url -> url, (v1, v2) -> v1));
        for (Map.Entry<String, Video91> entry : videoMap.entrySet()) {
            videoIntroductionMap.remove(entry.getKey());
        }
        for (Video91Parse video91Parse : videoIntroductionMap.values()) {
            parseVideoInfo(html, video91Parse);
            Video91 video91 = readVideo(video91Parse);
            if (Objects.isNull(video91)) {
                publishErrorVideo(video91Parse.getUrl(), html, ErrorRequestType.PARSE);
            }
            video91s.add(video91);
        }
        return video91s;
    }

    private void parseVideoInfo(String html, Video91Parse video91Parse) {
        Document document = Parser.parse(html, "");
        Elements elements = document.getElementsByClass("well");
        if (elements.isEmpty() || elements.size() < 2) {
            return;
        }
        Element element = elements.get(1);
        Elements wells = element.getElementsByClass("well");
        Element well = wells.get(0);
        if (Objects.isNull(well)) {
            return;
        }
        List<Node> nodes = well.childNodes();
        if (CollectionUtil.isEmpty(nodes) || nodes.size() < 15) {
            return;
        }
        TextNode authorNode = (TextNode) nodes.get(10);
        video91Parse.setAuthor(authorNode.text());
        TextNode lookNode = (TextNode) nodes.get(14);
        String lookText = lookNode.text();
        if (StringUtil.isNotBlank(lookText)) {
            video91Parse.setLookNum(Integer.parseInt(StringUtil.trimWhitespace(lookText)));
        }
        TextNode collectNode = (TextNode) nodes.get(16);
        String collectText = collectNode.text();
        if (StringUtil.isNotBlank(collectText)) {
            video91Parse.setCollectNum(Integer.parseInt(StringUtil.trimWhitespace(collectText)));
        }
    }

    public Video91 readVideo(Video91Parse video91Parse) {
        String url = video91Parse.getUrl();
        if (StringUtil.isBlank(url)) {
            return null;
        }
        String body = requestAsHtml(url);
        if (StringUtil.isBlank(body)) {
            return null;
        }
        String realUrl = JsEncodeUtil.encodeRealVideoUrl(body);
        log.info("realVideoUrl: {}", realUrl);
        if (StringUtil.isBlank(realUrl)) {
            return null;
        }
        Video91 video91 = new Video91();
        BeanUtils.copyProperties(video91Parse, video91);
        long id = parseVideoId(realUrl);
        video91.setId(id);
        video91.setRealUrl(realUrl);
        return video91;
    }

    private  long parseVideoId(String realUrl) {
        int endIndex = realUrl.indexOf(".m3u8");
        int startIndex = realUrl.lastIndexOf(StringPool.SLASH) + 1;
        String idStr = realUrl.substring(startIndex, endIndex);
        return Long.parseLong(idStr);
    }


    private Integer parsePageSize(String html) {
        Document document = Parser.parse(html, "");
        Elements elements = document.getElementsByClass("pagingnav");
        if (elements.isEmpty()) {
            return null;
        }
        Element pageNav = elements.get(0);
        if (Objects.isNull(pageNav)) {
            return null;
        }
        Elements allPage = pageNav.getAllElements();
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

    private  HttpRequest httpRequest(String url) {
        Random r = RandomHolder.RANDOM;
//        String proxy = getProxy();
//        int index = proxy.indexOf(StringPool.COLON);
//        String ip = proxy.substring(0, index);
//        int port = Integer.parseInt(proxy.substring(index + 1));

        return HttpRequest.get(url)
                .setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .setHeader(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent())
                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5")
                .setHeader("Connection", "keep-alive")
                .setHeader("X-Forwarded-For",r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256))
           //     .proxy(ip, port)
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5));
    }

    private  String getProxy() {
        Map<String, Object> proxyMap = HttpRequest.get("http://localhost:5010/get").execute().asMap(Object.class);
        if (CollectionUtil.isNotEmpty(proxyMap)) {
            String proxy = (String) proxyMap.get("proxy");
            if (StringUtil.isNotBlank(proxy)) {
                return proxy;
            }
        }
        return getProxy();
    }

}
