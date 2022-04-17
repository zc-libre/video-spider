package com.libre.video;

import com.libre.spider.DomMapper;
import com.libre.video.pojo.dto.Video91Parse;
import com.libre.video.toolkit.UserAgentContext;
import net.dreamlu.mica.http.HttpRequest;
import org.springframework.http.HttpHeaders;

import java.util.List;

public class Test {


    public static void main(String[] args) {

        String html = HttpRequest.get("http://www.91porn.com/v.php?category=rf&viewtype=basic")
                .setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .setHeader(HttpHeaders.USER_AGENT, UserAgentContext.getUserAgent())
                .setHeader(HttpHeaders.ACCEPT_LANGUAGE, "zh-cn,zh;q=0.5")
                .execute().asString();
        List<Video91Parse> list = DomMapper.readList(html, Video91Parse.class);
        list.forEach(System.out::println);
    }
}
