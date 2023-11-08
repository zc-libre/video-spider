package com.libre.video.pojo.dto;

import lombok.Data;

/**
 * @author: Libre
 * @Date: 2022/5/8 12:02 AM
 */
@Data
public class VideoQuery {

	private String title;

	private String author;

	private String sort;

	private Integer sortOrder;

}
