package com.libre.video.core.pojo.parse;

import com.libre.spider.CssQuery;
import lombok.Data;

@Data
@CssQuery(value = ".well")
public class Video91Parse implements VideoParse{

    @CssQuery(value = ".well .video-title", attr = "text")
    private String title;

    @CssQuery(value = ".well a", attr = "href")
    private String url;

    @CssQuery(value = ".well img", attr = "src")
    private String image;

    @CssQuery(value = ".well a > div > span", attr = "text")
    private String duration;

    @CssQuery(value = ".well", attr = "html", regex = "(?<=\\s{0,50}作者:</span>\\s{0,50}).*(?=\\s{0,50}<br>\\s{0,50}<span class=\"info\">查看:)")
    private String author;

    private Integer lookNum;

    private Integer collectNum;

    private Integer type;
}
