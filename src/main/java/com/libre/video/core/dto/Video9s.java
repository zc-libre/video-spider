package com.libre.video.core.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Video9s {


    private Long id;


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
