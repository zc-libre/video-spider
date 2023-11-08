package com.libre.video.core.pojo.parse;

import com.libre.spider.CssQuery;
import lombok.Data;

import java.util.List;

@Data
@CssQuery(value = ".colVideoList")
public class Video9sParse implements VideoParse {

	@CssQuery(value = ".video-elem .title", attr = "title")
	private String title;

	@CssQuery(value = ".video-elem > a:nth-child(1)", attr = "href")
	private String url;

	@CssQuery(value = ".video-elem > a > .img", attr = "style", regex = "(?<=\\(').*(?='\\))")
	private String image;

	@CssQuery(value = ".video-elem > a > small", attr = "text")
	private String duration;

	@CssQuery(value = ".video-elem .text-sub-title a", attr = "text")
	private String author;

	@CssQuery(value = ".video-elem >.text-sub-title > div:nth-child(2)", attr = "text", regex = "^\\d+")
	private String lookNum;

}
