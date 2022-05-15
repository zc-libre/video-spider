package com.libre.video.core.pojo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Video9sDTO {

    private Long videoId;

    private String url;

    private String realUrl;

    private String title;

    private String image;

    private String duration;

    private String author;

    private Integer lookNum;

    private Integer collectNum;

    private LocalDate publishTime;

}
