package com.libre.video;

import com.libre.spider.DomMapper;
import com.libre.video.core.dto.Video91DTO;
import com.libre.video.core.dto.Video91Parse;
import com.libre.video.toolkit.UserAgentContext;
import net.dreamlu.mica.core.utils.DateUtil;
import net.dreamlu.mica.http.HttpRequest;
import org.springframework.http.HttpHeaders;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Test {


    public static void main(String[] args) {

        String html = HttpRequest.get("https://91porn.com/view_video.php?viewkey=6771f60881182342452d&page=3696&viewtype=basic&category=mr")
                .setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .setHeader(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent())
                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5")
                .execute().asString();
		Video91DTO video91DTO = DomMapper.readValue(html, Video91DTO.class);

		System.out.println(video91DTO);

	}



}
