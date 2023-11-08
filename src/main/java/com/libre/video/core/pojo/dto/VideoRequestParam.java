package com.libre.video.core.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.libre.video.core.enums.RequestTypeEnum;
import lombok.Builder;
import lombok.Data;

/**
 * @author: Libre
 * @Date: 2022/4/20 12:54 AM
 */
@Data
@Builder
public class VideoRequestParam {

	private Integer requestType;

	@JsonIgnore
	private RequestTypeEnum requestTypeEnum;

	private Integer size;

}
