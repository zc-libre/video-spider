package com.libre.video.pojo.dto;

import com.libre.spider.CssQuery;
import lombok.Data;

@Data
@CssQuery(value = ".well")
public class Video91Parse {

    @CssQuery(value = ".well .video-title", attr = "text")
    private String title;

    @CssQuery(value = ".well a", attr = "href")
    private String url;

    @CssQuery(value = ".well img", attr = "src")
    private String image;

    @CssQuery(value = ".well a > div > span", attr = "text")
    private String duration;

    @CssQuery(value = ".well", attr = "html", regex = "(?<=作者:</span>\\s+).*(?=\\s+<br>\\s+<span class=\"info\">查看:)")
    private String author;

    @CssQuery(value = ".well", attr = "html", regex = "(?<=查看:</span>\\s+).*(?=\\s+<br>\\s+<span class=\"info\">收藏:)[^0-9]")
    private Integer lookNum;

    private Integer collectNum;

    private Integer type;
}
