package com.libre.video;

import com.libre.spider.DomMapper;
import com.libre.video.core.pojo.dto.VideoBaAvDTO;
import com.libre.video.core.pojo.parse.VideoBaAvParse;
import com.libre.video.pojo.BaAvVideo;
import com.libre.video.toolkit.RegexUtil;
import com.libre.video.toolkit.UserAgentContext;
import net.dreamlu.mica.http.HttpRequest;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {


    public static void main(String[] args) {
//        String html = HttpRequest.get("https://www.baav.xyz/embed/1038238.html")
//                .setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                .setHeader(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent())
//                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5")
//                .execute().asString();

		BaAvVideo baAvVideo = new BaAvVideo();
		baAvVideo.setPublishTime("03月20日");
		LocalDate time = baAvVideo.getTime();
		System.out.println(time);
	}
}
