package com.libre.video.core.pojo.parse;

import com.libre.spider.CssQuery;
import lombok.Data;

@CssQuery(value = "#videoShowPage")
@Data
public class Video9sDetailParse implements VideoParse {

	@CssQuery(value = "#video-play", attr = "data-src", regex = "(?<=/hls/).*(?=/index.m3u8)")
	private Long videoId;

	@CssQuery(value = "#video-play", attr = "data-src")
	private String realUrl;

	@CssQuery(value = ".favoriteBtn > span", attr = "text", regex = "^\\d+")
	private Integer collectNum;

	@CssQuery(value = "#videoShowTabAbout > div > div:nth-child(1) > div:nth-child(2)", attr = "html",
			regex = "(?<=</i>\\s{0,10}).*$")
	private String publishTime;

}
