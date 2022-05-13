package com.libre.video.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.libre.core.time.DatePattern;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("video")
@Document(indexName = "video", createIndex = false)
public class Video implements Serializable {

	@Id
    @TableId(type = IdType.INPUT)
    private Long id;

	@Field(type = FieldType.Keyword)
    private String url;

	@Field(type = FieldType.Keyword)
    private String realUrl;

	@Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

	@Field(type = FieldType.Keyword)
    private String image;

	@Field(type = FieldType.Keyword)
    private String duration;

	@Field(type = FieldType.Keyword)
    private String author;

	@Field(type = FieldType.Integer)
    private Integer lookNum;

	@Field(type = FieldType.Integer)
    private Integer collectNum;

    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
	@Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate publishTime;

	@TableField(fill = FieldFill.INSERT)
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
	private LocalDateTime createTime;


	@TableField(fill = FieldFill.UPDATE)
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
	@Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
	private LocalDateTime updateTime;

}
