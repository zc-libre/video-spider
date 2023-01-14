package com.libre.video.core.pojo.parse;

import com.libre.core.time.DatePattern;
import com.libre.spider.CssQuery;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @author: Libre
 * @Date: 2022/4/22 10:07 PM
 */
@Data
@CssQuery("#videodetails-content")
public class Video91DetailParse implements VideoParse{

	@DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
	@CssQuery(value = "div:nth-child(1) > span.title-yakov", attr = "text")
	private LocalDate publishTime;
}
