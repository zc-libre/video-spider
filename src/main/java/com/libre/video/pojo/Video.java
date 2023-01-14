package com.libre.video.pojo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.libre.core.time.DatePattern;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("video")
@Document(indexName = "video", createIndex = false)
public class Video implements Serializable {

	@Id
	@Field(type = FieldType.Keyword)
    @TableId(type = IdType.INPUT)
    private Long id;

	@Field(type = FieldType.Keyword)
	private Long videoId;

	@Field(type = FieldType.Keyword)
    private String url;

	@Field(type = FieldType.Keyword)
    private String realUrl;

	@Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
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


	private String m3u8Content;

	private String videoPath;

	private Integer videoWebsite;

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
