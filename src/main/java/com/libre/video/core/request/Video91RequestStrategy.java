package com.libre.video.core.request;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.libre.core.exception.LibreException;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.Video91Type;
import com.libre.video.pojo.Video;
import com.libre.video.core.dto.VideoRequestParam;
import com.libre.video.core.dto.Video91Parse;
import com.libre.video.service.VideoService;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.toolkit.JsEncodeUtil;
import com.google.common.collect.Lists;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@VideoRequest(RequestTypeEnum.REQUEST_91)
public class Video91RequestStrategy extends AbstractVideoRequestStrategy {

    private final static String PARAM_CATEGORY = "category";
    private final static String PARAM_PAGE = "page";
    private final VideoService videoService;

	public Video91RequestStrategy(VideoService videoService, VideoService videoService1) {
		super(videoService);
		this.videoService = videoService1;
	}

	@Override
    public void execute(VideoRequestParam requestParam) {
        Video91Type[] types = Video91Type.values();

		RequestTypeEnum requestTypeEnum = requestParam.getRequestTypeEnum();
		HttpUrl httpUrl = HttpUrl.get(requestTypeEnum.getBaseUrl());
        HttpUrl.Builder urlBuilder = getUrlBuilder(httpUrl);

        ThreadPoolTaskExecutor executor = ThreadPoolUtil.requestExecutor();
        for (Video91Type video91Type : types) {
            urlBuilder.addQueryParameter(PARAM_CATEGORY, video91Type.getName());
            String url = urlBuilder.build().toString();
            String html = requestAsHtml(url);
            urlBuilder.removeAllQueryParameters(PARAM_CATEGORY);
            if (StringUtil.isBlank(html)) {
                publishErrorVideo(urlBuilder.build().toString(), ErrorRequestType.REQUEST_CATEGORY);
                continue;
            }
            Integer pageSize = parsePageSize(html);
            if (pageSize == null) {
                continue;
            }
            readVideosAndSave(html, url);

            for (int i = 2; i <= pageSize; i++) {
                urlBuilder.removeAllQueryParameters(PARAM_PAGE);
                urlBuilder.addQueryParameter(PARAM_PAGE, String.valueOf(i));
                String requestUrl = urlBuilder.build().toString();
                String doc = requestAsHtml(requestUrl);
                if (StringUtil.isBlank(doc)) {
                    publishErrorVideo(urlBuilder.build().toString(), ErrorRequestType.REQUEST_PAGE);
                    continue;
                }
                executor.execute(() -> readVideosAndSave(doc, url));
            }
        }
        executor.shutdown();
        log.info("video request complete!");
    }


	@Override
    public List<Video> readVideoList(String html) {
        List<Video91Parse> introductionList = DomMapper.readList(html, Video91Parse.class);
        if (CollectionUtil.isEmpty(introductionList)) {
             throw new LibreException("html parse error");
        }
        List<Video> videos = Lists.newArrayList();
        Map<String, Video91Parse> videoIntroductionMap = introductionList.stream()
                .filter(video91Parse -> StringUtil.isNotBlank(video91Parse.getUrl()))
                .collect(Collectors.toMap(Video91Parse::getUrl, video91Parse -> video91Parse, (v1, v2) -> v1));

        List<Video> list = videoService.list(Wrappers.<Video>lambdaQuery().in(Video::getUrl, videoIntroductionMap.keySet()));
        Map<String, Video> videoMap = list.stream().collect(Collectors.toMap(Video::getUrl, url -> url, (v1, v2) -> v1));
        for (Map.Entry<String, Video> entry : videoMap.entrySet()) {
            videoIntroductionMap.remove(entry.getKey());
        }
        for (Video91Parse video91Parse : videoIntroductionMap.values()) {
            parseVideoInfo(html, video91Parse);
            Video video = readVideo(video91Parse);
            if (Objects.isNull(video)) {
                publishErrorVideo(video91Parse.getUrl(), html, ErrorRequestType.PARSE);
            }
            videos.add(video);
        }
        return videos;
    }

	private HttpUrl.Builder getUrlBuilder(HttpUrl httpUrl) {
		return httpUrl.newBuilder()
			.addQueryParameter("viewtype", "basic")
			.addQueryParameter(PARAM_PAGE, "1");
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

    public Video readVideo(Video91Parse video91Parse) {
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
		Video91Mapping mapping = Video91Mapping.INSTANCE;
		Video video = mapping.sourceToTarget(video91Parse);
        long id = parseVideoId(realUrl);
        video.setId(id);
        video.setRealUrl(realUrl);
        return video;
    }

    private  long parseVideoId(String realUrl) {
        int endIndex = realUrl.indexOf(".m3u8");
        int startIndex = realUrl.lastIndexOf(StringPool.SLASH) + 1;
        String idStr = realUrl.substring(startIndex, endIndex);
        return Long.parseLong(idStr);
    }


    public Integer parsePageSize(String html) {
        Document document = DomMapper.readDocument(html);
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

}
