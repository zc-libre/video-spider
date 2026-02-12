package com.libre.video.core.pojo.parse;

import com.libre.spider.CssQuery;
import lombok.Data;

/**
 * 黑料网视频列表页解析模型
 */
@Data
@CssQuery(value = ".video-item")
public class VideoHeiliaoParse implements VideoParse {

	@CssQuery(value = "a > .video-item-img > h3.title", attr = "text")
	private String title;

	@CssQuery(value = "a", attr = "href")
	private String url;

	@CssQuery(value = "a > .video-item-img > .placeholder-img > img", attr = "onload",
			regex = "(?<=loadImg\\(this,').*(?='\\))")
	private String image;

	@CssQuery(value = "a > .video-item-img > .date-xxx", attr = "text")
	private String publishDate;

}
