package com.libre.video.core.pojo.parse;

import com.libre.spider.CssQuery;
import lombok.Data;

/**
 * @author: Libre
 * @Date: 2022/5/12 12:13 AM
 */
@Data
@CssQuery(value = ".list_box > ul ")
public class VideoBaAvParse {

	@CssQuery(value = "a > .title", attr = "text")
	private String title;

	@CssQuery(value = "a", attr = "href")
	private String url;

	@CssQuery(value = "a > li.image > .lazy", attr = "img")
	private String image;

	@CssQuery(value = "a > li.image > span.note", attr = "text")
	private String duration;

	@CssQuery(value = ".view", attr = "html", regex = "(?<=<u><i class=\"icon icon-eye-open\"></i>\\s{1,50}).*(?=\\</u> <u><i)")
	private String lookNum;

	@CssQuery(value = ".view", attr = "html", regex = "(?<=<i class=\"icon icon-time\"></i>\\s{1,50}).*(?=</span>)")
	private String publishTime;

}
