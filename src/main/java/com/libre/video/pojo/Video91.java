package com.libre.video.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.libre.core.time.DatePattern;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Data
@Document(indexName = "video")
@TableName("video")
public class Video91 {

    @Id
    @TableId(type = IdType.INPUT)
    private Long id;

    @Field(type = FieldType.Keyword)
    private String url;

    @Field(type = FieldType.Keyword)
    private String realUrl;

    @Field(type = FieldType.Text, searchAnalyzer = "ik_max_word", analyzer = "ik_smart")
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

    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @Field(type = FieldType.Date)
    private LocalDate publishTime;
}
