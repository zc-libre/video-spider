package com.libre.video.core.request;

import com.google.common.collect.Lists;
import com.libre.core.exception.LibreException;
import com.libre.core.time.DatePattern;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringPool;
import com.libre.core.toolkit.StringUtil;
import com.libre.spider.DomMapper;
import com.libre.video.core.constant.RequestConstant;
import com.libre.video.core.enums.RequestTypeEnum;
import com.libre.video.core.enums.ErrorRequestType;
import com.libre.video.pojo.*;
import com.libre.video.core.dto.VideoRequestParam;
import com.libre.video.core.dto.Video9s;
import com.libre.video.core.dto.Video9sDTO;
import com.libre.video.core.dto.Video9sParse;
import com.libre.video.core.mapstruct.Video91Mapping;
import com.libre.video.core.mapstruct.Video9sMapping;
import com.libre.video.service.VideoService;
import com.libre.video.toolkit.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.http.HttpRequest;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@VideoRequest(RequestTypeEnum.REQUEST_9S)
public class Video9SRequestStrategy extends AbstractVideoRequestStrategy {

	public Video9SRequestStrategy(VideoService videoService) {
		super(videoService);
	}

	@Override
    public void execute(VideoRequestParam requestParam) {
        ThreadPoolTaskExecutor executor = ThreadPoolUtil.requestExecutor();
		RequestTypeEnum requestTypeEnum = requestParam.getRequestTypeEnum();
		String url = requestTypeEnum.getBaseUrl();
        String html = requestAsHtml(url);
        Integer pageSize = parsePageSize(html);

        if (StringUtil.isBlank(html)) {
            publishErrorVideo(url,ErrorRequestType.REQUEST_CATEGORY);
            return;
        }
        if (pageSize == null) {
            return;
        }
		Integer size = requestParam.getSize();
		if (Objects.nonNull(size) && size > 2) {
			pageSize = size;
			log.info("parse pageSize: {}", pageSize);
		}
		try {
			readVideosAndSave(html, url);
		} catch (Exception e) {
			log.error("save error: {}, message: {}", url, e.getMessage());
		}
		for (int i = 2; i <= pageSize; i++) {
            url = requestTypeEnum.getBaseUrl() + StringPool.SLASH + i;
            String doc = requestAsHtml(url);
            if (StringUtil.isBlank(doc)) {
                publishErrorVideo(url, ErrorRequestType.REQUEST_PAGE);
                continue;
            }
            String requestVideoUrl = url;
            executor.execute(() -> readVideosAndSave(doc, requestVideoUrl));
        }
        executor.shutdown();
        log.info("video request complete!");
    }


	public Video watchVideo(String url, Long id) {
		String html = requestAsHtml(url);
		List<Video> videoList = readVideoList(html);
		if (CollectionUtil.isEmpty(videoList)) {
			throw new LibreException("视频获取失败");
		}
		return videoList.stream().filter(v -> v.getId().equals(id)).findFirst().orElseThrow(() -> new LibreException("该视频不存在"));
	}

    @Override
    public List<Video> readVideoList(String html) {
        List<Video9sParse> parseList = DomMapper.readList(html, Video9sParse.class);
        if (CollectionUtil.isEmpty(parseList)) {
            throw new LibreException("html parse error");
        }
        List<Video9s> videos = Lists.newArrayList();
        Map<String, Video9sParse> parseMap = parseList.stream()
                .filter(video91Parse -> StringUtil.isNotBlank(video91Parse.getUrl()))
                .collect(Collectors.toMap(Video9sParse::getUrl, Video9sParse -> Video9sParse, (v1, v2) -> v1));

        for (Video9sParse video9sParse : parseMap.values()) {
			Video9s video9s = null;
			try {
				video9s = readVideo(video9sParse);
			} catch (Exception e) {
				log.error("视频数据解析错误： {}", e.getMessage());
				publishErrorVideo(video9sParse.getUrl(), html, ErrorRequestType.PARSE);
			}
			if (Objects.nonNull(video9s) && Objects.nonNull(video9s.getId())) {
				videos.add(video9s);
			}
        }
		Video91Mapping mapping = Video91Mapping.INSTANCE;
		return mapping.convertToVideo91List(videos);
    }

    private void parseVideoInfo(String html, Video9s video9s) {
        Video9sDTO video9sDTO = DomMapper.readValue(html, Video9sDTO.class);
		String publishTime = video9sDTO.getPublishTime();
		if (StringUtil.isNotBlank(publishTime)) {
			publishTime = StringUtil.trimWhitespace(publishTime);
			video9s.setPublishTime(LocalDate.parse(publishTime, DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
		}
		BeanUtils.copyProperties(video9sDTO, video9s);
    }

    private Video9s readVideo(Video9sParse video9sParse) {
		Video9sMapping mapping = Video9sMapping.INSTANCE;
		Video9s video9s = mapping.convertToVideo9s(video9sParse);
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
		log.debug("解析到一条视频数据: {}", video9s);
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

}
