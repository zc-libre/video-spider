package com.libre.video.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.libre.core.time.DatePattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("error_video")
public class ErrorVideo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String url;

    private Integer type;

    private String html;

	private Integer requestType;

	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
	private LocalDateTime createTime;
}
