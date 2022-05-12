package com.libre.video.core.pojo.dto;

import com.libre.spider.CssQuery;
import lombok.Data;

/**
 * @author: Libre
 * @Date: 2022/5/12 12:53 AM
 */
@Data
public class VideoBaAvDTO {

	@CssQuery(value = "script", attr = "allText")
	private String realUrl;
}
