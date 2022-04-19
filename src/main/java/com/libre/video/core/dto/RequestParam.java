package com.libre.video.core.dto;

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
public class RequestParam {

	private Integer requestType;

	@JsonIgnore
	private RequestTypeEnum requestTypeEnum;

	private Integer size;
}
