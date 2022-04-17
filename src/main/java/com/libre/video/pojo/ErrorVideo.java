package com.libre.video.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("error_video")
public class ErrorVideo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String url;

    private Integer type;

    private String html;

	private Integer requestType;
}
